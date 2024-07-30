/**
 * 
 */
package dsg.rounda.model;

/**
 * A Job to be executed at a particular simulation time
 */
public class Job implements Comparable<Job> {

    public static final int GLOBAL_OWNER = -1;
    public static final int REPEAT_FOREVER = -1;
    final int owner;
    final Runnable task;
    final Long startTime;
    final Long repeatInterval;
    final int numRepeats; 

    public Job(Runnable task, int owner, double startTime) {
        this(task, owner, (long) startTime);
    }

    public Job(Runnable task, int owner, double startTime, double repeatInterval) {
        this(task, owner, (long) startTime, (long) repeatInterval);
    }

    public Job(Runnable task, int owner, double startTime, double repeatInterval, int numRepeats) {
        this(task, owner, (long) startTime, (long) repeatInterval, numRepeats);
    }

    /**
     * 
     */
    public Job(Runnable task, int owner, long startTime) {
        this.owner = owner;
        this.task = task;
        this.startTime = startTime;
        this.repeatInterval = null;
        this.numRepeats = 0;
    }

    public Job(Runnable task, int owner, long startTime, long repeatInterval) {
        this(task, owner, startTime, repeatInterval, REPEAT_FOREVER);
    }

    public Job(Runnable task, int owner, long startTime, long repeatInterval, int numRepeats) {
        this.owner = owner;
        this.task = task;
        this.startTime = startTime;
        this.repeatInterval = repeatInterval;
        this.numRepeats = numRepeats;
    }

    /**
     * @return the owner
     */
    public int getOwner() {
        return owner;
    }

    /**
     * @return the task
     */
    public Runnable getTask() {
        return task;
    }

    /**
     * @return the startTime
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @return the repeatInterval
     */
    public Long getRepeatInterval() {
        return repeatInterval;
    }

    /**
     * @return the numRepeats
     */
    public int getNumRepeats() {
        return numRepeats;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Job other) {
        return startTime.compareTo(other.startTime);
    }

}
