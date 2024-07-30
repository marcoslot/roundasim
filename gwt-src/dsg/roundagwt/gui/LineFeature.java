/**
 * 
 */
package dsg.roundagwt.gui;

import com.vividsolutions.jts.geom.LineSegment;

/**
 *
 */
public class LineFeature {

    final LineSegment line;
    final String color;
    
    /**
     * @param line
     * @param color
     * @param expires
     */
    public LineFeature(LineSegment line, String color) {
        this.line = line;
        this.color = color;
    }

    /**
     * @return the line
     */
    public LineSegment getLine() {
        return line;
    }

    /**
     * @return the color
     */
    public String getColor() {
        return color;
    }


}
