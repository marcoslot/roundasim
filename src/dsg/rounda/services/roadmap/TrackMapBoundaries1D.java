/**
 * 
 */
package dsg.rounda.services.roadmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dsg.rounda.geometry.Direction;

/**
 * A collection of road network boundaries
 */
public class TrackMapBoundaries1D {

    private final VehicleTrackMap trackMap;
    private final Map<MapEdge,List<TrackBoundary1D>> boundaryMap;
    private final List<TrackBoundary1D> allBoundaries;
    
    // cache
    private TrackMapArea1D area;

    public TrackMapBoundaries1D(VehicleTrackMap trackMap) {
        this(trackMap, new ArrayList<TrackBoundary1D>());
    }

    public TrackMapBoundaries1D(VehicleTrackMap trackMap, Collection<TrackBoundary1D> boundaries) {
        this.trackMap = trackMap;
        this.boundaryMap = new HashMap<MapEdge,List<TrackBoundary1D>>();
        this.allBoundaries = new ArrayList<TrackBoundary1D>(boundaries);
        
        ConnectorGraph connGraph = trackMap.getConnectorGraph();
        
        for(TrackBoundary1D boundary : boundaries) {
            MapEdge edge = connGraph.getEdge(boundary);
            List<TrackBoundary1D> trackBoundaries = boundaryMap.get(edge);
            
            if(trackBoundaries == null) {
                trackBoundaries = new ArrayList<TrackBoundary1D>();
                boundaryMap.put(edge, trackBoundaries);
            }
            
            trackBoundaries.add(boundary);
        }
        
        for(List<TrackBoundary1D> trackBoundaries : boundaryMap.values()) {
            Collections.sort(trackBoundaries);
        }
    }

    public TrackMapBoundaries1D(TrackMapBoundaries1D copy) {
        this.trackMap = copy.trackMap;
        this.boundaryMap = new HashMap<MapEdge,List<TrackBoundary1D>>(copy.boundaryMap);
        this.allBoundaries = new ArrayList<TrackBoundary1D>(copy.getBoundaries());
    }

    /**
     * @return the boundaries
     */
    public Collection<TrackBoundary1D> getBoundaries() {
        return allBoundaries;
    }
    
