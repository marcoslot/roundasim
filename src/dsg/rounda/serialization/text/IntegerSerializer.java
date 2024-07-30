/**
 * 
 */
package dsg.rounda.serialization.text;

/**
 * @author slotm
 *
 */
public class IntegerSerializer implements TextSerializer<Integer> {


    /**
     * @see dsg.rounda.serialization.text.TextSerializer#serialize(java.lang.Object)
     */
    @Override
    public String serialize(Integer obj) {
        return obj == null ? "" : Integer.toString(obj);
    }

    /**
     * @see dsg.rounda.serialization.text.TextSerializer#deserialize(java.lang.String)
     */
    @Override
    public Integer deserialize(String text) throws Exception {
        return text == null || text.isEmpty() ? null : Integer.parseInt(text);
    }

}
