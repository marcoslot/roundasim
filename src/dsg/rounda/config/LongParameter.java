/**
 * 
 */
package dsg.rounda.config;

/**
 * @author slotm
 *
 */
public class LongParameter extends AbstractSimulationParameter<Long> {

    /**
     * 
     */
    public LongParameter() {
    }


    /**
     * @param key
     * @param defaultValue
     */
    public LongParameter(String key, Long defaultValue) {
        super(key, defaultValue);
    }


    /**
     * @param key
     */
    public LongParameter(String key) {
        super(key);
    }


    @Override
    public Class<?> getRangeType() {
        return LongRange.class;
    }
    

    @Override
    public Range<?> getDefaultRange() {
        return new LongRange(getDefaultValue());
    }


    @Override
    public Class<?> getType() {
        return Long.class;
    }
    
}
