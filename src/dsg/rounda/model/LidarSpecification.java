/**
 * 
 */
package dsg.rounda.model;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.math.Vector2D;

import dsg.rounda.geometry.BeamCalc;
/**
 * Specifications of a single LIDAR sensor
 */
public class LidarSpecification {

    final SensorPose pose;
    final double range;
    final double stepSize;
    final int numSteps;

    // cache
    private Double angle;
    private BeamCalc beamCalc;
    
    /**
     * @param pose
     * @param range
     */
    public LidarSpecification(SensorPose pose, double range, int numSteps, double stepSize) {
        super();
        this.pose = pose;
        this.range = range;
        this.numSteps = numSteps;
        this.stepSize = stepSize;
        this.beamCalc = new BeamCalc(this);
    }

    /**
     * @param pose
     * @param range
     */
    public LidarSpecification(Coordinate relativePosition, Vector2D orientation, double range, int numSteps, double stepSize) {
        super();
        this.pose = new SensorPose(relativePosition, orientation);
        this.range = range;
        this.numSteps = numSteps;
        this.stepSize = stepSize;
        this.beamCalc = new BeamCalc(this);
    }

    /**
     * @return the pose
     */
    public SensorPose getPose() {
        return pose;
    }

    /**
     * @return the range
     */
    public double getRange() {
        return range;
    }

    /**
     * @return the stepSize
     */
    public double getStepSize() {
        return stepSize;
    }

    /**
     * @return the numSteps
     */
    public int getNumSteps() {
        return numSteps;
    }
    
    /**
     * 
     * @return
     */
    public double getAngularRange() {
       return stepSize * (numSteps-1); 
    }
    
    /**
     * Efficient computation of beam orientation as a vector
     * 
     * @param beamIndex
     * @return
     */
    public Vector2D getBeamOrientation(int beamIndex) {
        return beamCalc.getVector(beamIndex);
    }
    
    public double getSensorOrientation() {
        if(angle != null) {
            return angle;
        }
        return angle = pose.getRelativeOrientation().angle();
    }

    public Coordinate getSensorPosition() {
        return pose.getRelativePosition();
    }

    /**
     * @param vehiclePosition
     * @param vehicleOrientation
     * @return
     * @see dsg.rounda.model.SensorPose#getAbsolutePosition(com.vividsolutions.jts.geom.Coordinate, com.vividsolutions.jts.math.Vector2D)
     */
    public Coordinate getAbsolutePosition(Coordinate vehiclePosition,
            Vector2D vehicleOrientation) {
        return pose.getAbsolutePosition(vehiclePosition, vehicleOrientation);
    }

    /**
     * @param vehicleOrientation
     * @return
     * @see dsg.rounda.model.SensorPose#getAbsoluteOrientation(com.vividsolutions.jts.math.Vector2D)
     */
    public Vector2D getAbsoluteOrientation(Vector2D vehicleOrientation) {
        return pose.getAbsoluteOrientation(vehicleOrientation);
    }

}
