/**
 * 
 */
package dsg.rounda.services.vertigo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dsg.rounda.Constants;
import dsg.rounda.logging.VehicleEventLog;
import dsg.rounda.model.Clock;
import dsg.rounda.model.Job;
import dsg.rounda.model.LocalizationSensors;
import dsg.rounda.model.Message;
import dsg.rounda.model.NetworkAdapter;
import dsg.rounda.model.ReceiveHandler;
import dsg.rounda.model.Scheduler;
import dsg.rounda.model.VehicleCapabilities;
import dsg.rounda.services.AbstractServiceFactory;
import dsg.rounda.services.ServiceFactory;
import dsg.rounda.services.comm.beaconing.Beaconer;
import dsg.rounda.services.membership.MembershipTuple;
import dsg.rounda.services.membership.MembershipView;
import dsg.rounda.services.membership.SingleHopMembershipProtocol;
import dsg.rounda.services.roadmap.DecayFlags;
import dsg.rounda.services.roadmap.TrackMapArea1D;

/**
 * Single-hop implementation of Vertigo
 */
public class SingleHopVertigo implements Constants, Vertigo {

    static final ServiceFactory FACTORY = new AbstractServiceFactory(type()) {
        @Override
        public Object create(VehicleCapabilities capabilities) {
            return new SingleHopVertigo(capabilities);
        }
    };

    public static Class<SingleHopVertigo> type() {
        return SingleHopVertigo.class;
    }
    
    private static final double MAX_RESPONSE_DELAY = 0.2 * SECONDS;
    
    final int vehicleID;
    
    final Beaconer beaconer;
    //final SingleHopAckProtocol ackProtocol;
    final SingleHopMembershipProtocol membershipProtocol;
    
    final Clock clock;
    final Scheduler scheduler;
    final Random random;
    final NetworkAdapter network;
    final VertigoSessionManager sessManager;
    final List<VertigoReceiveHandler> receivers;
    final LocalizationSensors localization;
    final VehicleEventLog eventLog;
    
    int numSessionsStarted;

    
    /**
     * Create a single hop Vertigo instance
     */
    public SingleHopVertigo(
            VehicleCapabilities capabilities) {
        this.clock = capabilities.getClock();
        this.scheduler = capabilities.getScheduler();
        this.vehicleID = capabilities.getId();
        this.random = capabilities.getRandom();
        this.eventLog = capabilities.getEventLog();
        this.localization = capabilities.getLocalizationSensors();
        this.sessManager = capabilities.getService(VertigoSessionManager.type());
        this.beaconer = capabilities.getService(Beaconer.type());
        this.membershipProtocol = capabilities.getService(SingleHopMembershipProtocol.type());
        //this.ackProtocol = capabilities.getService(SingleHopAckProtocol.type());
        this.network = capabilities.getNetwork();
        this.network.addReceiveHandler(receive);
        this.receivers = new ArrayList<VertigoReceiveHandler>();
        this.numSessionsStarted = 0;
    }
    
    /**
     * Add a receive handler that is called whenever a new query is received
     * 
     * @param receiver receive handler
     */
    @Override
    public void addReceiver(VertigoReceiveHandler receiver) {
        receivers.add(receiver);
    }

