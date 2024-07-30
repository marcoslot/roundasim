/**
 * 
 */
package dsg.rounda.gui;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Circle feature to draw on the screen
 */
public class CircleFeature {

    final Coordinate coordinate;
    final double radius;
    final RGBA color;
    /**
     * @param coordinate
     * @param radius
     * @param color
     */
    public CircleFeature(Coordinate coordinate, double radius, RGBA color) {
        this.coordinate = coordinate;
        this.radius = radius;
        this.color = color;
    }
    /**
     * @return the coordinate
     */
    public Coordinate getCoordinate() {
        return coordinate;
    }
    /**
     * @return the radius
     */
    public double getRadius() {
        return radius;
    }
    /**
     * @return the color
     */
    public RGBA getColor() {
        return color;
    }
    

}
