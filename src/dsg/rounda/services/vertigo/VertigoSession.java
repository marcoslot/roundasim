	/**
 * 
 */
package dsg.rounda.services.vertigo;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dsg.rounda.model.Message;
import dsg.rounda.services.membership.MembershipView;
import dsg.rounda.services.roadmap.TrackMapArea1D;

/**
 * Represents a Vertigo session
 */
public class VertigoSession {

    final int sourceID;
    final long sessionID;
    final Message message;
    final long receiveDeadline;
    final long resultDeadline;
    final int numResponseBytes;

    boolean isDelivered;
    
    final Set<Integer> confirmedReceivers;
    final Set<Integer> expectedReceivers;
    
    MembershipView membershipView;
    Map<Integer,MicroResponse> responses;
    
    long targetTime;
    TrackMapArea1D deliveryArea;
    
    Runnable doneCallback;
    
    /**
     * @param sessionID
     * @param msg
     * @param deliveryArea
     * @param receiveDeadline
     * @param resultDeadline
     * @param targetTime
     */
    public VertigoSession(
            int sourceID,
            long sessionID,
            long receiveDeadline,
            long resultDeadline,
            long targetTime,
            TrackMapArea1D deliveryArea,
            int numResponseBytes,
            Message msg) {
        this.confirmedReceivers = new HashSet<Integer>();
        this.expectedReceivers = new HashSet<Integer>();
        this.responses = new HashMap<Integer,MicroResponse>();
        
        this.isDelivered = false;
        this.sourceID = sourceID;
        this.sessionID = sessionID;
        this.receiveDeadline = receiveDeadline;
        this.resultDeadline = resultDeadline;
        this.targetTime = targetTime;
        this.deliveryArea = deliveryArea;
        this.numResponseBytes = numResponseBytes;
        this.message = msg;
    }
    
    public VertigoSession(VertigoQueryFooter query, Message msg) {
        this(
                query.getVehicleID(),
                query.getSessionID(),
                query.getReceiveDeadline(),
                query.getResultDeadline(),
                query.getTargetTime(),
                query.getDeliveryArea(),
                query.getNumResponseBytes(),
                msg
        );
    }

    public VertigoQueryFooter toQueryFooter() {
        return new VertigoQueryFooter(
                sourceID,
                sessionID,
                receiveDeadline,
                resultDeadline,
                targetTime,
                deliveryArea,
                numResponseBytes);
    }
    
    /**
     * @return the receiveDeadline
     */
    public long getReceiveDeadline() {
        return receiveDeadline;
    }

    /**
     * @return the sessionID
     */
    public long getID() {
        return sessionID;
    }
    /**
     * @return the msg
     */
    public Message getMessage() {
        return message;
    }
    /**
     * @return the sourceID
     */
    public int getSourceID() {
        return sourceID;
    }
    
    /**
     * 
     * @param receiverID
     */
    public void confirmReceiver(int receiverID) {
        confirmedReceivers.add(receiverID);
    }
    
    /**
     * @return the confirmedReceivers
     */
    public Collection<Integer> getConfirmedReceivers() {
        return confirmedReceivers;
    }

    public boolean isConfirmedReceiver(int targetID) {
        return confirmedReceivers.contains(targetID);
    }

    public boolean isExpectedReceiver(int targetID) {
        return expectedReceivers.contains(targetID);
    }
    
    /**
     * 
     * @return
     */
    public long getResultDeadline() {
        return resultDeadline;
    }
    
    /**
     * @return the membershipView
     */
    public MembershipView getMembershipView() {
        return membershipView;
    }

    /**
     * @param membershipView the membershipView to set
     */
    public void setMembershipView(MembershipView membershipView) {
        this.membershipView = membershipView;
    }

    /**
     * @return the isDelivered
     */
    public boolean isDelivered() {
        return isDelivered;
    }

    /**
     * @param isDelivered the isDelivered to set
     */
    public void setDelivered(boolean isDelivered) {
        this.isDelivered = isDelivered;
    }

    /**
     * @return the numResponseBytes
     */
    public int getNumResponseBytes() {
        return numResponseBytes;
    }

    /**
     * @return the doneCallback
     */
    public Runnable getDoneCallback() {
        return doneCallback;
    }

    /**
     * @param doneCallback the doneCallback to set
     */
    public void setDoneCallback(Runnable doneCallback) {
        this.doneCallback = doneCallback;
    }

    public void done() {
        if(doneCallback != null) {
            doneCallback.run();
        }
    }

    public void addResponse(int sender, MicroResponse response) {
        this.responses.put(sender, response);
        this.confirmedReceivers.add(sender);
    }
    
    public void addResponses(Map<Integer, MicroResponse> responses) {
        this.responses.putAll(responses);
        
        for(Integer sender : responses.keySet()) {
            this.confirmedReceivers.add(sender);
        }
    }
    
    public Map<Integer,MicroResponse> getResponses() {
        return this.responses;
    }
}
