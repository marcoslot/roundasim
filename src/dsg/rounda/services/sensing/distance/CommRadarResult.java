/**
 * 
 */
package dsg.rounda.services.sensing.distance;

import dsg.rounda.services.comm.neighbourhood.NeighbourState;

/**
 * @author slotm
 *
 */
public class CommRadarResult {

    final NeighbourState state;
    final double distance;
    /**
     * @param state
     * @param distance
     */
    public CommRadarResult(NeighbourState state, double distance) {
        super();
        this.state = state;
        this.distance = distance;
    }
    /**
     * @return the neighbour
     */
    public NeighbourState getState() {
        return state;
    }
    /**
     * @return the distance
     */
    public double getDistance() {
        return distance;
    }
    

}
