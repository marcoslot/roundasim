/**
 * 
 */
package dsg.rounda.services.vertigo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import dsg.rounda.model.Clock;
import dsg.rounda.model.Job;
import dsg.rounda.model.Message;
import dsg.rounda.model.Scheduler;
import dsg.rounda.model.VehicleCapabilities;
import dsg.rounda.services.AbstractServiceFactory;
import dsg.rounda.services.ServiceFactory;

/**
 * Vertig session manager
 */
public class VertigoSessionManager {

    static final ServiceFactory FACTORY = new AbstractServiceFactory(type()) {
        @Override
        public Object create(VehicleCapabilities capabilities) {
            return new VertigoSessionManager(capabilities);
        }
    };

    public static Class<VertigoSessionManager> type() {
        return VertigoSessionManager.class;
    }
    
    final int vehicleID;
    final Clock clock;
    final Scheduler scheduler;
    final Map<Long,VertigoSession> sessions;
    /**
     * 
     */
    public VertigoSessionManager(VehicleCapabilities capabilities) {
        this.vehicleID = capabilities.getId();
        this.clock = capabilities.getClock();
        this.scheduler = capabilities.getScheduler();
        this.sessions = new HashMap<Long,VertigoSession>();
    }

    public void registerSession(final VertigoSession session) {
        sessions.put(session.getID(), session);

        scheduler.schedule(new Job(
            new Runnable() {
                public void run() {
                    // Run done handler
                    session.done();
                    sessions.remove(session.getID());
                }
            },
            session.getSourceID(),
            session.getResultDeadline()
        ));
    }
    
    public Collection<VertigoSession> getSessions() {
        return sessions.values();
    }
    
    public VertigoSession getSession(long sessionID) {
        return sessions.get(sessionID);
    }

    public Collection<Long> getSessionIDs() {
        return sessions.keySet();
    }

    public VertigoSession getOrCreateSession(VertigoQueryFooter query, Message message) {
        VertigoSession session = sessions.get(query.getSessionID());

        if(session == null && clock.getTime() < query.getResultDeadline()) {
            // Create new session if it is still before the result deadline
            session = new VertigoSession(query, message);
            registerSession(session);
        }
        
        return session;
    }

}
