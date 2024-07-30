/**
 * 
 */
package dsg.rounda.services.coordination;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Polygon;

import dsg.rounda.model.Connector;
import dsg.rounda.model.Position1D;
import dsg.rounda.model.Track;
import dsg.rounda.model.TrackType;
import dsg.rounda.model.Trajectory1D;
import dsg.rounda.model.VehicleRouter;
import dsg.rounda.services.roadmap.TrackMap;
import dsg.rounda.services.roadmap.TrackMapArea1D;
import dsg.rounda.services.roadmap.TrackPoint1D;
import dsg.rounda.services.roadmap.TrackRange1D;
import dsg.rounda.services.roadmap.TrackRangeSequence;

/**
 * This tool is used by controllers to find potential conflicts
 * with a planned trajectory
 */
public class ConflictAreaFinder {

    final TrackMap trackMap;
    final Map<Integer,Collection<ConflictArea>> trackConflicts; 
    final Map<Integer,ConflictArea> conflictTable; 
    final VehicleRouter router;
    
    int newConflictID;
    /**
     * 
     */
    public ConflictAreaFinder(TrackMap trackMap) {
        this.trackConflicts = new HashMap<Integer,Collection<ConflictArea>>();
        this.conflictTable = new HashMap<Integer,ConflictArea>();
        this.trackMap = trackMap;
        this.router = new VehicleRouter(trackMap);
        this.newConflictID = 0;
        init();
    }
     
    private void init() {
        List<Track> tracks = trackMap.getRoads();
        
        trackConflicts.clear();
        newConflictID = 0;
        
        for(int a = 0; a < tracks.size()-1; a++) {
            Track baseTrack = tracks.get(a);
            
            for(int b = a+1; b < tracks.size(); b++) {
                Track otherTrack = tracks.get(b);

                if(baseTrack.getFrom() != null && otherTrack.getTo() != null
                && baseTrack.getFrom().getRoad() == otherTrack.getId()
                && otherTrack.getTo().getRoad() == baseTrack.getId()) {
                    // Tracks connect at tips
                    continue;
                }

                if(baseTrack.getTo() != null && otherTrack.getFrom() != null
                && baseTrack.getTo().getRoad() == otherTrack.getId()
                && otherTrack.getFrom().getRoad() == baseTrack.getId()) {
                    // Tracks connect at tips
                    continue;
                }
                
                findConflicts(baseTrack, otherTrack);
            }
        }
    }
    
    private Collection<ConflictArea> getOrCreateConflictSet(int trackID) {
        Collection<ConflictArea> conflictSet = trackConflicts.get(trackID);
        
        if(conflictSet == null) {
            conflictSet = new ArrayList<ConflictArea>();
            trackConflicts.put(trackID, conflictSet);
        }
        
        return conflictSet;
    }

