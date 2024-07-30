/**
 * 
 */
package dsg.rounda.serialization.binary;

/**
 * @author slotm
 *
 */
public class IntegerSerializer implements BinarySerializer<Integer> {

    private static final int NUM_BYTES = 4;

    @Override
    public byte[] serialize(Integer obj) {
        byte[] data = new byte[NUM_BYTES];
        int value = obj;
        
        for(int i = NUM_BYTES - 1; i >= 0; i--) {
            data[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        
        return data;
    }

    @Override
    public Integer deserialize(byte[] data, int index) throws Exception {
        if(data.length < index + NUM_BYTES) {
            throw new Exception("Insufficient number of bytes");
        }
        
        int value = 0;
        
        for(int i = 0; i < NUM_BYTES; i++) {
            value |= data[index+i];
            value <<= 8;
        }

        return value;
    }
    

}
