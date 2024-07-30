/**
 * 
 */
package dsg.rounda.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dsg.rounda.model.VehicleRouter.NoopTracer;
import dsg.rounda.services.roadmap.TrackPoint1D;
import dsg.rounda.services.roadmap.TrackRangeSequence;
import dsg.rounda.services.roadmap.TrackRangeSequenceCreatingTracer;
import dsg.rounda.services.roadmap.VehicleTrackMap;

/**
 * @author Marco
 *
 */
public class IndicatorDetector {

	public static final double FORWARD_LOOKING_DISTANCE = 60;
	final WorldState world;
	final VehicleState self;
	final TrackNetwork tracks;
	final VehicleTrackMap trackMap;
	final VehicleRouter router;
	private LaneChangeDirection additionalLaneDetection;

	public IndicatorDetector(WorldState world, VehicleState vehicle, VehicleTrackMap trackMap) {
		super();
		this.world = world;
		this.self = vehicle;
		this.tracks = world.getRoadNetwork();
		this.trackMap = trackMap;
		this.router = new VehicleRouter(tracks);
	}
	
	public IndicatorObservation getClosestObservation() {
		List<IndicatorObservation> observations = observe();
		
		if(observations.isEmpty()) {
			return null;
		}
		
		return Collections.min(observations);
	}

	public void setAdditionalLaneDetection(
			LaneChangeDirection additionalLaneDetection) {
		this.additionalLaneDetection = additionalLaneDetection;
	}

	public List<IndicatorObservation> observe() {
		List<IndicatorObservation> result = new ArrayList<IndicatorObservation>();

		Position1D currentPos = self.getBackPosition();

		TrackRangeSequence myArea = getForwardArea(currentPos);
		
		TrackRangeSequence targetLaneArea; 
		
		if(additionalLaneDetection != null) {
			Position1D projectedPos = tracks.translateOffset(currentPos, additionalLaneDetection);
			targetLaneArea = getForwardArea(projectedPos);
		} else {
			targetLaneArea = null;
		}
		
		for(VehicleState vehicle : world.getVehicles()) {
			if(vehicle.getId() == self.getId()) {
				continue;
			}
			
			IndicatorState indicator = vehicle.getIndicatorState();
			LaneChangeDirection laneChangeDirection = indicator == IndicatorState.LEFT ? LaneChangeDirection.LEFT : LaneChangeDirection.RIGHT;  

			if(indicator != IndicatorState.LEFT && indicator != IndicatorState.RIGHT) {
				continue;
			}

			TrackPoint1D vehiclePosition = toTrackPoint(vehicle.getBackPosition());
			TrackPoint1D projectedPosition = trackMap.translateToOtherLane(vehiclePosition, laneChangeDirection);
			
			if(projectedPosition == null) {
				// Still indicating, but there is no longer a lane
				continue;
			}
			
			Double vehicleDistance = myArea.getDistanceFromStart(projectedPosition);
			
			if(vehicleDistance == null) {
				if(targetLaneArea == null) {
					continue;
				}
				
				vehicleDistance = targetLaneArea.getDistanceFromStart(projectedPosition);
				
				if(vehicleDistance == null) {
				    continue;
				}
			}

			result.add(new IndicatorObservation(indicator, vehicleDistance));
		}
		
		Collections.sort(result);

		return result;
	}

	private TrackPoint1D toTrackPoint(Position1D pos) {
		return new TrackPoint1D(pos.getTrackID(), pos.getOffset());
	}

	TrackRangeSequence getForwardArea(Position1D start) {
		if(start == null) {
			return null;
		}
		
		TrackRangeSequenceCreatingTracer tracer = new TrackRangeSequenceCreatingTracer(trackMap);

		router.route(
				start, 
				new Trajectory1D(), 
				FORWARD_LOOKING_DISTANCE, 
				tracer);

		return tracer.getTrackRangeSequence();
	}

}
