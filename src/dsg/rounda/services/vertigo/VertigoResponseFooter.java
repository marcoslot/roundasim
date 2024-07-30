/**
 * 
 */
package dsg.rounda.services.vertigo;

import java.util.HashMap;
import java.util.Map;

import dsg.rounda.services.comm.Footer;

/**
 * Footer appended to a Vertigo response message
 */
public class VertigoResponseFooter implements Footer {

    long sessionID;
    Map<Integer,MicroResponse> responses;
    
    
    /**
     * 
     */
    public VertigoResponseFooter(long sessionID) {
        this.sessionID = sessionID;
        this.responses = new HashMap<Integer,MicroResponse>();
    }

    public void addResponse(int vehicleID, MicroResponse response) {
        responses.put(vehicleID, response);
    }

    public void addResponses(Map<Integer,MicroResponse> responses) {
        this.responses.putAll(responses);
    }
    
    public Map<Integer,MicroResponse> getResponses() {
        return responses;
    }

    public long getSessionID() {
        return sessionID;
    }

    

}
