/**
 * 
 */
package dsg.rounda.services.coordination;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import dsg.rounda.services.roadmap.TrackMapArea1D;
import dsg.rounda.services.roadmap.TrackPoint1D;
import dsg.rounda.services.roadmap.TrackRange1D;
import dsg.rounda.services.roadmap.TrackRangeSequence;
import dsg.rounda.services.roadmap.VehicleTrackMap;

/**
 * A trajectory with interleaved conflict/non-conflict parts
 */
public class ConflictTrajectory implements Iterable<ConflictTrajectoryPart> {

    final LinkedList<ConflictTrajectoryPart> parts;
    final Set<ConflictArea> conflictAreas;
    double length;
    
    // cache
    TrackMapArea1D trackMapArea; 
    
    public ConflictTrajectory() {
        this.parts = new LinkedList<ConflictTrajectoryPart>();
        this.conflictAreas = new HashSet<ConflictArea>();
        this.length = 0.0;
    }

    public Double distanceToFirstConflict(ConflictTrajectory other, TrackPoint1D startingPoint) {
        if(!contains(startingPoint)) {
            return distanceToFirstConflict(other);
        }
        
        boolean skipParts = true;
        double distance = 0.0;
        
        for(ConflictTrajectoryPart part : parts) {
            if(skipParts) {
                if(part.contains(startingPoint)) {
                    skipParts = false;
                } else {
                    continue;
                }
            }  
            if(part.conflictsWith(other)) {
                return distance;
            }
            distance += part.getLength();
        }
        
        return null;
    }

    public Double distanceToFirstConflict(ConflictTrajectory other) {
        double distance = 0.0;
        
        for(ConflictTrajectoryPart part : parts) {
            if(part.conflictsWith(other)) {
                return distance;
            }
            distance += part.getLength();
        }
        
        return null;
    }

    public boolean inConflictWith(ConflictTrajectory other, TrackPoint1D startingPoint) {
        if(!contains(startingPoint)) {
            return inConflictWith(other);
        }
        
        boolean skipParts = true;
        
        for(ConflictTrajectoryPart part : parts) {
            if(skipParts) {
                if(part.contains(startingPoint)) {
                    skipParts = false;
                } else {
                    continue;
                }
            }  
            if(part.conflictsWith(other)) {
                return true;
            }
        }
        
        return false;
    }

    public boolean inConflictWith(ConflictTrajectory other) {
        for(ConflictArea area : conflictAreas) {
            if(other.containsArea(area)) {
                return true;
            }
        }
        return false;
    }

