/**
 * 
 */
package dsg.rounda;

import dsg.rounda.model.Clock;

/**
 * Thread-safe simulation time clock in nanoseconds
 */
public class SimTimeClock implements Clock {

    final private Object simTimeLock;
    private long simTime;
    
    /**
     * 
     */
    public SimTimeClock() {
        this.simTimeLock = new Object();
        this.simTime = 0L;
    }

    /**
     * Get the simulation time
     */
    @Override
    public long getTime() {
        synchronized(simTimeLock) {
            return simTime;
        }
    }

    /**
     * Set the simulation time
     * 
     * @param simTime the new simulation time
     */
    public void setTime(long simTime) {
        synchronized(simTimeLock) {
            this.simTime = simTime;
        }
    }
}
