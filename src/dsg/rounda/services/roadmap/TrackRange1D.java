/**
 * 
 */
package dsg.rounda.services.roadmap;

import dsg.rounda.serialization.binary.BinarySerializationManager;
import dsg.rounda.serialization.binary.BinarySerializer;

/**
 * Represents a range between two offsets on a track
 */
public class TrackRange1D {

    int trackID;
    double start;
    double end;

    public TrackRange1D(TrackBoundary1D from, TrackBoundary1D to) {
        if(from.getTrackID() != to.getTrackID()) {
            throw new IllegalArgumentException("boundaries not from the same track");
        }
        
        this.trackID = from.getTrackID();
        this.start = Math.min(from.getOffset(), to.getOffset());
        this.end = Math.max(from.getOffset(), to.getOffset());
    }

    public TrackRange1D(TrackPoint1D from, TrackPoint1D to) {
        if(from.getTrackID() != to.getTrackID()) {
            throw new IllegalArgumentException("points not from the same track");
        }
        
        this.trackID = from.getTrackID();
        this.start = Math.min(from.getOffset(), to.getOffset());
        this.end = Math.max(from.getOffset(), to.getOffset());
    }

    /**
     * @param trackID
     * @param start
     * @param end
     */
    public TrackRange1D(int trackID, double start, double end) {
        this.trackID = trackID;
        this.start = Math.min(start, end);
        this.end = Math.max(start, end);
    }

    public TrackRange1D(TrackRange1D range) {
        this(range.trackID, range.start, range.end);
    }

    public TrackRange1D(TrackPoint1D point, double forward) {
        this(point.getTrackID(), point.getOffset(), point.getOffset()+forward);
    }

    /**
     * @return the trackID
     */
    public int getTrackID() {
        return trackID;
    }

    /**
     * @param trackID
     *            the trackID to set
     */
    public void setTrackID(int trackID) {
        this.trackID = trackID;
    }

    /**
     * @return the start
     */
    public double getStart() {
        return start;
    }

    /**
     * @param start
     *            the start to set
     */
    public void setStart(double start) {
        if (start > this.end) {
            throw new IllegalArgumentException(
                    "start can not be greater than end");
        }
        this.start = start;
    }
    
    public TrackPoint1D getStartPoint() {
        return new TrackPoint1D(trackID, start);
    }

    /**
     * @return the end
     */
    public double getEnd() {
        return end;
    }
    
    public TrackPoint1D getEndPoint() {
        return new TrackPoint1D(trackID, end);
    }

    /**
     * @param end
     *            the end to set
     */
    public void setEnd(double end) {
        if (end < this.start) {
            throw new IllegalArgumentException("end can not be less than start");
        }
        this.end = end;
    }

    public double getLength() {
        return end - start;
    }

    public boolean contains(TrackRange1D other) {
        return trackID == other.trackID && start <= other.start && other.end <= end;
    }

    public boolean intersects(TrackRange1D other) {
        return trackID == other.trackID && end >= other.start && start <= other.end;
    }

    public boolean contains(TrackPoint1D point) {
        return trackID == point.getTrackID() && contains(point.getOffset());
    }

    public boolean contains(double offset) {
        return start <= offset && offset <= end;
    }

    public TrackRange1D merge(TrackRange1D other) {
        if (this.trackID != other.trackID)
            return null;
        if (!intersects(other))
            return null;

        double maxEnd = Math.max(end, other.end);
        double minStart = Math.min(start, other.start);
        return new TrackRange1D(trackID, minStart, maxEnd);
    }

    public TrackRange1D computeIntersection(TrackRange1D other) {
        if (this.trackID != other.trackID)
            return null;
        if (!intersects(other))
            return null;

        double minEnd = Math.min(end, other.end);
        double maxStart = Math.max(start, other.start);
        return new TrackRange1D(trackID, maxStart, minEnd);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TrackRange1D(trackID=" + trackID + ", start=" + start
                + ", end=" + end + ")";
    }
    
    static {
        BinarySerializationManager.register(TrackRange1D.class, new BinarySerializer<TrackRange1D>() {

            @Override
            public byte[] serialize(TrackRange1D obj) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public TrackRange1D deserialize(byte[] data, int index) throws Exception {
                // TODO Auto-generated method stub
                return null;
            }

            
        });
    }

}
