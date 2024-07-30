/**
 * 
 */
package dsg.rounda.services.comm;

import dsg.rounda.model.Velocity1D;
import dsg.rounda.services.roadmap.TrackPoint1D;
import dsg.rounda.services.roadmap.TrackRangeSequence;

/**
 * @author slotm
 *
 */
public class VehicleStateFooter implements Footer {

    long time;
    TrackPoint1D position1D;
    Velocity1D velocity1D;
    TrackRangeSequence position1DRange;
    
    /**
     * 
     */
    public VehicleStateFooter() {
    }
    
    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * @return the position1D
     */
    public TrackPoint1D getPosition1D() {
        return position1D;
    }

    /**
     * @param position1d the position1D to set
     */
    public void setPosition1D(TrackPoint1D position1d) {
        position1D = position1d;
    }

    /**
     * @return the velocity1D
     */
    public Velocity1D getVelocity1D() {
        return velocity1D;
    }

    /**
     * @param velocity1d the velocity1D to set
     */
    public void setVelocity1D(Velocity1D velocity1d) {
        velocity1D = velocity1d;
    }

    public TrackRangeSequence getPosition1DRange() {
        return position1DRange;
    }

    public void setPosition1DRange(TrackRangeSequence position1dRange) {
        position1DRange = position1dRange;
    }

    

}
