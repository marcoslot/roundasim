/**
 * 
 */
package dsg.rounda.model;

import com.vividsolutions.jts.geom.LineSegment;

import dsg.rounda.model.TrackSegment.Type;

/**
 * A (track,segment) tuple used for very high performance
 * intersections of lines and tracks using SpatialGrid2D.
 */
public class TrackSegment {
    
    public enum Type {
        PATH,
        LEFT_INTERSECTOR,
        RIGHT_INTERSECTOR;
    }
    
    final Track track;
    final Type type;
    final LineSegment segment;
    final int segmentIndex;
    /**
     * @param track
     * @param segment
     * @param segmentIndex
     */
    public TrackSegment(Track track, Type type, LineSegment segment, int segmentIndex) {
        this.track = track;
        this.type = type;
        this.segment = segment;
        this.segmentIndex = segmentIndex;
    }
    /**
     * @return the track
     */
    public Track getTrack() {
        return track;
    }
    /**
     * @return the segment
     */
    public LineSegment getSegment() {
        return segment;
    }
    /**
     * @return the segmentIndex
     */
    public int getSegmentIndex() {
        return segmentIndex;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 31 * track.getId() + segmentIndex;
    }
    /* (non-Javadoc)
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
        if (!(obj instanceof TrackSegment)) {
            return false;
        }
        TrackSegment other = (TrackSegment) obj;
        if (track.getId() != other.track.getId()) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        if (segmentIndex != other.segmentIndex) {
            return false;
        }
        return true;
    }
    public Type getType() {
        return type;
    }
    
}
