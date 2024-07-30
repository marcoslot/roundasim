/**
 * 
 */
package dsg.rounda.services.vertigo;

import dsg.rounda.model.Message;

/**
 * Wrapper of a method that is called when a Vertigo message
 * is received
 */
public interface DeliverCallback {

    /**
     * Called when a new Vertigo query is received
     * 
     * @param msg the message that was received, including all its footers
     */
    void deliver(Message msg);
}
