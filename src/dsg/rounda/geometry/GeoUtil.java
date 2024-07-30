/**
 * 
 */
package dsg.rounda.geometry;

import java.util.Iterator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

/**
 * Utility methods for geometry
 */
public class GeoUtil {

    /**
     * 
     */
    private GeoUtil() {
    }

    public static double distance2(Coordinate from, Coordinate to) {
        double dx = from.x - to.x;
        double dy = from.y - to.y;
        return dx*dx + dy*dy;
    }

    public static double distance2(Point from, Point to) {
        double dx = from.getX() - to.getX();
        double dy = from.getY() - to.getY();
        return dx*dx + dy*dy;
    }
    
    public static <T> Iterable<T> toIterable(final Iterator<T> it) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return it;
            }
        };
    }

}
