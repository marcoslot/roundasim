/**
 * 
 */
package dsg.rounda.gui;

import com.vividsolutions.jts.geom.LineString;


/**
 * Line string to draw to the screen
 */
public class LineStringFeature {

    final LineString lineString;
    final double lineWidth;
    final RGBA color;
    
    /**
     * @param lineString
     * @param lineWidth
     * @param color
     */
    public LineStringFeature(LineString lineString, double lineWidth,
            RGBA color) {
        this.lineString = lineString;
        this.lineWidth = lineWidth;
        this.color = color;
    }
    /**
     * @return the lineString
     */
    public LineString getLineString() {
        return lineString;
    }
    /**
     * @return the lineWidth
     */
    public double getLineWidth() {
        return lineWidth;
    }
    /**
     * @return the color
     */
    public RGBA getColor() {
        return color;
    }
    
    
    
}
