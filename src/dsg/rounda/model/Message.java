/**
 * 
 */
package dsg.rounda.model;

import java.util.HashMap;
import java.util.Map;

import dsg.rounda.services.comm.Footer;

/**
 * @author slotm
 * 
 * A message is a sequence of footers with unique types
 *
 */
public class Message {
    
    public static final int ANY_DESTINATION = -1;

    final int source;
    final int destination;
    final Map<Class<?>,Footer> footers;
    
    boolean isBeacon;
    
    /**
     * @param source
     * @param destination
     * @param payload
     */
    public Message(int source, int destination) {
        this.source = source;
        this.destination = destination;
        this.footers = new HashMap<Class<?>,Footer>();
    }
    
    public boolean isBeacon() {
        return isBeacon;
    }

    public void setBeacon(boolean isBeacon) {
        this.isBeacon = isBeacon;
    }

    /**
     * @return the source
     */
    public int getSource() {
        return source;
    }
    /**
     * @return the destination
     */
    public int getDestination() {
        return destination;
    }
    
    public <T extends Footer> void addFooter(T footer) {
        if(footers.containsKey(footer.getClass())) {
            throw new IllegalStateException("Cannot add second footer of type " + footer.getClass());
        }
        
        footers.put(footer.getClass(), footer);
    }

    public <T extends Footer> boolean hasFooter(Class<T> footerType) {
        return footers.containsKey(footerType);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Footer> T getFooter(Class<T> footerType) {
        return (T) footers.get(footerType);
    }
    
}
