/**
 * 
 */
package dsg.rounda.services.roadmap;

/**
 * @author slotm
 *
 */
public class RoadBlockDetection {

    final RoadBlock roadBlock;
    final double distance;
    
    public RoadBlockDetection(RoadBlock roadBlock, double distance) {
        super();
        this.roadBlock = roadBlock;
        this.distance = distance;
    }
    /**
     * @return the roadBlock
     */
    public RoadBlock getRoadBlock() {
        return roadBlock;
    }
    /**
     * @return the distance
     */
    public double getDistance() {
        return distance;
    }
    

}
