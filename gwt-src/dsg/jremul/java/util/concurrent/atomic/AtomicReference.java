/**
 * 
 */
package java.util.concurrent.atomic;

/**
 * GWT/dummy implementation of Atomic Reference
 */
public class AtomicReference<V> {

    private V obj;

    public AtomicReference() {
        this.obj = null;
    }

    public AtomicReference(V obj) {
        this.obj = obj;
    }
    
    public final boolean compareAndSet(V expect, V update) {
        if(obj == expect) {
            obj = update;
            return true;
        } else {
            return false;
        }
    }
    
    public final V get() {
        return obj;
    }
    
    public final V getAndSet(V newValue) {
        V oldValue = obj;
        set(newValue);
        return oldValue;
    }

    public final void lazySet(V newValue) {
        set(newValue);
    }
    
    public final void set(V newValue) {
        obj = newValue;
    }
    
    public final String toString() {
        return obj == null ? "null" : obj.toString();
    }
    
    public final boolean weakCompareAndSet(V expect, V update) {
        return compareAndSet(expect, update);
    }
    
}
