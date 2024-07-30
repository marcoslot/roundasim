/**
 * 
 */
package dsg.rounda.services.coordination;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import dsg.rounda.model.Clock;
import dsg.rounda.model.VehicleCapabilities;
import dsg.rounda.services.AbstractServiceFactory;
import dsg.rounda.services.ServiceFactory;

/**
 * @author slotm
 *
 */
public class NegativeAllocator {

    static final ServiceFactory FACTORY = new AbstractServiceFactory(type()) {
        @Override
        public Object create(VehicleCapabilities capabilities) {
            return new NegativeAllocator(capabilities);
        }
    };

    public static Class<NegativeAllocator> type() {
        return NegativeAllocator.class;
    }

    final Clock clock;
    final Map<Integer,Set<NegativeAllocation>> negativeAllocations;
    
    /**
     * 
     */
    public NegativeAllocator(VehicleCapabilities capabilities) {
        this.clock = capabilities.getClock();
        this.negativeAllocations = new HashMap<Integer,Set<NegativeAllocation>>();
    }
    
    private Set<NegativeAllocation> getOrCreateNegativeAllocations(int conflictAreaID) {
        Set<NegativeAllocation> result = negativeAllocations.get(conflictAreaID);
        
        if(result == null) {
            result = new HashSet<NegativeAllocation>();
            negativeAllocations.put(conflictAreaID, result);
        }
        
        return result;
    }
    
    public void addNegativeAllocation(NegativeAllocation na) { 
        for(ConflictArea conflictArea : na.getConflicts()) {
            getOrCreateNegativeAllocations(conflictArea.getId()).add(na);
        }
    }
    
    public Set<NegativeAllocation> getNegativeAllocations(int conflictAreaID) {
        Set<NegativeAllocation> result = negativeAllocations.get(conflictAreaID);

        if(result == null) {
            return null;
        }
        
        // Remove expired allocations
        Iterator<NegativeAllocation> it = result.iterator();
        long time = clock.getTime();
        
        while(it.hasNext()) {
            NegativeAllocation na = it.next();
            
            if(time >= na.getLastEntryTime()) {
                na.setState(NegativeAllocation.State.RELEASED);
                it.remove();
            }
        }
        
        if(result.isEmpty()) {
            negativeAllocations.remove(conflictAreaID);
            return result;
        }
        
        return result;
    }
    
    public boolean isAllowedIn(int conflictAreaID) {
        return getNegativeAllocations(conflictAreaID) == null;
    }

}
