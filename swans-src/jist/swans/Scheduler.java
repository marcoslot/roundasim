/**
 *
 */
package jist.swans;

/**
 * @author slotm
 *
 */
public interface Scheduler {
	void schedule(Runnable task, long startTime);
	void scheduleForOwner(Runnable task, int owner, long startTime);
}
