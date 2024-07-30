/**
 * 
 */
package dsg.rounda.config;

/**
 * @author slotm
 *
 */
public class DoubleParameter extends AbstractSimulationParameter<Double> {

    /**
     * 
     */
    public DoubleParameter() {
    }

    /**
     * @param key
     * @param defaultValue
     */
    public DoubleParameter(String key, Double defaultValue) {
        super(key, defaultValue);
    }

    /**
     * @param key
     */
    public DoubleParameter(String key) {
        super(key);
    }

    @Override
    public Class<?> getRangeType() {
        return DoubleSequence.class;
    }

    @Override
    public Range<?> getDefaultRange() {
        return new DoubleSequence(getDefaultValue());
    }

    @Override
    public Class<?> getType() {
        return Double.class;
    }
    
    
}