    public TrackMapArea1D toMapArea() {
        if(area != null) {
            return area;
        }
        
        area = new TrackMapArea1D(trackMap);
        
        if(allBoundaries.isEmpty()) {
            return area;
        } 


        LinkedList<BoundaryVisit> unvisited = new LinkedList<BoundaryVisit>();
        
        // This index always contains all boundaries to avoid expensive misses
        Map<TrackBoundary1D,BoundaryVisit> boundaryVisits = new HashMap<TrackBoundary1D,BoundaryVisit>();
        
        for(Map.Entry<MapEdge,List<TrackBoundary1D>> edgeBoundaries : boundaryMap.entrySet()) {
            for(TrackBoundary1D boundary : edgeBoundaries.getValue()) {
                BoundaryVisit visit = new BoundaryVisit(boundary, edgeBoundaries.getKey());
                unvisited.add(visit);
                boundaryVisits.put(boundary, visit);
            }
        }
        
        while(!unvisited.isEmpty()) {
            BoundaryVisit visit = unvisited.removeFirst();
            
            if(visit.visited) {
                continue;
            }
            
            visit.visited = true;
            
            MapEdge edge = visit.edge;
            List<TrackBoundary1D> boundariesOnEdge = boundaryMap.get(edge);
            
            if(boundariesOnEdge.size() > 1) {
                int boundaryIndex = boundariesOnEdge.indexOf(visit.boundary);

                if(boundaryIndex < boundariesOnEdge.size()-1 && visit.boundary.getInclusive() == Direction.FORWARD) {
                    // Found a boundary directly in front of this one
                    TrackBoundary1D otherBoundary = boundariesOnEdge.get(boundaryIndex+1);

                    if(otherBoundary.getInclusive() != Direction.BACKWARD) {
                        throw new IllegalStateException("Inconsistent boundaries");
                    }

                    // Mark other boundary as visited so we don't start a search from there
                    boundaryVisits.get(otherBoundary).visited = true;

                    area.add(new TrackRange1D(visit.boundary, otherBoundary));
                    continue;
                }

                if(boundaryIndex > 0 && visit.boundary.getInclusive() == Direction.BACKWARD) {
                    // Found a boundary directly behind this one
                    TrackBoundary1D otherBoundary = boundariesOnEdge.get(boundaryIndex-1);

                    if(otherBoundary.getInclusive() != Direction.FORWARD) {
                        throw new IllegalStateException("Inconsistent boundaries");
                    }

                    // Mark other boundary as visited so we don't start a search from there
                    boundaryVisits.get(otherBoundary).visited = true;

                    area.add(new TrackRange1D(otherBoundary, visit.boundary));
                    continue;
                }
            }
            
            // We've handled the simple cases of two adjacent boundaries
            // pointing to each other
            // now we need to search the graph

            // Nodes to visit
            LinkedList<MapNode> bfsQueue = new LinkedList<MapNode>();
            
            // Use a plain set to keep track of edge visits,
            // because we do not know which edges we will visit
            Set<MapNode> visited = new HashSet<MapNode>();

            int trackID = visit.boundary.getTrackID();

            if(visit.boundary.getInclusive() == Direction.FORWARD) {
                area.add(new TrackRange1D(trackID, visit.boundary.getOffset(), edge.getEnd()));
                bfsQueue.add(edge.getTo());
                visited.add(edge.getTo());
            } else if(visit.boundary.getInclusive() == Direction.BACKWARD) {
                area.add(new TrackRange1D(trackID, edge.getStart(), visit.boundary.getOffset()));
                bfsQueue.add(edge.getFrom());
                visited.add(edge.getFrom());
            } else {
                throw new RuntimeException("Shouldn't be here");
            }
            
            while(!bfsQueue.isEmpty()) {
                MapNode node = bfsQueue.removeFirst();
                
                for(MapEdge nodeEdge : node.getEdges()) {
                    if(nodeEdge == edge) {
                        continue;
                    }
                    
                    trackID = nodeEdge.getTrackID();
                    boundariesOnEdge = boundaryMap.get(nodeEdge);
                    
                    boolean fromStart = nodeEdge.getFrom() == node;
                    
                    if(boundariesOnEdge == null || boundariesOnEdge.isEmpty()) {
                        area.add(nodeEdge.getTrackRange());

                        MapNode otherNode = nodeEdge.other(node);
                        
                        if(visited.contains(otherNode)) {
                            continue;
                        }
                        
                        visited.add(otherNode);
                        bfsQueue.add(otherNode);
                    } else if(fromStart) {
                        TrackBoundary1D otherBoundary = boundariesOnEdge.get(0);
                        
                        if(otherBoundary.getInclusive() != Direction.BACKWARD) {
                            throw new IllegalStateException("Inconsistent boundaries");
                        }
                        
                        boundaryVisits.get(otherBoundary).visited = true;
                        
                        area.add(new TrackRange1D(trackID, nodeEdge.getStart(), otherBoundary.getOffset()));
                        // No node to push, reached a dead end from the start of a track
                    } else {
                        TrackBoundary1D otherBoundary = boundariesOnEdge.get(boundariesOnEdge.size()-1);
                        
                        if(otherBoundary.getInclusive() != Direction.FORWARD) {
                            throw new IllegalStateException("Inconsistent boundaries");
                        }

                        boundaryVisits.get(otherBoundary).visited = true;
                        
                        area.add(new TrackRange1D(trackID, otherBoundary.getOffset(), nodeEdge.getEnd()));
                        // No node to push, reached a dead end from the end of a track
                    }
                }
            }
        }

        return area;
    }

    static class BoundaryVisit extends Visit {
        TrackBoundary1D boundary;
        MapEdge edge;
        
        public BoundaryVisit(TrackBoundary1D boundary, MapEdge edge) {
            this.boundary = boundary;
            this.edge = edge;
        }
    }

    static class Visit {
        boolean visited;
        
        public Visit() {
            this.visited = false;
        }
    }


}
