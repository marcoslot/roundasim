/**
 * 
 */
package dsg.rounda.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class allows you to select something.
 */
public class Chooser<K,V> {

    private final Map<K,V> items;

    /**
     * Create an empty chooser
     */
    public Chooser() {
        this.items = new LinkedHashMap<K,V>();
    }
    
    /**
     * Add an item with the given name
     * 
     * @param name the name of the item
     * @param item the item to add
     * @throws IllegalArgumentException if an item with the specified name already exists
     */
    public void add(K name, V item) {
        if(items.containsKey(name)) {
            throw new IllegalArgumentException("item with name '" + name + "' already exists");
        }
        items.put(name, item);
    }
    
    /**
     * Get the item with the given name
     * 
     * @param name the name
     * @return the item
     */
    public V get(K name) {
        return items.get(name);
    }
    
    /**
     * Get the names of the item in the order of addition
     * 
     * @return names the names
     */
    public Collection<K> getNames() {
        return items.keySet();
    }
}
