/**
 * 
 */
package dsg.rounda.gui;

import com.vividsolutions.jts.geom.Polygon;

/**
 * Polygon to draw to the screen
 */
public class PolygonFeature {

    final Polygon polygon;
    final double lineWidth;
    final RGBA color;
    
    /**
     * @param poly
     * @param color
     * @param expires
     */
    public PolygonFeature(Polygon poly, double lineWidth, RGBA color) {
        this.polygon = poly;
        this.lineWidth = lineWidth;
        this.color = color;
    }

    /**
     * @return the line
     */
    public Polygon getLine() {
        return polygon;
    }

    /**
     * @return the color
     */
    public RGBA getColor() {
        return color;
    }

    /**
     * 
     * @return the line width
     */
    public double getLineWidth() {
        return lineWidth;
    }
}
