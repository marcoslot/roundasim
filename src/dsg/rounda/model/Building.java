/**
 * 
 */
package dsg.rounda.model;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import dsg.rounda.geometry.MinimumBoundingCircle;

/**
 * Represents a building in the world
 */
public class Building {

    private static final GeometryFactory GEOM = new GeometryFactory();
    
    final Polygon polygon;
    final MinimumBoundingCircle circle;

    /**
     * @param polygon
     */
    public Building(Polygon polygon) {
        this.polygon = polygon;
        this.circle = new MinimumBoundingCircle(polygon);
    }

    public Building(List<Coordinate> coordinates) {
        this(coordinates.toArray(new Coordinate[coordinates.size()]));
    }

    public Building(Coordinate[] coordinates) {
        this(new CoordinateArraySequence(coordinates));
    }
    
    public Building(CoordinateSequence seq) {
        this(new LinearRing(seq, GEOM));
    }
    
    public Building(LinearRing ring) {
        this(new Polygon(ring, new LinearRing[0], GEOM));
    }

    /**
     * @return the polygon
     */
    public Polygon getPolygon() {
        return polygon;
    }
    
    /**
     * The distance of the building to the given coordinate
     * is at least as big as the value returned by this function
     *  
     * @param point
     * @return the minimum distance
     */
    public double minDistance(Coordinate point) {
        Coordinate circleCentre = circle.getCentre();
        double circleRadius = circle.getRadius();
        double dx = circleCentre.x - point.x;
        double dy = circleCentre.y - point.y;
        return Math.sqrt(dx*dx + dy*dy) - circleRadius;
    }
    
}
