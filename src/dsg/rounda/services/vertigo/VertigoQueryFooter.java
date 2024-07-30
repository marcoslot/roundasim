/**
 * 
 */
package dsg.rounda.services.vertigo;

import dsg.rounda.services.comm.Footer;
import dsg.rounda.services.roadmap.*;

/**
 * Footer appended to Vertigo query messages
 */
public class VertigoQueryFooter implements Footer {

    int vehicleID;
    long sessionID;
    long resultDeadline;
    long receiveDeadline;
    long targetTime;
    TrackMapArea1D targetArea;
    int numResponseBytes;
    
    
    /**
     * @param vehicleID
     * @param sessionID
     * @param resultDeadline
     */
    public VertigoQueryFooter(
            int vehicleID, 
            long sessionID, 
            long receiveDeadline, 
            long resultDeadline,
            long targetTime,
            TrackMapArea1D targetArea,
            int numResponseBytes) {
        this.vehicleID = vehicleID;
        this.sessionID = sessionID;
        this.receiveDeadline = receiveDeadline;
        this.resultDeadline = resultDeadline;
        this.targetTime = targetTime;
        this.targetArea = targetArea;
        this.numResponseBytes = numResponseBytes;
    }

    /**
     * @return the resultDeadline
     */
    public long getResultDeadline() {
        return resultDeadline;
    }

    /**
     * @param resultDeadline the resultDeadline to set
     */
    public void setResultDeadline(long resultDeadline) {
        this.resultDeadline = resultDeadline;
    }

    /**
     * @return the sessionID
     */
    public long getSessionID() {
        return sessionID;
    }

    /**
     * @param sessionID the sessionID to set
     */
    public void setSessionID(long sessionID) {
        this.sessionID = sessionID;
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
     * @return the receiveDeadline
     */
    public long getReceiveDeadline() {
        return receiveDeadline;
    }

    /**
     * @param receiveDeadline the receiveDeadline to set
     */
    public void setReceiveDeadline(long receiveDeadline) {
        this.receiveDeadline = receiveDeadline;
    }

    /**
     * @return the targetTime
     */
    public long getTargetTime() {
        return targetTime;
    }

    /**
     * @param targetTime the targetTime to set
     */
    public void setTargetTime(long targetTime) {
        this.targetTime = targetTime;
    }

    /**
     * @return the targetArea
     */
    public TrackMapArea1D getDeliveryArea() {
        return targetArea;
    }

    /**
     * @param targetArea the targetArea to set
     */
    public void setTargetArea(TrackMapArea1D targetArea) {
        this.targetArea = targetArea;
    }

    /**
     * @return the numResponseBytes
     */
    public int getNumResponseBytes() {
        return numResponseBytes;
    }

    /**
     * @param numResponseBytes the numResponseBytes to set
     */
    public void setNumResponseBytes(int numResponseBytes) {
        this.numResponseBytes = numResponseBytes;
    }
    

}
