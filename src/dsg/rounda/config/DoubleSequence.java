/**
 * 
 */
package dsg.rounda.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dsg.rounda.serialization.text.TextSerializationManager;
import dsg.rounda.serialization.text.TextSerializer;

/**
 * Range of strings
 */
public class DoubleSequence extends AbstractObjectRange<Double> {

    /**
     * 
     */
    public DoubleSequence() {
    }

    /**
     * @param values
     */
    public DoubleSequence(Collection<Double> values) {
        super(values);
    }

    /**
     * @param values
     */
    public DoubleSequence(Double... values) {
        super(values);
    }

    static {
        TextSerializationManager.register(DoubleSequence.class, new TextSerializer<DoubleSequence>() {

            @Override
            public String serialize(DoubleSequence range) {
                return doSerialize(range);
            }

            @Override
            public DoubleSequence deserialize(String text) throws Exception {
                String[] texts = text.split(",");
                List<Double> numbers = new ArrayList<Double>();
                
                for(String item : texts) {
                    numbers.add(Double.parseDouble(item));
                }
                
                return new DoubleSequence(numbers);
            }
        });
    }

}
