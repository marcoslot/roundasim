/**
 * 
 */
package dsg.rounda.model;

import java.util.ArrayList;
import java.util.List;

import dsg.rounda.logging.VehicleEventLog;

/**
 * Basic implementation of a network adapter without queuing
 */
public class NetworkAdapter implements NetworkDeliveryInterface {

    final NetworkTransmissionInterface transmitter;
    
    final Clock clock;
    final Scheduler scheduler;
    final VehicleEventLog eventLog;
    
    final int id;
    final List<ReceiveHandler> receivers;
    
    /**
     * 
     */
    public NetworkAdapter(
            int id, 
            Network network, 
            Clock clock, 
            Scheduler scheduler,
            VehicleEventLog eventLog) {
        this.id = id;
        this.scheduler = scheduler;
        this.clock = clock;
        this.transmitter = network.addAdapter(this);
        this.receivers = new ArrayList<ReceiveHandler>();
        this.eventLog = eventLog;
    }
    
    public void addReceiveHandler(ReceiveHandler receiver) {
        this.receivers.add(receiver);
    }

    public void removeReceiveHandler(ReceiveHandler receiver) {
        this.receivers.remove(receiver);
    }

    public int getId() {
        return id;
    }

    /**
     * Instant delivery 
     */
    public void deliver(final Message msg) {
        eventLog.log("receive", msg);
        
        for(final ReceiveHandler receiver : receivers) {
            scheduler.schedule(new Job(new Runnable() {
                public void run() {
                    receiver.receiveMessage(msg);
                }
            }, id, clock.getTime()));
        }
    }
    
    public void send(Message msg) {
        eventLog.log("send", msg);
        transmitter.send(msg);
    }

    
}
