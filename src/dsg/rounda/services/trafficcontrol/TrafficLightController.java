/**
 * 
 */
package dsg.rounda.services.trafficcontrol;

import java.util.HashMap;
import java.util.Map;

import dsg.rounda.model.Clock;
import dsg.rounda.model.Job;
import dsg.rounda.model.Scheduler;

/**
 * Component that controls a traffic light group
 */
public abstract class TrafficLightController {

    final Clock clock;
    final Scheduler scheduler;
    final Map<String,TrafficLightGroup> groups;
    
    boolean active;
    
    /**
     * 
     */
    public TrafficLightController(Clock clock, Scheduler scheduler) {
        this.clock = clock;
        this.scheduler = scheduler;
        this.groups = new HashMap<String,TrafficLightGroup>();
        this.active = false;
    }

    public void addGroup(String key, TrafficLightGroup group) { 
        this.groups.put(key, group);
    }

    public TrafficLightGroup getGroup(String key) {
        return this.groups.get(key);
    }
    
    public void start() {
        active = true;
        runController.run();
    }
    
    final Runnable runController = new Runnable() {
        public void run() {
            long nextEventTime = nextEvent();
            scheduler.schedule(new Job(runController, Job.GLOBAL_OWNER, nextEventTime));
        }
    };
    
    abstract public long nextEvent();

    public void stop() {
        active = false;
    }
}
