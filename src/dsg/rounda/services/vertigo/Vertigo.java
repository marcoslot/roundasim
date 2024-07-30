/**
 * 
 */
package dsg.rounda.services.vertigo;

import dsg.rounda.model.Message;
import dsg.rounda.services.roadmap.TrackMapArea1D;

/**
 * Interface to a Vertigo implementation
 */
public interface Vertigo {

    void addReceiver(VertigoReceiveHandler receiver);

    VertigoSession startSession(
            Message msg, 
            long receiveDeadline,
            long resultDeadline, 
            long targetTime, 
            TrackMapArea1D targetArea,
            int numResponseBytes,
            ResultHandler resultHandler);

}
