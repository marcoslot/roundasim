/**
 * 
 */
package dsg.rounda.stats;

import dsg.rounda.logging.Event;

/**
 * Interface for counting events
 */
public interface Counter {
    /**
     * Count an event
     * @param evt the event to count
     */
    void event(Event evt);
    
    /**
     * Returns the number of times an event occurred
     * 
     * @return the number of times an event occurred
     */
    Long longValue();
}
