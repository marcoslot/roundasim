/**
 * 
 */
package dsg.rounda.services.vertigo;

import java.util.Collection;
import java.util.Random;

import dsg.rounda.model.Clock;
import dsg.rounda.model.Job;
import dsg.rounda.model.Message;
import dsg.rounda.model.NetworkAdapter;
import dsg.rounda.model.Scheduler;
import dsg.rounda.model.VehicleCapabilities;
import dsg.rounda.services.AbstractServiceFactory;
import dsg.rounda.services.ServiceFactory;
import dsg.rounda.services.comm.beaconing.BeaconReceiver;
import dsg.rounda.services.comm.beaconing.BeaconSender;
import dsg.rounda.services.comm.beaconing.Beaconer;
import dsg.rounda.services.membership.SingleHopMembershipProtocol;

/**
 * Implementation of the single-hop ack protocol for Vertigo
 * 
 * Collects and distributes acknowledgements, and schedules retransmissions.
 */
public class SingleHopAckProtocol {

    static final ServiceFactory FACTORY = new AbstractServiceFactory(type()) {
        @Override
        public Object create(VehicleCapabilities capabilities) {
            return new SingleHopAckProtocol(capabilities);
        }
    };

    public static Class<SingleHopAckProtocol> type() {
        return SingleHopAckProtocol.class;
    }
    
    private static final int MAX_RETRANSMISSION_DELAY = 200000000;
    final int vehicleID;
    final Beaconer beaconer;
    final Clock clock;
    final Scheduler scheduler;
    final VertigoSessionManager sessManager;
    final NetworkAdapter network;
    final Random random;
    
    /**
     * 
     */
    public SingleHopAckProtocol(
            VehicleCapabilities capabilities) {
        this.vehicleID = capabilities.getId();
        this.sessManager = capabilities.getService(VertigoSessionManager.type());
        this.network = capabilities.getNetwork();
        this.clock = capabilities.getClock();
        this.scheduler = capabilities.getScheduler();
        this.random = capabilities.getRandom();
        this.beaconer = capabilities.getService(Beaconer.type());
        this.beaconer.addSender(beaconSender);
        this.beaconer.addReceiver(beaconReceiver);
    }

    final BeaconSender beaconSender = new BeaconSender() {
        @Override
        public void prepareBeacon(Message beacon) {
            AckFooter ackFooter = new AckFooter(sessManager.getSessionIDs());
            beacon.addFooter(ackFooter);
        }
    };
    
    final BeaconReceiver beaconReceiver = new BeaconReceiver() {
        @Override
        public void receiveBeacon(Message beacon) {
            AckFooter ackFooter = beacon.getFooter(AckFooter.class);
            
            if(ackFooter == null) {
                return;
            }
            
            Collection<Long> acks = ackFooter.getAcknowledgements();
            
            // Collect acks
            for(long sessionID : acks) {
                VertigoSession session = sessManager.getSession(sessionID);
                
                if(session == null) {
                    // We do not currently register acks
                    // for sessions we have not yet heard about
                    continue;
                }
                
                session.confirmReceiver(beacon.getSource());
            }
            
            // Schedule retransmissions
            for(VertigoSession session : sessManager.getSessions()) {
                if(!acks.contains(session.getID())) {
                    retransmit(session, beacon.getSource());
                }
            }
            
        }
    };

    void retransmit(final VertigoSession session, final int targetID) {
        long delay = (long) (random.nextDouble() * MAX_RETRANSMISSION_DELAY);
        long transmissionTime = clock.getTime() + delay;
        
        if(transmissionTime >= session.getReceiveDeadline()) {
            // Message will have expired by the time we try to transmit it
            return;
        }
        
        scheduler.schedule(new Job(
            new Runnable() {
                public void run() {
                    if(session.isConfirmedReceiver(targetID)
                    || session.isExpectedReceiver(targetID)) {
                        // We know or believe that this vehicle
                        // has received the message since scheduling
                        // the retransmission.
                        return;
                    }                   
                    
                    network.send(session.getMessage());
                }
            },
            vehicleID,
            transmissionTime
        ));
    }
}
