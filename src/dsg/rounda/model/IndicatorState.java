/**
 * 
 */
package dsg.rounda.model;

/**
 * @author Niall
 *
 */
public enum IndicatorState {

	LEFT(true),
	NONE(false),
	RIGHT(true),
	HAZARD(true);
	
	final boolean on;

	private IndicatorState(boolean on) {
		this.on = on;
	}

	public boolean isOn() {
		return on;
	}
	
	
}
