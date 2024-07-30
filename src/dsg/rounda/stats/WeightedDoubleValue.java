/**
 * 
 */
package dsg.rounda.stats;

/**
 * @author Marco
 *
 */
public class WeightedDoubleValue {

	final double value;
	final double weight;
	public WeightedDoubleValue(double value, double weight) {
		super();
		this.value = value;
		this.weight = weight;
	}
	public double getValue() {
		return value;
	}
	public double getWeight() {
		return weight;
	}
	
}
