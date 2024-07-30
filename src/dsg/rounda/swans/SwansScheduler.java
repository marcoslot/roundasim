/**
 * 
 */
package dsg.rounda.swans;

import dsg.rounda.model.Job;
import dsg.rounda.model.Scheduler;


/**
 * Wrapper around Scheduler for SWANS
 */
public class SwansScheduler implements jist.swans.Scheduler {

    final Scheduler scheduler;
    
    /**
     * @param clock
     * @param scheduler
     */
    public SwansScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * @see jist.swans.Scheduler#schedule(java.lang.Runnable, long)
     */
    @Override
    public void schedule(Runnable task, long startTime) {
        scheduler.schedule(new Job(task, Job.GLOBAL_OWNER, startTime));
    }

    /**
     * @see jist.swans.Scheduler#scheduleForOwner(java.lang.Runnable, int, long)
     */
    @Override
    public void scheduleForOwner(Runnable task, int owner, long startTime) {
        scheduler.schedule(new Job(task, Job.GLOBAL_OWNER, startTime));
    }

}
