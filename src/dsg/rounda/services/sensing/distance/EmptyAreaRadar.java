/**
 * 
 */
package dsg.rounda.services.sensing.distance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.RobustLineIntersector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.math.Vector2D;
import com.vividsolutions.jts.operation.buffer.BufferOp;
import com.vividsolutions.jts.operation.buffer.BufferParameters;

import dsg.rounda.Handler;
import dsg.rounda.config.SimulationParameters;
import dsg.rounda.geometry.Direction;
import dsg.rounda.geometry.GeoUtil;
import dsg.rounda.logging.VehicleEventLog;
import dsg.rounda.model.Actuators;
import dsg.rounda.model.LidarSnapshot;
import dsg.rounda.model.LidarSpecification;
import dsg.rounda.model.LocalizationSensors;
import dsg.rounda.model.Pose2D;
import dsg.rounda.model.RangingSensors;
import dsg.rounda.model.RangingSensorsSpecification;
import dsg.rounda.model.RangingSnapshot;
import dsg.rounda.model.SensorSnapshotAndConfig;
import dsg.rounda.model.Track;
import dsg.rounda.model.TrackSegment;
import dsg.rounda.model.VehicleCapabilities;
import dsg.rounda.services.AbstractServiceFactory;
import dsg.rounda.services.ServiceFactory;
import dsg.rounda.services.coordination.ConflictArea;
import dsg.rounda.services.roadmap.RoutePredictor;
import dsg.rounda.services.roadmap.TrackBoundary1D;
import dsg.rounda.services.roadmap.TrackMap;
import dsg.rounda.services.roadmap.TrackMapArea1D;
import dsg.rounda.services.roadmap.TrackMapBoundaries1D;
import dsg.rounda.services.roadmap.TrackPoint1D;
import dsg.rounda.services.roadmap.TrackRange1D;
import dsg.rounda.services.roadmap.TrackRangeSequence;
import dsg.rounda.services.roadmap.VehicleTrackMap;

/**
 * A sensing service that produces polygons of emptiness.
 * 
 * It does not currently take into account sensor inaccuracy
 */
public class EmptyAreaRadar {

	static final ServiceFactory FACTORY = new AbstractServiceFactory(type()) {
		@Override
		public Object create(VehicleCapabilities capabilities) {
			return new EmptyAreaRadar(capabilities);
		}
	};

	public static Class<EmptyAreaRadar> type() {
		return EmptyAreaRadar.class;
	}

	private static final GeometryFactory GEOM = new GeometryFactory(); 

	/**
	 * The minimum width of a car assumed by this service
	 */
	public static final double ASSUMED_MIN_CAR_WIDTH = 1.5; //m

	public static final int MAX_NUM_SKIP = 10;

	final VehicleEventLog eventLog;
	final RangingSensorsSpecification specs; 
	final RangingSensors rangingSensors;
	final LocalizationSensors localizationSensors;
	final VehicleTrackMap trackMap;

	final RoutePredictor routePredictor;
	final Actuators actuators;

	// cache results
	TrackPoint1D cachedPosition;
	RangingSnapshot cachedSnapshot;

	TrackMapArea1D cachedEmptiness1D;
	MultiPolygon cachedEmptiness2D;
	TrackMapBoundaries1D cachedBoundaries1D;
	TrackRangeSequence cachedEmptyRoute1D;

	double positionInaccuracy;

	public EmptyAreaRadar(VehicleCapabilities capabilities) {
		this.eventLog = capabilities.getEventLog();
		this.rangingSensors = capabilities.getRangingSensors();
		this.rangingSensors.addSnapshotHandler(handleSnapshot());
		this.specs = rangingSensors.getSpecification();
		this.localizationSensors = capabilities.getLocalizationSensors();
		this.trackMap = capabilities.getRoadMap();
		this.routePredictor = capabilities.getService(RoutePredictor.type());
		this.actuators = capabilities.getActuators();
		this.positionInaccuracy = capabilities.getConfig().get(SimulationParameters.POSITION_INACCURACY);
	}

	private Handler<RangingSnapshot> handleSnapshot() {
		return new Handler<RangingSnapshot>() {
			@Override
			public void handle(RangingSnapshot snap) {
				cachedPosition = localizationSensors.getPosition();
				cachedSnapshot = snap;
				eventLog.log("rangers", new SensorSnapshotAndConfig(cachedPosition, cachedSnapshot, specs));
				cachedBoundaries1D = null;
				cachedEmptiness1D = null;
				cachedEmptiness2D = null;
				cachedEmptyRoute1D = null;
			}
		};
	}

