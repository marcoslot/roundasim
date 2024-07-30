/**
 * 
 */
package dsg.rounda.stats;

import dsg.rounda.logging.Event;

/**
 * Keeps track of the latest value
 */
public class AverageStat extends AbstractStatistic {

    private double sum;
    private int count;
    
    /**
     * 
     */
    public AverageStat() {
    }

    /**
     * @see dsg.rounda.stats.Statistic#event(dsg.rounda.logging.Event)
     */
    @Override
    public void event(Event evt) {
        Object msg = evt.getMessage();
        
        if(msg instanceof Double) {
            sum += (Double) msg;
            count += 1;
        } else if(msg instanceof Integer) {
            sum += ((Integer) msg).doubleValue();
            count += 1;
        }
    }

    /**
     * @see dsg.rounda.stats.Statistic#doubleValue()
     */
    @Override
    public Double doubleValue() {
        return count == 0 ? null : sum / count;
    }

}
