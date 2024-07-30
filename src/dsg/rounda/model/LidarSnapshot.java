/**
 * 
 */
package dsg.rounda.model;

/**
 * Represents a single snapshot of a LIDAR at a given
 * point in time.
 */
public class LidarSnapshot {

    final double time;
    final double range;
    private final Double[] steps;
    
    public LidarSnapshot(double time, int numSteps, double range) {
        this.time = time;
        this.steps = new Double[numSteps];
        this.range = range;
    }

    public double getTime() {
        return time;
    }

    public int getNumSteps() {
        return steps.length;
    }
    
    public double getRange() {
        return range;
    }
    
    public double getDistance(int i) {
        return steps[i] != null ? steps[i] : range;
    }

    public double setDistance(int i, double distance) {
        return steps[i] = Math.min(distance, range);
    }

    public double setDistanceIfSmaller(int i, double distance) {
        return steps[i] = Math.min(distance, getDistance(i));
    }
    
}