    private void findConflicts(Track baseTrack, Track otherTrack) {
        Collection<ConflictArea> baseTrackConflicts = getOrCreateConflictSet(baseTrack.getId());
        Collection<ConflictArea> otherTrackConflicts = getOrCreateConflictSet(otherTrack.getId());
        
        Geometry baseConflictGeometry = otherTrack.getArea().intersection(baseTrack.getPath());
        
        // Treat these as independent conflicts
        List<LineString> baseConflictPaths = getLineStrings(baseConflictGeometry);
        
        boolean baseStartsOnOther = baseTrack.getFrom() != null && baseTrack.getFrom().getRoad() == otherTrack.getId();
        boolean otherStartsOnBase = otherTrack.getFrom() != null && otherTrack.getFrom().getRoad() == baseTrack.getId();
        boolean sharedStart = baseTrack.getFrom() != null && otherTrack.getFrom() != null &&
                              baseTrack.getFrom().getRoad() == otherTrack.getFrom().getRoad();
        
        for(LineString baseConflictPath : baseConflictPaths) {
            TrackRange1D baseConflictRange = toRange(baseTrack, baseConflictPath);
            Polygon baseRangeArea = (Polygon) baseConflictPath.buffer(baseTrack.getLaneWidth()*0.5);
            Geometry nearConflictGeometry = baseRangeArea.intersection(otherTrack.getPath());
            
            // Treat these as part of the same conflict
            // (there really shouldn't be more than one)
            List<LineString> nearConflictPaths = getLineStrings(nearConflictGeometry);
            
            if(nearConflictPaths.isEmpty()) {
                continue;
            }
            
            for(LineString nearConflictPath : nearConflictPaths) {
                TrackRange1D nearConflictRange = toRange(otherTrack, nearConflictPath);

                boolean nearIncludesStart = nearConflictRange.getStart() <= 0.0001; // 0.1 mm
                boolean nearStartsOnBase = otherStartsOnBase && nearIncludesStart;
                boolean baseStartsOnNear = baseStartsOnOther && nearConflictRange.contains(baseTrack.getFrom().getOffset());
                boolean isWeak = nearStartsOnBase || sharedStart || baseStartsOnNear; 

                ConflictRange.Type baseConflictType = isWeak ? ConflictRange.Type.WEAK : ConflictRange.Type.STRONG;
                ConflictRange.Type nearConflictType = isWeak ? ConflictRange.Type.WEAK : ConflictRange.Type.STRONG;
                
                // Create the conflict area
                ConflictArea conflict = new ConflictArea(
                        newConflictID++, 
                        new ConflictRange(baseConflictType, baseConflictRange), 
                        new ConflictRange(nearConflictType, nearConflictRange));
                
                baseTrackConflicts.add(conflict);
                otherTrackConflicts.add(conflict);
                conflictTable.put(conflict.getId(), conflict);
            }
            
        }
    }
    
    private List<LineString> getLineStrings(Geometry conflictGeometry) {
        List<LineString> ranges = new ArrayList<LineString>();
        
        if(conflictGeometry instanceof LineString) {
            LineString path = (LineString) conflictGeometry;
            ranges.add(path);
        } else if(conflictGeometry instanceof MultiLineString) {
            MultiLineString paths = (MultiLineString) conflictGeometry;

            for(int i = 0, numPaths = paths.getNumGeometries(); i < numPaths; i++) {
                LineString path = (LineString) paths.getGeometryN(i);
                ranges.add(path);
            }
        } 
        
        return ranges;
    }
    
    private TrackRange1D toRange(Track track, LineString lineString) {
        double startOffset = track.findOffset(lineString.getCoordinateN(0));
        double endOffset = track.findOffset(lineString.getCoordinateN(lineString.getNumPoints()-1));
        return new TrackRange1D(track.getId(), startOffset, endOffset);
    }
    
    public ConflictArea getConflict(int conflictID) {
        return conflictTable.get(conflictID);
    }

