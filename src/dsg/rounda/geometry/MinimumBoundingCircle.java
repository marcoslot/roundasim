/**
 * 
 */
package dsg.rounda.geometry;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Triangle;
import com.vividsolutions.jts.util.Assert;

/**
 * Computes the Minimum Bounding Circle (MBC)
 * for the points in a {@link Geometry}.
 * The MBC is the smallest circle which contains
 * all the input points 
 * (this is sometimes known as the Smallest Enclosing Circle).
 * This is equivalent to computing the Maximum Diameter 
 * of the input point set.
 * <p>
 * The geometric circle can be specified in two equivalent ways,
 * both of which are provide as output by this class:
 * <ul>
 * <li>As a centre point and a radius
 * <li>By the set of points defining the circle.
 * Depending on the number of points in the input
 * and their relative positions, this
 * will be specified by anywhere from 0 to 3 points. 
 * 0 or 1 points indicate an empty or trivial input point arrangment.
 * 2 or 3 points define a circle which contains 
 * all the input points.
 * </ul>
 * The class also provides a Geometry which approximates the
 * shape of the MBC (although as an approximation 
 * it is <b>not</b> guaranteed to <tt>cover</tt> all the input points.)
 * 
 * @author Martin Davis
 *
 */
public class MinimumBoundingCircle 
{
    /*
   * The algorithm used is based on the one by Jon Rokne in 
   * the article "An Easy Bounding Circle" in <i>Graphic Gems II</i>.
     */
    
    private Geometry input;
    private Coordinate[] extremalPts = null;
    private Coordinate centre = null;
    private double radius = 0.0;
    
    /**
     * Creates a new object for computing the minimum bounding circle for the
     * point set defined by the vertices of the given geometry.
     * 
     * @param geom the geometry to use to obtain the point set 
     */
    public MinimumBoundingCircle(Geometry geom)
    {
        this.input = geom;
    }
    
    /**
     * Gets a geometry which represents the Minimum Bounding Circle.
     * If the input is degenerate (empty or a single unique point),
     * this method will return an empty geometry or a single Point geometry.
     * Otherwise, a Polygon will be returned which approximates the 
     * Minimum Bounding Circle. 
     * (Note that because the computed polygon is only an approximation, 
     * it may not precisely contain all the input points.)
     * 
     * @return a Geometry representing the Minimum Bounding Circle.
     */
    public Geometry getCircle()
    {
        //TODO: ensure the output circle contains the extermal points.
        //TODO: or maybe even ensure that the returned geometry contains ALL the input points?
        
        compute();
        if (centre == null)
            return input.getFactory().createPolygon(null, null);
        Point centrePoint = input.getFactory().createPoint(centre);
        if (radius == 0.0)
            return centrePoint;
        return centrePoint.buffer(radius);
    }
    
    /**
     * Gets the extremal points which define the computed Minimum Bounding Circle.
     * There may be zero, one, two or three of these points,
     * depending on the number of points in the input
     * and the geometry of those points.
     * 
     * @return the points defining the Minimum Bounding Circle
     */
    public Coordinate[] getExtremalPoints() 
    {
        compute();
        return extremalPts;
    }
    
    /**
     * Gets the centre point of the computed Minimum Bounding Circle.
     * 
     * @return the centre point of the Minimum Bounding Circle
     * @return null if the input is empty
     */
    public Coordinate getCentre() 
    {
        compute();
        return centre;
    }
    
    /**
     * Gets the radius of the computed Minimum Bounding Circle.
     * 
     * @return the radius of the Minimum Bounding Circle
     */
    public double getRadius() 
    {
        compute();
        return radius;
    }
    
    private void computeCentre() 
    {
        switch (extremalPts.length) {
        case 0:
            centre = null;
            break;
        case 1:
            centre = extremalPts[0];
            break;
        case 2:
            centre = new Coordinate(
                    (extremalPts[0].x + extremalPts[1].x) / 2.0,
                    (extremalPts[0].y + extremalPts[1].y) / 2.0
                    );
            break;
        case 3:
            centre = Triangle.circumcentre(extremalPts[0], extremalPts[1], extremalPts[2]);
            break;
        }
    }
    
    private void compute()
    {       
        if (extremalPts != null) return;

        computeCirclePoints();
        computeCentre();
        if (centre != null)
            radius = centre.distance(extremalPts[0]);
    }
    
