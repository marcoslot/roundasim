/**
 * 
 */
package dsg.rounda.stats;

import dsg.rounda.logging.Event;

/**
 * Counts the number of times an event occurs
 */
public class BeanCounter implements Counter, Statistic {

    private int count;
    
    /**
     * Create a bean counter initialized to 0
     */
    public BeanCounter() {
        this(0);
    }

    /**
     * Create a bean counter initialized to the given value of count
     * 
     * @param count the initial value of count
     */
    public BeanCounter(int count) {
        this.count = count;
    }

    @Override
    public void event(Event evt) {
        count++;
    }

    @Override
    public Long longValue() {
        return new Long(count);
    }

    @Override
    public Double doubleValue() {
        return new Double(count);
    }

    @Override
    public String stringValue() {
        return Integer.toString(count);
    }
    
}
