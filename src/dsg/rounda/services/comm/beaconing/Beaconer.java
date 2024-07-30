/**
 * 
 */
package dsg.rounda.services.comm.beaconing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dsg.rounda.model.Clock;
import dsg.rounda.model.Job;
import dsg.rounda.model.Message;
import dsg.rounda.model.NetworkAdapter;
import dsg.rounda.model.ReceiveHandler;
import dsg.rounda.model.Scheduler;
import dsg.rounda.model.VehicleCapabilities;
import dsg.rounda.services.AbstractServiceFactory;
import dsg.rounda.services.ServiceFactory;
import dsg.rounda.services.comm.neighbourhood.NeighbourhoodWatch;

/**
 * Periodically broadcasts a message over the network
 */
public class Beaconer {

    static final ServiceFactory FACTORY = new AbstractServiceFactory(type()) {
        @Override
        public Object create(VehicleCapabilities capabilities) {
            return new Beaconer(capabilities);
        }
    };

    public static Class<Beaconer> type() {
        return Beaconer.class;
    }
    
    final int identity;
    final Clock clock;
    final NetworkAdapter network;
    final Scheduler scheduler;
    final Random random;
    final List<BeaconSender> beaconSenders;
    final List<BeaconReceiver> beaconReceivers;
    
    boolean active;
    long interval;
    
    /**
     * @param capabilities
     * @param interval interval in nanoseconds
     */
    public Beaconer(VehicleCapabilities capabilities) {
        super();
        this.identity = capabilities.getId();
        this.clock = capabilities.getClock();
        this.network = capabilities.getNetwork();
        this.scheduler = capabilities.getScheduler();
        this.random = capabilities.getRandom();
        this.active = false;
        this.beaconSenders = new ArrayList<BeaconSender>();
        this.beaconReceivers = new ArrayList<BeaconReceiver>();
    }

    public void start(double interval) {
        start((long) interval);
    }
    
    public void start(long iv) {
        if(active) {
            return;
        }
        interval = iv;
        active = true;
        network.addReceiveHandler(receiver);
   
        long startTime = roundUp(clock.getTime(), interval);

        scheduler.schedule(new Job(
                scheduleBeacon, 
                identity, 
                startTime, 
                interval));
    }

    long roundUp(long n, long roundTo) {
        // fails on negative?  What does that mean?
        if (roundTo == 0) return 0;
        return ((n + roundTo - 1) / roundTo) * roundTo; // edit - fixed error
    }
        
    public void addSender(BeaconSender sender) {
        beaconSenders.add(sender);
    }
    
    public void removeSender(BeaconSender sender) {
        beaconSenders.remove(sender);
    }
    
    public void addReceiver(BeaconReceiver sender) {
        beaconReceivers.add(sender);
    }
    
    public void removeReceiver(BeaconReceiver sender) {
        beaconReceivers.remove(sender);
    }

    final Runnable scheduleBeacon = new Runnable() {
        @Override
        public void run() {
            if(!active) {
                return;
            }

            scheduler.schedule(new Job(sender, identity, clock.getTime() + random.nextDouble() * interval));
        }
    };
    
    final Runnable sender = new Runnable() {

        @Override
        public void run() {
            if(!active) {
                return;
            }

            Message beacon = new Message(identity, Message.ANY_DESTINATION);
            beacon.setBeacon(true);
            
            beacon.addFooter(new BeaconFooter(interval));
            
            for(BeaconSender beaconSender : beaconSenders) {
                beaconSender.prepareBeacon(beacon);
            }
            
            network.send(beacon);
        }
        
    };

    final ReceiveHandler receiver = new ReceiveHandler() {
        @Override
        public void receiveMessage(Message beacon) {
            if(!beacon.hasFooter(BeaconFooter.class)) {
                return;
            }
            
            for(BeaconReceiver beaconReceiver : beaconReceivers) {
                beaconReceiver.receiveBeacon(beacon);
            }
        }
    };
    
    public void stop() {
        if(!active) {
            return;
        }
        active = false;
        network.removeReceiveHandler(receiver);
    }
    
}
