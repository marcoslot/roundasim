/**
 * 
 */
package dsg.rounda.services.roadmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dsg.rounda.geometry.Direction;
import dsg.rounda.model.Track;

/**
 * Road map area
 */
public class TrackMapArea1D {

    final VehicleTrackMap trackMap;
    final Map<Integer,TrackArea1D> areas;

    /**
     * 
     */
    public TrackMapArea1D(VehicleTrackMap roadMap) {
        this.trackMap = roadMap;
        this.areas = new HashMap<Integer,TrackArea1D>();
    }

    public TrackMapArea1D(TrackMapArea1D area) {
        this(area.trackMap);
        
        for(Map.Entry<Integer,TrackArea1D> entry : area.areas.entrySet()) {
            areas.put(entry.getKey(), new TrackArea1D(entry.getValue()));
        }
    }

    public void add(TrackMapArea1D other) {
        for(TrackArea1D area : other.areas.values()) {
            add(area);
        }
    }

    public void add(TrackArea1D trackArea1D) {
        TrackArea1D area = areas.get(trackArea1D.getTrackID());

        if(area == null) {
            areas.put(trackArea1D.getTrackID(), trackArea1D);
            return;
        } 

        area.add(trackArea1D);
    }

    public void add(TrackRange1D trackRange1D) {
        TrackArea1D area = areas.get(trackRange1D.getTrackID());

        if(area == null) {
            Track track = trackMap.getRoad(trackRange1D.getTrackID());
            area = new TrackArea1D(track.getId(), track.getPathLength());
            areas.put(track.getId(), area);
        } 

        area.add(trackRange1D);
    }

    public void remove(TrackMapArea1D other) {
        for(TrackArea1D area : other.areas.values()) {
            remove(area);
        }
    }

    public void remove(TrackArea1D trackArea1D) {
        TrackArea1D area = areas.get(trackArea1D.getTrackID());

        if(area == null) {
            return;
        }

        area.remove(trackArea1D);

        if(area.isEmpty()) {
            areas.remove(area.getTrackID());
        }
    }

    public void remove(TrackRange1D trackRange1D) {
        TrackArea1D area = areas.get(trackRange1D.getTrackID());

        if(area == null) {
            return;
        }

        area.remove(trackRange1D);

        if(area.isEmpty()) {
            areas.remove(area.getTrackID());
        }
    }

    public boolean contains(TrackPoint1D point) {
        TrackArea1D area = areas.get(point.getTrackID());

        if(area == null) {
            return false;
        }

        return area.contains(point);
    }

    public boolean contains(TrackRange1D range) {
        TrackArea1D area = areas.get(range.getTrackID());

        if(area == null) {
            return false;
        }

        return area.contains(range);
    }

    public boolean contains(TrackArea1D testArea) {
        TrackArea1D area = areas.get(testArea.getTrackID());

        if(area == null) {
            return false;
        }

        return area.contains(testArea);
    }

    public boolean contains(TrackMapArea1D testMapArea) {
        if(areas.isEmpty()) {
            return false;
        }
        
        for(TrackArea1D testArea : testMapArea.areas.values()) {
            if(!contains(testArea)) {
                return false;
            }
        }
        return true;
    }

    public void decay(double delta, int flags) {
        decay(delta, new DecayFlags(flags));
    }

    public void decay(double amount, DecayFlags flags) {
        if(amount <= 0.0) {
            return;
        }

        List<TrackBoundary1D> boundaries = toBoundariesList();
        
        for(TrackBoundary1D boundary : boundaries) {
            decayFromBoundary(boundary, amount, flags);
        }
    }

