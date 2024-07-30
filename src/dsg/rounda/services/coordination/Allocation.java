/**
 * 
 */
package dsg.rounda.services.coordination;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



/**
 * Allocation of an area until an end time.
 */
public class Allocation {
    
    public enum State {
        /**
         * I have created this allocation and am waiting to confirm
         * whether I have obtained it.
         */
        PENDING,
        /**
         * I have obtained this allocation, but have not yet entered
         * the conflict trajectory.
         */
        OBTAINED,
        /**
         * I have committed to this allocation by entering the 
         * conflict trajectory.
         */
        COMMITTED,
        /**
         * I have left the conflict area.
         * It may be removed from the system.
         */
        RELEASED,
        /**
         * I have received and accepted this allocation.
         */
        ACCEPTED;
    }
    
    final int vehicleID;
    final int allocationID;
    final ConflictTrajectory trajectory;
    final long startTime;
    final long lastEntryTime;
    final long etaTime;
    final Set<Allocation> dependencies;
    
    final Map<Integer,Collection<Allocation>> dependenciesByArea;

    State state;
    
    /**
     * @param area
     * @param endTime
     */
    public Allocation(
            int vehicleID, 
            int allocationID,
            ConflictTrajectory trajectory,
            long startTime,
            long lastEntryTime,
            long etaTime,
            State state) {
        this.allocationID = allocationID;
        this.vehicleID = vehicleID;
        this.trajectory = trajectory;
        this.startTime = startTime;
        this.lastEntryTime = lastEntryTime;
        this.etaTime = etaTime;
        this.state = state;
        this.dependencies = new HashSet<Allocation>();
        this.dependenciesByArea = new HashMap<Integer,Collection<Allocation>>();
    }
    
    /**
     * @return the allocationID
     */
    public int getAllocationID() {
        return allocationID;
    }

    /**
     * @return the vehicleID
     */
    public int getOwner() {
        return vehicleID;
    }
    /**
     * @return the startTime
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @return the state
     */
    public State getState() {
        return state;
    }

    /**
     * @return the trajectory
     */
    public ConflictTrajectory getTrajectory() {
        return trajectory;
    }

    public long getLastEntryTime() {
        return lastEntryTime;
    }
    
    public long getEtaTime() {
        return etaTime;
    }

    public void addDependency(Allocation depency) {
        this.dependencies.add(depency);
    
        for(ConflictArea area : depency.getTrajectory().getConflictAreas()) {
            Collection<Allocation> allocations = dependenciesByArea.get(area.getId());
            
            if(allocations == null) {
                allocations = new HashSet<Allocation>();
                dependenciesByArea.put(area.getId(), allocations);
            }
            
            allocations.add(depency);
        }
    
    }

    public boolean isInEntryInterval(long fromTime) {
        return startTime <= fromTime && fromTime < lastEntryTime;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Collection<Allocation> getDependencies() {
        return dependencies;
    }

    public void release() {
        setState(State.RELEASED);
    }

    public Collection<Allocation> getDependencyByAreaID(int id) {
        return dependenciesByArea.get(id);
    }

    public void removeDependency(Allocation dependency) {
        dependencies.remove(dependency);

        for(ConflictArea area : dependency.getTrajectory().getConflictAreas()) {
            dependenciesByArea.remove(area.getId());
        }
    }
}