    private void computeCirclePoints()
    {
        // handle degenerate cases
        if (input.isEmpty()) {
            extremalPts = new Coordinate[0];
            return;
        }
        if (input.getNumPoints() == 1) {
            Coordinate[] pts = input.getCoordinates();
            extremalPts = new Coordinate[] { new Coordinate(pts[0]) };
            return;
        }
        
        Geometry convexHull = input.convexHull();
        
        /**
         * Computing the convex hull also have the effect of eliminating duplicate points
         */
        
        // check for degenerate or trivial cases
        Coordinate[] hullPts = convexHull.getCoordinates();
        
        // strip duplicate final point, if any
        Coordinate[] pts = hullPts;
        if (hullPts[0].equals2D(hullPts[hullPts.length - 1])) {
            pts = new Coordinate[hullPts.length - 1];
            copyDeep(hullPts, 0, pts, 0, hullPts.length - 1);
        }
        
        if (pts.length <= 3) {
            extremalPts = CoordinateArrays.copyDeep(pts);
            return;
        }
        
        // find a point P with minimum Y ordinate
        Coordinate P = lowestPoint(pts);
        
        // find a point Q such that the angle that PQ makes with the x-axis is minimal
        Coordinate Q = pointWitMinAngleWithX(pts, P);
        
        /**
         * Iterate over the remaining points to find 
         * a pair or triplet of points which determine the minimal circle.
         * By the design of the algorithm, 
         * at most <tt>pts.length</tt> iterations are required to terminate 
         * with a correct result.
         */ 
        for (int i = 0; i < pts.length; i++) {
            Coordinate R = pointWithMinAngleWithSegment(pts, P, Q);
            
            // if PRQ is obtuse, then MBC is determined by P and Q
            if (Angle.isObtuse(P, R, Q)) {
                extremalPts = new Coordinate[] { new Coordinate(P), new Coordinate(Q) };
                return;
            }
            // if RPQ is obtuse, update baseline and iterate
            if (Angle.isObtuse(R, P, Q)) {
                P = R;
                continue;
            }
            // if RQP is obtuse, update baseline and iterate
            if (Angle.isObtuse(R, Q, P)) {
                Q = R;
                continue;
            }
            // otherwise all angles are acute, and the MBC is determined by the triangle PQR
            extremalPts = new Coordinate[] { new Coordinate(P), new Coordinate(Q), new Coordinate(R) };
            return;
        }
        Assert.shouldNeverReachHere("Logic failure in Minimum Bounding Circle algorithm"); 
    }

    /**
     * Creates a deep copy of a given section of a source {@link Coordinate} array
     * into a destination Coordinate array.
     * The destination array must be an appropriate size to receive
     * the copied coordinates.
     *
     * @param src an array of Coordinates
     * @param srcStart the index to start copying from
     * @param dest the 
     * @param destStart the destination index to start copying to
     * @param length the number of items to copy
     */
    private static void copyDeep(Coordinate[] src, int srcStart, Coordinate[] dest, int destStart, int length) {
      for (int i = 0; i < length; i++) {
        dest[destStart + i] = new Coordinate(src[srcStart + i]);
      }
    }

    private static Coordinate lowestPoint(Coordinate[] pts)
    {
        Coordinate min = pts[0];
        for (int i = 1; i < pts.length; i++) {
            if (pts[i].y < min.y)
                min = pts[i];
        }
        return min;
    }
    
    private static Coordinate pointWitMinAngleWithX(Coordinate[] pts, Coordinate P)
    {
        double minSin = Double.MAX_VALUE;
        Coordinate minAngPt = null;
        for (int i = 0; i < pts.length; i++) {
            
            Coordinate p = pts[i];
            if (p == P) continue;
            
            /**
             * The sin of the angle is a simpler proxy for the angle itself
             */
            double dx = p.x - P.x;
            double dy = p.y - P.y;
            if (dy < 0) dy = -dy;
            double len = Math.sqrt(dx * dx + dy * dy);
            double sin = dy / len;
            
            if (sin < minSin) {
                minSin = sin;
                minAngPt = p;
            }
        }
        return minAngPt;
    }
    
    private static Coordinate pointWithMinAngleWithSegment(Coordinate[] pts, Coordinate P, Coordinate Q)
    {
        double minAng = Double.MAX_VALUE;
        Coordinate minAngPt = null;
        for (int i = 0; i < pts.length; i++) {
            
            Coordinate p = pts[i];
            if (p == P) continue;
            if (p == Q) continue;
            
            double ang = Angle.angleBetween(P, p, Q);
            if (ang < minAng) {
                minAng = ang;
                minAngPt = p;
            }
        }
        return minAngPt;
        
    }
}