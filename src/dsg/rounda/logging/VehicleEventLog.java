/**
 * 
 */
package dsg.rounda.logging;

import dsg.rounda.model.Message;


/**
 * Event log for an individual vehicle
 */
public class VehicleEventLog {

    final Integer identity;
    final EventLog eventLog;
    
    public VehicleEventLog(int vehicleID, EventLog eventLog) {
        this.identity = vehicleID;
        this.eventLog = eventLog;
    }
    
    /**
     * Log a message
     * 
     * @param message the message
     */
    public <T> void log(T message) {
        eventLog.log(identity, message);
    }

    /**
     * Log a message
     * 
     * @param tag the tag
     * @param message the message
     */
    public <T> void log(String tag, T message) {
        eventLog.log(identity, tag, message);
    }

}
