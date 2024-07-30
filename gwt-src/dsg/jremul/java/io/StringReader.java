/**
 * 
 */
package java.io;

/**
 * @author slotm
 *
 */
public class StringReader extends Reader {

    int pos;
    final String text;
    
    public StringReader(String text) {
        this.text = text;
    }

    @Override
    public void close() {
    }

    @Override
    public int read() {
        return pos < text.length() ? text.charAt(pos++) : -1;
    }


}
