/**
 * 
 */
package dsg.rounda.model;

import java.util.ArrayList;
import java.util.List;

import dsg.rounda.Constants;
import dsg.rounda.Handler;

/**
 * Simulation of a set of ranging sensors.
 */
public class RangingSensors implements Constants {

    final WorldState world;
    final Clock clock;
    final VehicleState vehicle;
    
    final List<Lidar> lidars;
    final RangingSensorsSpecification specification;
    final List<Handler<RangingSnapshot>> lidarHandlers;
    
    RangingSnapshot latestSnapshot;
    
    /**
     * Create a new set of ranging sensors.
     * 
     */
    public RangingSensors(
            WorldState world, 
            Clock globalClock, 
            Scheduler scheduler,
            VehicleState vehicle, 
            RangingSensorsSpecification specs) {
        this.world = world;
        this.clock = globalClock;
        this.vehicle = vehicle;
        this.lidars = new ArrayList<Lidar>();
        this.specification = specs;
        this.lidarHandlers = new ArrayList<Handler<RangingSnapshot>>();
        
        for(LidarSpecification lidarSpec : specs.getLidarSpecs()) {
            lidars.add(new Lidar(world, globalClock, vehicle, lidarSpec));
        }
        
        // Try to synchronize lidar snapshots using GPS time
        long startTime = roundUp(clock.getTime(), specs.getInterval());

        scheduler.schedule(new Job(
                takeSnapshot, 
                vehicle.getId(), 
                startTime,
                specs.getInterval()));
    }
    
    long roundUp(long n, long roundTo) {
        // fails on negative?  What does that mean?
        if (roundTo == 0) return 0;
        return ((n + roundTo - 1) / roundTo) * roundTo; // edit - fixed error
    }
    final Runnable takeSnapshot = new Runnable() {
        public void run() {
            latestSnapshot = new RangingSnapshot(clock.getTime());
            
            for(Lidar lidar : lidars) {
                LidarSnapshot lidarSnapshot = lidar.takeSnapshot();
                latestSnapshot.add(lidarSnapshot);
            }
            
            for(Handler<RangingSnapshot> handler : lidarHandlers) {
                handler.handle(latestSnapshot);
            }
        }
    };

    public void addSnapshotHandler(Handler<RangingSnapshot> handler) {
        lidarHandlers.add(handler);
    }

    public void removeSnapshotHandler(Handler<RangingSnapshot> handler) {
        lidarHandlers.remove(handler);
    }
    

    /**
     * Get the specification of this sensor
     * 
     * @return the specification
     */
    public RangingSensorsSpecification getSpecification() {
        return specification;
    }


    /**
     * Get a combined snapshot of all lidar sensors.
     * 
     * @return the lidar snapshot
     */
    public RangingSnapshot getLatestSnapshot() {
        return latestSnapshot;
    }

    /**
     * Get the sensing interval in seconds
     * 
     * @return the interval in seconds
     */
    public double getInterval() {
        return (double) specification.getInterval() / Constants.SECONDS;
    }

}
