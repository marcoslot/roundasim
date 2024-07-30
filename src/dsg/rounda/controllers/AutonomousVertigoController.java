/**
 * 
 */
package dsg.rounda.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.trafficsimulation.IDM;
import de.trafficsimulation.IDMCar;
import dsg.rounda.Handler;
import dsg.rounda.logging.VehicleEventLog;
import dsg.rounda.model.Actuators;
import dsg.rounda.model.Clock;
import dsg.rounda.model.LocalizationSensors;
import dsg.rounda.model.Message;
import dsg.rounda.model.NetworkAdapter;
import dsg.rounda.model.RangingSensors;
import dsg.rounda.model.RangingSnapshot;
import dsg.rounda.model.Scheduler;
import dsg.rounda.model.Track;
import dsg.rounda.model.VehicleCapabilities;
import dsg.rounda.model.VehicleProperties;
import dsg.rounda.model.Velocity1D;
import dsg.rounda.services.comm.beaconing.BeaconReceiver;
import dsg.rounda.services.comm.beaconing.Beaconer;
import dsg.rounda.services.comm.neighbourhood.NeighbourState;
import dsg.rounda.services.comm.neighbourhood.NeighbourhoodWatch;
import dsg.rounda.services.coordination.Allocation;
import dsg.rounda.services.coordination.AllocationRequestFooter;
import dsg.rounda.services.coordination.ConflictArea;
import dsg.rounda.services.coordination.ConflictAreaAllocator;
import dsg.rounda.services.coordination.ConflictAreaFinder;
import dsg.rounda.services.coordination.ConflictTrajectory;
import dsg.rounda.services.coordination.ConflictTrajectoryPart;
import dsg.rounda.services.membership.MembershipTuple;
import dsg.rounda.services.membership.MembershipTupleFilter;
import dsg.rounda.services.membership.MembershipView;
import dsg.rounda.services.membership.SingleHopMembershipProtocol;
import dsg.rounda.services.roadmap.ConnectorGraph;
import dsg.rounda.services.roadmap.DecayFlags;
import dsg.rounda.services.roadmap.MapDijkstra;
import dsg.rounda.services.roadmap.MapEdge;
import dsg.rounda.services.roadmap.MapNode;
import dsg.rounda.services.roadmap.RoutePredictor;
import dsg.rounda.services.roadmap.TrackMapArea1D;
import dsg.rounda.services.roadmap.TrackPoint1D;
import dsg.rounda.services.roadmap.TrackRange1D;
import dsg.rounda.services.roadmap.TrackRangeSequence;
import dsg.rounda.services.roadmap.VehicleTrackMap;
import dsg.rounda.services.sensing.distance.EmptyAreaRadar;
import dsg.rounda.services.vertigo.MicroResponse;
import dsg.rounda.services.vertigo.ResultHandler;
import dsg.rounda.services.vertigo.SingleHopVertigo;
import dsg.rounda.services.vertigo.VertigoReceiveHandler;

/**
 * A controller that drives autonomously and uses Vertigo to coordinate with other vehicles
 */
public class AutonomousVertigoController implements VehicleController, dsg.rounda.Constants {
    
    public static final VehicleControllerFactory FACTORY = new VehicleControllerFactory() {
        @Override
        public VehicleController createController(VehicleCapabilities capabilities) {
            return new AutonomousVertigoController(capabilities);
        }
    };

    private static final double MAX_VELOCITY = 20.0; // m/s
    private static final double MAX_DISTANCE_TO_QUERY = 20;  // m
    private static final byte REJECTED = 1;
    private static final byte ACCEPTED = 2;
    private static final byte TENTATIVE = 3;

    private static final double BEACON_INTERVAL = 0.2 * SECONDS; // ns
    private static final double COMMIT_AREA_LENGTH = 3; // m
    private static final double SAFE_DISTANCE = 2; // m
    private static final double JUMP_ACCELERATION = 10; // m/s^2

