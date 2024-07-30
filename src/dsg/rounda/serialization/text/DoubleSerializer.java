/**
 * 
 */
package dsg.rounda.serialization.text;

/**
 * @author slotm
 *
 */
public class DoubleSerializer implements TextSerializer<Double> {

    /**
     * @see dsg.rounda.serialization.text.TextSerializer#serialize(java.lang.Object)
     */
    @Override
    public String serialize(Double obj) {
        return obj == null ? "" : Double.toString(obj);
    }

    /**
     * @see dsg.rounda.serialization.text.TextSerializer#deserialize(java.lang.String)
     */
    @Override
    public Double deserialize(String text) throws Exception {
        return text == null || text.isEmpty() ? null : Double.parseDouble(text);
    }

}
