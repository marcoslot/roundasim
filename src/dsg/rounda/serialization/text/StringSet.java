/**
 * 
 */
package dsg.rounda.serialization.text;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author slotm
 *
 */
public class StringSet extends HashSet<String> {

    /**
     * 
     */
    private static final long serialVersionUID = 3422150679716927209L;

    /**
     * 
     */
    public StringSet() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     */
    public StringSet(Collection<? extends String> arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     */
    public StringSet(int arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     * @param arg1
     */
    public StringSet(int arg0, float arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    public StringSet(String... args) {
        this(Arrays.asList(args));
    }

}
