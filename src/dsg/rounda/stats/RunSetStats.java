/**
 * 
 */
package dsg.rounda.stats;

import java.util.HashMap;
import java.util.Map;

import dsg.rounda.logging.Event;
import dsg.rounda.logging.EventFilter;
import dsg.rounda.logging.EventHandler;
import dsg.rounda.logging.EventLog;

/**
 * Keeps track of vehicle statistics
 */
public class RunSetStats extends AbstractStats {

	final String name;
	final EventLog globalLog;

	public RunSetStats(String name, EventLog log) {
		this.name = name;
		this.globalLog = log;
		this.globalLog.addHandler(
                EventLog.acceptTag("done"), 
                onDone()
        );
	}

	public void init() {
		addStatistic("sim-stat-density-average", "average", new AverageStat());
		addStatistic("sim-stat-velocity-km-average", "average", new AverageStat());
		addStatistic("sim-stat-vehicle-destroy-rate", "average", new AverageStat());
	}

	public void addEventLog(EventLog eventLog) {
		eventLog.addHandler(
				new EventFilter() {
					public boolean accept(Event event) {
						return event.getTag().startsWith("sim-stat-");
					}
				},
				onEvent());		
	}

	final EventHandler onDone() {
		return new EventHandler() {
			@Override
			public void event(Event event) {
				synchronized(globalLog) {
					globalLog.log(RunSetStats.this, "multi-run-stats", getStatistics());
				}
			}  
		};
	}

	final EventHandler onEvent() {
		return new EventHandler() {
			@Override
			public void event(Event event) {
				synchronized(RunSetStats.this) {
					consume(event);
				}
			}
		};
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
		return name;
	}

}
