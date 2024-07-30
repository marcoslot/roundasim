/**
 * 
 */
package dsg.rounda.model;

/**
 * @author slotm
 *
 */
public interface NetworkDeliveryInterface {

    /**
     * Tells the network what the identifier of the vehicle is
     * 
     * @return identifier of the vehicle
     */
    int getId();
    
    /**
     * Deliver the message to the vehicle
     * 
     * @param msg the message
     */
    void deliver(Message msg);
    
}