	public double measureDistance1D() {
		TrackPoint1D position = localizationSensors.getPosition();
		TrackMapArea1D emptiness = measureEmptiness1D();
		return computeDistance1D(emptiness, position);
	}

	public double computeDistance1D(TrackMapArea1D emptiness, TrackPoint1D vehiclePosition) {
		TrackRangeSequence route = routePredictor.predictRoute(
				vehiclePosition, 
				actuators.getTrajectory(), 
				emptiness);

		double routeLength = 0.0;

		for(TrackRange1D range : route) {
			routeLength += range.getLength();
		}

		return routeLength;
	}

	public TrackRangeSequence measureEmptyRoute1D() {
		if(cachedEmptyRoute1D == null) {
			cachedEmptyRoute1D = computeEmptyRoute1D(measureEmptiness1D());
			eventLog.log("empty-route", cachedEmptyRoute1D);
		}

		return cachedEmptyRoute1D;
	}

	private TrackRangeSequence computeEmptyRoute1D(TrackMapArea1D emptyArea1D) {
		return routePredictor.predictRoute(
				cachedPosition, 
				actuators.getTrajectory(), 
				emptyArea1D);
	}

	public TrackMapArea1D measureEmptiness1D() {
		if(cachedEmptiness1D == null) {
			cachedEmptiness1D = computeEmptiness1D(measureEmptinessBoundaries1D());
			eventLog.log("emptiness1D", cachedEmptiness1D);
		}

		return cachedEmptiness1D;
	}

	public long getLatestResultTime() {
		return cachedSnapshot.getTime();
	}

	private TrackMapArea1D computeEmptiness1D(
			TrackMapBoundaries1D boundaries) {
		return boundaries.toMapArea();
	}

	public TrackMapBoundaries1D measureEmptinessBoundaries1D() {
		if(cachedBoundaries1D == null) {
			cachedBoundaries1D = computeEmptinessBoundaries1D(measureEmptiness2D());
		}

		return cachedBoundaries1D;
	}

	public TrackMapBoundaries1D computeEmptinessBoundaries1D(MultiPolygon emptiness2D) {
		return computeEmptinessBoundaries1D(trackMap, emptiness2D);
	}

	public TrackMapBoundaries1D computeEmptinessBoundaries1D(VehicleTrackMap roadMap, MultiPolygon multiEmptiness) {
		Map<Integer,List<TrackBoundary1D>> boundaries1D = new HashMap<Integer,List<TrackBoundary1D>>();

		final RobustLineIntersector intersector = new RobustLineIntersector();
		List<TrackBoundary1D> allBoundaries = new ArrayList<TrackBoundary1D>();

		for(int polygonIndex = 0, numPolygons = multiEmptiness.getNumGeometries(); polygonIndex < numPolygons; polygonIndex++) {
			Polygon emptiness2D = (Polygon) multiEmptiness.getGeometryN(polygonIndex);
			Coordinate[] shell = emptiness2D.getExteriorRing().getCoordinates();

			if(shell.length > 2) {

				boolean isCounterClockWise = positionInaccuracy == 0.0 ? true : CGAlgorithms.isCCW(shell);

				for(int i = 0; i < shell.length-1; i++) {
					Iterable<TrackSegment> nearbySegments = roadMap.getTracksByPathSegment(new Envelope(shell[i], shell[i+1]));

					for(TrackSegment trackSegment : nearbySegments) {
						Track track = trackSegment.getTrack();
						LineSegment lineSegment = trackSegment.getSegment();
						int segmentIndex = trackSegment.getSegmentIndex();
						Coordinate shellSegmentStart = shell[i];
						Coordinate shellSegmentEnd = shell[i+1];

						intersector.computeIntersection(
								shellSegmentStart, 
								shellSegmentEnd, 
								lineSegment.p0, 
								lineSegment.p1);

						if(!intersector.hasIntersection()) {
							continue;
						}

						int shellOrientation = lineSegment.orientationIndex(shellSegmentStart);

						Direction inclusiveDirection;

						if((shellOrientation == 1 && isCounterClockWise) || (shellOrientation != 1 && !isCounterClockWise)) {
							// The start of the shell segment is left of the path segment.
							// Since the shell is defined in counter-clockwise direction,
							// this means the path segment is pointing inwards. This means
							// the end of the path is included in the area.
							inclusiveDirection = Direction.FORWARD;
						} else {
							// The path segment is pointing outwards, meaning the start
							// of the path is included in the area.
							inclusiveDirection = Direction.BACKWARD;
						}

						Coordinate intersection = intersector.getIntersection(0);

						// This is an intersection with the path.
						// Figure out the offset of the intersection. 
						// Thanks to caching, this is an O(1) operation.
						double projectionFactor = lineSegment.projectionFactor(intersection);
						double segmentOffset = track.getSegmentLength(segmentIndex) * projectionFactor;
						double offset = track.getPathLengthBeforeSegment(segmentIndex) + segmentOffset;

						TrackBoundary1D boundary = new TrackBoundary1D(track.getId(), offset, inclusiveDirection);

						List<TrackBoundary1D> trackBoundaries = boundaries1D.get(track.getId());

						if(trackBoundaries == null) {
							trackBoundaries = new ArrayList<TrackBoundary1D>();
							boundaries1D.put(track.getId(), trackBoundaries);
						}

						allBoundaries.add(boundary);
					}
				}
			}
		}

		TrackMapBoundaries1D result = new TrackMapBoundaries1D(roadMap, allBoundaries);
		return result;
	}

