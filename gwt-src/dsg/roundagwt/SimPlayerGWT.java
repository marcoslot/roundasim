/**
 * 
 */
package dsg.roundagwt;

import java.util.Date;

import com.google.gwt.user.client.Timer;

import dsg.rounda.SimController;
import dsg.rounda.SimPlayer;
import dsg.rounda.TimeMode;

/**
 * @author slotm
 *
 */
public class SimPlayerGWT implements SimPlayer {

    private static final int ROUND_DELAY = 25;

    final SimController sim;
    
    long lastRoundRealTime;
    TimeMode timeMode;
    
    /**
     * 
     */
    public SimPlayerGWT(SimController sim) {
        this.sim = sim;
        this.timeMode = TimeMode.REAL_TIME;
    }
    
    public void start() {
        if(sim.isActive()) {
            return;
        }
        
        sim.setActive(true);
        lastRoundRealTime = time();
        timer.schedule(ROUND_DELAY);
    }

    final Timer timer = new Timer() {
        @Override
        public void run() {
            if(!sim.isActive()) {
                return;
            }
            
            long startOfRoundRealTime = time();
            long elapsedRealTime = startOfRoundRealTime - lastRoundRealTime;

            if(elapsedRealTime > 1000000000 || elapsedRealTime < 0) {
                // Woah, sleeping took over a second or the clock freaked out
                // probably some debugging going on
                elapsedRealTime = 0;
            }
            
            // Progress the simulation
            sim.step(elapsedRealTime);

            // Remember start time of this round to compute elapsed time
            lastRoundRealTime = getTimeMode() == TimeMode.REAL_TIME ? startOfRoundRealTime : time();
            
            // Schedule next round
            schedule(ROUND_DELAY);
        }
    };
    
    long time() {
        return new Date().getTime() * 1000000;
    }

    public void stop(boolean blockUntilDone) {
        sim.setActive(false);
    }

    /**
     * 
     * @see dsg.rounda.SimController#reset()
     */
    public void stop() {
        stop(true);
        sim.reset();
    }

    /**
     * @param playSpeed
     * @see dsg.rounda.SimController#setPlaySpeed(double)
     */
    public void setPlaySpeed(double playSpeed) {
        sim.setPlaySpeed(playSpeed);
    }

    /**
     * @return the timeMode
     */
    public TimeMode getTimeMode() {
        return timeMode;
    }

    /**
     * @param timeMode the timeMode to set
     */
    public void setTimeMode(TimeMode timeMode) {
        this.timeMode = timeMode;
    }

}
