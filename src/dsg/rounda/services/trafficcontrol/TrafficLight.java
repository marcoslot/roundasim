/**
 * 
 */
package dsg.rounda.services.trafficcontrol;

import dsg.rounda.model.Position1D;
import dsg.rounda.services.roadmap.TrackPoint1D;

/**
 * A traffic light that can be in any of three states (GREEN/ORANGE/RED)
 */
public class TrafficLight {
    
    public enum Colour {
        GREEN,
        ORANGE,
        RED;
    }
    
    final TrackPoint1D position;
    Colour state;

    /**
     * Create a new traffic light with the given initial state
     */
    public TrafficLight(TrackPoint1D position, Colour initialState) {
        this.position = position;
        this.state = initialState;
    }

    /**
     * @return the position
     */
    public TrackPoint1D getPosition() {
        return position;
    }

    /**
     * @return the currentState
     */
    public Colour getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(Colour state) {
        this.state = state;
    }
    
    

}
