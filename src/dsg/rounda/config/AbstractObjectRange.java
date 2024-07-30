package dsg.rounda.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;


/**
 * Range of objects
 */
public abstract class AbstractObjectRange<T> implements Range<T> {

    private static final long serialVersionUID = 2502936386501542760L;
    
    private Collection<T> values;
    
    public AbstractObjectRange() {
        this.values = new ArrayList<T>();
        this.values.add(null);
    }
    
    /**
     * 
     */
    public AbstractObjectRange(Collection<T> values) {
        this.values = values;
    }
    
    /**
     * 
     */
    public AbstractObjectRange(T... values) {
        this.values = Arrays.asList(values);
    }

    /**
     * 
     */
    @Override
    public Iterator<T> iterator() {
        return values.iterator();
    }

    /**
     * 
     */
    @Override
    public int getNumValues() {
        return values.size();
    }

    public static <T> String doSerialize(AbstractObjectRange<T> range) {
        StringBuilder sb = new StringBuilder();
        
        Iterator<T> it = range.iterator();
        
        for(int i = 0; it.hasNext(); i++) {
            if(i>0) {
                sb.append(',');
            }
            sb.append(it.next());
        }
        
        return sb.toString();
    }

}