/**
 * 
 */
package dsg.rounda.services.coordination;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import dsg.rounda.model.Clock;
import dsg.rounda.model.LocalizationSensors;
import dsg.rounda.model.VehicleCapabilities;
import dsg.rounda.services.AbstractServiceFactory;
import dsg.rounda.services.ServiceFactory;
import dsg.rounda.services.comm.neighbourhood.NeighbourState;
import dsg.rounda.services.comm.neighbourhood.NeighbourhoodWatch;
import dsg.rounda.services.membership.MembershipTuple;
import dsg.rounda.services.membership.MembershipTupleFilter;
import dsg.rounda.services.membership.MembershipView;
import dsg.rounda.services.membership.SingleHopMembershipProtocol;
import dsg.rounda.services.roadmap.TrackMapArea1D;
import dsg.rounda.services.roadmap.TrackPoint1D;
import dsg.rounda.services.roadmap.VehicleTrackMap;

/**
 * Keeps track of resource allocations
 */
public class ConflictAreaAllocator {

    static final ServiceFactory FACTORY = new AbstractServiceFactory(type()) {
        @Override
        public Object create(VehicleCapabilities capabilities) {
            return new ConflictAreaAllocator(capabilities);
        }
    };

    public static Class<ConflictAreaAllocator> type() {
        return ConflictAreaAllocator.class;
    }

    final Map<Integer,Allocation> allocsByOwner;
    final Map<Integer,NegativeAllocation> negativeAllocsByConflictArea;
    
    final int vehicleID;
    final LocalizationSensors localization;
    final NeighbourhoodWatch neighbours;
    final SingleHopMembershipProtocol membership;
    final VehicleTrackMap trackMap;
    final Clock clock;

    public ConflictAreaAllocator(
            VehicleCapabilities capabilities) {
        this.vehicleID = capabilities.getId();
        this.clock = capabilities.getClock();
        this.localization = capabilities.getLocalizationSensors();
        this.allocsByOwner = new HashMap<Integer,Allocation>();
        this.negativeAllocsByConflictArea = new HashMap<Integer,NegativeAllocation>();
        this.membership = capabilities.getService(SingleHopMembershipProtocol.type());
        this.trackMap = capabilities.getRoadMap();
        this.neighbours = capabilities.getService(NeighbourhoodWatch.type());
    }

    public boolean conflictsWithMyAllocation(Allocation receivedAllocation) {
        Allocation myAllocation = getMyAllocation();
        
        if(myAllocation == null) {
            return false;
        }
        
        ConflictTrajectory receivedTraj = receivedAllocation.getTrajectory();
        Collection<ConflictArea> receivedConflictAreas = receivedTraj.getConflictAreas();
        
        ConflictTrajectory myTraj = getMyAllocation().getTrajectory();
        Collection<ConflictArea> myConflictAreas;
        
        TrackPoint1D position = localization.getPosition();
        
        if(myTraj.contains(position)) {
            myConflictAreas = myTraj.getConflictAreasAfter(localization.getPosition());
        } else {
            myConflictAreas = myTraj.getConflictAreas();
        }
        
        for(ConflictArea area : receivedConflictAreas) {
            if(myConflictAreas.contains(area)) {
                // conflict area in received allocation is 
                // also in my allocation
                return true;
            }
        }
        
        return false;
    }

    public void cancelMyAllocation() {
        Allocation myAllocation = getMyAllocation();

        if(myAllocation == null) {
            // already cancelled it
            return;
        }
        
        // Schedule this allocation for deletion
        myAllocation.release();
        // I no longer have an allocation
        allocsByOwner.remove(vehicleID);
    }

    public Allocation getMyAllocation() {
        return getAllocation(vehicleID);
    }

    public void addAllocation(Allocation allocation) {
        Allocation existingAlloc = allocsByOwner.get(allocation.getOwner());
        
        if(existingAlloc != null) {
            existingAlloc.release();
        }
        
        allocsByOwner.put(allocation.getOwner(), allocation);
    }

    public Allocation getAllocation(int owner) {
        Allocation allocation = allocsByOwner.get(owner);
        
        if(allocation != null && allocation.getState() == Allocation.State.RELEASED) {
            allocsByOwner.remove(owner);
            allocation = null;
        }
        
        return allocation;
    }


    public boolean hasUnresolvedDependencies(ConflictArea conflictArea) {
        Allocation myAllocation = getMyAllocation();

        if(myAllocation == null) {
            return false;
        }

        Collection<Allocation> dependencies = myAllocation.getDependencyByAreaID(conflictArea.getId());

        if(dependencies == null || dependencies.isEmpty()) {
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

    public boolean haveAllocation() {
        Allocation myAllocation = getMyAllocation();
        return myAllocation != null && (myAllocation.getState() == Allocation.State.OBTAINED || myAllocation.getState() == Allocation.State.COMMITTED);
    }
}