    @Override
    public VertigoSession startSession(
            final Message msg,
            final long receiveDeadline,
            final long resultDeadline,
            final long targetTime,
            final TrackMapArea1D targetArea,
            final int numResponseBytes,
            final ResultHandler resultHandler) {
        numSessionsStarted += 1;
        
        // Expand delivery area to include all vehicles that might be in the 
        // target area at the target time.
        final TrackMapArea1D deliveryArea = new TrackMapArea1D(targetArea);
        double timeUntilTargetSeconds = targetTime - clock.getTime();
        deliveryArea.decay(timeUntilTargetSeconds / Constants.SECONDS, DecayFlags.TIME | DecayFlags.CONSIDER_DIRECTION | DecayFlags.GROW);

        eventLog.log("query-start", deliveryArea);
        eventLog.log("query-start-time", clock.getTime());
        
        final long sessionID = ((long) vehicleID << 32) | numSessionsStarted;
        final VertigoSession session = new VertigoSession(
            vehicleID,
            sessionID,
            receiveDeadline,
            resultDeadline,
            targetTime,
            deliveryArea,
            numResponseBytes,
            msg
        );
        
        if(resultHandler != null) {
            session.setDoneCallback(new Runnable() {
                public void run() {
                    // Use single-hop membership view
                    MembershipView membershipView = membershipProtocol.getView();
                    // Construct the set {self} |_| confirmedReceivers
                    Set<Integer> desiredMembers = new HashSet<Integer>(session.getConfirmedReceivers());
                    desiredMembers.add(vehicleID);
                    // Only include tuples from confirmed receivers
                    MembershipTuple finalTuple = membershipView.collapse(desiredMembers);
                    // Decay the tuple to the target time
                    finalTuple.decay(targetTime - finalTuple.getTime());
                    // Determine success by checking whether the target area is contained by the membership area
                    boolean success = finalTuple.getArea().contains(targetArea);
                    // Report success back to the application
                    resultHandler.result(success, session.getResponses(), finalTuple);

                    
                    eventLog.log(success ? "query-success" : "query-fail", deliveryArea);
                    eventLog.log(success ? "membership-success" : "membership-fail", finalTuple.getArea());
                    
                    if(!success) {
                        double deliverySurface = deliveryArea.getTotalLength();
                        TrackMapArea1D coveredArea = deliveryArea.computeIntersection(finalTuple.getArea());
                        double coveredSurface = coveredArea.getTotalLength();
                        eventLog.log("coverage-fail", coveredSurface / deliverySurface);
                    }
                }
            });
        }
        
        sessManager.registerSession(session);
        
        msg.addFooter(session.toQueryFooter());
        network.send(msg);
        
        return session;
    }
    
    private final ReceiveHandler receive = new ReceiveHandler() {
        @Override
        public void receiveMessage(Message msg) {
            if(msg.hasFooter(VertigoQueryFooter.class)) {
                receiveQuery(msg);
            } 
            if(msg.hasFooter(VertigoResponseFooter.class)) {
                receiveResponse(msg);
            } 
        }
    };

    void receiveQuery(Message msg) {
        VertigoQueryFooter query = msg.getFooter(VertigoQueryFooter.class);
        
        if(!query.getDeliveryArea().intersects(localization.getPositionRange())) {
            // Outside the target area
            return;
        }
        
        VertigoSession session = sessManager.getOrCreateSession(query, msg);
        
        if(session == null) {
            // received a query for a session that has expired, ignore
            return;
        }
        
        if(!session.isDelivered() && clock.getTime() < session.getReceiveDeadline()) {
            // I know I got the message
            session.confirmReceiver(vehicleID);
            // I know whoever sent this got the message
            session.confirmReceiver(query.getVehicleID());
            
            // Deliver to application if time is before receive deadline
            for(VertigoReceiveHandler receiver : receivers) {
                MicroResponse response = receiver.receive(msg);
                
                if(response != null) {
                    session.addResponse(vehicleID, response);
                    respond(session);
                }
            }
        }
        
        session.setDelivered(true);
    }

    void receiveResponse(Message msg) {
        VertigoResponseFooter response = msg.getFooter(VertigoResponseFooter.class);
        VertigoSession session = sessManager.getSession(response.getSessionID());
        
        if(session == null) {
            // Not a session we started.
            // Have not received a query for this session.
            return;
        }
        
        if(session.getSourceID() != vehicleID) {
            // Don't care about responses for other
            // people's sessions
            return;
        }
        
        session.addResponses(response.getResponses());
    }

    void respond(VertigoSession session) {
        final Message msg = new Message(vehicleID, session.getSourceID());
        
        final VertigoResponseFooter response = new VertigoResponseFooter(session.getID());
        response.addResponses(session.getResponses());
        msg.addFooter(response);
        
        double timeRemaining = Math.min(MAX_RESPONSE_DELAY, session.getResultDeadline() - clock.getTime());
        long sendTime = clock.getTime() + (long) (random.nextDouble() * timeRemaining *  0.5);
        
        scheduler.schedule(new Job(
                new Runnable() {
                    public void run() {
                        network.send(msg);
                    }
                },
                vehicleID,
                sendTime
        ));
        
    }


}
