/**
 * 
 */
package dsg.rounda.model;

/**
 * Clock interface
 */
public interface Clock {
    /**
     * Returns the time in nanoseconds
     * @return the time in nanoseconds
     */
    long getTime();
    
    /**
     * Set the time in nanoseconds
     * @param time the time in nanoseconds
     */
    void setTime(long time);
}
