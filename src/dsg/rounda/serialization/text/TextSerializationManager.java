/**
 * 
 */
package dsg.rounda.serialization.text;

import java.util.HashMap;
import java.util.Map;

import dsg.rounda.config.DoubleSequence;

/**
 * Static, GWT-compatible registration of text-to-object
 * serializers
 */
public class TextSerializationManager {
    
    private static TextSerializationManager instance;
    
    public static TextSerializationManager instance() {
        if(instance == null) {
            instance = new TextSerializationManager();
        }
        return instance;
    }
    
    static {
        register(Integer.class, new IntegerSerializer());
        register(Long.class, new LongSerializer());
        register(Double.class, new DoubleSerializer());
        register(String.class, new StringSerializer());
        register(StringSet.class, new StringSetSerializer());
        new DoubleSequence();
    }
    
    private final Map<Class<?>,TextSerializer<?>> serializers;
    
    /**
     * 
     */
    public TextSerializationManager() {
        this.serializers = new HashMap<Class<?>,TextSerializer<?>>();
    }
    
    public static <T> void register(Class<T> type, TextSerializer<T> ts) {
        instance().serializers.put(type, ts);
    }
    
    public static <T> T deserialize(Class<T> type, String text) throws Exception {
        return (T) instance().serializers.get(type).deserialize(text);
    }
    
    public static <T> String serialize(Class<T> type, T obj) {
        TextSerializer<T> serializer = (TextSerializer<T>) instance().serializers.get(type);
        return serializer.serialize(obj);
    }
    
    public static <T> String serialize(T obj) {
        return serialize((Class<T>) obj.getClass(), obj);
    }

}
