/**
 * 
 */
package dsg.rounda.config;

import java.util.Iterator;

import dsg.rounda.serialization.text.TextSerializationManager;
import dsg.rounda.serialization.text.TextSerializer;


/**
 * Represents a range of longs
 */
public class LongRange extends AbstractNumberRange<Long> {

    private static final long serialVersionUID = 292191706519192568L;

    public LongRange() {
        super();
    }

    /**
     * @param value
     */
    public LongRange(Long value) {
        super(value);
    }

    /**
     * @param start
     * @param end
     * @param step
     */
    public LongRange(Long start, Long end) {
        this(start, end, 1L);
    }

    /**
     * @param start
     * @param end
     * @param step
     */
    public LongRange(Long start, Long end, Long step) {
        super(start, end, step);
    }

    @Override
    public int getNumValues() {
        return getStart() == null ? 1 : (int) ((getEnd() - getStart()) / getStep());
    }

    @Override
    public Iterator<Long> iterator() {
        return new Iterator<Long>() {
            
            Long value = getStart();

            @Override
            public boolean hasNext() {
                return value != null && value <= getEnd();
            }

            @Override
            public Long next() {
                Long result = value;
                
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
        TextSerializationManager.register(LongRange.class, new TextSerializer<LongRange>() {

            @Override
            public String serialize(LongRange range) {
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
            public LongRange deserialize(String text) throws Exception {
                if(text == null) {
                    return new LongRange();
                }
                
                text = text.trim();
                
                if(text.isEmpty()) {
                    return new LongRange();
                }
                
                String[] rangeStep = text.split(",");
                String[] range = rangeStep[0].split("-");
                
                if(range.length == 1) {
                    return new LongRange(
                            Long.parseLong(range[0])
                     );
                } else if(rangeStep.length == 1) {
                    return new LongRange(
                            Long.parseLong(range[0]),
                            Long.parseLong(range[1])
                    );   
                } else {
                    return new LongRange(
                            Long.parseLong(range[0]),
                            Long.parseLong(range[1]),
                            Long.parseLong(rangeStep[1])
                    );   
                }
            }
        });
    }

}
