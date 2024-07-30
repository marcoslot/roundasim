/**
 * 
 */
package dsg.rounda.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dsg.rounda.services.trafficcontrol.TrafficLight;

/**
 * Map of road-side infrastructure
 */
public class InfrastructureMap {
    
    final List<TrafficLight> trafficLightList;

    /**
     * 
     */
    public InfrastructureMap() {
        this.trafficLightList = new ArrayList<TrafficLight>();
    }
    
    public void addTrafficLight(TrafficLight trafficLight) {
        this.trafficLightList.add(trafficLight);
    }
    
    public List<TrafficLight> getTrafficLights() {
        return trafficLightList;
    }

    public void addTrafficLights(Collection<TrafficLight> trafficLights) {
        this.trafficLightList.addAll(trafficLights);
    }

}
