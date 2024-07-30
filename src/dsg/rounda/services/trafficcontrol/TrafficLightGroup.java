/**
 * 
 */
package dsg.rounda.services.trafficcontrol;

import java.util.ArrayList;
import java.util.List;

import dsg.rounda.services.roadmap.TrackPoint1D;

/**
 * A group of traffic lights that follow the same phases
 */
public class TrafficLightGroup {

    final List<TrafficLight> trafficLights;
    
    TrafficLight.Colour groupState;
    
    /**
     * Create an empty traffic light group.
     */
    public TrafficLightGroup(TrafficLight.Colour initialState) {
        this.trafficLights = new ArrayList<TrafficLight>();
        this.groupState = initialState;
    }
    
    public void addTrafficLightAt(TrackPoint1D position) {
        this.trafficLights.add(new TrafficLight(position, groupState));
    }
    
    public List<TrafficLight> getTrafficLights() {
        return trafficLights;
    }
    
    /**
     * Set the state of all the traffic lights in the group
     * 
     * @param state
     */
    public void setState(TrafficLight.Colour state) {
        groupState = state;
        
        for(TrafficLight trafficLight : trafficLights) {
            trafficLight.setState(state);
        }
    }
    
    public TrafficLight.Colour getState() {
        return groupState;
    }

}
