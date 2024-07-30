/**
 * 
 */
package dsg.rounda.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.trafficsimulation.IDM;
import de.trafficsimulation.IDMCar;
import dsg.rounda.Handler;
import dsg.rounda.config.SimulationParameters;
import dsg.rounda.config.VehicleConfig;
import dsg.rounda.geometry.Direction;
import dsg.rounda.logging.VehicleEventLog;
import dsg.rounda.model.Actuators;
import dsg.rounda.model.Clock;
import dsg.rounda.model.IndicatorDetector;
import dsg.rounda.model.IndicatorObservation;
import dsg.rounda.model.IndicatorState;
import dsg.rounda.model.LaneChangeDirection;
import dsg.rounda.model.LocalizationSensors;
import dsg.rounda.model.NetworkAdapter;
import dsg.rounda.model.RangingSensors;
import dsg.rounda.model.RangingSnapshot;
import dsg.rounda.model.Scheduler;
import dsg.rounda.model.Track;
import dsg.rounda.model.Trajectory1D;
import dsg.rounda.model.VehicleCapabilities;
import dsg.rounda.model.VehicleProperties;
import dsg.rounda.model.Velocity1D;
import dsg.rounda.services.comm.beaconing.Beaconer;
import dsg.rounda.services.comm.neighbourhood.NeighbourhoodWatch;
import dsg.rounda.services.roadmap.ConnectorGraph;
import dsg.rounda.services.roadmap.DecayFlags;
import dsg.rounda.services.roadmap.MapEdge;
import dsg.rounda.services.roadmap.MapEdgeSet;
import dsg.rounda.services.roadmap.MapNode;
import dsg.rounda.services.roadmap.RoadBlock;
import dsg.rounda.services.roadmap.RoadBlockDetection;
import dsg.rounda.services.roadmap.RoadBlockMap;
import dsg.rounda.services.roadmap.RoutePredictor;
import dsg.rounda.services.roadmap.TrackBoundary1D;
import dsg.rounda.services.roadmap.TrackMapArea1D;
import dsg.rounda.services.roadmap.TrackMapBoundaries1D;
import dsg.rounda.services.roadmap.TrackPoint1D;
import dsg.rounda.services.roadmap.TrackRange1D;
import dsg.rounda.services.roadmap.TrackRangeSequence;
import dsg.rounda.services.roadmap.VehicleTrackMap;
import dsg.rounda.services.sensing.distance.EmptyAreaRadar;
import dsg.rounda.services.sensing.distance.LaneGap;

/**
 * @author slotm
 *
 */
public class HighwayController implements VehicleController, dsg.rounda.Constants, SimulationParameters {


	public static final VehicleControllerFactory FACTORY = new VehicleControllerFactory() {
		@Override
		public VehicleController createController(VehicleCapabilities capabilities) {
			return new HighwayController(capabilities);
		}
	};

	private static final double BEACON_INTERVAL = 0.2 * SECONDS; // ns
	private static final double SAFE_DISTANCE = 2; // m

	static final double MAX_NEIGHBOUR_STATE_AGE = 1.0 * SECONDS; 
	static final int LEFT = 0; 
	static final int RIGHT = 1;

	protected static final double MIN_SUCC_DISTANCE = 1.5; // m 

	protected static final double MAX_ROAD_BLOCK_DISTANCE = Double.MAX_VALUE; // m
	protected static final double TIME_TO_LANE_CHANGE = 0.5; // seconds
	protected static final double INDICATOR_TIME = 1.0; // seconds
	//private static final double MINIMUM_INDICATOR_DISTANCE = 5.;
	protected static final double MINIMUM_LANE_CHANGE_DISTANCE = 10; //m
	static final LaneChangeDirection[] LANE_CHANGE_DIRECTIONS = new LaneChangeDirection[] {
		LaneChangeDirection.LEFT,
		LaneChangeDirection.RIGHT
	};
	static final int preferredLane = LEFT;

	final VehicleConfig config;
	final VehicleCapabilities capabilities;
	final VehicleProperties properties;
	final int vehicleID;
	final Clock clock;
	final Scheduler scheduler;
	final NetworkAdapter network;
	final Random random;
	final LocalizationSensors localization;
	final RangingSensors rangers;
	final IndicatorDetector indicatorDetector;
	final EmptyAreaRadar ear;
	final Actuators actuators;
	final VehicleTrackMap trackMap;
	final VehicleEventLog eventLog;
	final RoutePredictor routePredictor;
	final IDM carFollowingModel;
	final Beaconer beaconer;
	final NeighbourhoodWatch neighbourhood;
	RoadBlockMap roadBlockMap;

