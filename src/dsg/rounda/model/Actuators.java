/**
 * 
 */
package dsg.rounda.model;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

import dsg.rounda.geometry.BezierCurve;
import dsg.rounda.services.roadmap.TrackMap;
import dsg.rounda.services.roadmap.TrackPoint1D;


/**
 * Actuator interface of the vehicle
 */
public class Actuators {

    
    final VehicleState vehicle;
    final TrackNetwork tracks;

    /**
     * @param vehicle
     */
    public Actuators(
    		TrackNetwork tracks,
    		VehicleState vehicle) {
    	this.tracks = tracks;
        this.vehicle = vehicle;
    }

    public IndicatorState getIndicatorState() {
		return vehicle.getIndicatorState();
	}

	public void setIndicatorState(LaneChangeDirection dir) {
    	vehicle.setIndicatorState(dir);
    }

    public void setIndicatorState(IndicatorState dir) {
    	vehicle.setIndicatorState(dir);
    }
    
    /**
     * Set the velocity along the 1-dimensional
     * track the vehicle is currently on.
     * 
     * @param roadVelocity
     */
    public void setRoadVelocity(double roadVelocity) {
        vehicle.getVelocity().setRoadVelocity(roadVelocity);
    }
    
    /**
     * Add a track to follow to the queue.
     *  
     * @param track
     */
    public void addTrackToFollow(Track track) {
        vehicle.getTrajectory().addTrackToFollow(track);
    }
    
    /**
     * Get the planned trajectory
     */
    public Trajectory1D getTrajectory() {
        return vehicle.getTrajectory();
    }
    
    /**
     * Set the acceleration of the vehicle
     * 
     * @param acceleration the acceleration
     */
    public void setAcceleration(double acceleration) {
        vehicle.getVelocity().setAcceleration(acceleration);
    }
    

	public Track doLaneChange(TrackPoint1D laneChangeStartPos, double laneChangeDistance, LaneChangeDirection direction) {
		Track laneChange = tracks.createLaneChange(laneChangeStartPos, laneChangeDistance, direction);
		
		if(laneChange != null) {
		    vehicle.getTrajectory().addTrackToFollow(laneChange);
		}
		return laneChange;
	}
}
