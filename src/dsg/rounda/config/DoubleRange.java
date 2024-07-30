/**
 * 
 */
package dsg.rounda.config;

import java.util.Iterator;

import dsg.rounda.serialization.text.TextSerializationManager;
import dsg.rounda.serialization.text.TextSerializer;


/**
 * Represents a range of strings
 */
public class DoubleRange extends AbstractNumberRange<Double> {

    private static final long serialVersionUID = 3718579588347240597L;

    /**
     * 
     */
    public DoubleRange() {
    }

    /**
     * @param start
     * @param end
     * @param step
     */
    public DoubleRange(Double start, Double end, Double step) {
        super(start, end, step);
    }

    /**
     * @param value
     */
    public DoubleRange(Double value) {
        super(value);
    }

    @Override
    public int getNumValues() {
        return getStart() == null ? 1 : (int) ((getEnd() - getStart()) / getStep());
    }

    @Override
    public Iterator<Double> iterator() {
        return new Iterator<Double>() {
            
            Double value = getStart();

            @Override
            public boolean hasNext() {
                return value != null && value <= getEnd();
            }

            @Override
            public Double next() {
                Double result = value;
                
                if(value != null) {
                    value += getStep();
                }
                
                return result;
            }

            @Override
            public void remove() {
                // not implemented
            }
            
        };
    }

    static {
        TextSerializationManager.register(DoubleRange.class, new TextSerializer<DoubleRange>() {

            @Override
            public String serialize(DoubleRange range) {
                if(range.getStart() == null) {
                    return "";
                }
                String result = range.getStart() + "-" + range.getEnd();;
                
                if(range.getStep() != null) {
                    result += "," + range.getStep();
                }
                
                return result;
            }

            @Override
            public DoubleRange deserialize(String text) throws Exception {
                if(text == null) {
                    return new DoubleRange();
                }
                
                text = text.trim();
                
                if(text.isEmpty()) {
                    return new DoubleRange();
                }
                
                String[] rangeStep = text.split(",");
                String[] range = rangeStep[0].split("-");
                
                if(range.length == 1) {
                    return new DoubleRange(
                            Double.parseDouble(range[0])
                     );
                } else {
                    return new DoubleRange(
                            Double.parseDouble(range[0]),
                            Double.parseDouble(range[1]),
                            Double.parseDouble(rangeStep[1])
                    );   
                }
            }
        });
    }

}
