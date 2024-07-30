/**
 * 
 */
package dsg.rounda.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.vividsolutions.jts.geom.Coordinate;

import dsg.rounda.Constants;
import dsg.rounda.logging.EventLog;
import dsg.rounda.scenarios.TextWorld;
import dsg.rounda.stats.WeightedDoubleValue;

/**
 * Represents the actual state of the world, as opposed
 * to information about the state of the world.
 */
public class WorldState {

    // The knowledge of how vehicles move
    final VehicleRouter router;
    
    // The log doesn't really belong here, but whatever
    final EventLog log;
    
    // All the vehicles in the world
    long vehiclesVersion;
    final Map<Integer,VehicleState> vehicleTable;
    final List<VehicleState> vehicleList;
    final Map<Integer,Set<Integer>> trackToVehicleIndex;

    // The road network of the world
    final TrackNetwork roadNetwork;
    
    // The buildings in the world
    long buildingsVersion;
    final List<Building> buildingList;

    /**
     * 
     */
    public WorldState(EventLog log) {
        this.log = log;
        this.vehicleTable = new HashMap<Integer,VehicleState>();
        this.vehicleList = new CopyOnWriteArrayList<VehicleState>();
        this.trackToVehicleIndex = new HashMap<Integer,Set<Integer>>();
        this.roadNetwork = new TrackNetwork();
        this.buildingList = new CopyOnWriteArrayList<Building>();
        this.router = new VehicleRouter(roadNetwork);
        this.vehiclesVersion = 0;
        this.buildingsVersion = 0;
    }
    
    public void reset() {
        this.vehicleTable.clear();
        this.vehicleList.clear();
        this.roadNetwork.clear();
        this.buildingList.clear();
        this.trackToVehicleIndex.clear();
        this.buildingsVersion = 0;
        this.vehiclesVersion = 0;        
    }

    public synchronized void addBuilding(Building building) {
        buildingList.add(building);
        buildingsVersion++;
    }

    public synchronized void removeBuilding(Building building) {
        buildingList.remove(building);
        buildingsVersion++;
    }

    public synchronized void addVehicle(VehicleState vehicle) {
        if(vehicleTable.containsKey(vehicle.getId())) {
            throw new IllegalStateException("Cannot add multiple vehicles with the same ID");
        }

        vehicleTable.put(vehicle.getId(), vehicle);
        vehicleList.add(vehicle);
        getVehiclesOnTrack(vehicle.getBackPosition().getTrack().getId()).add(vehicle.getId());
        vehiclesVersion++;
    }

    /**
     * Remove a vehicle from the world
     * 
     * @param vehicleID
     */
    public synchronized VehicleState removeVehicle(int vehicleID) {
        VehicleState vehicle = vehicleTable.remove(vehicleID);
        Track currentTrack = vehicle.getBackPosition().getTrack();
        
        if(currentTrack != null) {
            getVehiclesOnTrack(currentTrack.getId()).remove(vehicleID);
        }
        
        vehicleList.remove(vehicle);
        vehiclesVersion++;
        return vehicle;
    }

    public synchronized VehicleState getVehicle(int sourceID) {
        return vehicleTable.get(sourceID);
    }

    public Collection<Building> getBuildings() {
        return this.buildingList;
    }

    public Collection<VehicleState> getVehicles() {
        return this.vehicleList;
    }

    public Collection<VehicleState> getVehiclesInCircle(Coordinate center, double radius) {
        List<VehicleState> vehicleInCircle = new ArrayList<VehicleState>(20);
        double radius2 = radius*radius;
        
        for(VehicleState vehicle : vehicleList) {
            Pose2D pose2D = vehicle.getBackPosition().getPose2D();
            double dx = pose2D.getX() - center.x;
            double dy = pose2D.getY() - center.y;
            
            if(dx*dx + dy*dy < radius2) {
                vehicleInCircle.add(vehicle);
            }
        }
        
        return vehicleInCircle;
    }

