/**
 * 
 */
package dsg.rounda.services.roadmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;

/**
 * A spatial grid data structure for quickly finding
 * the roads in an area
 *
 */
public class SpatialGrid2D<T> {

    private static final GeometryFactory GEOM = new GeometryFactory();

    final Envelope gridBox;
    final int numXCells;
    final int numYCells;
    final List<T>[][] spatialGrid;
    final double cellWidth;
    final double cellHeight;

    /**
     * 
     * @param gridSize the min/max coordinates of the spatial data structure
     * @param numXCells the number of horizontal cells
     * @param numYCells the number of vertical cells
     */
    @SuppressWarnings("unchecked")
    public SpatialGrid2D(Envelope gridSize, int numXCells, int numYCells) {
        this.gridBox = gridSize;
        this.numXCells = numXCells;
        this.numYCells = numYCells;
        this.spatialGrid = new List[numXCells][numYCells];
        this.cellWidth = (gridBox.getMaxX() - gridBox.getMinX()) / numXCells;
        this.cellHeight = (gridBox.getMaxY() - gridBox.getMinY()) / numYCells;
    }
    
    public void clear() {
    	for(int x = 0; x < numXCells; x++) {
    		for(int y = 0; y < numYCells; y++) {
    			spatialGrid[x][y] = null;
    		}
    	}
    }

    public Envelope getGridBox() {
		return gridBox;
	}

	public int getNumXCells() {
		return numXCells;
	}

	public int getNumYCells() {
		return numYCells;
	}

	/**
     * Add an object to the grid for all cells that intersect with
     * geom.
     * 
     * @param object the object to add
     * @param geom the geometry 
     */
    public void addToGrid(T object, Geometry geom) {
        Envelope geomBox = geom.getEnvelopeInternal();

        // Translate envelope to cell indices
        int minX = (int) ((geomBox.getMinX() - gridBox.getMinX()) / cellWidth);
        int maxX = (int) Math.min((geomBox.getMaxX() - gridBox.getMinX()) / cellWidth, numXCells-1);
        int minY = (int) ((geomBox.getMinY() - gridBox.getMinY()) / cellHeight);
        int maxY = (int) Math.min((geomBox.getMaxY() - gridBox.getMinY()) / cellHeight, numYCells-1);

        for(int x = minX; x <= maxX; x++) {
            for(int y = minY; y <= maxY; y++) {
                Geometry cellBox = getCellGeometry(x, y);

                if(!geom.intersects(cellBox)) {
                    continue;
                }

                add(x, y, object);
            }
        }
    }

    public void addToGrid(T object, LineSegment pathSegment) {
        addToGrid(object, pathSegment.p0, pathSegment.p1);
    }

    public void addToGrid(T object, Coordinate p0, Coordinate p1) {
        addToGrid(object, p0.x, p0.y, p1.x, p1.y);
    }
    
    public void addToGrid(T object, double x0, double y0, double x1, double y1) {
        // Make coordinates relative to grid
        x0 = (x0 - gridBox.getMinX()) / cellWidth;
        y0 = (y0 - gridBox.getMinY()) / cellHeight;
        x1 = (x1 - gridBox.getMinX()) / cellWidth;
        y1 = (y1 - gridBox.getMinY()) / cellHeight;
        
        // Ray-tracing on grid
        // http://playtechs.blogspot.ie/2007/03/raytracing-on-grid.html
        double dx = Math.abs(x1 - x0);
        double dy = Math.abs(y1 - y0);

        int x = (int) (x0);
        int y = (int) (y0 );

        int n = 1;
        int xIncrement, yIncrement;
        double error;

        if (dx == 0) {
            xIncrement = 0;
            error = Double.POSITIVE_INFINITY;
        } else if (x1 > x0) {
            xIncrement = 1;
            n += (int)((x1)) - x;
            error = (Math.floor(x0) + 1 - x0) * dy;
        } else {
            xIncrement = -1;
            n += x - (int)((x1));
            error = (x0 - Math.floor(x0)) * dy;
        }

        if (dy == 0) {
            yIncrement = 0;
            error -= Double.POSITIVE_INFINITY;
        } else if (y1 > y0) {
            yIncrement = 1;
            n += (int)((y1)) - y;
            error -= (Math.floor(y0) + 1 - y0) * dx;
        } else {
            yIncrement = -1;
            n += y - (int)((y1));
            error -= (y0 - Math.floor(y0)) * dx;
        }

        for (; n > 0; --n) {
            add(x, y, object);

            if (error > 0) {
                y += yIncrement;
                error -= dx;
            } else {
                x += xIncrement;
                error += dy;
            }
        }
    }

