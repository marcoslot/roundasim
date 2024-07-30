/**
 * 
 */
package dsg.rounda.serialization.binary;

import java.util.HashMap;
import java.util.Map;


/**
 * Manages serializers/deserializers for object <-> byte[] conversions
 */
public class BinarySerializationManager {

    private static BinarySerializationManager instance;
    
    public static BinarySerializationManager instance() {
        if(instance == null) {
            instance = new BinarySerializationManager();
        }
        return instance;
    }
    
    static {
        register(Integer.class, new IntegerSerializer());
        register(Long.class, new LongSerializer());
        register(Byte.class, new ByteSerializer());
        register(Double.class, new DoubleSerializer());
    }
    
    private final Map<Class<?>,BinarySerializer<?>> serializers;
    
    /**
     * 
     */
    public BinarySerializationManager() {
        this.serializers = new HashMap<Class<?>,BinarySerializer<?>>();
    }
    
    public static <T> void register(Class<T> type, BinarySerializer<T> ts) {
        instance().serializers.put(type, ts);
    }
    
    public static <T> T deserialize(Class<T> type, byte[] data, int index) throws Exception {
        return (T) instance().serializers.get(type).deserialize(data, index);
    }
    
    public static <T> byte[] serialize(Class<T> type, T obj) {
        BinarySerializer<T> serializer = (BinarySerializer<T>) instance().serializers.get(type);
        return serializer.serialize(obj);
    }
    
    public static <T> byte[] serialize(T obj) {
        return serialize((Class<T>) obj.getClass(), obj);
    }
}
