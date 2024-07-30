/**
 * 
 */
package dsg.rounda.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author slotm
 *
 */
public class PerfectNetwork implements Network, NetworkTransmissionInterface {

    private static final long COMM_DELAY = 0;
    
    final Map<Integer,NetworkDeliveryInterface> adapters;
    
    final Clock clock;
    final Scheduler scheduler;
    final WorldState world;
    
    /**
     * 
     */
    public PerfectNetwork(Clock clock, Scheduler scheduler, WorldState world) {
        this.adapters = new HashMap<Integer,NetworkDeliveryInterface>();
        this.clock = clock;
        this.scheduler = scheduler;
        this.world = world;
    }
    
    public NetworkTransmissionInterface addAdapter(NetworkDeliveryInterface adapter) {
        this.adapters.put(adapter.getId(), adapter);
        return this;
    }
    
    public void removeAdapter(int id) {
        this.adapters.remove(id);
    }
    
    public void send(final Message msg) {
        // Schedule as an event to avoid direction invocations
        // and add delay if desired
        scheduler.schedule(new Job(new Runnable() {
            public void run() {
                if(msg.getDestination() == Message.ANY_DESTINATION) {
                    for(NetworkDeliveryInterface adapter : adapters.values()) {
                        if(adapter.getId() == msg.getSource()) {
                            continue;
                        }
                        adapter.deliver(msg);
                    }
                } else {
                    NetworkDeliveryInterface adapter = adapters.get(msg.getDestination());

                    if(adapter != null) {
                        adapter.deliver(msg);
                    }
                }
            }
        }, msg.getSource(), clock.getTime() + COMM_DELAY));
    }

}
