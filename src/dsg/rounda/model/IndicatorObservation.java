/**
 * 
 */
package dsg.rounda.model;

/**
 * @author Niall
 * 
 */
public class IndicatorObservation implements Comparable<IndicatorObservation> {

	final IndicatorState direction;
	final double distance;

	public IndicatorObservation(IndicatorState direction, double distance) {
		super();
		this.direction = direction;
		this.distance = distance;
	}

	public IndicatorState getDirection() {
		return direction;
	}

	public double getDistance() {
		return distance;
	}

	@Override
	public int compareTo(IndicatorObservation o) {
		return Double.compare(distance, o.distance);
	}

}
