/**
 * 
 */
package dsg.rounda.config;

/**
 * @author slotm
 *
 */
public class StringParameter extends AbstractSimulationParameter<String> {

    /**
     * 
     */
    public StringParameter() {
    }

    /**
     * @param key
     * @param defaultValue
     */
    public StringParameter(String key, String defaultValue) {
        super(key, defaultValue);
    }

    /**
     * @param key
     */
    public StringParameter(String key) {
        super(key);
    }

    @Override
    public Class<?> getRangeType() {
        return StringRange.class;
    }

    @Override
    public Range<?> getDefaultRange() {
        return new StringRange(getDefaultValue());
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }
    

}