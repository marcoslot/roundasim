/**
 * 
 */
package dsg.rounda.stats;

import dsg.rounda.logging.Event;

/**
 * Statistic to measure a time between
 * the first and last event. 
 */
public class Timer implements Statistic {

    Long startTime;
    Long endTime;
    
    /**
     * 
     */
    public Timer() {
        
    }

    /**
     * @see dsg.rounda.stats.Statistic#event(dsg.rounda.logging.Event)
     */
    @Override
    public void event(Event evt) {
        if(!(evt.getMessage() instanceof Long)) {
            return;
        }
        
        Long time = (Long) evt.getMessage();
        
        if(startTime == null) {
            startTime = time;
        } else if(endTime == null) {
            endTime = time;
        }
    }

    /**
     * @see dsg.rounda.stats.Statistic#doubleValue()
     */
    @Override
    public Double doubleValue() {
        return new Double(longValue());
    }

    /**
     * @see dsg.rounda.stats.Statistic#longValue()
     */
    @Override
    public Long longValue() {
        return (endTime - startTime);
    }

    @Override
    public String stringValue() {
        return Long.toString(longValue());
    }
    
}
