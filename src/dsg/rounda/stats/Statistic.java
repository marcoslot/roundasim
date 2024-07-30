/**
 * 
 */
package dsg.rounda.stats;

import dsg.rounda.logging.Event;

/**
 * A statistic
 */
public interface Statistic {

    /**
     * Called when an event occurs
     * 
     * @param evt the event
     */
    void event(Event evt);

    /**
     * Returns the value of the statistic
     * 
     * @return the value of the statistic
     */
    Double doubleValue();

    /**
     * Returns the value of the statistic
     * 
     * @return the value of the statistic
     */
    Long longValue();

    String stringValue();
    
    
}