    /**
     * Progress the world by timeDiff nanoseconds
     * 
     * @param timeDiff time step in nanoseconds
     */
    public synchronized void progress(long timeDiff) {
        if(timeDiff > 0) {
            double timeDiffSeconds = (double) timeDiff / Constants.SECONDS;
            double halfTimeDiffSeconds2 = 0.5*timeDiffSeconds*timeDiffSeconds;
            
            for(VehicleState vehicle : vehicleList) {
                double initialVelocity = vehicle.getVelocity().getRoadVelocity();
                double acceleration = vehicle.getVelocity().getAcceleration();
                
                double distanceToDrive;
                double newVelocity;
                double maxAccelerationTime; 
                
                // Determine if we hit a velocity boundary before timeDiffSeconds has passed
                if(acceleration < 0) {
                    maxAccelerationTime = initialVelocity / -acceleration;
                } else if(acceleration > 0) {
                    maxAccelerationTime = (vehicle.getMaximumVelocity() - initialVelocity) / acceleration;
                } else {
                    maxAccelerationTime = Double.POSITIVE_INFINITY;;
                }
                
                if(maxAccelerationTime < timeDiffSeconds) {
                    // Figure out how far to drive
                    distanceToDrive = initialVelocity * maxAccelerationTime + acceleration * 0.5*maxAccelerationTime*maxAccelerationTime;
                    
                    // Velocity after reaching threshold
                    newVelocity = initialVelocity + acceleration * maxAccelerationTime;
                    
                    // Drive!
                    boolean isAlive = progressVehicle(
                            vehicle,
                            distanceToDrive,
                            newVelocity);
                    
                    if(!isAlive) {
                        // never mind the second leg, car's gone
                        continue;
                    }

                    // Cancel acceleration
                    vehicle.getVelocity().setAcceleration(0.0);
                    
                    // Prepare for the second leg
                    distanceToDrive = newVelocity * (timeDiffSeconds - maxAccelerationTime);
                } else {
                    // Figure out how far to drive
                    distanceToDrive = initialVelocity * timeDiffSeconds + acceleration * halfTimeDiffSeconds2;
                    
                    // Velocity after timeDiffSeconds
                    newVelocity = initialVelocity + acceleration * timeDiffSeconds;
                }

                // Drive!
                progressVehicle(
                        vehicle,
                        distanceToDrive,
                        newVelocity);

                log.log(vehicle.getId(), "distance-driven", distanceToDrive);
            }
        }
    }
    
    /**
     * @return true if the vehicle is still part of the world after the progress, false otherwise
     */
    private boolean progressVehicle(
            final VehicleState vehicle,
            double distanceToDrive,
            double newVelocity) {
        int initialRoadID = vehicle.getBackPosition().getTrack().getId();

        Position1D newPosition = router.route(
        		vehicle.getBackPosition(), 
        		vehicle.getTrajectory(), 
        		distanceToDrive);
        
        // Update the vehicle state
        vehicle.getVelocity().setRoadVelocity(newVelocity);
        vehicle.setPosition(newPosition);

        if(newPosition.getTrack() != null) {
            // Vehicle is still on the road network
            int newRoadID = newPosition.getTrack().getId();
            
            if(newRoadID != initialRoadID) {
                // Vehicle changed to another track, update track-to-vehicle index
                getVehiclesOnTrack(initialRoadID).remove(vehicle.getId());
                getVehiclesOnTrack(newRoadID).add(vehicle.getId());
            }
            
            return true;
        } else {
            // Last words
            log.log(
                    (Integer) vehicle.getId(), 
                    "bye", 
                    "I'll be back"
            ); 
            
            // Remove vehicle from track-to-vehicle index
            // Need to do this here, because track is already null
            getVehiclesOnTrack(initialRoadID).remove(vehicle.getId());
            
            // This vehicle reached the end of the track. Remove it from the world
            removeVehicle(vehicle.getId());
            
            return false;
        } 
    }

    /**
     * Get the set of vehicle identifiers on a road.
     * Creates and adds the set to the index if it does not exist.
     * 
     * @param roadID the track identifier
     * @return the set of vehicle identifiers
     */
    public Set<Integer> getVehiclesOnTrack(int roadID) {
        Set<Integer> vehiclesOnTrack = trackToVehicleIndex.get(roadID);
        
        if(vehiclesOnTrack == null) {
            vehiclesOnTrack = new HashSet<Integer>();
            trackToVehicleIndex.put(roadID, vehiclesOnTrack);
        }
        
        return vehiclesOnTrack;
    }

    /**
     * @return the vehiclesVersion
     */
    public synchronized long getVehiclesVersion() {
        return vehiclesVersion;
    }

    /**
     * @return the buildingsVersion
     */
    public synchronized long getBuildingsVersion() {
        return buildingsVersion;
    }

    /**
     * @param road
     * @see dsg.rounda.model.TrackNetwork#addRoad(dsg.rounda.model.Road)
     */
    public synchronized void addRoad(Track road) {
        roadNetwork.addRoad(road);
    }

    /**
     * @param roadID
     * @return
     * @see dsg.rounda.model.TrackNetwork#getRoad(int)
     */
    public synchronized Track getRoad(int roadID) {
        return roadNetwork.getRoad(roadID);
    }

    /**
     * @return
     * @see dsg.rounda.model.TrackNetwork#getRoads()
     */
    public Collection<Track> getRoads() {
        return roadNetwork.getRoads();
    }

    /**
     * @return
     * @see dsg.rounda.model.TrackNetwork#getRoadsVersion()
     */
    public synchronized long getRoadsVersion() {
        return roadNetwork.getRoadsVersion();
    }

    public TrackNetwork getRoadNetwork() {
        return roadNetwork;
    }

    public void load(TextWorld worldText) {
        for(Building building : worldText.getBuildings()) {
            addBuilding(building);
        }
        for(Track track : worldText.getTracks()) {
            addRoad(track);
        }
    }

	public int getNumberOfVehicles() {
		return this.vehicleList.size();
	}

}
