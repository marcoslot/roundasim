/**
 * 
 */
package dsg.rounda.model;


/**
 * Interface to the global network
 */
public interface Network {
    
    /**
     * Tell the network about the presence of a new adapter
     * 
     * @param id identifier of the vehicle
     * @param adapter adapter interface
     * @return a transmission interface
     */
    NetworkTransmissionInterface addAdapter(NetworkDeliveryInterface adapter);
    
    /**
     * Remove the adapter of a vehicle
     * 
     * @param id the identifier of the adapter
     */
    void removeAdapter(int id);
    

}
