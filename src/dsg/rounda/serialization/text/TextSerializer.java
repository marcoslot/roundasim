/**
 * 
 */
package dsg.rounda.serialization.text;

/**
 * Interface for serialization and deserialization
 */
public interface TextSerializer<T> {

    /**
     * Serialize an object to a string
     * @param obj object to serialize
     * @return serialized object
     */
    String serialize(T obj);
    
    /**
     * Deserialize an object from a string
     * @param serialized object
     * @return the object that was deserialized
     */
    T deserialize(String text) throws Exception;
}
