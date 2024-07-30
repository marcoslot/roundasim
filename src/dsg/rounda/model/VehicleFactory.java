/**
 * 
 */
package dsg.rounda.model;


/**
 * Creates and destroys vehicles.
 * 
 * A vehicle is a combination of state, capabilities, and a controller.
 * The initial state is provided by the scenario, and from there on
 * managed by the simulator. The capabilities and controller are created
 * by the simulator according to its configuration.
 */
public interface VehicleFactory {
    
    /**
     * Create a new vehicle and insert it into the world with the given
     * initial state and sensor specifications.
     * 
     * The caller of this method must ensure that initialState has a 
     * unique identifier.
     * 
     * @param initialState the initial state of the vehicle
     * @param rangingSepcs the ranging sensor specifications
     */
    void createVehicle(VehicleState initialState, RangingSensorsSpecification rangingSpecs);
    
}
