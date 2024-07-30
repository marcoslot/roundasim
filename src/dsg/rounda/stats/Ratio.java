/**
 * 
 */
package dsg.rounda.stats;

import dsg.rounda.logging.Event;

/**
 * @author slotm
 *
 */
public class Ratio extends AbstractStatistic {

    final Statistic n;
    final Statistic d;
    
    /**
     * 
     */
    public Ratio(Statistic n, Statistic d) {
        this.n = n;
        this.d = d;
    }

    /* (non-Javadoc)
     * @see dsg.rounda.stats.Statistic#event(dsg.rounda.logging.Event)
     */
    @Override
    public void event(Event evt) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see dsg.rounda.stats.Statistic#doubleValue()
     */
    @Override
    public Double doubleValue() {
        Double denomValue = d.doubleValue();
        return denomValue == null || denomValue == 0.0 ? null : (n.doubleValue() / d.doubleValue());
    }

}
