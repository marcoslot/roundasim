/**
 * 
 */
package dsg.rounda.services.vertigo;


/**
 * A micro response to a query
 */
public class MicroResponse {

    final byte[] data;
    
    /**
     * 
     */
    public MicroResponse(byte[] data) {
        this.data = data;
    }

    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    public Integer asInt() {
        return asInt(0);
    }
    
    public Integer asInt(int index) {
        if(data.length < index + 4) {
            return null;
        }
        
        return data[index] << 24 
             | data[index+1] << 16 
             | data[index+2] << 8 
             | data[index+3]; 
    }

    public static MicroResponse fromInt(int num) {
        return new MicroResponse(new byte[] {
                (byte) ((num >> 24) & 0xFF),
                (byte) ((num >> 16) & 0xFF),
                (byte) ((num >>  8) & 0xFF),
                (byte) ((num) & 0xFF),
        });
    }

    public static MicroResponse from(byte id, int num) {
        return new MicroResponse(new byte[] {
                id,
                (byte) ((num >> 24) & 0xFF),
                (byte) ((num >> 16) & 0xFF),
                (byte) ((num >>  8) & 0xFF),
                (byte) ((num) & 0xFF),
        });
    }

    public Byte asByte() {
        return asByte(0);
    }

    public Byte asByte(int index) {
        if(data.length < index) {
            return null;
        }
        
        return data[index];
    }

}
