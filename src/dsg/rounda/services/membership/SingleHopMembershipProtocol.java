/**
 * 
 */
package dsg.rounda.services.membership;

import java.util.Collections;
import java.util.Set;

import dsg.rounda.Constants;
import dsg.rounda.model.Clock;
import dsg.rounda.model.Job;
import dsg.rounda.model.Message;
import dsg.rounda.model.Scheduler;
import dsg.rounda.model.VehicleCapabilities;
import dsg.rounda.services.AbstractServiceFactory;
import dsg.rounda.services.ServiceFactory;
import dsg.rounda.services.comm.beaconing.BeaconReceiver;
import dsg.rounda.services.comm.beaconing.BeaconSender;
import dsg.rounda.services.comm.beaconing.Beaconer;
import dsg.rounda.services.roadmap.TrackMapArea1D;
import dsg.rounda.services.sensing.distance.EmptyAreaRadar;

/**
 * Single-hop membership implementation
 */
public class SingleHopMembershipProtocol implements Constants {

    static final ServiceFactory FACTORY = new AbstractServiceFactory(SingleHopMembershipProtocol.class) {
        @Override
        public Object create(VehicleCapabilities capabilities) {
            return new SingleHopMembershipProtocol(capabilities);
        }
    };

    public static Class<SingleHopMembershipProtocol> type() {
        return SingleHopMembershipProtocol.class;
    }

    private static final long MAX_AGE = SECONDS;
    private static final long SENSING_INTERVAL = (long) (0.2 * SECONDS);

    final int vehicleID;
    final Set<Integer> meSet;
    final Clock clock;
    final Scheduler scheduler;
    final EmptyAreaRadar ear;
    final MembershipView view;
    final Beaconer beaconer;
    
    // sensor cache
    TrackMapArea1D emptiness1D;
    long sensingTime;
    
    /**
     * 
     */
    public SingleHopMembershipProtocol(
            VehicleCapabilities capabilities) {
        this.vehicleID = capabilities.getId();
        this.clock = capabilities.getClock();
        this.scheduler = capabilities.getScheduler();
        this.ear = capabilities.getService(EmptyAreaRadar.type());
        this.beaconer = capabilities.getService(Beaconer.type());
        this.beaconer.addSender(beaconSender);
        this.beaconer.addReceiver(beaconReceiver);
        this.view = new MembershipView();
        this.meSet = Collections.singleton(vehicleID);

        long startTime = roundUp(clock.getTime(), SENSING_INTERVAL)+1;

        scheduler.schedule(new Job(
                sense, 
                vehicleID, 
                startTime, 
                SENSING_INTERVAL));
    }
    
    final Runnable sense = new Runnable() {
        @Override
        public void run() {
            try {
                emptiness1D = ear.measureEmptiness1D();
                sensingTime = ear.getLatestResultTime();
            } catch (IllegalStateException e) {
                // inconsistent boundaries :(
                System.err.println(vehicleID + ": " + e.getMessage());
            }
        }
    };

    long roundUp(long n, long roundTo) {
        // fails on negative?  What does that mean?
        if (roundTo == 0) return 0;
        return ((n + roundTo - 1) / roundTo) * roundTo; // edit - fixed error
    }
        
    final BeaconSender beaconSender = new BeaconSender() {
        @Override
        public void prepareBeacon(Message beacon) {
            if(emptiness1D == null) {
                return;
            }

            MembershipTuple tuple = new MembershipTuple(meSet, emptiness1D, sensingTime);
            view.add(tuple);

            MembershipFooter footer = new MembershipFooter(tuple);
            beacon.addFooter(footer);
        }
    };
    
    final BeaconReceiver beaconReceiver = new BeaconReceiver() {
        @Override
        public void receiveBeacon(Message beacon) {
            MembershipFooter footer = beacon.getFooter(MembershipFooter.class);
            
            if(footer == null) {
                return;
            }
            
            view.add(footer.getTuple());
        }
    };
    
    /**
     * Get the current membership view gathered from beacons
     *  
     * @return the view
     */
    public MembershipView getView() {
        try {
            // Add the latest available information to the view
            MembershipTuple tuple = new MembershipTuple(meSet, ear.measureEmptiness1D(), clock.getTime());
            view.add(tuple);
        } catch (IllegalStateException  e) {
            e.printStackTrace();
        }
        view.clean(clock.getTime() - MAX_AGE);
        return view;
    }

}
