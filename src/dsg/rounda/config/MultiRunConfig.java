/**
 * 
 */
package dsg.rounda.config;

import static dsg.rounda.serialization.text.TextSerializationManager.deserialize;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for a sequence of runs
 */
public class MultiRunConfig implements SimulationParameters {

    private static final long serialVersionUID = -3319585981410050136L;

    private Map<String,Range<?>> rangeValues;
    private Map<String,Object> singleValues;

    public MultiRunConfig() {
        rangeValues = new HashMap<String,Range<?>>();
        singleValues = new HashMap<String,Object>();
        set(SCENARIO_NAME);
        set(CONTROLLER_NAME);
    }

    public MultiRunConfig(Map properties) throws Exception {
        this();
        setProperties(properties);
    }
     
    public void setProperties(Map properties) throws Exception {
        for(Object entryObj : properties.entrySet()) {
            Map.Entry entry = (Map.Entry) entryObj;
            String key = (String) entry.getKey();
            String stringValue = (String) entry.getValue();
            Parameter parameter = ParameterManager.get(key);
            Class<Range<?>> rangeType = parameter.getRangeType();
            
            if(parameter.getRangeType() == null) {
                singleValues.put(key, deserialize(parameter.getType(), stringValue));
            } else {
                rangeValues.put(key, deserialize(rangeType, stringValue));
            }
        }
    }

    public MultiRunSequencer getSequencer() {
        return new MultiRunSequencer(this);
    }

    /**
     * @return the parameters
     */
    public Map<String, Range<?>> getRangeValues() {
        return rangeValues;
    }

    /**
     * @return the parameters
     */
    public Map<String, Object> getSingleValues() {
        return singleValues;
    }

    public void setSingle(String key, Object value) {
        this.singleValues.put(key, value);
    }

    public void set(String key, Range<?> value) {
        this.rangeValues.put(key, value);
    }

    public <T> void set(Parameter<T> param, Range<T> value) {
        set(param.getConfigKey(), value);
    }

    public <T> void set(Parameter<T> param) {
        set(param.getConfigKey(), param.getDefaultRange());
    }

    @SuppressWarnings("unchecked")
    public <T> Range<T> get(String key) {
        return (Range<T>) rangeValues.get(key);
    }

    public <T> Range<T> get(Parameter<T> param) {
        return get(param.getConfigKey());
    }

    @SuppressWarnings("unchecked")
    public <T> Range<T> get(Parameter<T> param, Range<T> def) {
        return rangeValues.containsKey(param.getConfigKey()) ? (Range<T>) get(param.getConfigKey()) : def;
    }

    public RunConfig getBaseConfig() throws Exception {
        return new RunConfig(singleValues);
    }


}
