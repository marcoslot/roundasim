/**
 * 
 */
package java.io;

/**
 * @author slotm
 *
 */
public class StringWriter extends Writer {

    final StringBuilder sb;
    
    /**
     * 
     */
    public StringWriter() {
        this.sb = new StringBuilder();
    }

    @Override
    public void write(String string) {
        sb.append(string);
    }
    
    public String toString() {
        return sb.toString();
    }

}
