/**
 * 
 */
package dsg.rounda.services.roadmap;

import java.util.ArrayList;
import java.util.List;

import dsg.rounda.model.Actuators;
import dsg.rounda.model.LocalizationSensors;
import dsg.rounda.model.Position1D;
import dsg.rounda.model.Track;
import dsg.rounda.model.Trajectory1D;
import dsg.rounda.model.VehicleCapabilities;
import dsg.rounda.model.VehicleRouter;
import dsg.rounda.services.AbstractServiceFactory;
import dsg.rounda.services.ServiceFactory;


/**
 * Predicts of how the vehicle will drive 
 * 
 * Currently uses an extremely accurate method,
 * namely calling the same function used by the
 * simulator.
 */
public class RoutePredictor {

    static final ServiceFactory FACTORY = new AbstractServiceFactory(type()) {
        @Override
        public Object create(VehicleCapabilities capabilities) {
            return new RoutePredictor(capabilities);
        }
    };

    public static Class<RoutePredictor> type() {
        return RoutePredictor.class;
    }
    
    final LocalizationSensors sensors;
    final Actuators actuators;
    final VehicleRouter router;
    final VehicleTrackMap roadMap;
    
    /**
     * @param vehicle
     */
    public RoutePredictor(VehicleCapabilities capabilities) {
        super();
        this.router = new VehicleRouter(capabilities.getRoadMap());
        this.sensors = capabilities.getLocalizationSensors();
        this.actuators = capabilities.getActuators();
        this.roadMap = capabilities.getRoadMap();
    }

    /**
     * Get a sequence of track ranges the vehicle is about to follow
     */
    public TrackRangeSequence predictRoute(
            Position1D vehiclePosition,
            Trajectory1D vehicleTrajectory, 
            double distanceToDrive) {
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
                new Position1D(vehiclePosition), 
                new Trajectory1D(vehicleTrajectory), 
                distanceToDrive,
                tracer
        );
        
        return new TrackRangeSequence(roadMap, result);
    }

    /**
     * Get a sequence of track ranges the vehicle is about to follow
     */
    public double distanceUntilArea(
            TrackPoint1D vehiclePosition,
            Trajectory1D vehicleTrajectory, 
            TrackMapArea1D areaNotToDrive) {
        final double[] result = new double[1];
        
        VehicleRouter.Tracer tracer = new VehicleRouter.Tracer() {
            double startOffset;
            
            @Override
            public void startOnTrack(int trackID, double startOffset) {
                this.startOffset = startOffset;
            }

            @Override
            public void endOnTrack(int trackID, double endOffset, boolean trackChange) {
                result[0] += endOffset - startOffset;
            }
        };

        Track track = roadMap.getRoad(vehiclePosition.getTrackID());

        router.routeUntil(
                new Position1D(track, vehiclePosition.getOffset()), 
                new Trajectory1D(vehicleTrajectory), 
                areaNotToDrive,
                tracer
        );
        
        return result[0];
    }

    public TrackRangeSequence predictRouteUntilArea(TrackMapArea1D areaNotToDrive) {
    	TrackRangeSequenceCreatingTracer tracer = new TrackRangeSequenceCreatingTracer(roadMap);

    	TrackPoint1D vehiclePosition = sensors.getPosition();
        Track track = roadMap.getRoad(vehiclePosition.getTrackID());

        router.routeUntil(
                new Position1D(track, vehiclePosition.getOffset()), 
                new Trajectory1D(actuators.getTrajectory()), 
				areaNotToDrive, 
				tracer);
        
        return tracer.getTrackRangeSequence();
	}

	/**
     * Get a sequence of track ranges the vehicle is about to follow
     */
    public TrackRangeSequence predictRoute(
            TrackPoint1D vehiclePosition,
            Trajectory1D vehicleTrajectory, 
            TrackMapArea1D areaToDrive) {
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

        Track track = roadMap.getRoad(vehiclePosition.getTrackID());

        router.route(
                new Position1D(track, vehiclePosition.getOffset()), 
                new Trajectory1D(vehicleTrajectory), 
                areaToDrive,
                tracer
        );
        
        return new TrackRangeSequence(roadMap, result);
    }

    public TrackRangeSequence predictRoute(
            TrackPoint1D vehiclePosition,
            Trajectory1D vehicleTrajectory, 
            double maxDistance) {
        
        Track track = roadMap.getRoad(vehiclePosition.getTrackID());

        return predictRoute(
                new Position1D(track, vehiclePosition.getOffset()), 
                vehicleTrajectory, 
                maxDistance
        );
    }

    public TrackRangeSequence predictRoute(
            double maxDistance) {
        
        return predictRoute(
                sensors.getPosition(), 
                actuators.getTrajectory(), 
                maxDistance
        );
    }

    /**
     * Get the position the vehicle is expected to be in
     */
    public Position1D predictPosition(
            Position1D vehiclePosition,
            Trajectory1D vehicleTrajectory, 
            double distanceToDrive) {

        return router.route(
                new Position1D(vehiclePosition), 
                new Trajectory1D(vehicleTrajectory), 
                distanceToDrive
        );
    }


    public TrackPoint1D predictTrackPoint1D(
            TrackPoint1D vehiclePosition,
            Trajectory1D vehicleTrajectory, 
            double distanceToDrive) {

    	Track track = roadMap.getRoad(vehiclePosition.getTrackID());
    	
        Position1D result = router.route(
                new Position1D(track, vehiclePosition.getOffset()), 
                new Trajectory1D(vehicleTrajectory), 
                distanceToDrive
        );
        
        return new TrackPoint1D(result.getTrackID(), result.getOffset());
    }

    public Position1D predictPosition(
            TrackPoint1D vehiclePosition,
            Trajectory1D vehicleTrajectory, 
            double distanceToDrive) {

        Track track = roadMap.getRoad(vehiclePosition.getTrackID());

        return predictPosition(
                new Position1D(track, vehiclePosition.getOffset()),
                vehicleTrajectory, 
                distanceToDrive
        );
    }

    
}
