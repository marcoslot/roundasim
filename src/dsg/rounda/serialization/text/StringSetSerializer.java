/**
 * 
 */
package dsg.rounda.serialization.text;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author slotm
 *
 */
public class StringSetSerializer implements TextSerializer<StringSet> {


    @Override
    public String serialize(StringSet set) {
        StringBuilder sb = new StringBuilder();
        
        Iterator<String> it = set.iterator();
        
        for(int i = 0; it.hasNext(); i++) {
            if(i>0) {
                sb.append(',');
            }
            sb.append(it.next());
        }
        
        return sb.toString();
    }

    @Override
    public StringSet deserialize(String text) throws Exception {
        StringSet result = new StringSet();
        
        for(String element : text.split(",")) {
            result.add(element);
        }
        
        return result;
    }

}
