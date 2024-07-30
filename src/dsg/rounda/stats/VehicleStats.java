/**
 * 
 */
package dsg.rounda.stats;

import java.util.HashMap;
import java.util.Map;

import dsg.rounda.logging.Event;
import dsg.rounda.logging.EventLog;


/**
 * Statistics for an individual vehicle
 */
public class VehicleStats extends AbstractStats {

    final int id;
    final EventLog eventLog;

    /**
     * @param eventLog 
     * 
     */
    public VehicleStats(int id, EventLog eventLog) {
        this.id = id;
        this.eventLog = eventLog;
    }
    
    public void consume(Event event) {
        if("bye".equals(event.getTag())) {
            finish();
        } else {
            super.consume(event);
        }
    }

    void finish() {
        Map<String,String> logs = new HashMap<String,String>();

        for(Map.Entry<String,Map<String,Statistic>> tagStats : tagsStats.entrySet()) {
            for(Map.Entry<String,Statistic> stat : tagStats.getValue().entrySet()) {
                logs.put(tagStats.getKey() + "-" + stat.getKey(), stat.getValue().stringValue());
            }
        }

        for(Map.Entry<String,String> entry : logs.entrySet()) {
            eventLog.log(id, "vehicle-stats", entry.getKey() + " " + entry.getValue());
        }
    }

    public String toString() {
        return "vehicle-stats";
    }
}
