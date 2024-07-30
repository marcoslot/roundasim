/**
 * 
 */
package dsg.rounda.services.roadmap;

import dsg.rounda.geometry.Direction;


/**
 * A single boundary of a 1D area on the road network
 */
public class TrackBoundary1D extends TrackPoint1D {

    final Direction inclusive;

    public TrackBoundary1D(int trackID, double offset, Direction inclusive) {
        super(trackID, offset);
        this.inclusive = inclusive;
    }

    /**
     * @return the inclusive
     */
    public Direction getInclusive() {
        return inclusive;
    }

    public String toString() {
        return "Boundary1D(" + trackID + "," + offset + "," + inclusive + ")";
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((inclusive == null) ? 0 : inclusive.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof TrackBoundary1D)) {
            return false;
        }
        TrackBoundary1D other = (TrackBoundary1D) obj;
        if (inclusive != other.inclusive) {
            return false;
        }
        return true;
    }
    
    @Override
    public int compareTo(TrackPoint1D other) {
        int compare = super.compareTo(other);
        
        if(compare != 0 
        || !(other instanceof TrackBoundary1D) 
        || inclusive == ((TrackBoundary1D)other).inclusive) {
            return compare;
        } else {
            return inclusive == Direction.FORWARD ? -1 : 1;
        }
    }

}
