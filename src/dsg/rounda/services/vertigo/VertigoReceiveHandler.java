/**
 * 
 */
package dsg.rounda.services.vertigo;

import dsg.rounda.model.Message;

/**
 * Wrapper for a method that is called when a new query is received
 */
public interface VertigoReceiveHandler {

    /**
     * Called when a new query is received
     * 
     * @param msg the message
     * @return response code, null if no response should be sent
     */
    MicroResponse receive(Message msg);
}
