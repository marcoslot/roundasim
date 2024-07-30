/**
 * 
 */
package dsg.rounda.services.roadmap;


/**
 * Connection point to a track
 */
public class RoadBlock extends TrackPoint1D {
    
    final int magnitude;
    
    final int direction;

    public RoadBlock(
            int trackID, 
            double offset,
            int direction,
            int magnitude) {
        
        super(trackID, offset);
        
        this.direction = direction;
        this.magnitude = magnitude;
    }

    /**
     * -1 for left
     *  1 for right
     *  0 for either
     * 
     */
    public int getDirection() {
        return direction;
    }
    
    public int getMagnitude() {
        return magnitude;
    }


}