    final VehicleCapabilities capabilities;
    final VehicleProperties properties;
    final int vehicleID;
    final Clock clock;
    final Scheduler scheduler;
    final NetworkAdapter network;
    final Random random;
    final LocalizationSensors localization;
    final RangingSensors rangers;
    final EmptyAreaRadar ear;
    final Actuators actuators;
    final VehicleTrackMap trackMap;
    final VehicleEventLog eventLog;

    final SingleHopVertigo vertigoInstance;
    final SingleHopMembershipProtocol membership;
    final IDM carFollowingModel;

    final RoutePredictor routePredictor;
    final ConflictAreaAllocator allocator;
    final Beaconer beaconer;
    final NeighbourhoodWatch neighbours;

    /**
     * @param capabilities
     */
    public AutonomousVertigoController(VehicleCapabilities capabilities) {
        this.capabilities = capabilities;
        this.vehicleID = capabilities.getId();
        this.properties = capabilities.getProperties();
        this.clock = capabilities.getClock();
        this.scheduler = capabilities.getScheduler();
        this.network = capabilities.getNetwork();
        this.localization = capabilities.getLocalizationSensors();
        this.rangers = capabilities.getRangingSensors();
        this.actuators = capabilities.getActuators();
        this.trackMap = capabilities.getRoadMap();
        this.random = capabilities.getRandom();
        this.eventLog = capabilities.getEventLog();
        this.membership = capabilities.getService(SingleHopMembershipProtocol.type());
        this.routePredictor = capabilities.getService(RoutePredictor.type());
        this.ear = capabilities.getService(EmptyAreaRadar.type());
        this.neighbours = capabilities.getService(NeighbourhoodWatch.type());
        this.beaconer = capabilities.getService(Beaconer.type());
        this.beaconer.addReceiver(beaconReceiver);
        this.allocator = capabilities.getService(ConflictAreaAllocator.type());
        this.vertigoInstance = capabilities.getService(SingleHopVertigo.type());
        this.vertigoInstance.addReceiver(receiveVertigoQuery);
        this.carFollowingModel = new IDMCar();
        this.carFollowingModel.set_v0(MAX_VELOCITY);
    }

