/**
 * 
 */
package dsg.rounda.services.coordination;

import dsg.rounda.services.roadmap.TrackMapArea1D;
import dsg.rounda.services.roadmap.TrackRange1D;
import dsg.rounda.services.roadmap.TrackRangeSequence;
import dsg.rounda.services.roadmap.VehicleTrackMap;

/**
 * An area in which only one vehicle may be at a time
 */
public class ConflictArea {

    final int id;
    final ConflictRange[] ranges;
    
    /**
     * 
     */
    public ConflictArea(int identifier, ConflictRange rangeA, ConflictRange rangeB) {
        this.id = identifier;
        this.ranges = new ConflictRange[]{rangeA, rangeB};
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    public TrackRange1D[] getRanges() {
        return new TrackRange1D[]{ranges[0].getRange(), ranges[1].getRange()};
    }


    public ConflictRange getConflictRange(int trackID) {
        for(int i = 0; i < ranges.length; i++) {
            if(ranges[i].getTrackID() == trackID) {
                return ranges[i];
            }
        }
        return null;
    }
    
    public TrackRange1D getRange(int trackID) {
        return getConflictRange(trackID).getRange();
    }

    public boolean intersects(TrackRange1D range) {
        for(int i = 0; i < ranges.length; i++) {
            if(ranges[i].getRange().intersects(range)) {
                return true;
            }
        }
        return false;
    }

    public boolean intersects(TrackRangeSequence seq) {
        for(int i = 0; i < ranges.length; i++) {
            if(seq.intersects(ranges[i].getRange())) {
                return true;
            }
        }
        return false;
    }

    public double getStart(int trackID) {
        return getRange(trackID).getStart();
    }

    public double getEnd(int trackID) {
        return getRange(trackID).getEnd();
    }

    public TrackMapArea1D asTrackMapArea(VehicleTrackMap trackMap) {
        TrackMapArea1D area = new TrackMapArea1D(trackMap);
        
        for(TrackRange1D range : getRanges()) {
            area.add(range);
        }
        
        return area;
    }

    public boolean hasStrongConflictIn(TrackRangeSequence area) {
        for(ConflictRange range : ranges) {
            if(range.isStrong() && area.contains(range.getRange())) {
                return true;
            }
        }
        return false;
    }
    
}
