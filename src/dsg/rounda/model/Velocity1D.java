/**
 * 
 */
package dsg.rounda.model;

/**
 * Represents a velocity and acceleration in 1D space
 */
public class Velocity1D {

    public static final double MAX_ACCELERATION = 10; //m/s^2
    public static final double MAX_DECELERATION = 20; //m/s^2
    
    double velocity;
    double acceleration;
    
    /**
     * 
     */
    public Velocity1D(double offsetVelocity) {
        this.velocity = offsetVelocity;
        this.acceleration = 0.0;
    }

    public Velocity1D(Velocity1D velocity) {
        this.velocity = velocity.velocity;
    }

    /**
     * @return the offsetVelocity
     */
    public synchronized double getRoadVelocity() {
        return velocity;
    }

    /**
     * @param offsetVelocity the offsetVelocity to set
     */
    public synchronized void setRoadVelocity(double offsetVelocity) {
        this.velocity = offsetVelocity;
    }

    public double getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(double acceleration) {
        if(acceleration > MAX_ACCELERATION) {
            acceleration = MAX_ACCELERATION;
        } else if(acceleration < -MAX_DECELERATION) {
            acceleration = -MAX_DECELERATION;
        }
        
        this.acceleration = acceleration;
    }

    /**
     * Compute the distance driven in timeDiffSeconds given the current
     * velocity and acceleration.
     * 
     * @param timeDiffSeconds the number of seconds
     * @return the distance the vehicle would drive in timeDiffSeconds
     * if the acceleration is not changed before then
     */
    public double computeDistance(double timeDiffSeconds) {
        return computeDistance(timeDiffSeconds, velocity, acceleration);
    }

    public double computeDistance(double timeDiffSeconds, double velocity, double acceleration) {
        double halfTimeDiffSeconds2 = 0.5*timeDiffSeconds*timeDiffSeconds;
        double distance = velocity * timeDiffSeconds + acceleration * halfTimeDiffSeconds2;
        return distance;
    }

    /**
     * The time it would take to drive distance at the current velocity
     * and acceleration.
     * 
     * @param distance the distance
     * @return the time it would take to drive the distance if no change to acceleration
     */
    public double computeTime(double distance) {
        return computeTimeDA(distance, acceleration);
    }

    public double computeTimeDA(double distance, double acceleration) {
        return computeTimeDVA(distance, velocity, acceleration);
    }

    public double computeTimeDVA(double distance, double velocity, double acceleration) {
        return (Math.sqrt(2*acceleration*distance+velocity*velocity) - velocity) / acceleration;
    }
    
    /**
     * Set to maximum deceleration
     */
    public void stop() {
        setAcceleration(-MAX_DECELERATION);
    }
    /**
     * Compute the distance required to stop
     * 
     * @return the distance required to stop
     */
    public double computeStoppingDistance() {
        return computeDistance(computeStoppingTime());
    }

    /**
     * Compute the time required to stop
     * 
     * @return the time required to stop
     */
    public double computeStoppingTime() {
        return computeStoppingTime(velocity);
    }

    public double computeStoppingTime(double initialVelocity) {
        return initialVelocity / MAX_DECELERATION;
    }

    public double computeMinDrivingTime(double distance) {
        return computeTimeDA(distance, MAX_ACCELERATION);
    }

    public double computeMinDrivingTimeFrom0(double distance) {
        return computeTimeDVA(distance, 0.0, MAX_ACCELERATION);
    }

    public double computeDelayedStoppingDistance(double dt) {
        return computeDistance(dt) + computeDistance(computeStoppingTime(), velocity, -MAX_DECELERATION);
    }

    public double computeStoppingDistance(double velocity) {
        return computeDistance(computeStoppingTime(velocity), velocity, -MAX_DECELERATION);
    }

}
