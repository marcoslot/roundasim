/**
 * 
 */
package dsg.rounda.logging;

/**
 * Represents a logged event that can be listened to
 */
public class Event {

    final long simTime;
    final Object source;
    final String tag;
    final Object message;
    
    /**
     * @param time
     * @param source
     * @param message
     */
    public Event(long time, Object source, String tag, Object message) {
        if(source == null) {
            throw new IllegalArgumentException("source of an event may not be null");
        }
        this.simTime = time;
        this.source = source;
        this.tag = tag;
        this.message = message;
    }

    /**
     * @return the time
     */
    public long getSimTime() {
        return simTime;
    }

    /**
     * @return the source
     */
    public Object getSource() {
        return source;
    }

    /**
     * @return the message
     */
    public Object getMessage() {
        return message;
    }

    /**
     * @return the tag
     */
    public String getTag() {
        return tag;
    }

}
