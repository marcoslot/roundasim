/**
 * 
 */
package dsg.rounda.stats;

import dsg.rounda.logging.Event;

/**
 * Keeps track of the latest value
 */
public class DoubleSumStat implements Statistic {

    private double sum;
    
    /**
     * 
     */
    public DoubleSumStat() {
    }

    /**
     * @see dsg.rounda.stats.Statistic#event(dsg.rounda.logging.Event)
     */
    @Override
    public void event(Event evt) {
        Object msg = evt.getMessage();
        
        if(msg instanceof Double) {
            sum += (Double) msg;
        } else if(msg instanceof Integer) {
            sum += ((Integer) msg);
        } else if(msg instanceof Long) {
            sum += ((Long) msg);
        }
    }

    /**
     * @see dsg.rounda.stats.Statistic#doubleValue()
     */
    @Override
    public Double doubleValue() {
        return sum;
    }

    /**
     * @see dsg.rounda.stats.Statistic#longValue()
     */
    @Override
    public Long longValue() {
        return (long) sum;
    }

    @Override
    public String stringValue() {
        return Double.toString(sum);
    }
    
}
