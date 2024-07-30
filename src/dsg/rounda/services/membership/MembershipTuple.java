/**
 * 
 */
package dsg.rounda.services.membership;

import java.util.HashSet;
import java.util.Set;

import dsg.rounda.Constants;
import dsg.rounda.services.roadmap.DecayFlags;
import dsg.rounda.services.roadmap.TrackMapArea1D;

/**
 * Membership tuple (M,A,T)
 */
public class MembershipTuple implements Comparable<MembershipTuple> {

    final Set<Integer> members;
    final TrackMapArea1D area;
    long time;
    
    /**
     * @param members
     * @param area
     * @param time
     */
    public MembershipTuple(Set<Integer> members, TrackMapArea1D area, long time) {
        this.members = members;
        this.area = area;
        this.time = time;
    }
    
    public MembershipTuple(MembershipTuple tuple) {
        this.members = new HashSet<Integer>(tuple.members);
        this.area = new TrackMapArea1D(tuple.area);
        this.time = tuple.time;
    }

    /**
     * Merges another tuple into this one
     * 
     * @param other the other tuple
     */
    public void merge(MembershipTuple other) {
        if(time != other.time) {
            throw new IllegalArgumentException("Cannot decay tuples from different times");
        }

        members.addAll(other.getMembers());
        area.add(other.getArea());
    }

    /**
     * Decay by time 
     * 
     * @param time nanoseconds to decay
     * @return the decayed tuple
     */
    public void decay(long dt) {
        area.decay((double) dt / Constants.SECONDS, new DecayFlags());
        time += dt;
    }
    
    public void decayTo(long timeStamp) {
        if(timeStamp < time) {
            throw new IllegalArgumentException("Cannot decay to the past");
        }
        
        decay(timeStamp - time);
    }
    
    /**
     * @return the members
     */
    public Set<Integer> getMembers() {
        return members;
    }
    /**
     * @return the area
     */
    public TrackMapArea1D getArea() {
        return area;
    }
    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }
    
    public boolean hasMember(int vehicleID) {
        return members.contains(vehicleID);
    }

    @Override
    public int compareTo(MembershipTuple other) {
        return (int) (time - other.time);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("M({");
        boolean isFirst = true;
        
        for(Integer member : members) {
            if(isFirst) {
               isFirst = false; 
            } else {
                sb.append(',');
            }
            sb.append(member);
        }
        
        sb.append("},");
        sb.append("area");
        sb.append(",");
        sb.append(time);
        sb.append(")");
        return sb.toString();
    }
    
}