    public void decayFromBoundary(
            TrackBoundary1D boundary, 
            double decay,
            DecayFlags flags) {
        ConnectorGraph connGraph = trackMap.getConnectorGraph();
        MapEdge boundaryEdge = connGraph.getEdge(boundary);
        decayFromBoundary(boundary, boundaryEdge, decay, flags);
    }
    private void decayFromBoundary(
            TrackBoundary1D boundary, 
            MapEdge boundaryEdge,
            double decay,
            DecayFlags flags) {

        Track track = trackMap.getRoad(boundary.getTrackID());
        TrackArea1D trackArea = areas.get(boundary.getTrackID());
        
        if(trackArea == null) {
            trackArea = new TrackArea1D(track.getId(), track.getPathLength());
            areas.put(track.getId(), trackArea);
        }
        
        double edgeStart = boundaryEdge.getStart();
        double edgeEnd = boundaryEdge.getEnd();
        
        boolean forwardDecay  = boundary.getInclusive() == Direction.FORWARD && flags.is(DecayFlags.SHRINK)
                             || boundary.getInclusive() == Direction.BACKWARD && flags.is(DecayFlags.GROW);
        boolean backwardDecay = boundary.getInclusive() == Direction.BACKWARD && flags.is(DecayFlags.SHRINK)
                             || boundary.getInclusive() == Direction.FORWARD && flags.is(DecayFlags.GROW);
        
        if(flags.is(DecayFlags.CONSIDER_DIRECTION)) {
            if((forwardDecay  && flags.is(DecayFlags.GROW))
            || (backwardDecay && flags.is(DecayFlags.SHRINK))) {
                // If considering direction
                // Cannot do forward growth or backwards shrinking
                return;
            }
        }

        double distance = flags.is(DecayFlags.TIME) ? decay * track.getMaxSpeed() : decay;
        double remainingDistance = distance;
        
        boolean doSegmentDecay =
                 (forwardDecay && boundary.getOffset() < edgeEnd)
              || (backwardDecay && boundary.getOffset() > edgeStart);


        if (doSegmentDecay) {
            double start;
            double end;
            
            if (forwardDecay) {
                // Decay in the direction of the edge 
                start = boundary.getOffset();

                if (start + distance <= edgeEnd) {
                    end = start + distance;
                    remainingDistance = 0.0;
                } else {
                    end = edgeEnd;
                    remainingDistance = distance - (end - start);
                }
            } else {
                // Decay in the opposite direction of the edge
                end = boundary.getOffset();

                if (end - distance >= edgeStart) {
                    start = end - distance;
                    remainingDistance = 0.0;
                } else {
                    start = edgeStart;
                    remainingDistance = distance - (end - start);
                }
            }

            // The actual decay
            if (flags.is(DecayFlags.SHRINK)) {
                trackArea.remove(start, end);

                if (trackArea.isEmpty()) {
                    areas.remove(boundary.getTrackID());
                }
            } else {
                trackArea.add(start, end);
            }
        }
        
        if (remainingDistance > 0) {
            double remainingDecay = flags.is(DecayFlags.TIME) ? decay * remainingDistance / distance : remainingDistance;

            if (forwardDecay) {
                decayFromConnector(boundaryEdge.getTo(), remainingDecay, flags);
            } else {
                decayFromConnector(boundaryEdge.getFrom(), remainingDecay, flags);
            }
        }
    }

    public void decayFromConnector(MapNode mapNode, double dt, DecayFlags flags) {
        for(MapEdge edge : mapNode.getEdges()) {
            TrackBoundary1D boundary = null;
            
            if (mapNode == edge.getFrom()) {
                if (flags.is(DecayFlags.SHRINK)) {
                    boundary = new TrackBoundary1D(
                            edge.getTrackID(), 
                            edge.getStart(), 
                            Direction.FORWARD
                    );
                } else if(!flags.is(DecayFlags.CONSIDER_DIRECTION)) {
                    boundary = new TrackBoundary1D(
                            edge.getTrackID(), 
                            edge.getStart(), 
                            Direction.BACKWARD
                    );
                }
            } else { // edge.getTo() == mapNode  
                if (flags.is(DecayFlags.GROW)) {
                    boundary = new TrackBoundary1D(
                            edge.getTrackID(), 
                            edge.getEnd(), 
                            Direction.FORWARD
                    );
                } else if(!flags.is(DecayFlags.CONSIDER_DIRECTION)) {
                    boundary = new TrackBoundary1D(
                            edge.getTrackID(), 
                            edge.getEnd(), 
                            Direction.BACKWARD
                    );
                }
            } 

            if(boundary != null) {
                decayFromBoundary(boundary, edge, dt, flags);
            }
        }
    }

    public TrackMapBoundaries1D toBoundaries() {
        return new TrackMapBoundaries1D(trackMap, toBoundariesList());
    }

