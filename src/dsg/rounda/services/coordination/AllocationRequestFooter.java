/**
 * 
 */
package dsg.rounda.services.coordination;

import dsg.rounda.services.comm.Footer;
import dsg.rounda.services.coordination.AllocationRequestFooter.Type;
import dsg.rounda.services.roadmap.TrackRangeSequence;

/**
 * Message footer for requesting an allocation
 */
public class AllocationRequestFooter implements Footer {

    public enum Type {
        REQUEST,
        NOTIFICATION
    };
    
    int vehicleID;
    int allocationID;
    Type type;
    TrackRangeSequence trajectory;
    long startTime;
    long endTime;
    long etaTime;
    
    /**
     * @param vehicleID
     * @param trajectory
     * @param startTime
     * @param endTime
     */
    public AllocationRequestFooter(
            int vehicleID,
            int allocationID,
            TrackRangeSequence trajectory,
            long startTime, 
            long endTime,
            long etaTime) {
        this.vehicleID = vehicleID;
        this.allocationID = allocationID;
        this.type = Type.REQUEST;
        this.trajectory = trajectory;
        this.startTime = startTime;
        this.endTime = endTime;
        this.etaTime = etaTime;
    }
    
    /**
     * 
     */
    public AllocationRequestFooter() {
    }

    /**
     * @return the vehicleID
     */
    public int getVehicleID() {
        return vehicleID;
    }
    /**
     * @param vehicleID the vehicleID to set
     */
    public void setVehicleID(int vehicleID) {
        this.vehicleID = vehicleID;
    }
    /**
     * @return the trajectory
     */
    public TrackRangeSequence getTrajectory() {
        return trajectory;
    }
    /**
     * @param trajectory the trajectory to set
     */
    public void setTrajectory(TrackRangeSequence trajectory) {
        this.trajectory = trajectory;
    }
    /**
     * @return the startTime
     */
    public long getStartTime() {
        return startTime;
    }
    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    /**
     * @return the endTime
     */
    public long getEndTime() {
        return endTime;
    }
    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    /**
     * @return the allocationID
     */
    public int getAllocationID() {
        return allocationID;
    }
    /**
     * @param allocationID the allocationID to set
     */
    public void setAllocationID(int allocationID) {
        this.allocationID = allocationID;
    }

    public void setETA(long etaTime) {
        this.etaTime = etaTime;
    }
    
    public long getETA() {
        return etaTime;
    }

    public void setType(Type type) {
        this.type = type;
    }
    
    public Type getType() {
        return type;
    }

}
