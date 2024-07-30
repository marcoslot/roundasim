/**
 * 
 */
package dsg.rounda.logging;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import dsg.rounda.SimController;
import dsg.rounda.model.Clock;

/**
 * Facility for logging events and consuming
 * logged events, possibly using filters.
 */
public class EventLog {

    final Clock clock;
    final List<FilteredHandler> handlers;
    
    public EventLog(Clock globalClock) {
        handlers = new CopyOnWriteArrayList<FilteredHandler>();
        clock = globalClock;
    }
    
    public EventLog() {
        handlers = new CopyOnWriteArrayList<FilteredHandler>();
        clock = null;
	}

	/**
     * Add a handler for consuming events. 
     * 
     * @param handler the handler
     */
    public void addHandler(EventHandler handler) {
        addHandler(acceptAll(), handler);
    }
    
    /**
     * Add a handler for consuming events with a filter.
     * 
     * @param filter the filter
     * @param handler the handler
     */
    public  void addHandler(EventFilter filter, EventHandler handler) {
        handlers.add(new FilteredHandler(filter, handler));
    }

    /**
     * Log a message from object source
     * 
     * @param source the source object
     * @param msg the message
     */
    public <S,L> void log(S source, L msg) {
        log(source, null, msg);
    }

    /**
     * Log a message from object source
     * 
     * @param source the source object
     * @param tag the tag
     * @param msg the message
     */
    public <S,L> void log(S source, String tag, L msg) {
        Event evt = new Event(clock == null ? 0L : clock.getTime(), source, tag, msg);
        
        for(FilteredHandler handler : handlers) {
            handler.event(evt);
        }
    }

    public static EventFilter acceptAll() {
        return new EventFilter() {
            @Override
            public boolean accept(Event evt) {
                return true;
            }
        };
    }

    public static EventFilter acceptReset() {
        return new EventFilter() {
            @Override
            public boolean accept(Event evt) {
                return evt.getSource() instanceof SimController && "reset".equals(evt.getTag());
            }
        };
    }

    public static EventFilter acceptTag(final String tag) {
        return new EventFilter() {
            @Override
            public boolean accept(Event evt) {
                return tag.equals(evt.getTag());
            }
        };
    }

    public static <T> EventFilter and(final EventFilter... filters) {
        return new EventFilter() {
            @Override
            public boolean accept(Event evt) {
                for(EventFilter filter : filters) {
                    if(!filter.accept(evt)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    public static <T> EventFilter acceptMessageType(final Class<T> type) {
        return new EventFilter() {
            @Override
            public boolean accept(Event evt) {
                return evt.getMessage().getClass().equals(type);
            }
        };
    }

    public static <T> EventFilter acceptSourceType(final Class<T> type) {
        return new EventFilter() {
            @Override
            public boolean accept(Event evt) {
                return evt.getSource().getClass().equals(type);
            }
        };
    }

    public static <T> EventFilter acceptVehicle(final int vehicleID) {
        return new EventFilter() {
            @Override
            public boolean accept(Event evt) {
                if(!(evt.getSource() instanceof Integer)) {
                    return false;
                }
                if(((Integer) evt.getSource()) != vehicleID) {
                    return false;
                }
                return true;
            }
        };
    }

    private static class FilteredHandler implements EventHandler {
        
        final EventFilter filter;
        final EventHandler handler;

        public FilteredHandler(EventFilter filter, EventHandler handler) {
            this.filter = filter;
            this.handler = handler;
        }

        public void event(Event event) {
            if(filter.accept(event)) {
                handler.event(event);
            }
        }
        
        
    }

}