	/**
	 * Measure the emptiness around the vehicle
	 * 
	 * @return an empty area
	 */
	public MultiPolygon measureEmptiness2D() {
		if(cachedEmptiness2D == null) {
			cachedEmptiness2D = computeEmptiness2D(cachedSnapshot, cachedPosition);
			eventLog.log("emptiness", cachedEmptiness2D);
		}
		return cachedEmptiness2D;
	}       

	/**
	 * Compute an empty area around the vehicle from a ranging sensor snapshot.
	 * 
	 * @param snapshot the snapshot
	 * @param vehiclePosition1D the current position of the vehicle
	 * @return an empty area
	 */
	public MultiPolygon computeEmptiness2D(
			RangingSnapshot snapshot, 
			TrackPoint1D vehiclePosition1D) {

		List<Coordinate> emptyRing = new ArrayList<Coordinate>(specs.getNumBeams()*2);
		List<LidarSnapshot> snapshots = snapshot.getLidarSnapshots();
		List<LidarSpecification> lidarSpecs = specs.getLidarSpecs();

		Track track = trackMap.getRoad(vehiclePosition1D.getTrackID());
		Pose2D vehiclePose = track.getPose2D(vehiclePosition1D.getOffset());
		Coordinate vehiclePosition = vehiclePose.getPosition();
		Vector2D vehicleOrientation = vehiclePose.getOrientation();

		for (int i = 0, numSensors = snapshots.size(); i < numSensors; i++) {
			LidarSnapshot currSnapshot = snapshots.get(i);
			LidarSnapshot nextSnapshot = snapshots.get((i+1) % numSensors);

			LidarSpecification currSensor = lidarSpecs.get(i);
			LidarSpecification nextSensor = lidarSpecs.get((i+1) % numSensors);

			Vector2D currSensorOrientation = currSensor.getAbsoluteOrientation(vehicleOrientation);
			Coordinate currSensorPosition = currSensor.getAbsolutePosition(vehiclePosition, vehicleOrientation);
			double currRange = Math.min(currSensor.getRange(), ASSUMED_MIN_CAR_WIDTH / Math.tan(currSensor.getStepSize()));

			Vector2D nextSensorOrientation = nextSensor.getAbsoluteOrientation(vehicleOrientation);
			Coordinate nextSensorPosition = nextSensor.getAbsolutePosition(vehiclePosition, vehicleOrientation);
			double nextRange = Math.min(nextSensor.getRange(), ASSUMED_MIN_CAR_WIDTH / Math.tan(nextSensor.getStepSize()));

			int numCoords = computeSingleSensorEmptiness(
					currSnapshot, 
					currSensor,
					currRange,
					currSensorPosition,
					currSensorOrientation,
					emptyRing);

			Coordinate currBeamEnd;

			if(numCoords > 0) {
				// regular ranging sensor 
				currBeamEnd = emptyRing.get(emptyRing.size()-1); 
			} else {
				// single-beam ranging sensor
				// we have not found any boundaries yet for this sensor
				currBeamEnd = computeBeamEnd(
						0, 
						Math.min(currSnapshot.getDistance(0), currRange), 
						currSensor, 
						currSensorPosition, 
						currSensorOrientation);
			}

			Coordinate nextBeamEnd = computeBeamEnd(
					0, 
					Math.min(nextSnapshot.getDistance(0), nextRange), 
					nextSensor, 
					nextSensorPosition, 
					nextSensorOrientation);

			LineSegment currBeam = new LineSegment(currSensorPosition, currBeamEnd);
			LineSegment nextBeam = new LineSegment(nextSensorPosition, nextBeamEnd);
			Coordinate beamIntersection = currBeam.lineIntersection(nextBeam);
			double baseDistance = currSensorPosition.distance(nextSensorPosition);
			double beamDistance = currBeam.distancePerpendicular(nextSensorPosition);
			double currBeamLength = currBeam.getLength();
			double currBeamLength2 = currBeamLength*currBeamLength;

			if (beamIntersection == null || GeoUtil.distance2(currSensorPosition, beamIntersection) > 1000*1000) {
				// Beams are perpendicular or almost perpendicular

				if (beamDistance < ASSUMED_MIN_CAR_WIDTH) {
					// Car could not fit in between the beams
					Coordinate nextEndProjOnCurr = currBeam.project(nextBeamEnd);

					if (GeoUtil.distance2(currSensorPosition, nextEndProjOnCurr) < currBeamLength2) {
						// Next beam stops before current beam
						emptyRing.add(nextEndProjOnCurr);
						emptyRing.add(nextBeamEnd);
					} else {
						// Current beam stops before next beam (or equal)
						Coordinate currEndProjOnNext = nextBeam.project(currBeamEnd);
						emptyRing.add(currBeamEnd);
						emptyRing.add(currEndProjOnNext);
					}
				} else {
					// Car could fit in between the beams, add sensor positions to ring
					emptyRing.add(currSensorPosition);
					emptyRing.add(nextSensorPosition);
				}

			} else {
				int divergence = CGAlgorithms.orientationIndex(currSensorPosition, nextSensorPosition, beamIntersection);
				double currInDistance = currSensorPosition.distance(beamIntersection);
				double nextInDistance = nextSensorPosition.distance(beamIntersection);
				double beamAngle = Math.acos((currInDistance * currInDistance + nextInDistance * nextInDistance - baseDistance * baseDistance) / (2 * currInDistance * nextInDistance));
				double maxInDistance = 1 / Math.tan(beamAngle);

				if (divergence < 0) {
					// Beams converge (intersection on rhs of base)
					// Don't plan on supporting this since it does not produce a ring
					throw new IllegalStateException("Sensors with intersecting beams are not supported");
				} else {
					// Beams diverge (intersection on lhs of base)
					emptyRing.add(getBeamEnd(beamIntersection, currBeamEnd, nextBeamEnd, maxInDistance));
					emptyRing.add(getBeamEnd(beamIntersection, nextBeamEnd, currBeamEnd, maxInDistance));
				}
			}


		}
		emptyRing.add(emptyRing.get(0));

		Coordinate[] emtpyShellCoordinates = emptyRing.toArray(new Coordinate[emptyRing.size()]);
		LinearRing emptyShell = GEOM.createLinearRing(emtpyShellCoordinates);
		Polygon emptyPolygon = GEOM.createPolygon(emptyShell, null);
		MultiPolygon result;

		if(positionInaccuracy > 0) {
			BufferOp bufOp = new BufferOp(emptyPolygon, new BufferParameters(BufferParameters.CAP_ROUND, BufferParameters.JOIN_ROUND));
			Geometry emptyGeom = bufOp.getResultGeometry(-positionInaccuracy);

			if(emptyGeom instanceof Polygon) {
				result = GEOM.createMultiPolygon(new Polygon[]{(Polygon) emptyGeom});
			} else if(emptyGeom instanceof MultiPolygon) {
				result = (MultiPolygon) emptyGeom;
			} else {
				result = GEOM.createMultiPolygon(new Polygon[]{});
			}
		} else {
			result = GEOM.createMultiPolygon(new Polygon[]{emptyPolygon});
		}

		return result;
	}

