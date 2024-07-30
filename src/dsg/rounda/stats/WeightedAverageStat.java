/**
 * 
 */
package dsg.rounda.stats;

import dsg.rounda.logging.Event;

/**
 * Keeps track of the latest value
 */
public class WeightedAverageStat extends AbstractStatistic {

    private double weightedValueSum;
    private double weightSum;
    
    /**
     * 
     */
    public WeightedAverageStat() {
    }

    /**
     * @see dsg.rounda.stats.Statistic#event(dsg.rounda.logging.Event)
     */
    @Override
    public void event(Event evt) {
        Object msg = evt.getMessage();
        
        if(msg instanceof WeightedDoubleValue) {
        	WeightedDoubleValue wdv = (WeightedDoubleValue) msg;
            weightedValueSum += wdv.getValue() * wdv.getWeight();
            weightSum += wdv.getWeight();
        }
    }

    /**
     * @see dsg.rounda.stats.Statistic#doubleValue()
     */
    @Override
    public Double doubleValue() {
        return weightSum == 0 ? 0 : weightedValueSum / weightSum;
    }

}
