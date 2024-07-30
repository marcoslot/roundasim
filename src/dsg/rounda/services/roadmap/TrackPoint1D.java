/**
 * 
 */
package dsg.rounda.services.roadmap;

/**
 * A point on a track
 */
public class TrackPoint1D implements Comparable<TrackPoint1D> {

    public static final double TRACK_POINT_EQUIVALENCE_DISTANCE = 0.001; // 1mm
    
    final int trackID;
    final double offset;
    /**
     * @param trackID
     * @param offset
     */
    public TrackPoint1D(int trackID, double offset) {
        this.trackID = trackID;
        this.offset = offset;
    }
    /**
     * @return the segmentID
     */
    public int getTrackID() {
        return trackID;
    }
    /**
     * @return the offset
     */
    public double getOffset() {
        return offset;
    }
    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 31 * trackID + (int) (offset / TRACK_POINT_EQUIVALENCE_DISTANCE);
    }
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TrackPoint1D)) {
            return false;
        }
        TrackPoint1D other = (TrackPoint1D) obj;
        if (trackID != other.trackID) {
            return false;
        }
        if (Math.abs(offset - other.offset) > TRACK_POINT_EQUIVALENCE_DISTANCE) {
            return false;
        }
        return true;
    }
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TrackPoint1D [trackID=" + trackID + ", offset=" + offset + "]";
    }
    @Override
    public int compareTo(TrackPoint1D other) {
        if(trackID != other.trackID) {
            throw new IllegalArgumentException("Cannot compare connectors on different tracks");
        }
        if(Math.abs(getOffset() - other.getOffset()) < TRACK_POINT_EQUIVALENCE_DISTANCE) {
            // Treat as equal
            return 0;
        }
        return Double.compare(getOffset(), other.getOffset());
    }
    
}
