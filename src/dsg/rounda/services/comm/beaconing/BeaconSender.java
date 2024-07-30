/**
 * 
 */
package dsg.rounda.services.comm.beaconing;

import dsg.rounda.model.Message;

/**
 * @author slotm
 *
 */
public interface BeaconSender {
    
    /**
     * Called to prepare a beacon for transmission
     * 
     * @param beacon
     */
    void prepareBeacon(Message beacon);
    
    
}
