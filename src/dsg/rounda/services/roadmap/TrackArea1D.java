/**
 * 
 */
package dsg.rounda.services.roadmap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import dsg.rounda.model.Track;


/**
 * Represents a set of ranges on a single track
 */
public class TrackArea1D {

    private final int trackID;
    private final LinkedList<TrackRange1D> ranges;
    private final double maxOffset;

    public TrackArea1D(int trackID, double maxOffset) {
        this.trackID = trackID;
        this.ranges = new LinkedList<TrackRange1D>();
        this.maxOffset = maxOffset;
    }

    public TrackArea1D(TrackArea1D value) {
        this(value.trackID, value.maxOffset);
        
        for(TrackRange1D range : value.ranges) {
            ranges.add(new TrackRange1D(range));
        }
    }

    public TrackArea1D(Track track) {
        this(track.getId(), track.getPathLength());
    }

    /**
     * @return the trackID
     */
    public int getTrackID() {
        return trackID;
    }

    public boolean isEmpty() {
        return ranges.isEmpty();
    }

    public double getMaxOffset() {
        return maxOffset;
    }

    public boolean isContiguous() {
        return ranges.size() == 1;
    }

    public boolean isContiguousFromTips() {
        return ranges.size() == 1
                || (ranges.size() == 2 && (containsStart() && containsEnd()));
    }

    public boolean isFullyCovered() {
        return ranges.size() == 1
                && containsStart()
                && containsEnd();
    }

    public List<TrackRange1D> getRanges() {
        return ranges;
    }

    public int getRangeCount() {
        return ranges.size();
    }

    public TrackRange1D getRange(int i) {
        return ranges.get(i);
    }

    public boolean contains(TrackRange1D testRange) {
        for (TrackRange1D range : ranges) {
            if (range.contains(testRange)) {
                return true;
            }
        }
        return false;
    }

    public TrackRange1D getRangeContaining(TrackPoint1D point) {
        if (trackID != point.getTrackID()) {
            return null;
        }
        return getRangeContaining(point.getOffset());
    }

    public TrackRange1D getRangeContaining(double offset) {
        for (TrackRange1D range : ranges) {
            if (range.contains(offset)) {
                return range;
            }
        }
        return null;
    }

    public TrackRange1D getRangeAfter(double offset) {
        TrackRange1D minRangeAfterOffset = null;
        
        for (TrackRange1D range : ranges) {
            if (range.contains(offset)) {
                return range;
            } else if(range.getStart() > offset 
                    && (minRangeAfterOffset == null 
                    || range.getStart() < minRangeAfterOffset.getStart())) {
                minRangeAfterOffset = range;
            }
        }

        return minRangeAfterOffset;
    }

    public void add(TrackArea1D other) {
        Iterable<TrackRange1D> otherRanges = other != this ? other.ranges : new ArrayList<TrackRange1D>(other.ranges);
        
        for (TrackRange1D range : otherRanges) {
            add(range);
        }
    }

    public void add(double start, double end) {
        if (start != end) {
            add(new TrackRange1D(trackID, start, end));
        }
    }

    public void add(TrackRange1D rangeToAdd) {
        double minStart = Math.max(0, rangeToAdd.start);
        double maxEnd = Math.min(rangeToAdd.end, maxOffset);
        
        if(maxEnd - minStart <= 0) {
            // Empty range, don't add
            return;
        }

        Iterator<TrackRange1D> it = ranges.iterator();
        
        while(it.hasNext()) {
            TrackRange1D range = it.next();
            
            if (rangeToAdd.intersects(range)) {
                minStart = Math.min(minStart, range.start);
                maxEnd = Math.max(maxEnd, range.end);
                it.remove();
            }
        }

        ranges.add(new TrackRange1D(trackID, minStart, maxEnd));
    }

    public void remove(double start, double end) {
        remove(new TrackRange1D(trackID, start, end));
    }

    public void remove(TrackArea1D area) {
        for (TrackRange1D range : area.getRanges()) {
            remove(range);
        }
    }

    public void remove(TrackRange1D rangeToRemove) {
        if (rangeToRemove.getTrackID() != trackID) {
            return;
        }
        if (rangeToRemove.getLength() <= 0) {
            return;
        }
        
        for(TrackRange1D range : new ArrayList<TrackRange1D>(ranges)) {
            if (rangeToRemove.intersects(range)) {
                if (rangeToRemove.contains(range)) {
                    ranges.remove(range);
                } else if (range.contains(rangeToRemove)) {
                    // range might split rangeToRemove in two

                    double oldRangeEnd = range.end;
                    
                    // reduce the existing range into the first part
                    range.end = rangeToRemove.start;
                    
                    if(range.getLength() <= 0) {
                        ranges.remove(range);
                    }
                    
                    // add a new range for the second part
                    add(new TrackRange1D(trackID, rangeToRemove.end, oldRangeEnd));
                    
                } else if (rangeToRemove.end < range.end) {
                    range.end = rangeToRemove.end;
                } else if (rangeToRemove.start > range.start) {
                    range.start = rangeToRemove.start;
                }
            }
        }
    }

    public boolean intersects(TrackRange1D other) {
        for (TrackRange1D range : ranges) {
            if (range.intersects(other)) {
                return true;
            }
        }
        return false;
    }

    public boolean intersects(TrackArea1D other) {
        for (TrackRange1D myRange : ranges) {
            for (TrackRange1D hisRange : other.ranges) {
                if (myRange.intersects(hisRange)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean contains(TrackPoint1D point) {
        for (TrackRange1D range : ranges) {
            if (range.contains(point)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(double offset) {
        for (TrackRange1D range : ranges) {
            if (range.contains(offset)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(TrackArea1D testArea) {
        if(isEmpty()) {
            return false;
        }
        
        for (TrackRange1D range : testArea.ranges) {
            if (!contains(range)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsStart() {
        return contains(0.0);
    }

    public boolean containsEnd() {
        return contains(maxOffset);
    }

    public String toString() {
        return ranges.toString();
    }

    public Double getFirstIntersection(TrackRange1D range) {
        Double result = null;
        
        for (TrackRange1D areaRange : ranges) {
            if (areaRange.contains(range.getStart())) {
                return range.getStart();
            } else if(areaRange.intersects(range) && (result == null || areaRange.getStart() < result)) {
                result = areaRange.getStart();
            }
        }
        
        return result;
    }
    
    public double getTotalLength() {
        double length = 0;
        
        for(TrackRange1D range : ranges) {
            length += range.getLength();
        }
        
        return length;
    }

    public TrackArea1D computeIntersection(TrackArea1D otherArea) {
        if(otherArea.getTrackID() != trackID) {
            return null;
        }
        
        TrackArea1D intersection = new TrackArea1D(trackID, maxOffset);

        for (TrackRange1D range : ranges) {
            for(TrackRange1D otherRange : otherArea.ranges) {
                TrackRange1D intersectionRange = range.computeIntersection(otherRange);
                
                if (intersectionRange == null) {
                    continue;
                }
                
                intersection.add(intersectionRange);
            }
        }
        
        return intersection;
    }
}
