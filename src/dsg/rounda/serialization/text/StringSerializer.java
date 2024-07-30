/**
 * 
 */
package dsg.rounda.serialization.text;

/**
 * @author slotm
 *
 */
public class StringSerializer implements TextSerializer<String> {

    /**
     * @see dsg.rounda.serialization.text.TextSerializer#serialize(java.lang.Object)
     */
    @Override
    public String serialize(String obj) {
        return obj;
    }

    /**
     * @see dsg.rounda.serialization.text.TextSerializer#deserialize(java.lang.String)
     */
    @Override
    public String deserialize(String text) throws Exception {
        return text;
    }

}
