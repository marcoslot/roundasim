/**
 * 
 */
package dsg.rounda.config;

/**
 * Base class for simulation parameters
 */
public abstract class AbstractSimulationParameter<T> implements Parameter<T> {

    private String key;
    private T defaultValue;
    
    public AbstractSimulationParameter() {
        init();
    }

    /**
     * @param key
     */
    public AbstractSimulationParameter(String key) {
        this.key = key;
        init();
    }

    public AbstractSimulationParameter(String key, T defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
        init();
    }

    private void init() {
        if(getConfigKey() == null) {
            throw new NullPointerException("config key cannot be null");
        }
        
        // Any instance of this type is automatically registered as parameters
        ParameterManager.register(this);
    }

    @Override
    public String getConfigKey() {
        return key;
    }

    @Override
    public abstract Class<?> getType();

    @Override
    public Class<?> getRangeType() {
        return null;
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
    }

}
