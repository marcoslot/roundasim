/**
 * 
 */
package dsg.rounda.model;

import java.util.ArrayList;
import java.util.List;

import dsg.rounda.Constants;

/**
 * Represents the specifications of ranging sensors
 * mounted on the vehicle. This is generated by the
 * manufacturer and used by the factory to put the 
 * sensors in the right place, and the control software 
 * to interpret the sensor data.
 */
public class RangingSensorsSpecification {

    private static final double DEFAULT_LIDAR_INTERVAL = 0.1 * Constants.SECONDS;
    
    final List<LidarSpecification> lidarSpecs;
    final long interval;

    public RangingSensorsSpecification() {
        this((long) DEFAULT_LIDAR_INTERVAL);
    }
    
    /**
     * Create a new, empty specification.
     * 
     * @param interval ranging sensor interval in nanoseconds
     */
    public RangingSensorsSpecification(long interval) {
        this.lidarSpecs = new ArrayList<LidarSpecification>();
        this.interval = interval;
    }

    /**
     * Add a LIDAR specification to the ranging sensor specification
     * 
     * @param config specifications of the LIDAR sensor
     */
    public void add(LidarSpecification config) {
        lidarSpecs.add(config);
    }

    /**
     * Get the specifications of individual LIDAR sensors
     * 
     * @return the lidar specifications
     */
    public List<LidarSpecification> getLidarSpecs() {
        return lidarSpecs;
    }

    public int getNumBeams() {
        int numBeams = 0;
        
        for(LidarSpecification spec : lidarSpecs) {
            numBeams += spec.getNumSteps();
        }
        
        return numBeams;
    }

    /**
     * @return the interval
     */
    public long getInterval() {
        return interval;
    }
    

}
