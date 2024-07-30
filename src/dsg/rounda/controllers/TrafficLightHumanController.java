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
import dsg.rounda.logging.VehicleEventLog;
import dsg.rounda.model.Actuators;
import dsg.rounda.model.Clock;
import dsg.rounda.model.InfrastructureMap;
import dsg.rounda.model.LocalizationSensors;
import dsg.rounda.model.RangingSensors;
import dsg.rounda.model.RangingSnapshot;
import dsg.rounda.model.Scheduler;
import dsg.rounda.model.Track;
import dsg.rounda.model.VehicleCapabilities;
import dsg.rounda.model.VehicleProperties;
import dsg.rounda.model.Velocity1D;
import dsg.rounda.services.roadmap.ConnectorGraph;
import dsg.rounda.services.roadmap.MapDijkstra;
import dsg.rounda.services.roadmap.MapEdge;
import dsg.rounda.services.roadmap.MapNode;
import dsg.rounda.services.roadmap.RoutePredictor;
import dsg.rounda.services.roadmap.TrackPoint1D;
import dsg.rounda.services.roadmap.TrackRange1D;
import dsg.rounda.services.roadmap.TrackRangeSequence;
import dsg.rounda.services.roadmap.VehicleTrackMap;
import dsg.rounda.services.sensing.distance.EmptyAreaRadar;
import dsg.rounda.services.trafficcontrol.TrafficLight;

/**
 * A human mimicking controller that respects traffic lights
 */
public class TrafficLightHumanController  implements VehicleController, dsg.rounda.Constants {
    
    public static final VehicleControllerFactory FACTORY = new VehicleControllerFactory() {
        @Override
        public VehicleController createController(VehicleCapabilities capabilities) {
            return new TrafficLightHumanController(capabilities);
        }
    };

    private static final double MAX_VELOCITY = 20.0; // m/s
    private static final double SAFE_DISTANCE = 2; // m
    
    final int vehicleID;
    final VehicleProperties properties;
    final Clock clock;
    final Scheduler scheduler;
    final LocalizationSensors localization;
    final InfrastructureMap infrastructure;
    final VehicleEventLog eventLog;
    final Actuators actuators;
    final VehicleTrackMap trackMap;
    final RangingSensors rangers;
    final Random random;
    
    final EmptyAreaRadar ear;
    final IDM carFollowingModel;
    final RoutePredictor routePredictor;

    public TrafficLightHumanController(VehicleCapabilities capabilities) {
        this.vehicleID = capabilities.getId();
        this.properties = capabilities.getProperties();
        this.clock = capabilities.getClock();
        this.scheduler = capabilities.getScheduler();
        this.eventLog = capabilities.getEventLog();
        this.localization = capabilities.getLocalizationSensors();
        this.actuators = capabilities.getActuators();
        this.trackMap = capabilities.getRoadMap();
        this.random = capabilities.getRandom();
        this.rangers = capabilities.getRangingSensors();
        this.infrastructure = capabilities.getInfrastructureMap();
        this.ear = capabilities.getService(EmptyAreaRadar.type());
        this.routePredictor = capabilities.getService(RoutePredictor.type());
        this.carFollowingModel = new IDMCar();
        this.carFollowingModel.set_v0(MAX_VELOCITY);
    }

    @Override
    public void start() {
        findRoute();
        rangers.addSnapshotHandler(snapshotHandler);
    }

    void findRoute() {
        int currentTrackID = localization.getPosition().getTrackID();
        ConnectorGraph graph = trackMap.getConnectorGraph();
        MapNode startNode = graph.getStartNode(currentTrackID);
        MapDijkstra dijkstra = new MapDijkstra(startNode);
        List<MapNode> exits = new ArrayList<MapNode>(dijkstra.getExits());
        List<MapEdge> path = dijkstra.getPath(exits.get(random.nextInt(exits.size())));
        
        for(MapEdge edge : path) {
            if(edge.getTrackID() != currentTrackID) {
                Track track = trackMap.getRoad(edge.getTrackID());
                
                if(track.getFrom() != null && track.getFrom().getRoad() == currentTrackID) {
                    // We only add tracks to the trajectory if they explicitly start
                    // on a track we are on.
                    actuators.addTrackToFollow(track);
                }
            }
            currentTrackID = edge.getTrackID();
        }
    }
    
