/**
 * 
 */
package dsg.rounda.gui.awt;

import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;

/**
 * A feature to be displayed in the GUI
 */
public class FeatureAWT {

    public enum Type {
        DRAW,
        FILL
    }
    
    final Shape shape;
    final Stroke stroke;
    final Color color;
    final Type type;
    final double expires;
    /**
     * @param shape
     * @param stroke
     * @param color
     * @param expires
     */
    public FeatureAWT(Shape shape, Stroke stroke, Color color, Type type, double expires) {
        this.shape = shape;
        this.stroke = stroke;
        this.color = color;
        this.type = type;
        this.expires = expires;
    }
    /**
     * @return the shape
     */
    public Shape getShape() {
        return shape;
    }
    /**
     * @return the stroke
     */
    public Stroke getStroke() {
        return stroke;
    }
    /**
     * @return the color
     */
    public Color getColor() {
        return color;
    }
    /**
     * @return the expires
     */
    public double getExpires() {
        return expires;
    }
    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }
    
    
    
}
