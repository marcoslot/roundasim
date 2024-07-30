/**
 * 
 */
package dsg.rounda.config;

/**
 * Represents a simulation parameter
 */
public interface Parameter<T> {
    
    /**
     * Returns the key used for this parameter in configured files
     * 
     * @return the key used for this parameter in configured files
     */
    String getConfigKey();
    
    /**
     * Returns the type of range
     * 
     * @return the type of range
     */
    Class<?> getRangeType();

    /**
     * Returns the default range value 
     * @return the default range value
     */
    Range<?> getDefaultRange();
    
    /**
     * Get the default value for the parameter
     * 
     * @return
     */
    T getDefaultValue();

    /**
     * Returns the type of the parameter
     * @return the type of the parameter
     */
    Class<?> getType();


}
