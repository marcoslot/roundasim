/**
 * 
 */
package dsg.rounda.controllers;

import java.util.List;
import java.util.Random;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import de.trafficsimulation.Constants;
import de.trafficsimulation.IDM;
import de.trafficsimulation.IDMCar;
import dsg.rounda.Handler;
import dsg.rounda.logging.VehicleEventLog;
import dsg.rounda.model.Actuators;
import dsg.rounda.model.Clock;
import dsg.rounda.model.Job;
import dsg.rounda.model.LocalizationSensors;
import dsg.rounda.model.Message;
import dsg.rounda.model.NetworkAdapter;
import dsg.rounda.model.RangingSensors;
import dsg.rounda.model.RangingSnapshot;
import dsg.rounda.model.Scheduler;
import dsg.rounda.model.SensorSnapshotAndConfig;
import dsg.rounda.model.Track;
import dsg.rounda.model.VehicleCapabilities;
import dsg.rounda.services.comm.beaconing.BeaconReceiver;
import dsg.rounda.services.comm.beaconing.BeaconSender;
import dsg.rounda.services.comm.beaconing.Beaconer;
import dsg.rounda.services.comm.neighbourhood.NeighbourState;
import dsg.rounda.services.comm.neighbourhood.NeighbourhoodWatch;
import dsg.rounda.services.roadmap.TrackMapArea1D;
import dsg.rounda.services.roadmap.TrackMapBoundaries1D;
import dsg.rounda.services.roadmap.TrackPoint1D;
import dsg.rounda.services.roadmap.VehicleTrackMap;
import dsg.rounda.services.sensing.distance.CommRadar;
import dsg.rounda.services.sensing.distance.CommRadarResult;
import dsg.rounda.services.sensing.distance.EmptyAreaRadar;
import dsg.rounda.services.sensing.distance.RangingFooter;

/**
 * @author slotm
 *
 */
public class DizzyController implements VehicleController, dsg.rounda.Constants {
    
    public static final VehicleControllerFactory FACTORY = new VehicleControllerFactory() {
        @Override
        public VehicleController createController(VehicleCapabilities capabilities) {
            return new DizzyController(capabilities);
        }
    };

    private static final GeometryFactory GEOM = new GeometryFactory(); 
    
    private static final double LANE_CHANGE_INTERVAL = 5; // seconds
    private static final double BEACONING_INTERVAL = 0.5; // seconds
    private static final double VELOCITY_UPDATE_INTERVAL = 0.2; // seconds
    private static final double LIDAR_UPDATE_INTERVAL = 0.1; // seconds
    private static final double MAX_VELOCITY = 30.0; // m/s
    private static final double MIN_VELOCITY = 5.0; // m/s

    final VehicleCapabilities capabilities;
    final int id;
    final Clock clock;
    final Scheduler scheduler;
    final NetworkAdapter network;
    final Random random;
    final LocalizationSensors localization;
    final RangingSensors rangers;
    final EmptyAreaRadar ear;
    final Actuators actuators;
    final VehicleTrackMap roadMap;
    final VehicleEventLog eventLog;

    final Beaconer beaconer;
    final NeighbourhoodWatch neighbourhoodWatch;
    final CommRadar commRadar;
    final IDM carFollowingModel;

    SensorSnapshotAndConfig latestSnapshot;
    
    /**
     * @param capabilities
     */
    public DizzyController(VehicleCapabilities capabilities) {
        this.capabilities = capabilities;
        this.id = capabilities.getId();
        this.clock = capabilities.getClock();
        this.scheduler = capabilities.getScheduler();
        this.network = capabilities.getNetwork();
        this.localization = capabilities.getLocalizationSensors();
        this.rangers = capabilities.getRangingSensors();
        this.actuators = capabilities.getActuators();
        this.roadMap = capabilities.getRoadMap();
        this.random = capabilities.getRandom();
        this.eventLog = capabilities.getEventLog();
        this.beaconer = capabilities.getService(Beaconer.type());
        this.beaconer.addSender(rangingSnapshotSender);
        this.beaconer.addReceiver(rangingSnapshotReceiver);
        this.neighbourhoodWatch = capabilities.getService(NeighbourhoodWatch.type());
        this.commRadar = capabilities.getService(CommRadar.type());
        this.ear = capabilities.getService(EmptyAreaRadar.type());
        this.carFollowingModel = new IDMCar();
        this.carFollowingModel.set_v0(MAX_VELOCITY);
    }