    public void start() { 
        findRoute();
        rangers.addSnapshotHandler(snapshotHandler);
        neighbours.start();
        beaconer.start(BEACON_INTERVAL);
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
    
    final BeaconReceiver beaconReceiver = new BeaconReceiver() {
        @Override
        public void receiveBeacon(Message beacon) {
        }
    };
    
    final Handler<RangingSnapshot> snapshotHandler = new Handler<RangingSnapshot>() {
        
        // Latest sensor results
        TrackPoint1D backPosition;
        TrackPoint1D frontPosition;
        Velocity1D currentVelocity;
        
        double measuredDistance;
        double oldMeasuredDistance;
        double availableDistance;
        double allocatedDistance;
        double jumpDistance;
        TrackRangeSequence emptyRoute;
        
        int nextAllocationID = 0;
        
        long backOffTime = 0L; 
        long backOffStep = 1*SECONDS;
        double commFactor = 1;
        
        @Override
        public void handle(RangingSnapshot ranges) {
            try {
                perception();
                
                if(frontPosition == null) {
                    // About to die
                    return;
                }
                
                availableDistance = measuredDistance;
                 
                prepareForConflict();
                considerAllocatedDistance();
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
                jumpDistance = 0;
                return -Velocity1D.MAX_DECELERATION;
            } 
            
            if(jumpDistance > 0) {
                // Even though we may only have little space available, we accelerate
                // a little to commit to the trajectory and avoid requiring
                // another allocation.
                
                // Calculate distance after an interval of acceleration
                double jumpDiffDistance = currentVelocity.computeDistance(
                        rangers.getInterval(), 
                        currentVelocity.getRoadVelocity(), 
                        JUMP_ACCELERATION);
                
                // Calculate stopping distance after acceleration
                double jumpStopDistance = currentVelocity.computeStoppingDistance(
                        currentVelocity.getRoadVelocity() + JUMP_ACCELERATION * rangers.getInterval());
                
                if(jumpDistance > jumpDiffDistance + jumpStopDistance + properties.getLength() + SAFE_DISTANCE) {
                    // Keep accelerating to make it to the jumpDistance faster
                    jumpDistance -= jumpDiffDistance;
                    return JUMP_ACCELERATION;
                } else {
                    // if we kept accelerating we would drive past the jump distance
                    jumpDistance = 0;
                    
                    // continue to look at the regular spacing to determine acceleration 
                }
                
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

        void prepareForConflict() {
            Allocation currentAllocation = allocator.getMyAllocation();
            
            if(currentAllocation != null) {
                // We already have an allocation
                switch(currentAllocation.getState()) {
                    case OBTAINED:    
                        if(currentAllocation.getTrajectory().contains(frontPosition)) {
                           eventLog.log("commit-time", clock.getTime());
                           currentAllocation.setState(Allocation.State.COMMITTED);
                            System.out.println(vehicleID + ": committing to allocation " + currentAllocation.getAllocationID());
                            // fall into committed section
                        } else if(clock.getTime() >= currentAllocation.getLastEntryTime()) {
                            System.out.println(vehicleID + ": failed to enter allocation " + currentAllocation.getAllocationID());
                            allocator.cancelMyAllocation();
                            currentAllocation = null;
                            break;
                        } else {
                            // Not yet in area, but still have time
                            return;
                        }
                    case COMMITTED:
                        // I should now have a guarantee that no one else
                        // will enter the conflict areas I enter
                        if(!currentAllocation.getTrajectory().hasConflictAreasAfter(backPosition)
                        && !currentAllocation.getTrajectory().hasConflictAreasAfter(frontPosition)) {
                            // We're done with this conflict trajectory
                            System.out.println(vehicleID + ": done with allocation " + currentAllocation.getAllocationID());
                            allocator.cancelMyAllocation();
                            currentAllocation = null;
                            eventLog.log("allocation-done", true);
                            break;
                        }
                        return;
                    case PENDING:
                        // Still waiting for responses
                        return;
                    case RELEASED:
                        // Allocation was released, remove it
                        currentAllocation = null;
                        break;
                    default:
                        throw new Error("shouldn't be here");
                }
            }
            
            if(clock.getTime() < backOffTime) {
                // We are pausing before getting another allocation
                return;
            }

            // We don't currently have an allocation
            ConflictAreaFinder conflictFinder = trackMap.getConflictFinder();
            
            // Find how far away we are from conflict
            double distanceToConflict = conflictFinder.findStrongConflictDistance(
                    emptyRoute);
            
            if(distanceToConflict >= MAX_DISTANCE_TO_QUERY) {
                // Too far from conflict to query for it
                return;
            }

            // Assume that no continuous conflict of more than 60m exists 
            TrackRangeSequence predictedRoute = routePredictor.predictRoute(60);

            // Get the conflict trajectory for the visible path
            ConflictTrajectory currentConflictTrajectory = conflictFinder.findStrongConflictTrajectory(
                    backPosition,
                    predictedRoute,
                    properties.getLength() + SAFE_DISTANCE*2,
                    measuredDistance,
                    60);

            if(!currentConflictTrajectory.hasStrongConflicts()) {
                // We have some conflicts ahead, but they're weak.
                // We can continue without an allocation if 
                // we make sure the weak conflicts are resolved.
                return;
            }
            
            // We need to remove the conflict-free start space, since we commit as soon as we enter the conflict trajectory.
            // However, we leave a small area such that we can commit even if we cannot enter the first conflict area.
            double conflictFreeStartSpace = currentConflictTrajectory.getConflictFreeStartSpace();
            currentConflictTrajectory.removeStartSpace(conflictFreeStartSpace - COMMIT_AREA_LENGTH);

            // Get the merger of all conflict areas
            TrackMapArea1D area = currentConflictTrajectory.asTrackMapArea(trackMap);
            
            // Time available for communication from now
            double communicationTimeSeconds = 0.3 * commFactor;
            long communicationTime = (long) (communicationTimeSeconds * SECONDS);

            // Include all vehicles could also query for one of the conflict areas
            area.decay(MAX_DISTANCE_TO_QUERY, DecayFlags.DISTANCE | DecayFlags.CONSIDER_DIRECTION | DecayFlags.GROW);
            
            // TODO: rewrite additional expansion algorithm

            long startTime = clock.getTime() + communicationTime;
            long lastEntryTime = startTime + (long) (1.3 * SECONDS);
            long targetTime = startTime;
            long resultDeadline = clock.getTime() + communicationTime;
            long receiveDeadline = resultDeadline;
            long etaTime = clock.getTime() + (long) (currentVelocity.computeMinDrivingTime(distanceToConflict) * SECONDS);
            
            Message query = new Message(vehicleID, Message.ANY_DESTINATION); // can be sent to vehicleID rather than ANY_DESTINATION
            
            final AllocationRequestFooter arf = new AllocationRequestFooter();
            arf.setTrajectory(currentConflictTrajectory.asTrackRangeSequence(trackMap));
            arf.setStartTime(startTime);
            arf.setEndTime(lastEntryTime);
            arf.setETA(etaTime);
            arf.setVehicleID(vehicleID);
            arf.setAllocationID(++nextAllocationID);
            query.addFooter(arf);
            
            backOffTime = lastEntryTime + backOffStep;

            final Allocation newAllocation = new Allocation(
                    vehicleID,
                    nextAllocationID,
                    currentConflictTrajectory,
                    startTime,
                    lastEntryTime,
                    etaTime,
                    Allocation.State.PENDING);

            allocator.addAllocation(newAllocation);
            
            System.out.println(vehicleID + ": requesting allocation " + nextAllocationID);
            vertigoInstance.startSession(
                    query, 
                    receiveDeadline, 
                    resultDeadline, 
                    targetTime, 
                    area, 
                    5,
                    new ResultHandler() {
                        @Override
                        public void result(
                                boolean success,
                                Map<Integer, MicroResponse> responses,
                                MembershipTuple membership) {
                            if(newAllocation.getState() == Allocation.State.RELEASED) {
                                // I've already cancelled my allocation
                                return;
                            }

                            System.out.print(vehicleID + ": received responses from");
                            
                            for(Integer responder : responses.keySet()) {
                                System.out.print(" " + responder);
                            }
                            
                            System.out.println();
                            
                            if(!success) {
                                // Did not receive a response from all members
                                System.out.println(vehicleID + ": query for allocation " + newAllocation.getAllocationID() + " failed");
                                allocator.cancelMyAllocation();
                                backOffTime += (long) (random.nextDouble() * SECONDS);
                                return;
                            }

                            commFactor = 1;
                            
                            for(Map.Entry<Integer,MicroResponse> entry : responses.entrySet()) {
                                Integer responder = entry.getKey();
                                MicroResponse response = entry.getValue();
                                
                                if(response.asByte() == REJECTED) {
                                    // At least one responder rejected my request
                                    allocator.cancelMyAllocation();
                                    System.out.println(vehicleID +  ": allocation " + newAllocation.getAllocationID() + " rejected");
                                    backOffTime += (long) (random.nextDouble() * SECONDS);
                                    return;
                                } else if(response.asByte() == TENTATIVE) {
                                    int tentativeAllocationID = response.asInt(1);
                                    
                                    // acceptance depends on another vehicle passing
                                    Allocation dependency = allocator.getAllocation(responder);
                                    
                                    if(dependency == null || dependency.getAllocationID() != tentativeAllocationID) {
                                        // I do not know about the allocation I depend on. Give up
                                        allocator.cancelMyAllocation();
                                        System.out.println(vehicleID +  ": missing dependency for " + newAllocation.getAllocationID() + " (" + responder + ":" + tentativeAllocationID + ")");
                                        return;
                                    }
                                    
                                    newAllocation.addDependency(dependency);
                                }
                            }
                            
                            // Not rejected, so allocated
                            newAllocation.setState(Allocation.State.OBTAINED);
                            System.out.print(vehicleID +  ": obtained allocation " + newAllocation.getAllocationID() + " with " + newAllocation.getDependencies().size() + " dependencies");
                            
                            for(Allocation dep : newAllocation.getDependencies()) {
                                System.out.print(" (" + dep.getOwner() + ":" + dep.getAllocationID() + ")");
                            }
                            
                            System.out.println();
                        }
                    });
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

        void considerAllocatedDistance() {
            allocatedDistance = getAllocatedDistance();
            
            eventLog.log("allocated-distance", allocatedDistance - properties.getLength());
            
            if(allocatedDistance <= availableDistance) {
                availableDistance = allocatedDistance;
            }
        }

        double getAllocatedDistance() {
            ConflictAreaFinder conflictFinder = trackMap.getConflictFinder();
            Allocation myAllocation = allocator.getMyAllocation();

            // Find the first conflict area we might encounter
            // up to at most measuredDistance meters away (since the available
            // distance will never be greater than the measured distance).
            double distanceToConflict = conflictFinder.findStrongConflictDistance(
                    emptyRoute);

            if(myAllocation == null 
            ||(myAllocation.getState() != Allocation.State.OBTAINED
            && myAllocation.getState() != Allocation.State.COMMITTED)) {
                // Don't currently have a confirmed allocation.
                allocatedDistance = distanceToConflict - COMMIT_AREA_LENGTH;
               
                if(allocatedDistance > currentVelocity.computeDelayedStoppingDistance(0.2) + properties.getLength() + SAFE_DISTANCE) {
                    // a conflict area is coming up, but we're going
                    // to pretend it doesn't exist, so that we only
                    // slow down if we really have to
                    allocatedDistance = Double.POSITIVE_INFINITY;
                } 
                
                return allocatedDistance;
            }
            
            // We have an allocation that is either obtained or
            // committed for an upcoming conflict trajectory.
            // We are allowed to at least drive into the conflict
            // trajectory and, maybe further if there are no conflicts
            
            ConflictTrajectory conflicts = myAllocation.getTrajectory();
            Set<ConflictArea> checked = new HashSet<ConflictArea>();
            TrackRange1D partRange = null;
            
            double safeDistance = 0.0;
            boolean foundFirstPart = false;
            boolean inTrajectory = conflicts.contains(backPosition);
            
            if(!inTrajectory) {
                // Find the distance to the conflict trajectory
                double distanceToTrajectory = routePredictor.distanceUntilArea(
                        backPosition,
                        actuators.getTrajectory(),
                        conflicts.asTrackMapArea(trackMap));
                
                // The distance to the trajectory is safe since it does
                // not require allocation.
                safeDistance = distanceToTrajectory;
                
                if(myAllocation.getState() == Allocation.State.OBTAINED) {
                    // See if we can make it into the trajectory before the last entry time
                    double minEntrySeconds = currentVelocity.computeMinDrivingTime(distanceToTrajectory);
                    long minETA = clock.getTime() + (long) (minEntrySeconds * SECONDS);
                    
                    if(minETA > myAllocation.getLastEntryTime()) {
                        // No way of getting there on time, don't go further than safe distance
                        return distanceToTrajectory;
                    } else {
                        // We might make it to the first conflict area if we accelerate a bit
                        jumpDistance = distanceToConflict;
                    }
                }
            }

            for(ConflictTrajectoryPart part : conflicts) {
                partRange = part.getRange();
                
                if(inTrajectory && !foundFirstPart) {
                    // Vehicle is in the trajectory, need to find
                    // the part where it starts.
                    if(partRange.contains(backPosition)) {
                        // Found the part in which the vehicle currently
                        // resides. Start the count from this point, knowing that
                        // the remainder of the range is surely safe.
                        // (unless we accidentally drove into the range...)
                        safeDistance = partRange.getEnd() - backPosition.getOffset();
                        foundFirstPart = true;
                    } 
                    continue;
                }

                for(ConflictArea conflictArea : part.getConflictAreas()) {
                    if(checked.contains(conflictArea)) {
                        // We've already checked this conflict area
                        // and found no conflict with dependencies
                        continue;
                    }
                    
                    checked.add(conflictArea);
                    
                    if(isInConflict(myAllocation, conflictArea)) {
                        // We have not resolved all dependencies for the conflict
                        // area
                        return safeDistance;
                    }
                }
                safeDistance += part.getLength();
            }
            
            // ok, so the whole conflict trajectory is allocated
            
            if(safeDistance < measuredDistance) {
                // Our measurementDistance goes beyond the end of our allocation, 
                // but we may run into another conflict area before measurementDistance

                safeDistance += conflictFinder.findStrongConflictDistance(
                        emptyRoute.subSequence(partRange.getEndPoint()));
            }

            return safeDistance;
        }

        private boolean isInConflict(Allocation myAllocation,
                ConflictArea conflictArea) {

            Collection<Allocation> dependencies = myAllocation.getDependencyByAreaID(conflictArea.getId());
            
            if(dependencies == null) {
                return false;
            }
            
            for(final Allocation dependency : dependencies) {
                if(dependency.getState() == Allocation.State.RELEASED) {
                    myAllocation.removeDependency(dependency);
                    continue;
                }
                
                final ConflictTrajectory traj = dependency.getTrajectory();
                final TrackMapArea1D trajArea = traj.asTrackMapArea(trackMap);

                Boolean inTrajectory = null;
                
                // Try to figure out whether vehicle is in area from beacons
                NeighbourState neighbour = neighbours.getNeighbourState(dependency.getOwner());

                if(neighbour != null) {
                    // try to confirm absence using beacons
                    inTrajectory = traj.intersects(neighbour.getPosition1DRange());
                    
                    if(!inTrajectory && neighbour.getTime() >= dependency.getLastEntryTime()) {
                        // this car was outside the area after the last entry time
                        // it has not committed to the allocation or has already released it
                        System.out.println(vehicleID + ": allocation distance - releasing dependency (" + dependency.getOwner() + ":" + dependency.getAllocationID() + "), position outside area after last entry time");
                        myAllocation.removeDependency(dependency);
                        continue;
                    }
                }

                if(inTrajectory == null) {
                    // try to confirm absence using membership
                    // this is necessary for liveness since we may
                    // never receive a beacon again
                    MembershipView membershipView = membership.getView();
                    
                    // Construct a membership tuple from tuples received from
                    // vehicles other than the allocation owner.
                    // If the allocation owner is in the area, this will leave
                    // a gap.
                    MembershipTuple membershipTuple = membershipView.collapse(new MembershipTupleFilter() {
                        @Override
                        public boolean accept(MembershipTuple tuple) {
                            return tuple.getArea().intersects(trajArea) && !tuple.hasMember(dependency.getOwner());
                        }
                    });
                    
                    membershipTuple.decayTo(clock.getTime());

                    if(membershipTuple.getArea().contains(trajArea)) {
                        if(membershipTuple.getTime() >= dependency.getLastEntryTime()) {
                            // this car was outside the area after the last entry time
                            // it has not committed to the allocation or has already released it
                            System.out.println(vehicleID + ": allocation distance - releasing dependency (" + dependency.getOwner() + ":" + dependency.getAllocationID() + "), not in membership area after last entry time");
                            myAllocation.removeDependency(dependency);
                            continue;
                        } else {
                            // The vehicle is outside the area, but may still enter
                            inTrajectory = false;
                        }
                    }
                }
                
                if(inTrajectory == null) {
                    // unable to establish whether this vehicle is in the area or not
                    // this means we cannot confirm that the vehicle has passed this conflict
                    // area, and that we have completed measuring the allocated distance
                    System.out.println(vehicleID + ": allocation distance - cannot confirm (" + dependency.getOwner() + ":" + dependency.getAllocationID() + ") passed area " + conflictArea.getId());
                    return true;
                }
                
                if(!inTrajectory) {
                    // the car is not in the area, but the most recent available
                    // information is from before the last entry time. This means
                    // the car could still be in front of the area (and most likely is).
                    // This means we have to wait for the car before proceeding through
                    // this conflict area
                    System.out.println(vehicleID + ": allocation distance - waiting for (" + dependency.getOwner() + ":" + dependency.getAllocationID() + ") to enter trajectory");
                    return true;
                } 

                // The car is in the area, but we need to confirm whether
                // it has passed this conflict area

                // We can only confirm the car is in the area using the
                // neighbourhood service, so neighbour cannot be null
                
                if(traj.isConflictAreaAfter(conflictArea, neighbour.getBackPosition())
                || traj.isConflictAreaAfter(conflictArea, neighbour.getFrontPosition())) {
                    // this car has yet to pass this conflict area
                    System.out.println(vehicleID + ": allocation distance - conflicting with (" + dependency.getOwner() + ":" + dependency.getAllocationID() + "), it has not passed area " + conflictArea.getId());
                    return true;
                } 
                
                // this car has passed the conflict area
                // it is not in conflict with this part of our trajectory
            }

            System.out.println(vehicleID + ": allocation distance - no conflict for area " + conflictArea.getId());
            return false;
        }
        
    };
    
    final VertigoReceiveHandler receiveVertigoQuery = new VertigoReceiveHandler() {
        @Override
        public MicroResponse receive(Message query) {
            AllocationRequestFooter arf = query.getFooter(AllocationRequestFooter.class);
            
            if(arf == null) {
                return null;
            }

            Allocation receivedAllocation = toAllocation(arf);
            Allocation myAllocation = allocator.getMyAllocation();

            if(myAllocation == null) {
                // I have no allocation, but an implicit allocation
                
            }
            
            if(!allocator.conflictsWithMyAllocation(receivedAllocation)) {
                allocator.addAllocation(receivedAllocation);
                System.out.println(vehicleID + ": received query - no conflict (" + receivedAllocation.getOwner() + ":" + receivedAllocation.getAllocationID() + ")");
                return MicroResponse.from(ACCEPTED, 0); 
            }

            switch(myAllocation.getState()) { 
                case PENDING:
                    if(myAllocation.getEtaTime() < receivedAllocation.getEtaTime()) {
                        System.out.println(vehicleID + ": received query - rejected as lower time priority " + receivedAllocation.getAllocationID() + " from " + receivedAllocation.getOwner());
                        return MicroResponse.from(REJECTED, 0);
                    } 
                    if(myAllocation.getEtaTime() == receivedAllocation.getEtaTime()
                    && myAllocation.getOwner() < receivedAllocation.getOwner()) {
                        System.out.println(vehicleID + ": received query - rejected as lower id priority " + receivedAllocation.getAllocationID() + " from " + receivedAllocation.getOwner());
                        return MicroResponse.from(REJECTED, 0);
                    }
                    // The received allocation supersedes mine
                    allocator.cancelMyAllocation();
                    allocator.addAllocation(receivedAllocation);
                    System.out.println(vehicleID + ": received query - accepted as higher priority " + receivedAllocation.getAllocationID() + " from " + receivedAllocation.getOwner());
                    return MicroResponse.from(ACCEPTED, 0);
                case OBTAINED:
                case COMMITTED:
                    System.out.println(vehicleID + ": received query - tentatively accepted " + receivedAllocation.getAllocationID() + " from " + receivedAllocation.getOwner() + ", dependent on (" + vehicleID + ":" + myAllocation.getAllocationID() + ")");
                    receivedAllocation.addDependency(myAllocation);
                    allocator.addAllocation(receivedAllocation);
                    return MicroResponse.from(TENTATIVE, myAllocation.getAllocationID());
                default:
                    throw new Error("My allocation is not pending or obtained");
            }
            
        }
    };


    private Allocation toAllocation(AllocationRequestFooter arf) {
        ConflictAreaFinder conflictFinder = trackMap.getConflictFinder();
        ConflictTrajectory traj = conflictFinder.findConflictTrajectory(arf.getTrajectory());
        
        return new Allocation(
                arf.getVehicleID(),
                arf.getAllocationID(),
                traj,
                arf.getStartTime(),
                arf.getEndTime(),
                arf.getEndTime(),
                // We only store a received allocation if it is accepted
                Allocation.State.ACCEPTED 
        );
    }

}
