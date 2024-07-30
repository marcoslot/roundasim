/**
 * 
 */
package dsg.rounda.stats;

import dsg.rounda.logging.Event;

/**
 * Keeps track of the latest value
 */
public class TimeAverageStat extends AbstractStatistic {

	private double weightedSum;
	private double count;

	private Long lastEventTime;

	/**
	 * 
	 */
	public TimeAverageStat() {
		lastEventTime = 0L;
	}

	/**
	 * @see dsg.rounda.stats.Statistic#event(dsg.rounda.logging.Event)
	 */
	@Override
	public void event(Event evt) {
		Object msg = evt.getMessage();

		if(lastEventTime != null) {
			double weight = (evt.getSimTime() - lastEventTime) / 1000000.;

			if(msg instanceof Double) {
				weightedSum += weight * (Double) msg;
				count += weight;
			} else if(msg instanceof Integer) {
				weightedSum += weight * ((Integer) msg).doubleValue();
				count += weight;
			}
		}

		lastEventTime = evt.getSimTime();
	}

	/**
	 * @see dsg.rounda.stats.Statistic#doubleValue()
	 */
	@Override
	public Double doubleValue() {
		return count == 0 ? null : weightedSum / count;
	}

}
