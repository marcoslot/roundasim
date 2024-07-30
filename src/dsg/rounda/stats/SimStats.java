/**
 * 
 */
package dsg.rounda.stats;

import java.util.HashMap;
import java.util.Map;

import dsg.rounda.SimController;
import dsg.rounda.config.Parameter;
import dsg.rounda.config.RunConfig;
import dsg.rounda.config.SimulationParameters;
import dsg.rounda.logging.Event;
import dsg.rounda.logging.EventHandler;
import dsg.rounda.logging.EventLog;
import dsg.rounda.scenarios.Scenario;

/**
 * Keeps track of vehicle statistics
 */
public class SimStats extends AbstractStats implements SimulationParameters {

	final EventLog eventLog;
    final VehicleStatsFactory statsFactory;
    final Map<Integer,VehicleStats> vehicleStats;
    final long gracePeriod;
    
    public SimStats(RunConfig config, EventLog eventLog) {
        this(config, eventLog, new VehicleStatsFactory.Default());
    }
    
    public SimStats(RunConfig config, EventLog eventLog, VehicleStatsFactory statsFactory) {
    	this.gracePeriod = config.get(STATS_GRACE_PERIOD)*1000000000L;
        this.eventLog = eventLog;
        this.statsFactory = statsFactory;
        this.vehicleStats = new HashMap<Integer,VehicleStats>();
    }
    
    public void init() {
        eventLog.addHandler(
                onEvent
        );
        eventLog.addHandler(
                EventLog.acceptSourceType(Integer.class), 
                onVehicleEvent
        );
        eventLog.addHandler(
                EventLog.and(EventLog.acceptSourceType(SimController.class), EventLog.acceptTag("reset")), 
                onReset
        );
        eventLog.addHandler(
                EventLog.and(EventLog.acceptSourceType(SimController.class), EventLog.acceptTag("done")), 
                onDone
        );

        Statistic successCount;
        Statistic startCount;

        addStatistic("density", "average", new AverageStat());
        addStatistic("scenario-length", "latest", new LatestValueStat());
        addStatistic("vehicle-count", "average", new TimeAverageStat());
        addStatistic("velocity", "average", new AverageStat());
        addStatistic("velocity-km", "average", new AverageStat());
        addStatistic("vehicle-create", "count", new BeanCounter());
        addStatistic("vehicle-destroy", "count", new BeanCounter());
        addStatistic("vehicle-destroy", "rate", new RateStat());
        addStatistic("vehicle-age", "average", new AverageStat());
        addStatistic("vehicle-age", "dist", new BoxPlotStat());
        addStatistic("query-success", "count", successCount = new BeanCounter());
        addStatistic("query-start", "count", startCount = new BeanCounter());
        addStatistic("query-fail", "count", new BeanCounter());
        addStatistic("query-success", "ratio", new Ratio(successCount, startCount));
        addStatistic("coverage-fail", "dist", new BoxPlotStat());
        addStatistic("vertigo-time", "dist", new BoxPlotStat());
        addStatistic("vertigo-time", "average", new AverageStat());
        addStatistic("bytes-sent", "sum", new SumStat());
        addStatistic("bytes-received", "sum", new SumStat());
        addStatistic("application-bytes-sent", "sum", new SumStat());
    }

    final EventHandler onDone = new EventHandler() {
        @Override
        public void event(Event event) {
            Map<String,Statistic> stats = getStatistics(); 
            
            for(Map.Entry<String,Statistic> entry : stats.entrySet()) {
                eventLog.log(SimStats.this, "sim-stats", entry.getKey() + " " + entry.getValue().stringValue());
                eventLog.log(SimStats.this, "sim-stat-" + entry.getKey(), entry.getValue().doubleValue());
            }
        }  
    };
    
    final EventHandler onReset = new EventHandler() {
        @Override
        public void event(Event event) {
            reset();
        }  
    };
    
    public void reset() {
        this.vehicleStats.clear();
    }
    
    final EventHandler onEvent = new EventHandler() {
        @Override
        public void event(Event event) {
        	if(event.getSimTime() < gracePeriod && !(event.getSource() instanceof Scenario)) {
        		return;
        	}
            consume(event);
        }
    };
     
    final EventHandler onVehicleEvent = new EventHandler() {
        @Override
        public void event(Event event) {
            Integer sourceID = (Integer) event.getSource();
            VehicleStats vehicleStats = getOrCreateVehicleStats(sourceID);
            vehicleStats.consume(event);
        }
    };

    public VehicleStats getOrCreateVehicleStats(int sourceID) {
        VehicleStats stats = vehicleStats.get(sourceID);
        
        if(stats == null) {
            stats = statsFactory.createVehicleStats(sourceID, eventLog);
            vehicleStats.put(sourceID, stats);
        }
        
        return stats;
    }
    
    public Map<String,Statistic> getStatistics() {
    	Map<String,Statistic> result = new HashMap<String,Statistic>();

        for(Map.Entry<String,Map<String,Statistic>> tagStats : tagsStats.entrySet()) {
            for(Map.Entry<String,Statistic> stat : tagStats.getValue().entrySet()) {
                result.put(tagStats.getKey() + "-" + stat.getKey(), stat.getValue());
            }
        }
        
    	return result;
    }
    
    public String toString() {
        return "stats";
    }

}