	final double desiredVelocity;
	final double desiredTimeHeadway;
	final double minLaneChangeTimeHeadway;

	/**
	 * @param capabilities
	 */
	public HighwayController(VehicleCapabilities capabilities) {
		this.config = capabilities.getConfig();
		this.capabilities = capabilities;
		this.vehicleID = capabilities.getId();
		this.properties = capabilities.getProperties();
		this.clock = capabilities.getClock();
		this.scheduler = capabilities.getScheduler();
		this.network = capabilities.getNetwork();
		this.localization = capabilities.getLocalizationSensors();
		this.rangers = capabilities.getRangingSensors();
		this.indicatorDetector = capabilities.getIndicatorDetector();
		this.actuators = capabilities.getActuators();
		this.trackMap = capabilities.getRoadMap();
		this.random = capabilities.getRandom();
		this.eventLog = capabilities.getEventLog();
		this.routePredictor = capabilities.getService(RoutePredictor.type());
		this.ear = capabilities.getService(EmptyAreaRadar.type());
		this.neighbourhood = capabilities.getService(NeighbourhoodWatch.type());
		this.beaconer = capabilities.getService(Beaconer.type());
		
		minLaneChangeTimeHeadway = capabilities.getConfig().get(MIN_LANE_CHANGE_TIME_HEADWAY);
		desiredTimeHeadway = capabilities.getConfig().get(DESIRED_TIME_HEADWAY);
		desiredVelocity = capabilities.getConfig().get(DESIRED_VELOCITY)
		                * (capabilities.getRandom().nextDouble() < capabilities.getConfig().get(SLOW_VEHICLE_FRACTION) ? capabilities.getConfig().get(SLOW_VEHICLE_FACTOR) : 1);
		
		this.carFollowingModel = new IDMCar();
		this.carFollowingModel.set_v0(desiredVelocity);
		this.carFollowingModel.set_T(desiredTimeHeadway);
		this.carFollowingModel.set_a(3.4);
	}

	@Override
	public void start() {
		ConnectorGraph trackGraph = trackMap.getConnectorGraph();

		TrackPoint1D position = localization.getPosition();
		MapEdge startingEdge = trackGraph.getEdge(position);
		MapNode startingNode = startingEdge.getFrom();

		ConnectorGraph destinationsGraph = trackGraph.getPossibleDestinationsGraph(startingNode);
		List<MapEdgeSet> terminatingEdgeSets = destinationsGraph.getTerminatingEdgeSets();

		List<MapEdgeSet> weightedEdgeSets = new ArrayList<MapEdgeSet>();

		for(MapEdgeSet edgeSet : terminatingEdgeSets) {
			for(MapEdge edge : edgeSet.getEdges()) {
				weightedEdgeSets.add(edgeSet);
			}
		}

		MapEdgeSet destinationEdgeSet = weightedEdgeSets.get(random.nextInt(terminatingEdgeSets.size()));
		MapNode destinationNode = destinationEdgeSet.getEndNodes().get(0);

		ConnectorGraph routingGraph = destinationsGraph.getPossibleSourcesGraph(destinationNode);

		roadBlockMap = routingGraph.findRoadBlocks(destinationNode);

		rangers.addSnapshotHandler(snapshotHandler);
		// To detect an upcoming road block
		// RoadBlockDetection roadBlock = roadBlockMap.getFirstLaneBlock(routePredictor.predictRoute(Double.MAX_VALUE));

		neighbourhood.start();
		beaconer.start(BEACON_INTERVAL);
	}

	public enum LaneChangeState {
		NOT_LANE_CHANGING,
		BEFORE_LANE_CHANGE,
		INDICATING,
		IN_LANE_CHANGE
	}

