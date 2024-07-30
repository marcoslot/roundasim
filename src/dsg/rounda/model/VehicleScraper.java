/**
 * 
 */
package dsg.rounda.model;

/**
 * Implementations of this interface now how to
 * scrape vehicles.
 */
public interface VehicleScraper {

    /**
     * Remove a vehicle from the world
     * 
     * @param id the identifier of the vehicle
     */
    void destroyVehicle(int id);
}
