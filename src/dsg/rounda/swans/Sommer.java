/**
 * 
 */
package dsg.rounda.swans;

import jist.swans.field.PathLoss;
import jist.swans.misc.Location;
import jist.swans.radio.RadioInfo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import dsg.rounda.model.Building;
import dsg.rounda.model.WorldState;

/**
 * Path loss model considering buildings based on
 * http://www.ccs-labs.org/bib/pdf/sommer2011computationally.pdf
 */
public class Sommer implements PathLoss {

    static final GeometryFactory GEOM = new GeometryFactory();
    static final double DEFAULT_BETA = 9.2; // dB
    static final double DEFAULT_GAMMA = 0.32; // dB/m

    final WorldState world;
    final PathLoss baseModel;
    
    double beta;
    double gamma;
    
    /**
     * 
     */
    public Sommer(WorldState world, PathLoss baseModel) {
        this.world = world;
        this.baseModel = baseModel;
        this.beta = DEFAULT_BETA;
        this.gamma = DEFAULT_GAMMA;
    }

    /**
     * @param srcRadio
     * @param srcLocation
     * @param dstRadio
     * @param dstLocation
     * @return
     * @see jist.swans.field.PathLoss#compute(jist.swans.radio.RadioInfo, jist.swans.misc.Location, jist.swans.radio.RadioInfo, jist.swans.misc.Location)
     */
    public double compute(
            RadioInfo srcRadio, Location srcLocation,
            RadioInfo dstRadio, Location dstLocation) {
        double baseLoss = baseModel.compute(srcRadio, srcLocation, dstRadio, dstLocation);
        Coordinate srcCoord = new Coordinate(srcLocation.getX(), dstLocation.getY());
        Coordinate dstCoord = new Coordinate(dstLocation.getX(), dstLocation.getY());
        LineString transLine = new LineSegment(srcCoord, dstCoord).toGeometry(GEOM);
        
        int numWalls = 0;
        double distanceInBuildings = 0.;
        
        for(Building building : world.getBuildings()) {
            Polygon buildingGeometry = building.getPolygon();
            
            Object intersections = buildingGeometry.intersection(transLine);
            
            if(intersections instanceof LineString) {
                Coordinate[] coordinates = ((LineString) intersections).getCoordinates();
                
                for(int i = 0; i < coordinates.length-1; i += 2) {
                    numWalls += 2;
                    distanceInBuildings += coordinates[i].distance(coordinates[i+1]);
                }
            }
        }
        
        return baseLoss + numWalls * beta + distanceInBuildings * gamma;
    }

}
