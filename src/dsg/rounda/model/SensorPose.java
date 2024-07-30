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
public class SensorPose {

    final Coordinate relativePosition;
    final Vector2D relativeOrientation;

    /**
     * @param relativePosition
     * @param orientation
     */
    public SensorPose(Coordinate relativePosition, Vector2D orientation) {
        super();
        this.relativePosition = relativePosition;
        this.relativeOrientation = orientation;
    }

    /**
     * @return the relativePosition
     */
    public Coordinate getRelativePosition() {
        return new Coordinate(relativePosition);
    }

    /**
     * @return the orientation
     */
    public Vector2D getRelativeOrientation() {
        return relativeOrientation;
    }

    public Coordinate getAbsolutePosition(Coordinate vehiclePosition, Vector2D vehicleOrientation) {

        Coordinate rotatedRelativeSensorPosition = new Coordinate( 
            vehicleOrientation.getX() * relativePosition.x - vehicleOrientation.getY() * relativePosition.y,
            vehicleOrientation.getY() * relativePosition.x + vehicleOrientation.getX() * relativePosition.y
        );
        
        Coordinate sensorPosition = new Coordinate(
            vehiclePosition.x + rotatedRelativeSensorPosition.x,
            vehiclePosition.y + rotatedRelativeSensorPosition.y
        );
        
        return sensorPosition;
    }
    
    public Vector2D getAbsoluteOrientation(Vector2D vehicleOrientation) {

        Vector2D sensorOrientation = new Vector2D( 
            vehicleOrientation.getX() * relativeOrientation.getX() - vehicleOrientation.getY() * relativeOrientation.getY(),
            vehicleOrientation.getY() * relativeOrientation.getX() + vehicleOrientation.getX() * relativeOrientation.getY()
        );
        
        return sensorOrientation;
    }
}
