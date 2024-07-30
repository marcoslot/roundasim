/**
 * 
 */
package dsg.rounda.config;

import java.util.Iterator;

/**
 * A range that repeats the configuration a given number of times
 */
public class RepeatRange implements Range<Integer> {

    private int numValues;
    
    public RepeatRange() {
        this(1);
    }
    /**
     * 
     */
    public RepeatRange(int numValues) {
        this.numValues = numValues;
    }

    /**
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {

            int i = 0;
            
            @Override
            public boolean hasNext() {
                return i < numValues;
            }

            @Override
            public Integer next() {
                return i++;
            }

            @Override
            public void remove() {
                
            }
        };
    }

    /**
     * @see dsg.rounda.config.Range#getNumValues()
     */
    @Override
    public int getNumValues() {
        return numValues;
    }

}
