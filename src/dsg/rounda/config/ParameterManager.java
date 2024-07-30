/**
 * 
 */
package dsg.rounda.config;

import java.util.HashMap;
import java.util.Map;


/**
 * Simulation parameter manager
 */
public class ParameterManager  {
    
    private static ParameterManager instance;
    private final Map<String,Parameter<?>> parameters;
    
    public ParameterManager() {
        this.parameters = new HashMap<String, Parameter<?>>();
    }

    public static void register(Parameter<?> parameter) {
        instance().parameters.put(parameter.getConfigKey(), parameter);
    }
    
    public static Parameter<?> get(String configKey) {
        return instance().parameters.get(configKey);
    }
    
    private static ParameterManager instance() {
        if(instance == null) {
            instance = new ParameterManager();
        }
        return instance;
    }
    
}