    /**
     * Add an object to the grid for all cells that intersect with
     * envelope geomBox.
     * 
     * @param object the object to add
     * @param geomBox the envelope  
     */
    public void addToGrid(T object, Envelope geomBox) {
        // Translate envelope to cell indices
        int minX = (int) ((geomBox.getMinX() - gridBox.getMinX()) / cellWidth);
        int maxX = (int) ((geomBox.getMaxX() - gridBox.getMinX()) / cellWidth);
        int minY = (int) ((geomBox.getMinY() - gridBox.getMinY()) / cellHeight);
        int maxY = (int) ((geomBox.getMaxY() - gridBox.getMinY()) / cellHeight);

        for(int x = minX; x <= maxX; x++) {
            for(int y = minY; y <= maxY; y++) {
                add(x, y, object);
            }
        }
    }

    /**
     * Add an object to cell (x,y)
     * 
     * @param x 
     * @param y
     * @param object
     */
    public void add(int x, int y, T object) {
        if(spatialGrid[x][y] == null) {
            spatialGrid[x][y] = new ArrayList<T>();
        }

        spatialGrid[x][y].add(object);
    }

    private Geometry getCellGeometry(int x, int y) {
        return GEOM.toGeometry(getCellBox(x, y));
    }

    private Envelope getCellBox(int x, int y) {
        double minX = gridBox.getMinX() + x * cellWidth;
        double maxX = minX + cellWidth;
        double minY = gridBox.getMinY() + x * cellHeight;
        double maxY = minY + cellHeight;
        return new Envelope(minX, maxX, minY, maxY);
    }

    /**
     * Get objects near the given line segment
     * 
     * @param line
     * @return
     */
    public Collection<T> getNearbyObjects(LineSegment line) {
        return getNearbyObjects(line.p0, line.p1);
    }

    public Collection<T> getNearbyObjects(Coordinate p0, Coordinate p1) {
        return getNearbyObjects(p0.x, p0.y, p1.x, p1.y);
    }

    public Collection<T> getNearbyObjects(double x0, double y0, double x1, double y1) {
        // Make coordinates relative to grid
        x0 = (x0 - gridBox.getMinX()) / cellWidth;
        y0 = (y0 - gridBox.getMinY()) / cellHeight;
        x1 = (x1 - gridBox.getMinX()) / cellWidth;
        y1 = (y1 - gridBox.getMinY()) / cellHeight;
        
        // Ray-tracing on grid
        // http://playtechs.blogspot.ie/2007/03/raytracing-on-grid.html
        final double dx = Math.abs(x1 - x0);
        final double dy = Math.abs(y1 - y0);

        int x = (int) (x0);
        int y = (int) (y0);

        int n = 1;
        int xIncrement, yIncrement;
        double error;

        if (dx == 0) {
            xIncrement = 0;
            error = Double.POSITIVE_INFINITY;
        } else if (x1 > x0) {
            xIncrement = 1;
            n += (int)(x1) - x;
            error = (Math.floor(x0) + 1 - x0) * dy;
        } else {
            xIncrement = -1;
            n += x - (int)((x1));
            error = (x0 - Math.floor(x0)) * dy;
        }

        if (dy == 0) {
            yIncrement = 0;
            error -= Double.POSITIVE_INFINITY;
        } else if (y1 > y0) {
            yIncrement = 1;
            n += (int)(y1) - y;
            error -= (Math.floor(y0) + 1 - y0) * dx;
        } else {
            yIncrement = -1;
            n += y - (int)((y1));
            error -= (y0 - Math.floor(y0)) * dx;
        }
        
        final Set<T> objects = new HashSet<T>();
        
        for (; n > 0; --n) {
            if(0 <= x && x < numXCells
            && 0 <= y && y < numYCells
            && spatialGrid[x][y] != null) {
                objects.addAll(spatialGrid[x][y]);
            }

            if (error > 0) {
                y += yIncrement;
                error -= dx;
            } else {
                x += xIncrement;
                error += dy;
            }
        }
        
        return objects;
    }

    /**
     * Get an iterable for objects that are near queryBox
     * 
     * @param queryBox the envelope to query for
     * @return a superset of objects in queryBox
     */
    public Collection<T> getNearbyObjects(Envelope queryBox) {
        // Get relative rectangle
        double minX = (queryBox.getMinX() - gridBox.getMinX()) / cellWidth;
        double maxX = (queryBox.getMaxX() - gridBox.getMinX()) / cellWidth;
        double minY = (queryBox.getMinY() - gridBox.getMinY()) / cellHeight;
        double maxY = (queryBox.getMaxY() - gridBox.getMinY()) / cellHeight;

        final int minXCell = (int) Math.min(Math.max(minX, 0), numXCells - 1);
        final int maxXCell = (int) Math.min(Math.max(maxX, 0), numXCells - 1);
        final int minYCell = (int) Math.min(Math.max(minY, 0), numYCells - 1);
        final int maxYCell = (int) Math.min(Math.max(maxY, 0), numYCells - 1);

        final Set<T> objects = new HashSet<T>();

        for(int x = minXCell; x <= maxXCell; x++) {
            for(int y = minYCell; y <= maxYCell; y++) {
                if(spatialGrid[x][y] != null) {
                    objects.addAll(spatialGrid[x][y]);
                }
            }
        }
        return objects;
    }

}
