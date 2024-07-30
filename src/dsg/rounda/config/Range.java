/**
 * 
 */
package dsg.rounda.config;

/**
 * Represents a range of values of type T
 */
public interface Range<T> extends Iterable<T> {
    /**
     * Get the number of values in the range
     * @return the number of values in the range
     */
    int getNumValues();
}
