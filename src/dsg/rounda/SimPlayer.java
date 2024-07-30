/**
 * 
 */
package dsg.rounda;

/**
 * @author slotm
 *
 */
public interface SimPlayer {

    /**
     * Start or resume running the simulation
     */
    void start();
    
    /**
     * Pause the simulation
     * 
     * @param blockUntilDone whether the method should block until done (optional to implement)
     */
    void stop(boolean blockUntilDone);
    
    /**
     * Stop the simulation and reset its state
     */
    void stop();
    
    /**
     * Set simulation speed
     * 
     * @param simTimeRatio
     */
    void setPlaySpeed(double simTimeRatio);
}
