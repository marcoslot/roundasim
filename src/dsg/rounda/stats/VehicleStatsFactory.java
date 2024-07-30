/**
 * 
 */
package dsg.rounda.stats;

import dsg.rounda.logging.EventLog;

/**
 * An implementation of this interface create statistics objects
 */
public interface VehicleStatsFactory {

    /**
     * Create a vehicle statistics object for the given vehicle ID
     * @param vehicleID the vehicle ID
     * @return the new vehicle statistics object
     */
    VehicleStats createVehicleStats(int vehicleID, EventLog eventLog);
    
    public class Default implements VehicleStatsFactory {

        @Override
        public VehicleStats createVehicleStats(int vehicleID, EventLog eventLog) {
            VehicleStats stats = new VehicleStats(vehicleID, eventLog);
            Timer lifeTimer = new Timer();
            stats.addStatistic("vehicle-create", "timer", lifeTimer);
            stats.addStatistic("vehicle-destroy", "timer", lifeTimer);
            stats.addStatistic("vehicle-age", "latest", new LatestValueStat());
            stats.addStatistic("vertigo-time", "value", new LatestValueStat());
            stats.addStatistic("allocated-distance", "latest", new LatestValueStat());
            stats.addStatistic("measured-distance", "latest", new LatestValueStat());
            stats.addStatistic("available-distance", "latest", new LatestValueStat());
            stats.addStatistic("track", "latest", new LatestValueStat());
            stats.addStatistic("offset", "latest", new LatestValueStat());
            stats.addStatistic("velocity", "latest", new LatestValueStat());
            stats.addStatistic("acceleration", "latest", new LatestValueStat());
            stats.addStatistic("send", "count", new BeanCounter());
            stats.addStatistic("receive", "count", new BeanCounter());
            stats.addStatistic("query-success", "count", new BeanCounter());
            stats.addStatistic("query-fail", "count", new BeanCounter());
            stats.addStatistic("query-start", "count", new BeanCounter());
            stats.addStatistic("query-start-time", "first", new FirstValueStat());
            stats.addStatistic("commit-time", "first", new FirstValueStat());
            stats.addStatistic("coverage-fail", "average", new AverageStat());
            stats.addStatistic("velocity", "average", new TimeAverageStat());
            stats.addStatistic("distance-driven", "sum", new SumStat());
            return stats;
        }
        
    }
    
}
