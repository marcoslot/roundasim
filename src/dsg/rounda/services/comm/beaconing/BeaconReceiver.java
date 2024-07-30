/**
 * 
 */
package dsg.rounda.services.comm.beaconing;

import dsg.rounda.model.Message;

/**
 * @author slotm
 *
 */
public interface BeaconReceiver {

    /**
     * Called when a beacon is received
     * 
     * @param beacon
     */
    void receiveBeacon(Message beacon);
}
