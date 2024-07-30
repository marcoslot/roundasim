/**
 * 
 */
package dsg.rounda.services.comm.neighbourhood;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dsg.rounda.model.Clock;
import dsg.rounda.model.LocalizationSensors;
import dsg.rounda.model.Message;
import dsg.rounda.model.Track;
import dsg.rounda.model.VehicleCapabilities;
import dsg.rounda.services.AbstractServiceFactory;
import dsg.rounda.services.ServiceFactory;
import dsg.rounda.services.comm.VehicleStateFooter;
import dsg.rounda.services.comm.beaconing.BeaconReceiver;
import dsg.rounda.services.comm.beaconing.BeaconSender;
import dsg.rounda.services.comm.beaconing.Beaconer;
import dsg.rounda.services.roadmap.TrackMapArea1D;
import dsg.rounda.services.roadmap.VehicleTrackMap;


/**
 * Keeps track of positions and speeds of neighbours from beacons
 */
public class NeighbourhoodWatch {

    static final ServiceFactory FACTORY = new AbstractServiceFactory(type()) {
        @Override
        public Object create(VehicleCapabilities capabilities) {
            return new NeighbourhoodWatch(capabilities);
        }
    };

    public static Class<NeighbourhoodWatch> type() {
        return NeighbourhoodWatch.class;
    }
    
    final int identity;
    final Clock clock;
    final LocalizationSensors sensors;
    final Beaconer beaconer;
    final VehicleTrackMap roadMap;

    final Map<Integer,NeighbourState> neighbours;
    final Map<Integer,Set<Integer>> roadNeighbourIndex;
    
    /**
     * @param identity
     * @param localizationSensors
     * @param beaconer
     */
    public NeighbourhoodWatch(VehicleCapabilities capabilities) {
        super();
        this.identity = capabilities.getId();
        this.clock = capabilities.getClock();
        this.sensors = capabilities.getLocalizationSensors();
        this.roadMap = capabilities.getRoadMap();
        this.beaconer = capabilities.getService(Beaconer.type());
        this.neighbours = new HashMap<Integer,NeighbourState>();
        this.roadNeighbourIndex = new HashMap<Integer,Set<Integer>>();
    }
    
    public void start() {
        beaconer.addSender(sender);
        beaconer.addReceiver(receiver);
    }
    
    public NeighbourState getNeighbourState(int neighbourID) {
        return neighbours.get(neighbourID);
    }

    final BeaconSender sender = new BeaconSender() {
        @Override
        public void prepareBeacon(Message beacon) {
            VehicleStateFooter stateInfo = new VehicleStateFooter();
            stateInfo.setTime(clock.getTime());
            stateInfo.setPosition1D(sensors.getPosition());
            stateInfo.setVelocity1D(sensors.getVelocity());
            stateInfo.setPosition1DRange(sensors.getPositionRange());
            beacon.addFooter(stateInfo);
        }
    };

    final BeaconReceiver receiver = new BeaconReceiver() {
        @Override
        public void receiveBeacon(Message beacon) {
            int neighbourID = beacon.getSource();
            
            if(neighbourID == identity) {
                // Don't keep track of self
                return;
            }
            
            VehicleStateFooter stateInfo = beacon.getFooter(VehicleStateFooter.class);
            
            if(stateInfo == null) {
                return;
            }
            
            NeighbourState neighbour = neighbours.get(neighbourID);
            
            if(neighbour == null) {
                neighbour = new NeighbourState(neighbourID);
                neighbour.setTime(-1);
                neighbours.put(neighbourID, neighbour);
            }
            
            if(stateInfo.getTime() > neighbour.getTime()) {
                
                // Remove from old road
                if(neighbour.getPosition1D() != null) {
                    Track oldRoad = roadMap.getRoad(neighbour.getPosition1D().getTrackID());
                    
                    if(oldRoad != null) {
                        Set<Integer> oldRoadSet = getOrCreateNeighbourSet(oldRoad.getId());
                        oldRoadSet.remove(neighbourID);
                    }
                }
                // Add to new road 
                Track newRoad = roadMap.getRoad(stateInfo.getPosition1D().getTrackID());
                
                if(newRoad != null) {
                    Set<Integer> newRoadSet = getOrCreateNeighbourSet(newRoad.getId());
                    newRoadSet.add(neighbourID);
                }

                neighbour.setTime(stateInfo.getTime());
                neighbour.setPosition1D(stateInfo.getPosition1D());
                neighbour.setVelocity1D(stateInfo.getVelocity1D());
                neighbour.setPosition1DRange(stateInfo.getPosition1DRange());
            }
        }
    };
    
    Set<Integer> getOrCreateNeighbourSet(int roadID) {
        Set<Integer> neighbours = roadNeighbourIndex.get(roadID);
        
        if(neighbours == null) {
            neighbours = new HashSet<Integer>();
            roadNeighbourIndex.put(roadID, neighbours);
        }
        
        return neighbours;
    }

    public Set<Integer> getNeighboursOnRoad(int roadID) {
        Set<Integer> neighbours = roadNeighbourIndex.get(roadID);
        
        return neighbours != null ? Collections.unmodifiableSet(neighbours) : Collections.<Integer>emptySet();
    }

    public Set<NeighbourState> getNeighboursInArea(TrackMapArea1D area) {
    	Set<NeighbourState> neighbours = new HashSet<NeighbourState>();
    	
    	for(Integer trackID : area.getTrackIDs()) {
    		Set<Integer> neighboursOnTrack = roadNeighbourIndex.get(trackID);
    		
    		if(neighboursOnTrack == null) {
    			continue;
    		}
    		
    		for(Integer neighbourID : neighboursOnTrack) {
    			NeighbourState neighbour = this.getNeighbourState(neighbourID);
    			
    			if(area.contains(neighbour.getBackPosition())
    			|| area.contains(neighbour.getFrontPosition())) {
    				neighbours.add(neighbour);
    			}
    		}
    	}
    	
    	return neighbours;
    }
    
    public void stop() {
        beaconer.removeSender(sender);
        beaconer.removeReceiver(receiver);
    }

    public Collection<NeighbourState> getNeighbours() {
        return neighbours.values();
    }
    
}
