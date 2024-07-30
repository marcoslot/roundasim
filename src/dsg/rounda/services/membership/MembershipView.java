/**
 * 
 */
package dsg.rounda.services.membership;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import dsg.rounda.services.roadmap.TrackMapArea1D;

/**
 * A set of membership tuples
 */
public class MembershipView {

    List<MembershipTuple> tuples;
    
    /**
     * 
     */
    public MembershipView() {
        this.tuples = new ArrayList<MembershipTuple>();
    }
    
    /**
     * Remove any tuples from before minTime
     * 
     * @param minTime the minimum time of remaining tuples
     */
    public void clean(long minTime) {
        List<MembershipTuple> cleaned = new ArrayList<MembershipTuple>();
        
        for(MembershipTuple tuple : tuples) {
            if(tuple.getTime() >= minTime) {
                cleaned.add(tuple);
            }
        }
        
        tuples = cleaned;
    }
    
    /**
     * Add a membership tuple  to the view
     * 
     * @param members the set of members
     * @param area the membership area
     * @param time the time to which the tuple applies
     */
    public void add(Set<Integer> members, TrackMapArea1D area, long time) {
        add(new MembershipTuple(members, area, time));
    }
    
    /**
     * Add a membership tuple to the view
     * 
     * @param membershipTuple the membership tuple
     */
    public void add(MembershipTuple membershipTuple) {
        tuples.add(membershipTuple);
    }

    /**
     * Collapse all available tuples into a single tuple
     * 
     * @return the collapsed tuple
     */
    public MembershipTuple collapse() {
        return collapse(new MembershipTupleFilter() {
            @Override
            public boolean accept(MembershipTuple tuple) {
                return true;
            }
        });
    }
    
    /**
     * Collapse tuples that are accepted by the filter into a single tuple
     * 
     * @param filter the filter
     * @return the collapsed tuple
     */
    public MembershipTuple collapse(MembershipTupleFilter filter) {
        if(tuples.isEmpty()) {
            return null;
        }
        
        Collections.sort(tuples);
        
        MembershipTuple oneTuple = null;
        
        for(int i = 1; i < tuples.size(); i++) {
            MembershipTuple tuple = tuples.get(i);
            
            if(filter != null && !filter.accept(tuple)) {
                continue;
            }
            
            if(oneTuple == null) {
                oneTuple = new MembershipTuple(tuple);
                continue;
            }
            
            oneTuple.decay(tuple.getTime() - oneTuple.getTime());
            
            // Standard filter logic
            if(oneTuple.getArea().contains(tuple.getArea())) {
                // Don't merge tuples whose area is already fully contained by oneTuple
                // They can only make things worse by adding false positives
                continue;
            }
            
            oneTuple.merge(tuple);
        }
        
        return oneTuple;
    }

    /**
     * Collapse tuples whose members are all in includeOnly into a single tuple
     * 
     * @param includeOnly include only tuples whose membership set is a subset of this one
     * @return the collapsed tuple
     */
    public MembershipTuple collapse(final Collection<Integer> includeOnly) {
        return collapse(new MembershipTupleFilter() {
            @Override
            public boolean accept(MembershipTuple tuple) {
                for(Integer member : tuple.getMembers()) {
                    if(!includeOnly.contains(member)) {
                        return false;
                    }
                }
                return true;
            }
        });
    }

    /**
     * Collapse tuples whose area is contained by targetArea
     * 
     * @param targetArea the target area
     * @return the collapsed tuple
     */
    public MembershipTuple collapseContained(final TrackMapArea1D targetArea) {
        return collapse(new MembershipTupleFilter() {
            @Override
            public boolean accept(MembershipTuple tuple) {
                return targetArea.contains(tuple.getArea());
            }
        });
    }

    /**
     * Collapse tuples whose area is contained by targetArea
     * 
     * @param targetArea the target area
     * @return the collapsed tuple
     */
    public MembershipTuple collapseIntersecting(final TrackMapArea1D targetArea) {
        return collapse(new MembershipTupleFilter() {
            @Override
            public boolean accept(MembershipTuple tuple) {
                return targetArea.intersects(tuple.getArea());
            }
        });
    }

    /**
     * Collapse tuples whose area intersects with targetArea and
     *  whose members are all in includeOnly into a single tuple
     * 
     * @param targetArea the target area
     * @param includeOnly include only tuples whose membership set is a subset of this one
     * @return the collapsed tuple
     */
    public MembershipTuple collapse(final Set<Integer> includeOnly, final TrackMapArea1D targetArea) {
        return collapse(new MembershipTupleFilter() {
            @Override
            public boolean accept(MembershipTuple tuple) {
                for(Integer member : tuple.getMembers()) {
                    if(!includeOnly.contains(member)) {
                        return false;
                    }
                }
                return targetArea.intersects(tuple.getArea());
            }
        });
    }
    
    


}
