/**
 * 
 */
package dsg.rounda.config;

/**
 * Interface to configuration properties
 */
public interface VehicleConfig {

    /**
     * Get the value of a simulation parameter
     * 
     * @param param the simulatino parameter
     * @return 
     */
    <T> T get(Parameter<T> param);

}
