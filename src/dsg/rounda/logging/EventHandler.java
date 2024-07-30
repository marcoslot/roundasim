/**
 * 
 */
package dsg.rounda.logging;

/**
 * Callback interface for handling logged events
 */
public interface EventHandler {
    /**
     * Called when an event occurs
     * 
     * @param event the event
     */
    void event(Event event);
}
