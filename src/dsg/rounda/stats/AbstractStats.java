/**
 * 
 */
package dsg.rounda.stats;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import dsg.rounda.logging.Event;

/**
 * @author slotm
 *
 */
public abstract class AbstractStats {

    final Map<String,Map<String,Statistic>> tagsStats;
    
    /**
     * 
     */
    public AbstractStats() {
        this.tagsStats = new HashMap<String,Map<String,Statistic>>();
    }

    public void consume(Event event) {
        if(event.getTag() == null) {
            return;
        }
        
        Map<String,Statistic> stats = tagsStats.get(event.getTag());
        
        if(stats == null) {
            stats = new HashMap<String,Statistic>();
            tagsStats.put(event.getTag(), stats);
        }
        
        for(Statistic stat : stats.values()) {
            stat.event(event);
        }
    }

    public void addStatistic(
            String tag,
            String statName, 
            Statistic stat) {
        Map<String,Statistic> stats = tagsStats.get(tag);
        
        if(stats == null) {
            stats = new HashMap<String,Statistic>();
            tagsStats.put(tag, stats);
        }
        
        stats.put(statName, stat);
    }

    public Integer getIntValue(String tag, String statName) {
        Statistic stat = getStatistic(tag, statName);
        
        if(stat == null) {
            return null;
        }
        
        Long longValue = stat.longValue();
        
        if(longValue == null) {
            return null;
        }
        
        return longValue.intValue();
    }

    public Long getLongValue(String tag, String statName) {
        Statistic stat = getStatistic(tag, statName);
        
        if(stat == null) {
            return null;
        }
        
        return stat.longValue();
    }

    public Double getDoubleValue(String tag, String statName) {
        Statistic stat = getStatistic(tag, statName);
        
        if(stat == null) {
            return null;
        }
        
        return stat.doubleValue();
    }
     
    public Statistic getStatistic(String tag, String statName) {
        Map<String,Statistic> stats = tagsStats.get(tag);
        
        if(stats == null) {
            return null;
        }
        
        Statistic stat = stats.get(statName);
        
        if(stat == null) {
            return null;
        }
        
        return stat;
    }


}
