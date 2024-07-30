/**
 * 
 */
package dsg.rounda.config;

import static dsg.rounda.serialization.text.TextSerializationManager.register;
import dsg.rounda.serialization.text.TextSerializer;

/**
 * @author slotm
 *
 */
public class EnumParameter<T extends Enum<T>> extends AbstractSimulationParameter<T> {

    private final Class<T> type;

    public EnumParameter(Class<T> type, String key) {
        this(type, key, null);
    }

    public EnumParameter(Class<T> type, String key, T defaultValue) {
        super(key, defaultValue);
        this.type = type;
        register(type, serializer);
    }
    
    final TextSerializer<T> serializer = new TextSerializer<T>() {
        @Override
        public String serialize(T obj) {
            return obj.toString();
        }

        @Override
        public T deserialize(String text) throws Exception {
            return Enum.valueOf(type, text);
        }
    };
    
    @Override
    public Class<?> getType() {
        return type;
    }


    @Override
    public Range<?> getDefaultRange() {
        return new StringRange(getDefaultValue().toString());
    }

    @Override
    public Class<?> getRangeType() {
        return StringRange.class;
    }
    
}