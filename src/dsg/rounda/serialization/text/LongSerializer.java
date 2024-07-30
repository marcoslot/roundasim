/**
 * 
 */
package dsg.rounda.serialization.text;

/**
 * @author slotm
 *
 */
public class LongSerializer implements TextSerializer<Long> {


    /**
     * @see dsg.rounda.serialization.text.TextSerializer#serialize(java.lang.Object)
     */
    @Override
    public String serialize(Long obj) {
        return obj == null ? "" : Long.toString(obj);
    }

    /**
     * @see dsg.rounda.serialization.text.TextSerializer#deserialize(java.lang.String)
     */
    @Override
    public Long deserialize(String text) throws Exception {
        return text == null || text.isEmpty() ? null : Long.parseLong(text);
    }

}
