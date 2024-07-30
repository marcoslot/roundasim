/**
 * 
 */
package dsg.rounda.model;

import java.util.ArrayList;
import java.util.List;

import dsg.rounda.services.roadmap.TrackPoint1D;
import dsg.rounda.services.roadmap.TrackRange1D;
import dsg.rounda.services.roadmap.TrackRangeSequence;
import dsg.rounda.services.roadmap.VehicleTrackMap;

/**
 * @author slotm
 *
 */
public class LocalizationSensors {

    final WorldState world;
    final VehicleState vehicle;
    final VehicleTrackMap trackMap;
    final VehicleRouter router;
    
    /**
     * @param world
     * @param vehicle
     */
    public LocalizationSensors(
            WorldState world, 
            VehicleState vehicle,
            VehicleTrackMap trackMap) {
        this.world = world;
        this.vehicle = vehicle;
        this.trackMap = trackMap;
        this.router = new VehicleRouter(trackMap);
    }

    public TrackPoint1D getPosition() {
        // Very precise GPS simulation
        Position1D position = vehicle.getBackPosition();
        return position.toTrackPoint();
    }

    public TrackRangeSequence getPositionRange() {
        Position1D position = new Position1D(vehicle.getBackPosition());
        Trajectory1D traj = new Trajectory1D(vehicle.getTrajectory());

        // Bit redundant with RoutePredictor, but can't access a service from 
        // a device and VehicleRouter is not supposed to know about maps
        final List<TrackRange1D> result = new ArrayList<TrackRange1D>();
        
        VehicleRouter.Tracer tracer = new VehicleRouter.Tracer() {
            int startTrackID;
            double startOffset;
            
            @Override
            public void startOnTrack(int trackID, double startOffset) {
                this.startTrackID = trackID;
                this.startOffset = startOffset;
            }

            @Override
            public void endOnTrack(int trackID, double endOffset, boolean trackChange) {
                result.add(new TrackRange1D(startTrackID, startOffset, endOffset));
            }
        };
        
        router.route(
                position, 
                traj, 
                vehicle.getLength(),
                tracer
        );
        
        return new TrackRangeSequence(trackMap, result);
    }
    public TrackPoint1D getFrontPosition() {
        Position1D position = new Position1D(vehicle.getBackPosition());
        Trajectory1D traj = new Trajectory1D(vehicle.getTrajectory());
        Position1D frontPosition = router.route(position, traj, vehicle.getLength());
        return frontPosition.getTrack() == null ? null : frontPosition.toTrackPoint();
    }

    public Velocity1D getVelocity() {
        // Very precise speedometer 
        return vehicle.getVelocity();
    }

}
