/**
 * 
 */
package dsg.rounda.stats;


/**
 * @author slotm
 *
 */
public abstract class AbstractStatistic implements Statistic {

    /**
     * @see dsg.rounda.stats.Statistic#longValue()
     */
    @Override
    public Long longValue() {
        Double doubleValue = doubleValue();
        return doubleValue != null ? doubleValue.longValue() : null;
    }

    /* (non-Javadoc)
     * @see dsg.rounda.stats.Statistic#stringValue()
     */
    @Override
    public String stringValue() {
        Double doubleValue = doubleValue();
        return doubleValue != null ? Double.toString(doubleValue()) : "null";
    }

    public String toString() {
    	return stringValue();
    }
}
