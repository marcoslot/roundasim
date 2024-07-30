/**
 * 
 */
package dsg.rounda.serialization.binary;

/**
 * Interface for serialization and deserialization
 */
public interface BinarySerializer<T> {

    /**
     * Serialize an object to a byte array
     */
    byte[] serialize(T obj);
    
    /**
     * Deserialize an object from a byte array
     */
    T deserialize(byte[] data, int index) throws Exception;
}
