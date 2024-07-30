/**
 * 
 */
package dsg.rounda.swans;

import dsg.rounda.model.Clock;

/**
 * @author slotm
 *
 */
public class SwansClock implements jist.swans.Clock {

    final Clock clock;
    
    /**
     * 
     */
    public SwansClock(Clock clock) {
        this.clock = clock;
    }

    /**
     * @see jist.swans.Clock#getNanos()
     */
    @Override
    public long getNanos() {
        return clock.getTime();
    }

}
