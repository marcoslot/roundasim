/**
 * 
 */
package dsg.rounda.model;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Represents the thoughts of this simulator on the global
 * political, societal, economic, and cultural stage.
 * 
 * It also describes a rectangle of interest in the 
 * simulator.  
 */
public class WorldView {

    double westX;
    double southY;
    double width;
    double height;
    
    public WorldView(
            double westX,
            double southY, 
            double width, 
            double height) {
        this.westX = westX;
        this.southY = southY;
        this.width = width;
        this.height = height;
    }


    public WorldView(WorldView worldView) {
        this(
             worldView.westX,
             worldView.southY,
             worldView.width,
             worldView.height
        );
    }


    /**
     * @return the worldViewX
     */
    public double getWestX() {
        return westX;
    }

    /**
     * @param worldViewX the worldViewX to set
     */
    public void setWestX(double worldViewX) {
        this.westX = worldViewX;
    }

    /**
     * @return the worldViewY
     */
    public double getNorthY() {
        return southY + height;
    }

    /**
     * @param southY the southY to set
     */
    public void setSouthY(double southY) {
        this.southY = southY;
    }


    /**
     * 
     * @return
     */
    public double getSouthY() {
        return southY;
    }
    
    /**
     * @return the worldWidth
     */
    public double getWidth() {
        return width;
    }

    /**
     * @param worldWidth the worldWidth to set
     */
    public void setWidth(double worldWidth) {
        this.width = worldWidth;
    }

    /**
     * @return the worldHeight
     */
    public double getHeight() {
        return height;
    }

    /**
     * @param worldHeight the worldHeight to set
     */
    public void setHeight(double worldHeight) {
        this.height = worldHeight;
    }

    /**
     * 
     * @return
     */
    public Coordinate getCentre() {
        return new Coordinate(westX + 0.5*width, southY +  0.5*height);
    }

    /**
     * 
     * @param other
     */
    public void set(WorldView other) {
        this.westX = other.westX;
        this.southY = other.southY;
        this.width = other.width;
        this.height = other.height;
    }


    /**
     * 
     * @param w
     * @param s
     */
    public void setWestSouth(double w, double s) {
        this.westX = w;
        this.southY = s;
    }

    /**
     * 
     * @return
     */
    public double getEastX() {
        return westX + width;
    }

    /**
     * 
     * @param newWestX
     * @param newSouthY
     * @param newWidth
     * @param newHeight
     */
    public void set(
            double newWestX, 
            double newSouthY, 
            double newWidth,
            double newHeight) {
        setWestX(newWestX);
        setSouthY(newSouthY);
        setWidth(newWidth);
        setHeight(newHeight);
    }
    
    /**
     * Zoom this world view towards a given zoom point
     * 
     * @param centre zoom point
     * @param zoomFactor zoom factor
     * @return
     */
    public WorldView zoomed(
            Coordinate centre, 
            double zoomFactor) {
        return zoomed(this, centre, zoomFactor);
    }
    
    /**
     * Zoom this world view towards a given zoom point
     * from an existing world view. The new world view
     * is a subset of the current world view if zooming in.
     * 
     * @param currentWorldView current world view
     * @param centre zoom point 
     * @param zoomFactor zoom factor relative to this worldview
     * @return
     */
    public WorldView zoomed(
            WorldView currentWorldView,
            Coordinate centre, 
            double zoomFactor) {
        double newWidth = getWidth() / zoomFactor;
        double newHeight = getHeight() / zoomFactor;
        
        // dx and dy to the top left corner of the screen from the zooming centre
        double dx = currentWorldView.getWestX() - centre.x;
        double dy = currentWorldView.getSouthY() - centre.y;
        
        // New size / old size ratio should be the same for width and height
        double ratio = newWidth / currentWorldView.getWidth();

        double newWestX = centre.x + dx * ratio;
        newWestX = Math.max(newWestX, getWestX());
        newWestX = Math.min(newWestX, getEastX() - newWidth);
        
        double newSouthY = centre.y + dy * ratio;
        newSouthY = Math.max(newSouthY, getSouthY());
        newSouthY = Math.min(newSouthY, getNorthY() - newHeight);
        
        return new WorldView(
                newWestX,
                newSouthY,
                newWidth,
                newHeight
        );
    }


}
