/**
 * 
 */
package dsg.rounda.serialization.binary;

import static dsg.rounda.serialization.binary.BinarySerializationManager.serialize;

import java.util.ArrayList;
import java.util.List;

/**
 * @author slotm
 *
 */
public class SerializationBuffer {

    List<byte[]> data;
    
    /**
     * 
     */
    public SerializationBuffer() {
        this.data = new ArrayList<byte[]>();
    }

    public void pushByte(Byte value) {
        data.add(serialize(value));
    }
    
    public void pushInt(Integer value) {
        data.add(serialize(value));
    }

    public void pushLong(Long value) {
        data.add(serialize(value));
    }

}
