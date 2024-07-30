/**
 * 
 */
package dsg.rounda;

import dsg.rounda.gui.SimPresenter;

/**
 * An implementation of SimPlayer that uses JRE Threads
 */
public class SimPlayerJRE implements SimPlayer {

    final SimController sim;

    private final Object sleepLock;

    final private Object runningLock;
    private boolean running;

    final private Object modeLock;
    TimeMode timeMode;

    /**
     * @param sim
     * @param paletPresenter 
     */
    public SimPlayerJRE(
            SimController sim, 
            SimPresenter paletPresenter) {
        this.sim = sim;
        this.sleepLock = new Object();
        this.runningLock = new Object();
        this.running = false;
        this.modeLock = new Object();
        this.timeMode = TimeMode.REAL_TIME;
    }

    /**
     * Start or resume the simulation
     */
    public synchronized void start() {
        if(sim.isActive()) {
            // Already started
            return;
        }

        // Make sure the previous controller thread has stopped
        blockUntilDone();

        // Enable entry into the main loop
        sim.setActive(true);

        // Mark thread as running
        synchronized(runningLock) {
            running = true;
        }

        // Start a new controller thread
        new Thread(runner).start();
    }

    /**
     * Runnable code for the controller thread
     */
    Runnable runner = new Runnable() {
        public void run() {
            // Bootstrap first round
            long lastRoundRealTime = time();

            while(sim.isActive()) {
                long startOfRoundRealTime = time();

                // We've slept for this long
                long elapsedRealTime = startOfRoundRealTime - lastRoundRealTime;

                if(elapsedRealTime > 1000000000 || elapsedRealTime < 0) {
                    // Woah, sleeping took over a second or System.nanoTime freaked out
                    // probably some debugging going on
                    elapsedRealTime = 0;
                }

                sim.step(elapsedRealTime);

                // Remember start time of this round to compute elapsed time
                lastRoundRealTime = getTimeMode() == TimeMode.REAL_TIME ? startOfRoundRealTime : time();

                // Sleep a while before next round
                sleep();
            }

            // This thread is really done now, let blockUntilDone() return
            synchronized(runningLock) {
                running = false;
                runningLock.notifyAll();
            }
        }

        void sleep() {
            synchronized(sleepLock) {
                try {
                    // Sleep 1ms or stop immediately if stop() is called
                    sleepLock.wait(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

    };

    long time() {
        return System.nanoTime();
    }

    /**
     * Stop the simulation (can be resumed using start())
     * 
     * @param blockUntilDone if true, block until controller thread is really done
     */
    public synchronized void stop(boolean blockUntilDone) {
        // Tell the controller thread to break out of the main loop
        sim.setActive(false);

        synchronized(sleepLock) {
            sleepLock.notifyAll();
        }

        if(blockUntilDone) {
            // Wait until controller thread has broken out of the main loop
            blockUntilDone();
        }
    }

    /**
     * Blocks until current controller thread is definitely going
     * to return.
     */
    private void blockUntilDone() {
        synchronized(runningLock) {
            try {
                while(running) {
                    runningLock.wait();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 
     * @see dsg.rounda.SimController#reset()
     */
    public void stop() {
        stop(true);
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
        synchronized(modeLock) {
            return timeMode;
        }
    }

    /**
     * @param timeMode the timeMode to set
     */
    public void setTimeMode(TimeMode timeMode) {
        synchronized(modeLock) {
            this.timeMode = timeMode;
        }
    }

}
