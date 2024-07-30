/**
 * 
 */
package dsg.rounda.config;

import dsg.rounda.serialization.text.TextSerializationManager;
import dsg.rounda.serialization.text.TextSerializer;

/**
 * @author slotm
 *
 */
public class RepeatParameter extends AbstractSimulationParameter<Integer> {

    /**
     * @param key
     * @param defaultValue
     */
    public RepeatParameter(String key, Integer defaultValue) {
        super(key, defaultValue);
        RepeatRange.class.getName();
    }

    /**
     * @param key
     */
    public RepeatParameter(String key) {
        super(key);
    }

    @Override
    public Range<?> getDefaultRange() {
        return new RepeatRange(getDefaultValue());
    }

    @Override
    public Class<?> getType() {
        return Integer.class;
    }

    /**
     * @see dsg.rounda.config.AbstractSimulationParameter#getRangeType()
     */
    @Override
    public Class<?> getRangeType() {
        return RepeatRange.class;
    }


    static {
        TextSerializationManager.register(RepeatRange.class, new TextSerializer<RepeatRange>() {

            @Override
            public String serialize(RepeatRange range) {
                return Integer.toString(range.getNumValues());
            }

            @Override
            public RepeatRange deserialize(String text) throws Exception {
                if(text == null) {
                    return new RepeatRange();
                }
                
                return new RepeatRange(Integer.parseInt(text));
            }
        });
    }
}
