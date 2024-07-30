/**
 * 
 */
package dsg.rounda.services;

import dsg.rounda.model.VehicleCapabilities;
import dsg.rounda.util.Chooser;

/**
 * Factory interface for creating service instances
 */
public interface ServiceFactory extends ServiceType {
    
    public static final Chooser<Class<?>,ServiceFactory> CHOOSER = new Chooser<Class<?>,ServiceFactory>(); 

    /**
     * Creates a service instance that uses the given capabilities interface.
     * This method does not use generics for brevity.
     * 
     * @param capabilities the capabilities interface
     * @return the service instance
     */
    Object create(VehicleCapabilities capabilities);
}
