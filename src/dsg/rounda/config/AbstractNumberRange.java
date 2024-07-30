/**
 * 
 */
package dsg.rounda.config;


/**
 * @author slotm
 *
 */
public abstract class AbstractNumberRange<T extends Comparable<T>> implements Range<T> {

    private static final long serialVersionUID = 6980579449544585285L;
    
    private T start;
    private T end;
    private T step;

    public AbstractNumberRange() {
        this.start = null;
        this.end = null;
        this.step = null;
    }

    public AbstractNumberRange(T value) {
        this.start = value;
        this.end = value;
        this.step = null;
    }
    
    public AbstractNumberRange(T start, T end, T step) {
        if((start == null || end == null) && start != end) {
            throw new IllegalArgumentException("start or end is null");
        }
        if(end != null && end.compareTo(start) != 0 && step == null) {
            throw new IllegalArgumentException("step cannot be null if start != end");
        }
        if(end != null && end.compareTo(start) < 0) {
            throw new IllegalArgumentException("end cannot be less than start");
        }
        this.start = start;
        this.end = end;
        this.step = step;
    }
    
    /**
     * @return the start
     */
    public T getStart() {
        return start;
    }

    /**
     * @return the end
     */
    public T getEnd() {
        return end;
    }

    /**
     * @return the step
     */
    public T getStep() {
        return step;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((end == null) ? 0 : end.hashCode());
        result = prime * result + ((start == null) ? 0 : start.hashCode());
        result = prime * result + ((step == null) ? 0 : step.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbstractNumberRange)) {
            return false;
        }
        AbstractNumberRange other = (AbstractNumberRange) obj;
        if (end == null) {
            if (other.end != null) {
                return false;
            }
        } else if (!end.equals(other.end)) {
            return false;
        }
        if (start == null) {
            if (other.start != null) {
                return false;
            }
        } else if (!start.equals(other.start)) {
            return false;
        }
        if (step == null) {
            if (other.step != null) {
                return false;
            }
        } else if (!step.equals(other.step)) {
            return false;
        }
        return true;
    }

}
