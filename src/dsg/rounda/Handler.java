/**
 * 
 */
package dsg.rounda;

/**
 * Handles an object
 */
public interface Handler<T> {

    /**
     * Called when the object is to be handled
     * 
     * @param obj the object to be handled
     */
    void handle(T obj);
}
