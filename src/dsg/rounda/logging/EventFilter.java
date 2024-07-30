/**
 * 
 */
package dsg.rounda.logging;

/**
 * Filters events for consumers
 */
public interface EventFilter {
    /**
     * 
     * @param evt the event
     * @return whether the event should be accepted
     */
    boolean accept(Event evt);
}