	// Compute point
	Coordinate getBeamEnd(
			Coordinate beamIntersection, 
			Coordinate currBeamEnd, 
			Coordinate otherBeamEnd, 
			double maxDistanceFromIntersection) {

		LineSegment currBeamExtended = new LineSegment(beamIntersection, currBeamEnd);
		LineSegment otherBeamExtended = new LineSegment(beamIntersection, otherBeamEnd);
		Coordinate currBeamMaxCoord = currBeamExtended.pointAlong(maxDistanceFromIntersection / currBeamExtended.getLength());
		Coordinate otherEndProjOnCurr = currBeamExtended.project(otherBeamEnd);
		Coordinate otherBeamMaxCoord = otherBeamExtended.pointAlong(maxDistanceFromIntersection / otherBeamExtended.getLength());
		Coordinate otherMaxProjOnCurr = currBeamExtended.project(otherBeamMaxCoord);

		Coordinate[] options = new Coordinate[] { currBeamEnd, currBeamMaxCoord, otherEndProjOnCurr, otherMaxProjOnCurr };
		int minIndex = 0;

		for (int i = 0; i < options.length; i++) {
			if (GeoUtil.distance2(beamIntersection, options[i]) < GeoUtil.distance2(beamIntersection, options[minIndex])) {
				minIndex = i;
			}
		}

		return options[minIndex];
	}

