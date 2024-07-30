/**
 * 
 */
package dsg.rounda.stats;

import dsg.rounda.logging.Event;

/**
 * Gives the rate per hour
 */
public class RateStat extends AbstractStatistic {

    private int count;
    private long time;
    
    /**
     * 
     */
    public RateStat() {
    }

    /**
     * @see dsg.rounda.stats.Statistic#event(dsg.rounda.logging.Event)
     */
    @Override
    public void event(Event evt) {   
        count += 1;
        time = evt.getSimTime();
    }

    /**
     * @see dsg.rounda.stats.Statistic#doubleValue()
     */
    @Override
    public Double doubleValue() {
        return 3600 * count / (time * 0.000000001);
    }

}