    public boolean inConflictWith(Collection<ConflictArea> other) {
        for(ConflictArea area : conflictAreas) {
            if(other.contains(area)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsArea(ConflictArea area) {
        return conflictAreas.contains(area);
    }

    public Collection<ConflictArea> getConflictAreasExclusivelyBefore(TrackPoint1D point) {
        Collection<ConflictArea> before = new HashSet<ConflictArea>(conflictAreas);
        
        boolean isAfter = false;
        
        for(ConflictTrajectoryPart part : parts) {
            if(!isAfter && part.getRange().contains(point)) {
                isAfter = true;
            }
            before.removeAll(part.getConflictAreas());
        }
        
        return before;
    }

    public Collection<ConflictArea> getConflictAreasExclusivelyAfter(TrackPoint1D point) {
        Collection<ConflictArea> after = new HashSet<ConflictArea>(conflictAreas);
        
        for(ConflictTrajectoryPart part : parts) {
            if(part.getRange().contains(point)) {
                break;
            }
            
            after.removeAll(part.getConflictAreas());
        }
        
        return after;
    }

    /**
     * Add a part to the trajectory
     * 
     * @param range the range of the part
     * @param conflictAreas the conflicts in the part
     */
    public void addPart(TrackRange1D range, Collection<ConflictArea> conflictAreas) {
        parts.add(new ConflictTrajectoryPart(range, conflictAreas));
        this.conflictAreas.addAll(conflictAreas);
        length += range.getLength();
        
        // clear cache on write
        trackMapArea = null;
    }

    @Override
    public Iterator<ConflictTrajectoryPart> iterator() {
        return parts.iterator();
    }

    public boolean isEmpty() {
        return parts.isEmpty();
    }

    public Collection<ConflictArea> getConflictAreas() {
        return conflictAreas;
    }
    
    public boolean hasStrongConflicts() {
        for(ConflictTrajectoryPart part : parts) {
            if(part.hasStrongConflict()) {
                return true;
            }
        }
        return false;
    }
    
    public TrackRangeSequence asTrackRangeSequence(VehicleTrackMap trackMap) {
        List<TrackRange1D> ranges = new ArrayList<TrackRange1D>();
        TrackRange1D pendingRange = null;
        
        for(ConflictTrajectoryPart part : parts) {
            TrackRange1D range = part.getRange();
            
            if(pendingRange != null) {
                if(pendingRange.intersects(range)) {
                    // Defer adding a range
                    pendingRange = pendingRange.merge(range);
                    continue;
                } else {
                    // Pending range is finished
                    ranges.add(pendingRange);
                    pendingRange = null;
                }
            } 
            
            // Start a new pendingRange if the
            // existing pendingRange is null or finished
            pendingRange = range;
        }
        
        if(pendingRange != null) {
            ranges.add(pendingRange);
        }

        return new TrackRangeSequence(trackMap, ranges);
    }

    public TrackMapArea1D asTrackMapArea(VehicleTrackMap trackMap) {
        if(trackMapArea != null) {
            return trackMapArea;
        }
        
        TrackMapArea1D area = new TrackMapArea1D(trackMap);
        
        for(ConflictArea conflictArea : conflictAreas) {
            for(TrackRange1D range : conflictArea.getRanges()) {
                area.add(range);
            }
        }
        
        return trackMapArea = area;
    }

    public boolean hasConflictAreasAfter(TrackPoint1D point) {
        boolean isAfter = false;
        
        for(ConflictTrajectoryPart part : parts) {
            if(part.getRange().contains(point)) {
                isAfter = true;
            }
            if(isAfter && !part.getConflictAreas().isEmpty()) {
                return true;
            }
        }
        
        return false;
    }

    public Collection<ConflictArea> getConflictAreasAfter(TrackPoint1D point) {
        Collection<ConflictArea> after = new HashSet<ConflictArea>();
        
        boolean isAfter = false;
        
        for(ConflictTrajectoryPart part : parts) {
            if(part.getRange().contains(point)) {
                isAfter = true;
            }
            if(isAfter) {
                after.addAll(part.getConflictAreas());
            }
        }
        
        return after;
    }

    public boolean contains(TrackPoint1D point) {
        for(ConflictTrajectoryPart part : parts) {
            if(part.getRange().contains(point)) {
                return true;
            }
        }
        return false;
    }

    public boolean isConflictAreaAfter(
            ConflictArea conflictArea,
            TrackPoint1D position) {
        boolean isAfter = false;
        
        for(ConflictTrajectoryPart part : parts) {
            if(part.getRange().contains(position)) {
                isAfter = true;
            }
            if(isAfter && part.containsConflictArea(conflictArea)) {
                return true;
            }
        }
        
        return false;
    }

    public boolean intersects(TrackRangeSequence ranges) {
        for(ConflictTrajectoryPart part : parts) {
            if(ranges.intersects(part.getRange())) {
                return true;
            }
        }
        return false;
    }

    public ConflictTrajectoryPart getFirstPart() {
        return parts.getFirst();
    }

    public double getConflictFreeStartSpace() {
        double startSpace = 0.0;
        
        for(ConflictTrajectoryPart part : parts) {
            if(!part.getConflictAreas().isEmpty()) {
                break;
            }
            startSpace = part.getLength();
        }
        
        return startSpace;
    }

    public void removeStartSpace(double spaceToRemove) {
        Iterator<ConflictTrajectoryPart> it = parts.iterator();
        
        while(it.hasNext() && spaceToRemove > 0) {
            ConflictTrajectoryPart part = it.next();
            
            if(part.hasConflicts()) {
                break;
            }
            
            if(part.getLength() < spaceToRemove) {
                it.remove();
                spaceToRemove -= part.getLength();
            } else {
                part.getRange().setStart(part.getStart() + spaceToRemove);
                break;
            }
        }
    }

    public boolean containsConflictArea(ConflictArea conflictArea) {
        return conflictAreas.contains(conflictArea);
    }

    public double getLength() {
        return length;
    }

    public int getNumConflictAreas() {
        return conflictAreas.size();
    }
    
    public int getNumParts() {
        return parts.size();
    }


}