	final Handler<RangingSnapshot> snapshotHandler = new Handler<RangingSnapshot>() {

		TrackPoint1D backPosition;
		TrackPoint1D frontPosition;
		Velocity1D currentVelocity;

		double measuredDistance;
		double availableDistance;
		double oldMeasuredDistance;
		TrackRangeSequence emptyRoute;

		LaneChangeState laneChangeState = LaneChangeState.NOT_LANE_CHANGING;
		LaneChangeDirection laneChangeDirection;
		TrackRangeSequence startIndicationArea;

		Track laneChange;
		TrackMapArea1D queryArea;

		TrackMapArea1D emptyArea1D;
		TrackMapBoundaries1D emptinessBoundaries1D;

		TrackPoint1D[] lanePositions = new TrackPoint1D[2];
		LaneGap[] laneGaps = new LaneGap[2];
		TrackRangeSequence beforeLaneChange;

		Double overrideAcceleration;

		@Override
		public void handle(RangingSnapshot obj) {
			try {
				perception(); // converts to 1D

				if(frontPosition == null) {
					// About to die
					return;
				}

				overrideAcceleration = null;
				availableDistance = measuredDistance;

				TrackRangeSequence route = routePredictor.predictRoute(MAX_ROAD_BLOCK_DISTANCE);
				RoadBlockDetection roadBlockDetection = roadBlockMap.getFirstRoadBlock(route);

				switch(laneChangeState) {
				case NOT_LANE_CHANGING:

						if(roadBlockDetection != null && roadBlockDetection.getDistance() < roadBlockDetection.getRoadBlock().getMagnitude() * 120) {
							prepareRequiredLaneChange(roadBlockDetection);
							laneChangeState = LaneChangeState.BEFORE_LANE_CHANGE;
						} else {
							LaneChangeDirection oppDirection = prepareOptionalLaneChange(roadBlockDetection);

							if(oppDirection != null) {

								double v = this.currentVelocity.getRoadVelocity();
								double laneChangeDistance = 0.0118 * v * v + 0.0862 * v + 20.943;					

								double indicatorDistance = v * 0.85 + 5;

								beforeLaneChange = routePredictor.predictRoute(
										backPosition, 
										actuators.getTrajectory(), 
										indicatorDistance);

								TrackPoint1D startOfLaneChange = beforeLaneChange.getEnd();
								startIndicationArea = beforeLaneChange;

								laneChangeDirection = oppDirection;
								laneChange = actuators.doLaneChange(startOfLaneChange, laneChangeDistance, laneChangeDirection);

								if(laneChange == null){
									// Oops, we can't acually do this
									break;
								}

								trackMap.registerLaneChange(laneChange);
								actuators.setIndicatorState(laneChange.getLaneChangeDirection());
								indicatorDetector.setAdditionalLaneDetection(laneChangeDirection);
								
								laneChangeState = LaneChangeState.INDICATING;
							}
					}

					break;

				case BEFORE_LANE_CHANGE:
					if(startIndicationArea.contains(backPosition)) {

						laneChangeState = LaneChangeState.INDICATING;
						actuators.setIndicatorState(laneChange.getLaneChangeDirection());
						indicatorDetector.setAdditionalLaneDetection(laneChangeDirection);

					} else {

						anticipatePredecessor();
						break;
					}

				case INDICATING:
					if(backPosition.getTrackID() == laneChange.getId()) {
						laneChangeState = LaneChangeState.IN_LANE_CHANGE;
						actuators.setIndicatorState(laneChange.getLaneChangeDirection());
						indicatorDetector.setAdditionalLaneDetection(laneChangeDirection);
					}

				case IN_LANE_CHANGE: 
					if(laneChangeState == LaneChangeState.IN_LANE_CHANGE && backPosition.getTrackID() != laneChange.getId()) {
						laneChangeState = LaneChangeState.NOT_LANE_CHANGING;
						actuators.setIndicatorState(IndicatorState.NONE);
						indicatorDetector.setAdditionalLaneDetection(null);
						break;
					}

					considerPredecessor();
				}

				considerIndicators();

				if(roadBlockDetection != null) {
					double lastLaneChangeChanceDistance = roadBlockDetection.getDistance()-50*roadBlockDetection.getRoadBlock().getMagnitude();
					availableDistance = Math.min(availableDistance, lastLaneChangeChanceDistance);
				} else {
					considerWorldEnd();
				}
				eventLog.log("available-distance", availableDistance - properties.getLength());

				double acceleration = acceleration();

				eventLog.log("acceleration", acceleration);

				actuators.setAcceleration(acceleration);

			} catch (IllegalStateException e) {
				System.err.println(vehicleID + ": " + e.getMessage());
				// inconsistent boundaries :(
				return;
			}
		}

		private LaneChangeDirection prepareOptionalLaneChange(RoadBlockDetection roadBlockDetection) {
			for(int laneChangeSide = 0; laneChangeSide <= 1; laneChangeSide++) {
				LaneGap gap  = laneGaps[laneChangeSide];

				if(gap == null) {
					// There's no gap, we definitely cannot make an opportunistic lane change
					continue;
				}

				if(!gap.isValid()) {
					// There is a gap, but we cannot match it with last round's gap
					// and get the predecessor velocity. skip this round
					continue;
				}

				LaneChangeDirection direction = LANE_CHANGE_DIRECTIONS[laneChangeSide];
				TrackPoint1D lanePoint = lanePositions[laneChangeSide];
				

				TrackRangeSequence route = routePredictor.predictRoute(lanePoint, new Trajectory1D(), MAX_ROAD_BLOCK_DISTANCE);
				RoadBlockDetection targetLaneBlock = roadBlockMap.getFirstRoadBlock(route);
				
				if(targetLaneBlock != null && targetLaneBlock.getDistance() < 120 * (1+targetLaneBlock.getRoadBlock().getMagnitude())) {
					continue;
				}
				
				Double succDistance = gap.getSuccessorDistance(lanePoint);
				double succVelocity = gap.getSuccessorVelocity();
				Double predDistance = gap.getPredecessorDistance(lanePoint);
				double predVelocity = gap.getPredecessorVelocity();
				
				if(succDistance == null || predDistance == null) {
				       // Exceptional situation when the lanePoint falls outside 
				       // the gap. This shouldn't happen, but does, because of 
				       // rounding errors. We should treat this as if there is no
				       // gap.
				       continue;
				}

				if(succDistance > succVelocity * minLaneChangeTimeHeadway
						&& predDistance > currentVelocity.getRoadVelocity() * minLaneChangeTimeHeadway
						&& predVelocity >= currentVelocity.getRoadVelocity()
						&& succVelocity <= currentVelocity.getRoadVelocity()
						&& predDistance > measuredDistance
						&& laneChangeSide == preferredLane) {
					return direction;
				}
				
				
				if(succDistance > succVelocity * minLaneChangeTimeHeadway
						&& predDistance > currentVelocity.getRoadVelocity() * minLaneChangeTimeHeadway
						&& predVelocity >= currentVelocity.getRoadVelocity()
						&& succVelocity <= currentVelocity.getRoadVelocity()
						&& laneChangeSide != preferredLane
						&& predDistance > measuredDistance
						&& currentVelocity.getRoadVelocity() < 0.94379 *desiredVelocity ) {
					return direction;
				}

			}
			return null;
		}

		private void anticipatePredecessor() {
			int laneChangeSide = laneChangeDirection == LaneChangeDirection.LEFT ? LEFT : RIGHT;
			LaneGap gap  = laneGaps[laneChangeSide];
			TrackPoint1D lanePoint = lanePositions[laneChangeSide];

			if(gap == null) {
				// Another car is right besides us, try to get behind it
				overrideAcceleration = -3.;
				return;
			}

			if(!gap.isValid()) {
				// There is a gap, but we cannot match it with last round's gap
				// and get the predecessor velocity. skip this round
				return;
			}

			double succDistance = gap.getSuccessorDistance(lanePoint);
			double succVelocity = gap.getSuccessorVelocity();
			double predDistance = gap.getPredecessorDistance(lanePoint);
			double predVelocity = gap.getPredecessorVelocity();
			TrackPoint1D predPosition = gap.getPredecessorPosition();

			TrackPoint1D predPositionOnMyLane = 
					trackMap.translateToOtherLane(predPosition, laneChangeDirection.inverse());

			if(predPositionOnMyLane == null) {
				// Predecessor does not have a laneChangeDirection.inverse() lane
				return;
			}

			if(!beforeLaneChange.contains(predPositionOnMyLane)) {
				// Predecessor as passed the start of the lane change
				return;
			}

			TrackRangeSequence predRoute = routePredictor.predictRoute(
					predPositionOnMyLane, 
					new Trajectory1D(), 
					beforeLaneChange.getArea());

			TrackRangeSequence myRoute = routePredictor.predictRoute(
					backPosition, 
					new Trajectory1D(), 
					beforeLaneChange.getArea());

			double predecessorDistanceToLaneChange = predRoute.getLength();
			double myDistanceToLaneChange = myRoute.getLength();
			double predecessorETA = predecessorDistanceToLaneChange / predVelocity;
			double myDesiredETA = predecessorETA + desiredTimeHeadway;
			double bottomVelocity = 2 * myDistanceToLaneChange / (0.5 * myDesiredETA) - currentVelocity.getRoadVelocity() - predVelocity;

			if(bottomVelocity < currentVelocity.getRoadVelocity()) {
				overrideAcceleration = Math.max((bottomVelocity - currentVelocity.getRoadVelocity()) / (0.5 * myDesiredETA), -5.);  
			}
		}

		private void prepareRequiredLaneChange(RoadBlockDetection roadBlockDetection) {
			RoadBlock roadBlock = roadBlockDetection.getRoadBlock();
			laneChangeDirection = LaneChangeDirection.getDirection(roadBlock.getDirection()); 

			double v = this.currentVelocity.getRoadVelocity();
			double laneChangeDistance = 0.0118 * v * v + 0.0862 * v + 20.943;					

			double indicatorDistance = v * 0.85 + 5;

			double distanceToLaneChange = v * 0.85 + 15;

			beforeLaneChange = routePredictor.predictRoute(
					backPosition, 
					actuators.getTrajectory(), 
					distanceToLaneChange + indicatorDistance);

			TrackPoint1D startOfLaneChange = beforeLaneChange.getEnd();
			startIndicationArea = beforeLaneChange.subSequence(distanceToLaneChange);

			laneChange = actuators.doLaneChange(startOfLaneChange, laneChangeDistance, laneChangeDirection);

			if(laneChange == null){
				// We're going too fast to make a lane change in time, slow down now!
				overrideAcceleration = -7.5;
				return;
			}

			trackMap.registerLaneChange(laneChange);

			/*ConnectorGraph roadGraph = trackMap.getConnectorGraph();
			MapNode endOfLaneChangeNode = roadGraph.getEndNode(laneChange.getId());

			queryArea = new TrackMapArea1D(trackMap);
			TrackBoundary1D endOfLaneChangeBoundary = new TrackBoundary1D(
					laneChange.getTo().getRoad(),
					laneChange.getTo().getOffset(),
					Direction.FORWARD);

// possible successor beacons
			queryArea.decayFromBoundary(
					endOfLaneChangeBoundary, 
					100, // dependent on distance to lane change
					new DecayFlags(DecayFlags.CONSIDER_DIRECTION | DecayFlags.DISTANCE | DecayFlags.GROW));
			 */
		}

		private void considerPredecessor() {
			int laneChangeSide = laneChangeDirection == LaneChangeDirection.LEFT ? LEFT : RIGHT;
			LaneGap gap  = laneGaps[laneChangeSide];

			if(gap == null) {
				availableDistance = properties.getLength();
				return;
			}

			double succDistance = gap.getSuccessorDistance(lanePositions[laneChangeSide]);
			double predDistance = gap.getPredecessorDistance(lanePositions[laneChangeSide]);

			if(succDistance < MIN_SUCC_DISTANCE) {
				availableDistance = properties.getLength();
			}

			availableDistance = Math.min(predDistance, availableDistance);
		}

		private void considerIndicators() {
			List<IndicatorObservation> indicatorObservations = indicatorDetector.observe();

			double graceDistance = actuators.getIndicatorState().isOn() ? 0.0 : properties.getLength() + 2.0;

			for(IndicatorObservation obs : indicatorObservations) {
				if(obs.getDistance() < graceDistance) {
					continue;
				}

				if(obs.getDistance() < availableDistance) {
					availableDistance = obs.getDistance();
				}

				break;
			}
		}

		void perception() {
			backPosition = localization.getPosition();
			frontPosition = localization.getFrontPosition();

			Track currentTrack = trackMap.getRoad(backPosition.getTrackID());

			switch(currentTrack.getType()) {
			case LANE_CHANGE:
				lanePositions = new TrackPoint1D[2];

				int laneChangeSide = laneChangeDirection == LaneChangeDirection.LEFT ? LEFT : RIGHT;

				TrackPoint1D laneChangeStart = new TrackPoint1D(currentTrack.getFrom().getRoad(), currentTrack.getFrom().getOffset());

				TrackPoint1D startOfLaneChangeProjection = trackMap.translateToOtherLane(
						laneChangeStart, 
						laneChangeDirection);

				double relativeOffset = backPosition.getOffset() / currentTrack.getPathLength();
				double translatedOffset = relativeOffset * currentTrack.getLaneChangeDistance();

				lanePositions[laneChangeSide] = routePredictor.predictTrackPoint1D(
						startOfLaneChangeProjection, 
						new Trajectory1D(), 
						translatedOffset);
				break;
			default:
				lanePositions[LEFT] = trackMap.translateToOtherLane(backPosition, LaneChangeDirection.LEFT);
				lanePositions[RIGHT] = trackMap.translateToOtherLane(backPosition, LaneChangeDirection.RIGHT);
			}

			currentVelocity = localization.getVelocity();
			emptinessBoundaries1D = ear.measureEmptinessBoundaries1D();
			emptyArea1D = ear.measureEmptiness1D();
			emptyRoute = ear.measureEmptyRoute1D();
			measuredDistance = emptyRoute.getLength();

			findTheGaps();

			eventLog.log("track", backPosition.getTrackID());
			eventLog.log("offset", backPosition.getOffset());
			eventLog.log("velocity", currentVelocity.getRoadVelocity());
			eventLog.log("acceleration", currentVelocity.getAcceleration());
			eventLog.log("measured-distance", measuredDistance - properties.getLength());
		}


		void findTheGaps() {
			boolean leftIsEmpty = isInEmptiness(lanePositions[LEFT]);
			boolean rightIsEmpty = isInEmptiness(lanePositions[RIGHT]);

			if(!leftIsEmpty) {
				laneGaps[LEFT] = null;
			}

			if(!rightIsEmpty) {
				laneGaps[RIGHT] = null;
			}

			if(!leftIsEmpty && !rightIsEmpty) {
				return;
			}

			for(TrackBoundary1D boundary : emptyArea1D.toBoundariesList()) {
				if(boundary.getInclusive() == Direction.BACKWARD) {
					continue;
				}

				TrackRangeSequence successorRoute = routePredictor.predictRoute(
						boundary, 
						new Trajectory1D(), 
						emptyArea1D);

				if(leftIsEmpty) {
					findGap(LEFT, successorRoute);
				}
				if(rightIsEmpty) {
					findGap(RIGHT, successorRoute);
				}
			}

		}

		private void findGap(int side, TrackRangeSequence successorRoute) {
			if( successorRoute.contains(lanePositions[side])) {
				if(laneGaps[side] == null) {
					laneGaps[side] = new LaneGap(successorRoute);
				} else {
					// TODO: in case of branching, pick the shorter gap 
					laneGaps[side].updateGap(successorRoute, 0.1);
				}
			}
		}
		private boolean isInEmptiness(TrackPoint1D pos) {
			return pos == null ? false : emptyArea1D.contains(pos);
		}


		double acceleration() {
			double spacing = Math.max(0, availableDistance - properties.getLength() - SAFE_DISTANCE);

			double stoppingDistance = currentVelocity.computeStoppingDistance();

			if(spacing <= stoppingDistance) { 
				// emergency break
				return -7.5;
			}

			if(overrideAcceleration != null) {
				return overrideAcceleration;
			}

			double distanceChangeSpeed = (availableDistance - oldMeasuredDistance) / rangers.getInterval();
			double vehicleVelocity = Math.min(Math.max(distanceChangeSpeed + currentVelocity.getRoadVelocity(), 0), config.get(MAX_VELOCITY));

			if(availableDistance > measuredDistance) {
				// only the case when we reach the end of the world
				vehicleVelocity = properties.getMaximumVelocity();
			}

			oldMeasuredDistance = availableDistance;

			double idm = carFollowingModel.calcAcc(
					currentVelocity.getRoadVelocity(), 
					vehicleVelocity, 
					spacing);

			return idm;
		}

		void considerWorldEnd() {
			if(!emptyRoute.isEmpty()) {
				TrackRange1D lastRange = emptyRoute.get(emptyRoute.size()-1);
				Track lastTrack = trackMap.getRoad(lastRange.getTrackID());

				if(lastTrack.getTo() == null
						&& lastRange.getEnd() >= lastTrack.getPathLength()) {
					// route runs off the world
					availableDistance = Double.POSITIVE_INFINITY;
				}
			} 
		}

	};
}
