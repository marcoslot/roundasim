/**
 * 
 */
package dsg.rounda.services.coordination;

import java.util.Collection;


/**
 * @author slotm
 *
 */
public class NegativeAllocation {

    public enum State {
        /**
         * The allocation is active, I'm not allowed to enter any
         * part of the conflict areas.
         */
        ACTIVE,
        /**
         * The allocation has expired, I'm allowed to enter again.
         */
        RELEASED
    }
    
    final Collection<ConflictArea> conflicts;
    final long startTime;
    final long lastEntryTime;
    State state;
    
    public NegativeAllocation(Collection<ConflictArea> conflicts, long startTime, long lastEntryTime) {
        super();
        this.conflicts = conflicts;
        this.startTime = startTime;
        this.lastEntryTime = lastEntryTime;
        this.state = State.ACTIVE;
    }

    /**
     * @return the state
     */
    public State getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(State state) {
        this.state = state;
    }

    /**
     * @return the trajectory
     */
    public Collection<ConflictArea> getConflicts() {
        return conflicts;
    }

    /**
     * @return the startTime
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @return the lastEntryTime
     */
    public long getLastEntryTime() {
        return lastEntryTime;
    }
    
    
    
    

}
