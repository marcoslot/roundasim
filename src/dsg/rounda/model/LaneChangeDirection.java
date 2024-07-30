/**
 * 
 */
package dsg.rounda.model;

/**
 * @author Niall
 *
 */
public enum LaneChangeDirection {

	LEFT(-1),
	RIGHT(1);
	
	int direction;
	
	LaneChangeDirection(int dir) {
		this.direction = dir;
	}
	
	public static LaneChangeDirection getDirection(int dir) {
		for(LaneChangeDirection lcd : values()) {
			if(lcd.direction == dir) {
				return lcd;
			}
		}
		return null;
	}

	public LaneChangeDirection inverse() {
		return this == LEFT ? RIGHT : LEFT;
	}
}
