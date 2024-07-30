/**
 * 
 */
package dsg.rounda.serialization.binary;

/**
 * @author slotm
 *
 */
public class DoubleSerializer implements BinarySerializer<Double> {

    private static final int NUM_BYTES = 8;
    private static final double PRECISION = 1000000;

    @Override
    public byte[] serialize(Double obj) {
        byte[] data = new byte[NUM_BYTES];
        long value = (long) (obj * PRECISION);
        
        for(int i = NUM_BYTES - 1; i >= 0; i--) {
            data[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        
        return data;
    }

    @Override
    public Double deserialize(byte[] data, int index) throws Exception {
        if(data.length < index + NUM_BYTES) {
            throw new Exception("Insufficient number of bytes");
        }
        
        long value = 0;
        
        for(int i = 0; i < NUM_BYTES; i++) {
            value |= data[index+i];
            value <<= 8;
        }

        return value / PRECISION;
    }
    

}
