/**
 * 
 */
package java.util.concurrent;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author slotm
 *
 */
public class CopyOnWriteArrayList<E> extends ArrayList<E> {

    /**
     * 
     */
    private static final long serialVersionUID = 1026774165960921890L;

    /**
     * 
     */
    public CopyOnWriteArrayList() {
        
    }

    /**
     * @param arg0
     */
    public CopyOnWriteArrayList(int arg0) {
        super(arg0);
        
    }

    /**
     * @param arg0
     */
    public CopyOnWriteArrayList(Collection<? extends E> arg0) {
        super(arg0);
        
    }

}
