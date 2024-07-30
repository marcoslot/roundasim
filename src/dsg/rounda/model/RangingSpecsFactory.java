/**
 * 
 */
package dsg.rounda.model;

/**
 * Interface for generating ranging sensor specifications
 */
public interface RangingSpecsFactory {

    /**
     * Generate ranging sensor specifications for the given vehicle
     * 
     * @param vehicle the vehicle
     * @return ranging sensor specifications
     */
    RangingSensorsSpecification createRangingSpecs(VehicleState vehicle);
}
