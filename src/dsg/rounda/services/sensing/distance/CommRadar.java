/**
 * 
 */
package dsg.rounda.services.sensing.distance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dsg.rounda.Constants;
import dsg.rounda.model.Actuators;
import dsg.rounda.model.Clock;
import dsg.rounda.model.LocalizationSensors;
import dsg.rounda.model.Position1D;
import dsg.rounda.model.Trajectory1D;
import dsg.rounda.model.VehicleCapabilities;
import dsg.rounda.services.AbstractServiceFactory;
import dsg.rounda.services.ServiceFactory;
import dsg.rounda.services.comm.neighbourhood.NeighbourState;
import dsg.rounda.services.comm.neighbourhood.NeighbourhoodWatch;
import dsg.rounda.services.roadmap.RoutePredictor;
import dsg.rounda.services.roadmap.TrackPoint1D;
import dsg.rounda.services.roadmap.TrackRange1D;
import dsg.rounda.services.roadmap.VehicleTrackMap;

/**
 * Measures forward distance using only communication (unreliable!)
 */
public class CommRadar implements Constants {

    static final ServiceFactory FACTORY = new AbstractServiceFactory(type()) {
        @Override
        public Object create(VehicleCapabilities capabilities) {
            return new CommRadar(capabilities);
        }
    };

    public static Class<CommRadar> type() {
        return CommRadar.class;
    }
    
    static final double DEFAULT_MAX_DISTANCE = 60; // m
    static final double MAX_NEIGHBOUR_STATE_AGE = 1.0 * SECONDS; 
    
    final int identity;
    final Clock clock;
    final LocalizationSensors sensors;
    final Actuators actuators;
    final RoutePredictor router;
    final NeighbourhoodWatch watch;
    final VehicleTrackMap roadMap;
    
    double maxDistance;
    
    /**
     * @param capabilities
     * @param watch
     */
    public CommRadar(
            VehicleCapabilities capabilities) {
        super();
        this.identity = capabilities.getId();
        this.clock = capabilities.getClock();
        this.sensors = capabilities.getLocalizationSensors();
        this.actuators = capabilities.getActuators();
        this.roadMap = capabilities.getRoadMap();
        this.router = capabilities.getService(RoutePredictor.type());
        this.watch = capabilities.getService(NeighbourhoodWatch.type());
        this.maxDistance = DEFAULT_MAX_DISTANCE;
    }
    
    /**
     * @return the maxDistance
     */
    public double getMaxDistance() {
        return maxDistance;
    }

    /**
     * @param maxDistance the maxDistance to set
     */
    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }
    
    private static class NeighbourExpectation {
        final int neighbourID;
        final Position1D expectedPosition;
        
        public NeighbourExpectation(int neighbourID, Position1D expectedPosition) {
            this.neighbourID = neighbourID;
            this.expectedPosition = expectedPosition;
        }
    }
    
    private static class TrackRangeDistance {
        final double distance;
        final TrackRange1D range;

        public TrackRangeDistance(double distance, TrackRange1D range) {
            this.distance = distance;
            this.range = range;
        }
    }
    
    /**
     * Find the nearest vehicle on the expected trajectory in 1D. 
     * 
     * @return the vehicle and the distance
     */
    public CommRadarResult findVehicleAhead1D() {
        double currentTime = clock.getTime();
        TrackPoint1D currentPosition = sensors.getPosition();
        Trajectory1D currentTrajectory = actuators.getTrajectory();
        
        List<TrackRange1D> route = router.predictRoute(
                currentPosition, 
                currentTrajectory, 
                this.maxDistance
        );
        
        Map<Integer,TrackRangeDistance> routeTable = new HashMap<Integer,TrackRangeDistance>();
        
        double distanceFromStart = 0.0;
        
        for(TrackRange1D range : route) {
            TrackRangeDistance trd = new TrackRangeDistance(distanceFromStart, range);
            routeTable.put(range.getTrackID(), trd);
            distanceFromStart += range.getLength();
        }
        
        NeighbourState closestNeighbour = null;
        double minDistance = DEFAULT_MAX_DISTANCE;

        for(NeighbourState neighbourState : watch.getNeighbours()) {
            double age = currentTime - neighbourState.getTime();
            
            if(age > MAX_NEIGHBOUR_STATE_AGE) {
                continue;
            }
            
            Position1D expectedNeighbourPosition = router.predictPosition(
                    neighbourState.getPosition1D(),
                    new Trajectory1D(), // don't know the trajectory 
                    neighbourState.getVelocity1D().getRoadVelocity() * age / SECONDS
            );
            
            if(expectedNeighbourPosition.getTrack() == null) {
                // We expect this vehicle to be destroyed!
                continue;
            }
            
            TrackRangeDistance trd = routeTable.get(expectedNeighbourPosition.getTrack().getId());
            
            if(trd == null) {
                continue;
            }
            
            double neighbourOffset = expectedNeighbourPosition.getOffset();
            
            if(!trd.range.contains(neighbourOffset)) {
                continue;
            }
            
            double distance = trd.distance + (neighbourOffset - trd.range.getStart());
            
            if(distance < minDistance) {
                minDistance = distance;
                closestNeighbour = neighbourState;
            }
        }

        
        return new CommRadarResult(closestNeighbour, minDistance);
    }
    
    
}
