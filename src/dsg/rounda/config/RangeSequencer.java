/**
 * 
 */
package dsg.rounda.config;

import java.util.Iterator;

/**
 * @author slotm
 *
 */
public class RangeSequencer<T> {

    private final Range<T> range;
    private final Parameter<T> parameter;
    private Iterator<T> iterator;
    private T current;
    private boolean hasNext;
    
    /**
     * @param ranger 
     * 
     */
    public RangeSequencer(Range<T> range, Parameter<T> ranger) {
        this.range = range;
        this.parameter = ranger;
        reset();
    }
    
    /**
     * @param config
     * @param value
     * @see dsg.rounda.config.Parameter#set(dsg.rounda.config.RunConfig, java.lang.Object)
     */
    public void set(RunConfig config) {
        config.set(parameter, current());
    }
    
    public T current() {
        return current;
    }

    public void reset() {
        this.iterator = range.iterator();
        next();
    }
    
    public boolean hasNext() {
        return hasNext;
    }
    
    public void next() {
        this.current = iterator.hasNext() ? iterator.next() : null;
        this.hasNext = iterator.hasNext();
    }

    public int getNumValues() {
        return range.getNumValues();
    }

    public String getName() {
        return parameter.getConfigKey();
    }

}
