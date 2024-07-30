/**
 * 
 */
package dsg.rounda.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * @author slotm
 *
 */
public class Scheduler {

    static final int INITIAL_CAPACITY = 4096;

    final Clock clock;
    final Queue<Job> events;
    
    final Set<Integer> blackList;

    /**
     * 
     */
    public Scheduler(Clock clock) {
        this.clock = clock;
        this.events = new PriorityQueue<Job>(4096);
        this.blackList = new HashSet<Integer>();
    }

    public void reset() {
        this.events.clear();
    }

    public void schedule(Job event) {
        if(blackList.contains(event.getOwner())) {
            // Quietly ignore this job
            return;
        }
        
        events.add(event);
    }

    public Long getNextEventTime() {
        return events.isEmpty() ? null : Math.max(events.peek().getStartTime(), clock.getTime());
    }

    public void runEvent() {
        Job event = events.remove();
        
        event.getTask().run();

        int numRemaining = event.getNumRepeats();

        if(numRemaining != 0) {
            Long repeatInterval = event.getRepeatInterval();

            if(numRemaining > 0) {
                numRemaining -= 1;
            }

            schedule(new Job(
                    event.getTask(),
                    event.getOwner(),
                    event.getStartTime() + repeatInterval,
                    repeatInterval,
                    numRemaining
            ));
        }
    }
    
    /**
     * Remove any pending events with the given owner
     * 
     * @param owner the owner to remove events for
     */
    public void removeEventsWithOwner(int owner) {
        Iterator<Job> it = events.iterator();
        List<Job> toBeRemoved = new ArrayList<Job>();
        
        while(it.hasNext()) {
            Job event = it.next();
            
            if(event.getOwner() == owner) {
                toBeRemoved.add(event);
            }
        }
        
        events.removeAll(toBeRemoved);
    }

    /**
     * Add an owner id to the blacklist, preventing
     * any events from this owner from being scheduled.
     * However, it does not remove already scheduled events.
     * 
     * @param id owner identifier
     */
    public void blackListOwner(int id) {
        blackList.add(id);
    }

}
