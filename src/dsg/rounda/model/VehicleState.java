/**
 * 
 */
package dsg.rounda.model;

import java.util.concurrent.atomic.AtomicReference;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Represents the state of a rectangular vehicle on the 
 * 1-dimensional road network
 */
public class VehicleState implements VehicleProperties {
    
    final int id;
    final Velocity1D velocity;
    final Trajectory1D trajectory;
    final double length;
    final double width;
    final double maxVelocity;

    // Position1D objects are immutable to ensure atomic updates
    // of offset and track. This value is read and set very frequently.
    // AtomicReference is used because synchronized gave poor 
    // performance. 
    private final AtomicReference<Position1D> positionRef;
	
    volatile IndicatorState indicatorDirection; 
    
    /**
     * 
     */
    public VehicleState(
            int id, 
            Position1D position, 
            Velocity1D velocity, 
            double width, 
            double length,
            double maxVelocity) {
        this.id = id;
        this.positionRef = new AtomicReference<Position1D>(new Position1D(position));
        this.velocity = new Velocity1D(velocity);
        this.trajectory = new Trajectory1D();
        this.width = width;
        this.length = length;
        this.maxVelocity = maxVelocity;
        this.indicatorDirection = IndicatorState.NONE;
    }
    
    /**
     * Compute the 2D geometry of the vehicle as a polygon in absolute space.
     * This method is deferred to Position1D.java, because the result changes
     * only if the position changes. Thus it is easier to cache it in Position1D. 
     * 
     * @return the geometry of the vehicle as a polygon
     */
    public Polygon getVehicleGeometry() {
        return getBackPosition().getVehicleGeometry(getWidth(), getLength());
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the position
     */
    public Position1D getBackPosition() {
        return positionRef.get();
    }

    /**
     * 
     * @param newPosition
     */
    public void setPosition(Position1D newPosition) {
        positionRef.set(newPosition);
    }
    
    /**
     * @return the velocity
     */
    public Velocity1D getVelocity() {
        return velocity;
    }

    /**
     * @return the trajectory
     */
    public Trajectory1D getTrajectory() {
        return trajectory;
    }

    /**
     * @return the length
     */
    public double getLength() {
        return length;
    }

    /**
     * @return the width
     */
    public double getWidth() {
        return width;
    }

    public Coordinate getPosition2D() {
        return getBackPosition().getPose2D().getPosition();
    }

    public double getMaximumVelocity() {
        return maxVelocity;
    }

	public void setIndicatorState(LaneChangeDirection dir) {
		switch(dir) {
		case LEFT:
			indicatorDirection = IndicatorState.LEFT;
			break;
		case RIGHT:
			indicatorDirection = IndicatorState.RIGHT;
			break;
		}
	}

	public IndicatorState getIndicatorState() {
		return indicatorDirection;
	}

	public void setIndicatorState(IndicatorState dir) {
		this.indicatorDirection = dir;
	}

    
}
    
