/**
 * 
 */
package dsg.rounda.services.coordination;

import java.util.Collection;

import dsg.rounda.services.roadmap.TrackPoint1D;
import dsg.rounda.services.roadmap.TrackRange1D;
import dsg.rounda.services.roadmap.TrackRangeSequence;

/**
 * Part of a conflict trajectory
 */
public class ConflictTrajectoryPart {

    final TrackRange1D range;
    final Collection<ConflictArea> conflictAreas;
    /**
     * @param range
     * @param conflictAreaID
     */
    public ConflictTrajectoryPart(TrackRange1D range, Collection<ConflictArea> conflictAreas) {
        this.range = range;
        this.conflictAreas = conflictAreas;
    }

    public void addConflictArea(ConflictArea conflictArea) {
        this.conflictAreas.add(conflictArea);
    }
    
    /**
     * @return
     * @see dsg.rounda.services.roadmap.TrackRange1D#getTrackID()
     */
    public int getTrackID() {
        return range.getTrackID();
    }

    /**
     * @return the range
     */
    public TrackRange1D getRange() {
        return range;
    }

    /**
     * @return the conflictAreas
     */
    public Collection<ConflictArea> getConflictAreas() {
        return conflictAreas;
    }

    /**
     * @return
     * @see dsg.rounda.services.roadmap.TrackRange1D#getStart()
     */
    public double getStart() {
        return range.getStart();
    }

    /**
     * @return
     * @see dsg.rounda.services.roadmap.TrackRange1D#getEnd()
     */
    public double getEnd() {
        return range.getEnd();
    }

    /**
     * @return
     * @see dsg.rounda.services.roadmap.TrackRange1D#getLength()
     */
    public double getLength() {
        return range.getLength();
    }

    public boolean containsConflictArea(ConflictArea conflictArea) {
        return this.conflictAreas.contains(conflictArea);
    }

    public boolean hasConflicts() {
        return !conflictAreas.isEmpty();
    }

    public boolean hasStrongConflict() {
        for(ConflictArea conflictArea : conflictAreas) {
            ConflictRange conflictRange = conflictArea.getConflictRange(getTrackID());
            
            if(conflictRange.isStrong()) {
                return true;
            }
        }
        return false;
    }

    public boolean conflictsWith(ConflictTrajectory other) {
        for(ConflictArea conflictArea : conflictAreas) {
            if(other.containsConflictArea(conflictArea)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(TrackPoint1D startingPoint) {
        return range.contains(startingPoint);
    }

    public boolean hasStrongConflictIn(TrackRangeSequence area) {
        for(ConflictArea conflictArea : conflictAreas) {
            if(conflictArea.hasStrongConflictIn(area)) {
                return true;
            }
        }
        return false;
    }


}
