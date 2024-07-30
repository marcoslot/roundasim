/**
 * 
 */
package dsg.rounda.model;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import dsg.rounda.services.roadmap.TrackPoint1D;

/**
 * Immutable position state
 */
public class Position1D {

    private static final GeometryFactory GEOM = new GeometryFactory();

    final Track track;
    final double offset;
    
    // Cached computations
    private Pose2D pose2D;
    private Polygon polygon;
    
    /**
     * 
     */
    public Position1D(Track track, double offset) {
        this.track = track;
        this.offset = offset;
        this.pose2D = null;
        this.polygon = null;
    }

    public Position1D(Position1D position) {
        this.track = position.track;
        this.offset = position.offset;
        this.pose2D = position.pose2D;
        this.polygon = position.polygon;
    }

    /**
     * Compute the 2D geometry of the vehicle as a polygon in absolute space.
     * 
     * @param width the width of the vehicle
     * @param height the height of the vehicle
     * @return the geometry of the vehicle as a polygon
     */
    public Polygon getVehicleGeometry(double width, double length) {
        synchronized(this) {
            if(polygon != null) {
                return polygon;
            }
        }
        
        Pose2D pose = getPose2D();
        double carFrontX = length * pose.getOrientation().getX();
        double carFrontY = length * pose.getOrientation().getY();
        Coordinate carFront = new Coordinate(pose.getX() + carFrontX, pose.getY() + carFrontY);
        Coordinate carBack = pose.getPosition();
        LineSegment vehicleBase = new LineSegment(carBack, carFront);
        Coordinate backLeft = vehicleBase.pointAlongOffset(0.0, width * 0.5);
        Coordinate[] vehicleBoundaries = new Coordinate[] {
                // back left
                backLeft,
                // front left
                vehicleBase.pointAlongOffset(1.0, width * 0.5),
                // front right
                vehicleBase.pointAlongOffset(1.0, -width * 0.5),
                // back right
                vehicleBase.pointAlongOffset(0.0, -width * 0.5),
                // wrap around
                backLeft
        };
        CoordinateSequence coordinates = new CoordinateArraySequence(vehicleBoundaries);
        LinearRing shell = new LinearRing(coordinates, GEOM);
        Polygon result = new Polygon(shell, new LinearRing[0], GEOM);
        
        synchronized(this) {
            polygon = result;
        }
        
        return result;
    }

    public synchronized Pose2D getPose2D() {
        if(pose2D == null) {
            pose2D = track.getPose2D(offset);
        }
        return pose2D;
    }


    /**
     * @return the offset
     */
    public double getOffset() {
        return offset;
    }

    /**
     * @return the track
     */
    public Track getTrack() {
        return track;
    }

    public int getTrackID() {
        return track.getId();
    }
    
    public TrackPoint1D toTrackPoint() {
        return new TrackPoint1D(getTrack().getId(), getOffset());
    }

}
