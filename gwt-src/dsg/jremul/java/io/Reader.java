/**
 * 
 */
package java.io;

/**
 * @author slotm
 *
 */
public abstract class Reader {

    /**
     * 
     */
    public Reader() {
    }

    public abstract void close();
    public abstract int read();
}