    public void start() {
        // Change lanes periodically
        scheduler.schedule(new Job(
                changeLanes, 
                id, 
                clock.getTime() + LANE_CHANGE_INTERVAL * random.nextDouble() * SECONDS, 
                LANE_CHANGE_INTERVAL * SECONDS));

        // Adapt velocity to distance to predecessor periodically 
        scheduler.schedule(new Job(
                adjustVelocity(), 
                id, 
                clock.getTime() + VELOCITY_UPDATE_INTERVAL * random.nextDouble() * SECONDS,
                VELOCITY_UPDATE_INTERVAL * SECONDS));

        rangers.addSnapshotHandler(useLidar);
        
        // Start beaconing after a random delay 
        scheduler.schedule(new Job(
                startBeaconing, 
                id, 
                clock.getTime() + BEACONING_INTERVAL * random.nextDouble() * SECONDS));
    }
    
    private double roundUp(double value, double factor) {
      final double up =
          value < 0 ? Math.ceil(value / factor) * factor : Math.floor(value / factor) * factor;
      // need to normalize -0 with +0 or we get a silly "-0" on our scale! :(
      return up == 0L ? 0.0d : up;
    }
    
    final Runnable startBeaconing = new Runnable() {
        @Override
        public void run() {
            beaconer.start(BEACONING_INTERVAL*SECONDS);
            neighbourhoodWatch.start();
        }
    };


    final BeaconSender rangingSnapshotSender = new BeaconSender() {
        @Override
        public void prepareBeacon(Message beacon) {
            if(latestSnapshot == null) {
                return;
            }
            
            beacon.addFooter(new RangingFooter(
                    latestSnapshot.getRangingSpecs(),
                    latestSnapshot.getRanges()
            ));     
        }
    };
    
    final BeaconReceiver rangingSnapshotReceiver = new BeaconReceiver() {
        @Override
        public void receiveBeacon(Message beacon) {
            
        }
    };

    final Handler<RangingSnapshot> useLidar = new Handler<RangingSnapshot>() {
        
        @Override
        public void handle(RangingSnapshot ranges) {
            TrackPoint1D position = localization.getPosition();
            
            eventLog.log("rangers", new SensorSnapshotAndConfig(position, ranges, rangers.getSpecification()));

        }
    };


    final Runnable changeLanes = new Runnable() {
        @Override
        public void run() {
            TrackPoint1D position = localization.getPosition();
            Track road = roadMap.getRoad(position.getTrackID());
            List<Track> changes = roadMap.getTracksStartingFrom(road.getId(), position.getOffset());

            if(!changes.isEmpty()) {
                Track laneChangeRoad = changes.get(0);
                actuators.addTrackToFollow(laneChangeRoad);
            }
        }
    };

    Runnable adjustVelocity() {
        return new Runnable() {
            @Override
            public void run() {
                final double currentVelocity = localization.getVelocity().getRoadVelocity();
                CommRadarResult vehicleAhead = commRadar.findVehicleAhead1D();
                NeighbourState vehicleAheadState = vehicleAhead.getState();

                double acceleration; // m/s^2
                double spacing = vehicleAhead.getDistance() - 4; // (4 = current car length)

                if(spacing < 1.0 * currentVelocity) {
                    // Less than 1 seconds distance, emergency break
                    acceleration = -Constants.MAX_BRAKING;
                } else {
                    double velocityAhead;
                    
                    if(vehicleAheadState != null) {
                        velocityAhead = vehicleAheadState.getVelocity1D().getRoadVelocity();
                    } else {
                        // No vehicle measured, assume the next vehicle is driving at max velocity
                        velocityAhead = MAX_VELOCITY;
                    }
                    
                    acceleration = carFollowingModel.calcAcc(
                            currentVelocity, 
                            velocityAhead, 
                            spacing);
                }

                double newVelocity = currentVelocity + acceleration * VELOCITY_UPDATE_INTERVAL;

                newVelocity = Math.max(newVelocity, MIN_VELOCITY);
                newVelocity = Math.min(newVelocity, MAX_VELOCITY);

                actuators.setRoadVelocity(newVelocity);
            }
        };
    }



}