    public List<ConflictArea> findConflicts(TrackRangeSequence ranges) {
        List<ConflictArea> result = new ArrayList<ConflictArea>();
        
        for(TrackRange1D range : ranges) {
            Collection<ConflictArea> conflicts = trackConflicts.get(range.getTrackID());
            
            for(ConflictArea conflict : conflicts) {
                if(conflict.intersects(range)) {
                    result.add(conflict);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Find a conflict trajectory for the current path of the vehicle
     * with at least minOpenSpaceLength meters of open space available
     * at the end of it
     * 
     * @param vehiclePosition current position of the vehicle
     * @param vehicleTrajectory current trajectory of the vehicle
     * @param desiredEndSpaceLength minimum required open space
     * @return the conflict trajectory
     */
    public ConflictTrajectory findStrongConflictTrajectory(
            TrackPoint1D vehiclePoint,
            TrackRangeSequence rangeSequence,
            double desiredEndSpaceLength,
            double maxStartSpaceLength,
            double maxTotalDistance) {
        ConflictTrajectory result = new ConflictTrajectory();
        
        boolean passedConflict = false;
        double openSpace = 0.0;
        
        for(TrackRange1D currentRange : rangeSequence) {
            int trackID = currentRange.getTrackID();
            Track currentTrack = trackMap.getRoad(trackID);
            
            // Sort conflicts by start offset on the range track
            Queue<ConflictArea> startSorted = getStartSortedStrongConflictAreas(currentRange);

            // Sort conflicts by end offset on the range track
            Queue<ConflictArea> endSorted = getEndSortedStrongConflictAreas(currentRange);

            // Find the conflict areas that we overlap with from the start
            Set<ConflictArea> conflictSet = getStartingStrongConflictAreas(currentRange);

            if(!conflictSet.isEmpty()) {
                passedConflict = true;
            }

            double startOfCurrentRange = currentRange.getStart();

            EncounterType encounter = null;
            
            do {
                boolean hasConflict = !conflictSet.isEmpty();
                
                ConflictArea startMin = startSorted.peek();
                ConflictArea endMin = endSorted.peek();

                encounter = getEncounterType(trackID, startMin, endMin);
                
                double partRangeLength;
                TrackRange1D changeRange;
                
                if(encounter == EncounterType.START_OF_CONFLICT_RANGE) {
                    // a new conflict range is encountered
                    startSorted.remove();
                    
                    changeRange = startMin.getRange(trackID);
                    partRangeLength = changeRange.getStart() - startOfCurrentRange;
                } else if(encounter == EncounterType.END_OF_CONFLICT_RANGE) {
                    // a current conflict range is ending
                    endSorted.remove();
                    
                    changeRange = endMin.getRange(trackID);
                    
                    if(changeRange.getEnd() >= currentRange.getEnd()) {
                        // Add everything that's still in the set
                        break;
                    }

                    partRangeLength = changeRange.getEnd() - startOfCurrentRange;
                } else {
                    changeRange = null;
                    partRangeLength = currentRange.getEnd() - startOfCurrentRange;
                }

                boolean reachedStartSpaceLimit = !passedConflict && !hasConflict && openSpace + partRangeLength >= maxStartSpaceLength;
                //boolean reachedDistanceLimit = distance + partRangeLength >= maxTotalDistance;
                boolean reachedEndSpaceLimit = passedConflict && !hasConflict && currentTrack.getType() != TrackType.LANE_CHANGE && openSpace + partRangeLength >= desiredEndSpaceLength;
                boolean reachedLimit = reachedEndSpaceLimit || reachedStartSpaceLimit;// || reachedDistanceLimit;
                
                double rangeEnd;
                    
                if(reachedStartSpaceLimit) {
                    // reached the maximum amount of open space before a conflict
                    rangeEnd = startOfCurrentRange + maxStartSpaceLength - openSpace;
                } else if(reachedEndSpaceLimit) {
                    // reached the maximum amount of open space after a conflict
                    rangeEnd = startOfCurrentRange + desiredEndSpaceLength - openSpace;
                } else /*if(reachedDistanceLimit) {
                    // reached the maximum distance
                    rangeEnd = startOfCurrentRange + maxTotalDistance - distance;
                } else */if(encounter == EncounterType.START_OF_CONFLICT_RANGE) {
                    // range ends where startMin starts
                    rangeEnd = changeRange.getStart();
                } else if(encounter == EncounterType.END_OF_CONFLICT_RANGE) {
                    // range ends where endMin ends
                    rangeEnd = changeRange.getEnd();
                } else {
                    // reaching end of current range
                    rangeEnd = currentRange.getEnd();
                }
                
                TrackRange1D partRange = new TrackRange1D(trackID, startOfCurrentRange, rangeEnd);
                
                result.addPart(partRange, new ArrayList<ConflictArea>(conflictSet));

                if(reachedLimit) {
                    return result;
                }

                if(hasConflict || currentTrack.getType() == TrackType.LANE_CHANGE) {
                    // Current range has one or more conflicts, reset the openSpace
                    openSpace = 0.0;
                } else {
                    // This range added to the amount of open space
                    openSpace += partRangeLength;
                }

                if(encounter == EncounterType.START_OF_CONFLICT_RANGE) {
                    // Start a new range
                    startOfCurrentRange = changeRange.getStart();
                    
                    // From this point onwards, startMin is an active conflict
                    conflictSet.add(startMin);
                    
                    // if this is the first conflict range encountered,
                    // we have now passed a conflict
                    passedConflict = true;
                    
                } else if(encounter == EncounterType.END_OF_CONFLICT_RANGE) {
                    // Start a new range
                    startOfCurrentRange = changeRange.getEnd();

                    // endMin is no longer one of the conflicts for the remainder
                    conflictSet.remove(endMin);
                }
            } while(encounter != EncounterType.END_OF_CURRENT_RANGE);
        }
        
        return result;
    }

    enum EncounterType {
        START_OF_CONFLICT_RANGE,
        END_OF_CONFLICT_RANGE,
        END_OF_CURRENT_RANGE
    }

    private EncounterType getEncounterType(int trackID, ConflictArea startMin, ConflictArea endMin) {
        if(startMin != null && endMin != null) {
            if(startMin.getStart(trackID) <= endMin.getEnd(trackID)) {
                // there are starts and ends on the current range, but the first start is smaller
                return EncounterType.START_OF_CONFLICT_RANGE;
            } else {
                // there are starts and ends on the current range, but the first end is smaller
                return EncounterType.END_OF_CONFLICT_RANGE;
            }
        } else if(startMin != null) {
            // there are no more conflict end on the current range, but there still is a start
            return EncounterType.START_OF_CONFLICT_RANGE;
        } else if(endMin != null) {
            // there are no more conflict starts on the current range, but there still is an end
            return EncounterType.END_OF_CONFLICT_RANGE;
        } else {
            // no more conflict range starts or ends before the end of the current range
            return EncounterType.END_OF_CURRENT_RANGE;
        }
    }

    public double findStrongConflictDistance(
            TrackRangeSequence vehicleTrajectory) {
        double openSpace = 0.00;
        
        for(TrackRange1D currentRange : vehicleTrajectory) {
            // Find the conflict areas that we overlap with from the start
            Set<ConflictArea> baseSet = getStartingStrongConflictAreas(currentRange);

            if(!baseSet.isEmpty()) {
                return openSpace;
            }

            // Sort conflicts by start offset on the range track
            Queue<ConflictArea> startSorted = getStartSortedStrongConflictAreas(currentRange);

            if(!startSorted.isEmpty()) {
                return openSpace + startSorted.remove().getStart(currentRange.getTrackID()) - currentRange.getStart();
            }
            
            openSpace += currentRange.getLength();
        }
        
        return Double.POSITIVE_INFINITY;
    }
    
    private Queue<ConflictArea> getStartSortedConflictAreas(
            TrackRange1D currentRange) {
        
        final int trackID = currentRange.getTrackID();
        final Collection<ConflictArea> conflicts = trackConflicts.get(trackID);
        
        if(conflicts == null || conflicts.isEmpty()) {
            return new LinkedList<ConflictArea>();
        }
        
        Queue<ConflictArea> startSorted = createStartSortedConflictAreaQueue(trackID, conflicts.size());
        
        // Only include intersecting conflicts
        for(ConflictArea conflict : conflicts) {
            if(conflict.getRange(trackID).getStart() >= currentRange.getStart()
            && conflict.getRange(trackID).getStart() < currentRange.getEnd()) {
                startSorted.add(conflict);
            }
        };
        
        return startSorted;
    }

    private Queue<ConflictArea> getStartSortedStrongConflictAreas(
            TrackRange1D currentRange) {
        
        final int trackID = currentRange.getTrackID();
        final Collection<ConflictArea> conflicts = trackConflicts.get(trackID);
        
        if(conflicts == null || conflicts.isEmpty()) {
            return new LinkedList<ConflictArea>();
        }
        
        Queue<ConflictArea> startSorted = createStartSortedConflictAreaQueue(trackID, conflicts.size());
        
        // Only include intersecting conflicts
        for(ConflictArea conflict : conflicts) {
            if(conflict.getRange(trackID).getStart() >= currentRange.getStart()
            && conflict.getRange(trackID).getStart() < currentRange.getEnd()
            && conflict.getConflictRange(trackID).isStrong()) {
                startSorted.add(conflict);
            }
        };
        
        return startSorted;
    }

    private Queue<ConflictArea> createStartSortedConflictAreaQueue(
            final int trackID, 
            final int initialCapacity) {
        return new PriorityQueue<ConflictArea>(initialCapacity, new Comparator<ConflictArea>() {
            @Override
            public int compare(ConflictArea left, ConflictArea right) {
                return Double.compare(left.getStart(trackID), right.getStart(trackID));
            } 
        });
    }

    private Queue<ConflictArea> createEndSortedConflictAreaQueue(
            final int trackID, 
            final int initialCapacity) {
        return new PriorityQueue<ConflictArea>(initialCapacity, new Comparator<ConflictArea>() {
            @Override
            public int compare(ConflictArea left, ConflictArea right) {
                return Double.compare(left.getEnd(trackID), right.getEnd(trackID));
            }
        });
    }

    private Queue<ConflictArea> getEndSortedConflictAreas(
            TrackRange1D currentRange) {
        
        final int trackID = currentRange.getTrackID();
        final Collection<ConflictArea> conflicts = trackConflicts.get(trackID);

        if(conflicts == null || conflicts.isEmpty()) {
            return new LinkedList<ConflictArea>();
        }
        
        Queue<ConflictArea> endSorted = createEndSortedConflictAreaQueue(trackID, conflicts.size());

        // Only include intersecting conflicts
        for(ConflictArea conflict : conflicts) {
            if(conflict.getRange(trackID).getEnd() >= currentRange.getStart()
            && conflict.getRange(trackID).getEnd() < currentRange.getEnd()) {
                endSorted.add(conflict);
            }
        }
        return endSorted;
    }

    private Queue<ConflictArea> getEndSortedStrongConflictAreas(
            TrackRange1D currentRange) {
        
        final int trackID = currentRange.getTrackID();
        final Collection<ConflictArea> conflicts = trackConflicts.get(trackID);

        if(conflicts == null || conflicts.isEmpty()) {
            return new LinkedList<ConflictArea>();
        }
        
        Queue<ConflictArea> endSorted = createEndSortedConflictAreaQueue(trackID, conflicts.size());

        // Only include intersecting conflicts
        for(ConflictArea conflict : conflicts) {
            if(conflict.getRange(trackID).getEnd() >= currentRange.getStart()
            && conflict.getRange(trackID).getEnd() < currentRange.getEnd()
            && conflict.getConflictRange(trackID).isStrong()) {
                endSorted.add(conflict);
            }
        }
        return endSorted;
    }

    private Set<ConflictArea> getStartingConflictAreas(
            TrackRange1D currentRange) {
        final int trackID = currentRange.getTrackID();
        final Collection<ConflictArea> conflicts = trackConflicts.get(trackID);
        
        Set<ConflictArea> baseSet = new HashSet<ConflictArea>();

        if(conflicts == null) {
            return baseSet;
        }
        
        // Find the conflict areas that we overlap with from the start
        for(ConflictArea conflict : conflicts) {
            if(conflict.getStart(trackID) <= currentRange.getStart()
            && conflict.getEnd(trackID) > currentRange.getStart()) {
                baseSet.add(conflict);
            }
        }
        
        return baseSet;
    }

    private Set<ConflictArea> getStartingStrongConflictAreas(
            TrackRange1D currentRange) {
        final int trackID = currentRange.getTrackID();
        final Collection<ConflictArea> conflicts = trackConflicts.get(trackID);
        
        Set<ConflictArea> baseSet = new HashSet<ConflictArea>();

        if(conflicts == null) {
            return baseSet;
        }
        
        // Find the conflict areas that we overlap with from the start
        for(ConflictArea conflict : conflicts) {
            if(conflict.getStart(trackID) <= currentRange.getStart()
            && conflict.getEnd(trackID) > currentRange.getStart()
            && conflict.getConflictRange(trackID).isStrong()) {
                baseSet.add(conflict);
            }
        }
        
        return baseSet;
    }
    
    /**
     * Find a trajectory with conflict information from a
     * plain track range sequence
     * 
     * @param ranges the track range sequence
     * @return the conflict trajectory
     */
    public ConflictTrajectory findConflictTrajectory(TrackRangeSequence ranges) {
        ConflictTrajectory result = new ConflictTrajectory();
        
        for(TrackRange1D currentRange : ranges) {
            final int trackID = currentRange.getTrackID();

            // Sort conflicts by start offset on the range track
            Queue<ConflictArea> startSorted = getStartSortedConflictAreas(currentRange);

            // Sort conflicts by end offset on the range track
            Queue<ConflictArea> endSorted = getEndSortedConflictAreas(currentRange);

            // Find the conflict areas that we overlap with from the start
            Set<ConflictArea> baseSet = getStartingConflictAreas(currentRange);

            double startOfCurrentRange = currentRange.getStart();
            
            while(!startSorted.isEmpty() || !endSorted.isEmpty()) {
                ConflictArea startMin = startSorted.peek();
                ConflictArea endMin = endSorted.peek();

                EncounterType encounter = getEncounterType(trackID, startMin, endMin);
                
                if(encounter == EncounterType.START_OF_CONFLICT_RANGE) {
                    startSorted.remove();
                    
                    TrackRange1D startingRange = startMin.getRange(trackID);
                    
                    TrackRange1D partRange = new TrackRange1D(trackID, startOfCurrentRange, startingRange.getStart());
                    result.addPart(partRange, new ArrayList<ConflictArea>(baseSet));

                    // Start a new range
                    startOfCurrentRange = startingRange.getStart();
                    
                    // From this point onwards, start is one of the conflicts
                    baseSet.add(startMin);
                    
                } else {
                    endSorted.remove();
                    
                    TrackRange1D endingRange = endMin.getRange(trackID);
                    
                    if(endingRange.getEnd() >= currentRange.getEnd()) {
                        // Add everything that's still in the set
                        break;
                    }

                    TrackRange1D partRange = new TrackRange1D(trackID, startOfCurrentRange, endingRange.getEnd());
                    result.addPart(partRange, new ArrayList<ConflictArea>(baseSet));
                    
                    // Start a new range
                    startOfCurrentRange = endingRange.getEnd();

                    // endMin is no longer one of the conflicts for the remainder
                    baseSet.remove(endMin);
                }
                
            }
            
            TrackRange1D finalRange = new TrackRange1D(trackID, startOfCurrentRange, currentRange.getEnd());
            result.addPart(finalRange, new ArrayList<ConflictArea>(baseSet));

        }
        
        return result;
    }

    public ConflictTrajectory findWeakConflictTrajectory(TrackRangeSequence route, TrackMapArea1D emptiness) {
        ConflictTrajectory weakConflictTraj = new ConflictTrajectory();
        ConflictTrajectory strongConflictTraj = findConflictTrajectory(route);
        
        base:
        for(ConflictTrajectoryPart part : strongConflictTraj) {
            if(part.hasStrongConflictIn(route)) {
                break base;
            } else {
                // Only weak areas, make sure they are empty
                for(ConflictArea conflictArea : part.getConflictAreas()) {
                    for(TrackRange1D range : conflictArea.getRanges()) {
                        if(!emptiness.contains(range)) {
                            break base;
                        }
                    }
                }
            }
            weakConflictTraj.addPart(part.getRange(), part.getConflictAreas());
        }
        
        return weakConflictTraj;
    }

    public Collection<ConflictArea> getConflictsIn(TrackMapArea1D area) {
        Set<ConflictArea> conflictsInArea = new HashSet<ConflictArea>();
        
        for(ConflictArea conflictArea : conflictTable.values()) {
            for(TrackRange1D conflictRange : conflictArea.getRanges()) {
                if(area.intersects(conflictRange)) {
                    conflictsInArea.add(conflictArea);
                    break;
                }
            }
        }
        
        return conflictsInArea;
    }


}