    final Handler<RangingSnapshot> snapshotHandler = new Handler<RangingSnapshot>() {
        
        // Latest sensor results
        TrackPoint1D backPosition;
        TrackPoint1D frontPosition;
        Velocity1D currentVelocity;
        double measuredDistance;
        double oldMeasuredDistance;
        double availableDistance;
        TrackRangeSequence emptyRoute;
        
        
        @Override
        public void handle(RangingSnapshot ranges) {
            try {
                perception();
                
                if(frontPosition == null) {
                    // About to die
                    return;
                }

                availableDistance = measuredDistance;

                considerTrafficLights();
                considerWorldEnd();

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

        void perception() {
            backPosition = localization.getPosition();
            frontPosition = localization.getFrontPosition();
            currentVelocity = localization.getVelocity();
            emptyRoute = ear.measureEmptyRoute1D();
            measuredDistance = emptyRoute.getLength();

            eventLog.log("track", backPosition.getTrackID());
            eventLog.log("offset", backPosition.getOffset());
            eventLog.log("velocity", currentVelocity.getRoadVelocity());
            eventLog.log("acceleration", currentVelocity.getAcceleration());
            eventLog.log("measured-distance", measuredDistance - properties.getLength());
        }

        double acceleration() {
            double spacing = availableDistance - properties.getLength() - SAFE_DISTANCE;

            double stoppingDistance = currentVelocity.computeStoppingDistance();
            
            if(spacing <= stoppingDistance) { 
                // emergency break
                return -Velocity1D.MAX_DECELERATION;
            } 
            
            if(currentVelocity.getRoadVelocity() < 5 // m/s
            && spacing > 10) { //m
                // Standing start
                return 15; // m/s
            }

            double distanceChangeSpeed = (measuredDistance - oldMeasuredDistance) / rangers.getInterval();
            double vehicleVelocity = Math.min(Math.max(distanceChangeSpeed + currentVelocity.getRoadVelocity(), 0), 30);
            
            if(availableDistance > measuredDistance) {
                // only the case when we reach the end of the world
                vehicleVelocity = MAX_VELOCITY;
            }
            
            oldMeasuredDistance = measuredDistance;

            return carFollowingModel.calcAcc(
                    currentVelocity.getRoadVelocity(), 
                    vehicleVelocity, 
                    spacing);
        }

        private void considerTrafficLights() {
            for(TrafficLight trafficLight : infrastructure.getTrafficLights()) {
                Double distance = emptyRoute.getDistanceFromStart(trafficLight.getPosition());
                
                if(distance != null && distance < availableDistance) {
                    if(trafficLight.getState() == TrafficLight.Colour.RED
                    ||(trafficLight.getState() == TrafficLight.Colour.ORANGE && currentVelocity.computeDelayedStoppingDistance(0.2) < distance)) {
                        availableDistance = distance;
                    }
                }
            }
        }

        void considerWorldEnd() {
            if(!emptyRoute.isEmpty()) {
                TrackRange1D lastRange = emptyRoute.get(emptyRoute.size()-1);
                Track lastTrack = trackMap.getRoad(lastRange.getTrackID());
                
                if(lastTrack.getTo() == null
                && lastRange.getEnd() >= lastTrack.getPathLength()
                && emptyRoute.getLength() <= availableDistance) {
                    // route runs off the world
                    availableDistance = Double.POSITIVE_INFINITY;
                }
            } 
        }


    };
    

}
