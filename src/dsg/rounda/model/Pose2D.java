/**
 * 
 */
package dsg.rounda.model;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.math.Vector2D;

/**
 * @author slotm
 *
 */
public class Pose2D {

    final Coordinate position;
    final Vector2D orientation;
    
    // cache
    private Double angle;
    /**
     * @param x
     * @param y
     * @param orientation
     */
    public Pose2D(Coordinate position, Vector2D orientation) {
        this.position = position;
        this.orientation = orientation;
    }
    /**
     * @return the x
     */
    public double getX() {
        return position.x;
    }
    /**
     * @return the y
     */
    public double getY() {
        return position.y;
    }
    /**
     * 
     * @return
     */
    public double getAngle() {
       if(angle != null) {
           return angle;
       }
       return angle = orientation.angle();
    }
    /**
     * @return the orientation
     */
    public Vector2D getOrientation() {
        return orientation;
    }
    /**
     * @return the position
     */
    public Coordinate getPosition() {
        return position;
    }
    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((orientation == null) ? 0 : orientation.hashCode());
        result = prime * result
                + ((position == null) ? 0 : position.hashCode());
        return result;
    }
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Pose2D)) {
            return false;
        }
        Pose2D other = (Pose2D) obj;
        if (orientation == null) {
            if (other.orientation != null) {
                return false;
            }
        } else if (!orientation.equals(other.orientation)) {
            return false;
        }
        if (position == null) {
            if (other.position != null) {
                return false;
            }
        } else if (!position.equals(other.position)) {
            return false;
        }
        return true;
    }
    

}