	int computeSingleSensorEmptiness(
			LidarSnapshot snapshot,
			LidarSpecification spec,
			double range,
			Coordinate sensorPosition,
			Vector2D sensorOrientation,
			List<Coordinate> emptinessBounds) {
		int startSize = emptinessBounds.size();
		int numSkipped = 0;

		// This code is a little messy because it is heavily optimized
		// to be both performant and reduce the number of points in the output
		for (int i = 0, numSteps = snapshot.getNumSteps(); i < numSteps; i++) {
			double minMeas =  Math.min(snapshot.getDistance(i), range);

			if(i > 0) {
				double prevMeas = snapshot.getDistance(i - 1);

				if(prevMeas < minMeas) {
					minMeas = prevMeas;
				}
			}

			if(i < numSteps - 1) {
				double nextMeas = snapshot.getDistance(i + 1);

				if(nextMeas < minMeas) {
					minMeas = nextMeas;
				}
			}

			Coordinate currBeamEnd = computeBeamEnd(
					i,
					minMeas,
					spec,
					sensorPosition,
					sensorOrientation
					);

			if(emptinessBounds.size() > 1) {
				Coordinate lastPoint = emptinessBounds.get(emptinessBounds.size()-1);
				Coordinate secondLastPoint = emptinessBounds.get(emptinessBounds.size()-2); 

				if(numSkipped >= MAX_NUM_SKIP) {
					numSkipped = 0;
				} else if(CGAlgorithms.orientationIndex(
						secondLastPoint, 
						currBeamEnd,
						lastPoint) != 1
						&& CGAlgorithms.distancePointLinePerpendicular(
								lastPoint, 
								secondLastPoint, 
								currBeamEnd) < 1.0) {
					emptinessBounds.set(emptinessBounds.size()-1, currBeamEnd);
					numSkipped += 1;
					continue;
				}
			} 
			emptinessBounds.add(currBeamEnd);
		}

		return emptinessBounds.size() - startSize;
	}

	Coordinate computeBeamEnd(
			int beamIndex,
			double beamLength,
			LidarSpecification spec,
			Coordinate sensorPosition,
			Vector2D sensorOrientation) {

		// Get beam vector relative to sensor orientation
		Vector2D beamVector = spec.getBeamOrientation(beamIndex);

		// Translate beam vector to absolute orientation
		double beamDX = sensorOrientation.getX() * beamVector.getX() - sensorOrientation.getY() * beamVector.getY();
		double beamDY = sensorOrientation.getY() * beamVector.getX() + sensorOrientation.getX() * beamVector.getY();

		// Translate sensor position to end of beam
		Coordinate beamTip = new Coordinate(
				beamDX * beamLength + sensorPosition.x,
				beamDY * beamLength + sensorPosition.y
				);

		return beamTip;
	}

	public boolean isEmpty(ConflictArea conflictArea) {
		return measureEmptiness1D().contains(conflictArea.asTrackMapArea(trackMap));
	}



}
