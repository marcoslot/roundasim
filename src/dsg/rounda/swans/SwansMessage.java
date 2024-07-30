/**
 * 
 */
package dsg.rounda.swans;

import dsg.rounda.model.Message;

/**
 * @author slotm
 *
 */
public class SwansMessage implements jist.swans.misc.Message {

    final Message message; 
    
    /**
     * 
     */
    public SwansMessage(Message message) {
        this.message = message;
    }
    

    /**
     * @return the message
     */
    public Message getMessage() {
        return message;
    }


    /**
     * @see jist.swans.misc.Message#getSize()
     */
    @Override
    public int getSize() {
        return 1024;
    }

    /**
     * @see jist.swans.misc.Message#getBytes(byte[], int)
     */
    @Override
    public void getBytes(byte[] msg, int offset) {
        
    }

}
