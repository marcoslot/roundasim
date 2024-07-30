/**
 * 
 */
package dsg.rounda.services.comm;

/**
 * @author slotm
 *
 */
public class ByteArrayFooter implements Footer {

    private byte[] bytes;
    
    /**
     * 
     */
    public ByteArrayFooter(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }
    

}
