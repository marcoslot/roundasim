/**
 * 
 */
package dsg.rounda.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a set of LIDAR snapshots, one for each LIDAR
 * mounted on the vehicle.
 */
public class RangingSnapshot {

    final long time;
    final List<LidarSnapshot> snapshots;
    
    public RangingSnapshot(long time) {
        this.time = time;
        this.snapshots = new ArrayList<LidarSnapshot>();
    }

    /**
     * @param snapshot
     * @return
     */
    public boolean add(LidarSnapshot snapshot) {
        return snapshots.add(snapshot);
    }

    /**
     * @return the snapshots
     */
    public synchronized List<LidarSnapshot> getLidarSnapshots() {
        return snapshots;
    }

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }
    
    

}
