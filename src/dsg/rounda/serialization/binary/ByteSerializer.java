/**
 * 
 */
package dsg.rounda.serialization.binary;

/**
 * @author slotm
 *
 */
public class ByteSerializer implements BinarySerializer<Byte> {

    private static final int NUM_BYTES = 1;

    @Override
    public byte[] serialize(Byte obj) {
        return new byte[]{obj};
    }

    @Override
    public Byte deserialize(byte[] data, int index) throws Exception {
        if(data.length < index + NUM_BYTES) {
            throw new Exception("Insufficient number of bytes");
        }
        
        return data[index];
    }
    
}
