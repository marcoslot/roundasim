/**
 * 
 */
package dsg.rounda.stats;

import dsg.rounda.logging.Event;

/**
 * Keeps track of the latest value
 */
public class LatestValueStat extends AbstractStatistic {

    private Double doubleValue;
    private Long longValue;
    
    /**
     * 
     */
    public LatestValueStat() {
    }

    /**
     * @see dsg.rounda.stats.Statistic#event(dsg.rounda.logging.Event)
     */
    @Override
    public void event(Event evt) {
        Object msg = evt.getMessage();
        
        if(msg instanceof Double) {
            doubleValue = (Double) msg;
            longValue = doubleValue.longValue();
        } else if(msg instanceof Integer) {
            longValue = ((Integer) msg).longValue();
            doubleValue = longValue.doubleValue();
        } else if(msg instanceof Long) {
            longValue = (Long) msg;
            doubleValue = longValue.doubleValue();
        }
    }

    /**
     * @see dsg.rounda.stats.Statistic#doubleValue()
     */
    @Override
    public Double doubleValue() {
        return doubleValue;
    }

    /* (non-Javadoc)
     * @see dsg.rounda.stats.AbstractStatistic#longValue()
     */
    @Override
    public Long longValue() {
        return longValue;
    }
    
}
