/**
 * 
 */
package dsg.rounda.config;

import java.util.Collection;

import dsg.rounda.serialization.text.TextSerializationManager;
import dsg.rounda.serialization.text.TextSerializer;

/**
 * Range of strings
 */
public class StringRange extends AbstractObjectRange<String> {

    /**
     * 
     */
    public StringRange() {
    }

    /**
     * @param values
     */
    public StringRange(Collection<String> values) {
        super(values);
    }

    /**
     * @param values
     */
    public StringRange(String... values) {
        super(values);
    }

    static {
        TextSerializationManager.register(StringRange.class, new TextSerializer<StringRange>() {

            @Override
            public String serialize(StringRange range) {
                return doSerialize(range);
            }

            @Override
            public StringRange deserialize(String text) throws Exception {
                return new StringRange(text.split(","));
            }
        });
    }

}