    public List<TrackBoundary1D> toBoundariesList() {
        List<TrackBoundary1D> boundaries = new ArrayList<TrackBoundary1D>();
        ConnectorGraph connGraph = trackMap.getConnectorGraph();

        for(TrackArea1D area : areas.values()) {
            Track track = trackMap.getRoad(area.getTrackID());
            List<TrackRange1D> ranges = area.getRanges();

            for(TrackRange1D range : ranges) {
                if(range.getStart() > 0.0) {
                    boundaries.add(new TrackBoundary1D(track.getId(), range.getStart(), Direction.FORWARD));
                } 

                if(range.getEnd() < track.getPathLength()) {
                    boundaries.add(new TrackBoundary1D(track.getId(), range.getEnd(), Direction.BACKWARD));
                }

                Collection<MapNode> nodes = connGraph.getNodes(track.getId(), range.getStart(), range.getEnd());

                for(MapNode node : nodes) {
                	if(node.isStart()) {
                        MapEdge fromEdge = node.getFromEdge(track.getId());
                        boundaries.add(new TrackBoundary1D(track.getId(), fromEdge.getStart(), Direction.FORWARD));
                	} else if(hasOpenEdges(track.getId(), node, boundaries)) {
                        // Need to insert boundaries around the node

                        MapEdge toEdge = node.getToEdge(track.getId());
                        
                        if(toEdge != null) {
                            boundaries.add(new TrackBoundary1D(track.getId(), toEdge.getEnd(), Direction.BACKWARD));
                        }
                        
                        MapEdge fromEdge = node.getFromEdge(track.getId());
                        
                        if(fromEdge != null) {
                            boundaries.add(new TrackBoundary1D(track.getId(), fromEdge.getStart(), Direction.FORWARD));
                        }
                        
                        
                    }
                }
            }
        }
        return boundaries;
    }

    private boolean hasOpenEdges(
            int currentTrackID,
            MapNode node,
            List<TrackBoundary1D> boundaries) {
        for(MapEdge edge : node.getFrom()) {
            if(edge.getTrackID() == currentTrackID) {
                continue;
            }
            if(contains(edge.getTrackID(), edge.getStart())) {
                continue;
            }
            return true;
        }
        for(MapEdge edge : node.getTo()) {
            if(edge.getTrackID() == currentTrackID) {
                continue;
            }
            if(contains(edge.getTrackID(), edge.getEnd())) {
                continue;
            }
            return true;
        }
        
        return false;
    }

    private boolean contains(int trackID, double offset) {
        TrackArea1D area = areas.get(trackID);

        if(area == null) {
            return false;
        }

        return area.contains(offset);
    }

    public boolean containsStart(int trackID) {
        TrackArea1D area = areas.get(trackID);

        if(area == null) {
            return false;
        }

        return area.containsStart();
    }

    public boolean containsEnd(int trackID) {
        TrackArea1D area = areas.get(trackID);

        if(area == null) {
            return false;
        }

        return area.containsEnd();
    }

    public boolean intersects(TrackMapArea1D other) {
        for(TrackArea1D area : areas.values()) {
            TrackArea1D otherArea = other.getArea(area.getTrackID());
            
            if(otherArea == null) {
                continue;
            }
            
            if(area.intersects(otherArea)) {
                return true;
            }
        }
        
        return false;
    }

    public TrackArea1D getArea(int trackID) {
        return areas.get(trackID);
    }

    public Collection<TrackArea1D> getAreas() {
        return areas.values();
    }

    public TrackRange1D getRange(TrackPoint1D position) {
        TrackArea1D area = getArea(position.getTrackID());
        
        if(area == null) {
            return null;
        }
        
        return area.getRangeContaining(position);
    }

    public boolean intersects(TrackRange1D range) {
        TrackArea1D area = getArea(range.getTrackID());
        
        if(area == null) {
            return false;
        }
        
        return area.intersects(range);
    }

    public boolean intersects(TrackRangeSequence ranges) {
        for(TrackRange1D range : ranges) {
            if(intersects(range)) {
                return true;
            }
        }
        return false;
    }

    public void clear() {
        this.areas.clear();
    }
    
    public double getTotalLength() {
        double length = 0.0;
        
        for(TrackArea1D area : areas.values()) {
            length += area.getTotalLength();
        }
        
        return length;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        boolean isFirst = true;
        
        sb.append("area [");
        
        for(TrackArea1D area : areas.values()) {
            if(isFirst) {
                isFirst = false;
            } else {
                sb.append(';');
            }
            
            sb.append(area);
        }

        sb.append("]");
        
        return sb.toString();
    }

    public TrackMapArea1D computeIntersection(TrackMapArea1D other) {
        TrackMapArea1D intersection = new TrackMapArea1D(trackMap);
        
        for(TrackArea1D area : areas.values()) {
            TrackArea1D otherArea = other.getArea(area.getTrackID());
            
            if(otherArea == null) {
                continue;
            }
            
            intersection.add(area.computeIntersection(otherArea));
        }
        
        return intersection;
    }
    
    public Set<Integer> getTrackIDs() {
    	return areas.keySet();
    }
    

}
