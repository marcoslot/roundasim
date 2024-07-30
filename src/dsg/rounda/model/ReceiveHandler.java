/**
 * 
 */
package dsg.rounda.model;

/**
 * Wrapper of a method to receive a message from the network
 */
public interface ReceiveHandler {
    /**
     * Called when receiving a message
     * 
     * @param msg message that is received
     */
    void receiveMessage(Message msg);
}
