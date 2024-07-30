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
import java.util.Queue;
import java.util.Set;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.PrecisionModel;

import dsg.rounda.model.Connector;
import dsg.rounda.model.Track;
import dsg.rounda.model.TrackProvider;

/**
 * Connector graph
 */
public class ConnectorGraph {

    private static final PrecisionModel PM = new PrecisionModel(1000);

    final TrackProvider network;
    final Map<Integer,List<MapEdge>> edgeLists;
    final Collection<MapNode> nodes;
    final Collection<MapEdgeSet> edgeSets;

    public ConnectorGraph(TrackProvider network) {
        this.network = network;
        this.edgeSets = new HashSet<MapEdgeSet>();
        this.edgeLists = new HashMap<Integer,List<MapEdge>>();

        Map<Coordinate,MapNode> nodeIndex = new HashMap<Coordinate,MapNode>();

        // connector index
        final Map<Integer,List<Connector>> connIndex = new HashMap<Integer,List<Connector>>();

        // Index connectors by the track they start from or end on
        for(Track current : network.getRoads()) {
            if(current.getFrom() != null) {
                Track fromTrack = network.getRoad(current.getFrom().getRoad());

                if(fromTrack != null 
                        && Math.abs(current.getFrom().getOffset() - fromTrack.getPathLength()) > Connector.CONNECTOR_EQUIVALENCE_DISTANCE) {
                    addToIndex(current.getFrom(), connIndex);
                }
            }
            if(current.getTo() != null && current.getTo().getOffset() > 0) {
                addToIndex(current.getTo(), connIndex);
            }
        }

        sortIndex(connIndex);

        final Set<Track> processedTracks = new HashSet<Track>();

        // Split tracks into edges, except for start and end
        for(Track track : network.getRoads()) {
            if(processedTracks.contains(track)) {
                continue;
            }


            List<Track> lanes = network.getLanes(track.getId());
            
            // All lanes are processed in one iteration of this loop
            processedTracks.addAll(lanes);

            TrackPoint1DIndex nodePointIndex = new TrackPoint1DIndex();
            List<MapEdgeSet> mapEdgeSets = new ArrayList<MapEdgeSet>();

            // Create edge sets or each intersection on a lane
            for(Track lane : lanes) {
                List<Connector> conns = connIndex.get(lane.getId());

                if(conns == null) {
                    continue;
                }

                double startOffset = 0.0;
                double endOffset;

               for(int i = 0, numConns = conns.size(); i < numConns; i++) {
                    endOffset = conns.get(i).getOffset();

                    if(endOffset > startOffset) {
                        // Space between the connectors, add a new edge in between
                        TrackPoint1D nodePoint = new TrackPoint1D(lane.getId(), endOffset);
                        nodePointIndex.add(nodePoint);

                        // Every time we find a connector on a lane, we'll have
                        // a new edge on every lane, and therefore a new edge set
                        // across the lanes.

                        mapEdgeSets.add(new MapEdgeSet());
                    }

                    startOffset = endOffset;
                }
            }

            mapEdgeSets.add(new MapEdgeSet());

            // Expand the intersections to each lane
            for(TrackPoint1D nodePoint : nodePointIndex.getAllTrackPoints()) {
                Track reference = network.getRoad(nodePoint.getTrackID());

                double offset = nodePoint.getOffset();

                for(Track currentTrack = reference; 
                	currentTrack.getRightLane() != null; 
                	currentTrack = network.getRoad(currentTrack.getRightLane())) {
                    TrackPoint1D rightTrackPoint = new TrackPoint1D(
                            currentTrack.getRightLane(), 
                            translateOffsetToRight(currentTrack, offset));

                    nodePointIndex.add(rightTrackPoint);
                }

                for(Track currentTrack = reference; currentTrack.getLeftLane() != null; currentTrack = network.getRoad(currentTrack.getLeftLane())) {
                    TrackPoint1D leftTrackPoint = new TrackPoint1D(
                            currentTrack.getLeftLane(), 
                            translateOffsetToLeft(currentTrack, offset));

                    nodePointIndex.add(leftTrackPoint);
                }
            }

            for(Track lane : lanes) {
            	List<MapEdge> edges = getOrCreateEdges(lane.getId());
                List<TrackPoint1D> nodePoints = nodePointIndex.getTrackPoints(lane.getId());

                Collections.sort(nodePoints);
                
                Coordinate fromCoord = lane.getPath().getCoordinateN(0);
                Coordinate toCoord = lane.getPath().getCoordinateN(lane.getPath().getNumPoints()-1);

                // Find the start node
                // Tracks that end at the start of this track will find this as their end node
                MapNode startNode = getOrCreateNode(nodeIndex, fromCoord);
                double startOffset = 0.0;

                MapNode endNode;
                double endOffset;

                MapEdgeSet mapEdgeSet;
                
                
                // adding edges to each of the edgesets
                for(int i = 0, len = nodePoints.size(); i < len; i++) {
                    mapEdgeSet = mapEdgeSets.get(i);
                    
                    TrackPoint1D nodePoint = nodePoints.get(i);

                    endOffset = nodePoint.getOffset();
                    endNode = getOrCreateNode(nodeIndex, lane.getPose2D(endOffset).getPosition());

                    if(endOffset > startOffset) {
                        // Space between the connectors, add a new edge in between
                        TrackRange1D trackRange = new TrackRange1D(lane.getId(), startOffset, endOffset);
                        MapEdge edge = new MapEdge(startNode, endNode, trackRange, mapEdgeSet);
                        edges.add(edge);
                        mapEdgeSet.addRight(edge);
                    }

                    startNode = endNode;
                    startOffset = endOffset;
                }

                // Add edges for the last edge set (only edge set if there are no intersections)
                mapEdgeSet = mapEdgeSets.get(mapEdgeSets.size()-1);

                // Find the end node
                endNode = getOrCreateNode(nodeIndex, toCoord);
                endOffset = lane.getPathLength();

                if(endOffset > startOffset) {
                    // Add last edge
                    TrackRange1D trackRange = new TrackRange1D(lane.getId(), startOffset, endOffset);
                    MapEdge edge = new MapEdge(startNode, endNode, trackRange,  mapEdgeSet);
                    edges.add(edge);
                    mapEdgeSet.addRight(edge);
                }
            }

            edgeSets.addAll(mapEdgeSets);
        }

        this.nodes = nodeIndex.values();
    }



    public ConnectorGraph(
            TrackProvider network,
            Map<Integer, List<MapEdge>> edgeLists,
            Collection<MapNode> nodes,
            Set<MapEdgeSet> edgeSets) {
        this.network = network;
        this.edgeLists = edgeLists;
        this.nodes = nodes;
        this.edgeSets = edgeSets;
    }

    public RoadBlockMap findRoadBlocks(MapNode endNode) {
        RoadBlockMap roadBlockMap = new RoadBlockMap();
        
        Queue<MapNode> nodesToVisit = new LinkedList<MapNode>();
        nodesToVisit.addAll(endNode.getAllIncomingNodes());

        Set<MapNode> visitedNodes = new HashSet<MapNode>();
        Set<MapEdgeSet> visitedEdgeSets = new HashSet<MapEdgeSet>();

        while(!nodesToVisit.isEmpty()) {
            MapNode currentNode = nodesToVisit.remove();
            
            if(visitedNodes.contains(currentNode)) {
                continue;
            }
            
            visitedNodes.add(currentNode);

            for(MapEdgeSet currentEdgeSet : currentNode.getIncomingEdgeSets()) {
                if(visitedEdgeSets.contains(currentEdgeSet)) {
                    continue;
                }
                
                visitedEdgeSets.add(currentEdgeSet);
                
                List<MapEdge> edges = currentEdgeSet.getEdges();
                List<MapNode> endNodes = currentEdgeSet.getEndNodes();
                int currentIndex = endNodes.indexOf(currentNode);
                
                for(int i = 0, numEndNodes = endNodes.size(); i < numEndNodes; i++) {
                    if(i == currentIndex) {
                        // endNodes.get(i) == currentNode
                        continue;
                    }
                    
                    MapNode node = endNodes.get(i);
                    
                    if(!node.terminates()) {
                        continue;
                    }
                    
                    int magnitude = Math.abs(i - currentIndex);
                    int direction = currentIndex - i < 0 ? -1 : 1;
                    
                    MapEdge edge = edges.get(i);
                    
                    RoadBlock roadBlock = new RoadBlock(
                            edge.getTrackID(),
                            edge.getEnd(),
                            direction,
                            magnitude);
                    
                    roadBlockMap.add(roadBlock);
                }
                
                nodesToVisit.addAll(currentEdgeSet.getStartNodes());
            }
        }

        return roadBlockMap;
    }

    public double translateOffsetToRight(Track reference, double offset) {
        Track right = network.getRoad(reference.getRightLane());

        return right.getPathLength() * offset / reference.getPathLength();
    }


    public double translateOffsetToLeft(Track reference, double offset) {
        Track left = network.getRoad(reference.getLeftLane());

        return left.getPathLength() * offset / reference.getPathLength();
    }




    private MapNode getOrCreateNode(
            Map<Coordinate,MapNode> nodeIndex,
            Coordinate conn) {
        Coordinate key = new Coordinate(conn);
        PM.makePrecise(key);

        MapNode node = nodeIndex.get(key);

        if(node == null) {
            node = new MapNode();
            nodeIndex.put(key, node);
        }

        return node;
    }

    private void addToIndex(
            Connector conn,
            Map<Integer, List<Connector>> index) {
        if(conn == null) {
            return;
        }

        int baseID = conn.getRoad();
        List<Connector> connectedTracks = index.get(baseID);

        if(connectedTracks == null) {
            connectedTracks = new ArrayList<Connector>();
            index.put(baseID, connectedTracks);
        }

        if(!connectedTracks.isEmpty()
                && connectedTracks.get(connectedTracks.size()-1).equals(conn)) {
            return;
        }

        connectedTracks.add(conn);
    }

    private void sortIndex(Map<Integer, List<Connector>> index) {
        for(Map.Entry<Integer,List<Connector>> entry : index.entrySet()) {
            Collections.sort(entry.getValue());
        }

    }

    public MapNode getStartNode(int trackID) {
        List<MapEdge> edges = edgeLists.get(trackID);

        if(edges == null || edges.isEmpty()) {
            return null;
        } else {
            return edges.get(0).getFrom();
        }
    }

    public MapNode getEndNode(int trackID) {
        List<MapEdge> edges = edgeLists.get(trackID);

        if(edges == null || edges.isEmpty()) {
            return null;
        } else {
            return edges.get(edges.size()-1).getTo();
        }
    }

    /**
     * 
     * @param trackID
     * @return
     */
    public List<MapEdge> getEdges(int trackID) {
        return edgeLists.get(trackID);
    }

    List<MapEdge> getOrCreateEdges(int trackID) {
        List<MapEdge> edges = getEdges(trackID);

        if(edges == null) {
            edges = new ArrayList<MapEdge>();
            edgeLists.put(trackID, edges);
        }

        return edges;
    }

    public MapEdge getEdge(TrackPoint1D point) {
        List<MapEdge> edges = getEdges(point.getTrackID());

        if(edges == null) {
            return null;
        }

        for(int i = 0, numEdges = edges.size(); i < numEdges; i++) {
            MapEdge edge = edges.get(i);

            if(edge.getTrackRange().contains(point)) {
                return edge;
            }
        }

        return null;
    }

    public Collection<MapNode> appendNodes(int trackID, Collection<MapNode> nodes) {
        appendNodes(trackID, 0.0, Double.MAX_VALUE, nodes);
        return nodes;
    }

    public Collection<MapNode> appendNodesStartingAt(int trackID, double startOffset, Collection<MapNode> nodes) {
        appendNodes(trackID, startOffset, Double.MAX_VALUE, nodes);
        return nodes;
    }

    public Collection<MapNode> appendNodesEndingAt(int trackID, double endOffset, Collection<MapNode> nodes) {
        appendNodes(trackID, 0.0, endOffset, nodes);
        return nodes;
    }

    public Collection<MapNode> appendNodes(int trackID, double startOffset, double endOffset, Collection<MapNode> nodes) {
        List<MapEdge> edgeList = getEdges(trackID);

        if(edgeList == null || edgeList.isEmpty()) {
            return nodes;
        }

        int numEdges = edgeList.size();
        int startIndex;
        int endIndex;

        // Separate cases to optimize performance
        if(startOffset <= 0.0) {
            startIndex = 0;

            // Start at beginning of edge sequence, add "from" node 
            nodes.add(edgeList.get(0).getFrom());

            // Find end from start
            for(endIndex = 0; endIndex < numEdges; endIndex++) {
                MapEdge edge = edgeList.get(endIndex);

                if(edge.getTrackRange().getEnd() > endOffset) {
                    // the "to" should *not* be included in the node list
                    break;
                }
            }
        } else {
            double lastOffset = edgeList.get(edgeList.size()-1).getTrackRange().getEnd();

            if(endOffset >= lastOffset) {
                endIndex = numEdges;

                // Find start from end
                for(startIndex = numEdges-1; startIndex >= 0; startIndex--) {
                    MapEdge edge = edgeList.get(startIndex);

                    if(edge.getTrackRange().getStart() < startOffset) {
                        break;
                    }
                }
            } else {
                // start and end are somewhere in the middle
                // take a wild guess, this is a primitive binary search
                startIndex = (int) Math.min(edgeList.size() * startOffset / lastOffset, edgeList.size()-1);
                double guessedOffset = edgeList.get(startIndex).getEnd();
                int direction  = (int) Math.signum(startOffset - guessedOffset);

                if(direction < 0) {
                    for(;startIndex > 0; startIndex--) {
                        MapEdge edge = edgeList.get(startIndex-1);

                        if(edge.getTrackRange().getEnd() < startOffset) {
                            // if we go back one more, we find an edge whose
                            // end is left of the range
                            break;
                        }
                    }
                } else if(direction > 0) {
                    for(startIndex++ ;startIndex < numEdges; startIndex++) {
                        MapEdge edge = edgeList.get(startIndex);

                        if(edge.getTrackRange().getEnd() > startOffset) {
                            // the end of edgeList[startIndex] now lies beyond startOffset
                            // possibly within the range 
                            break;
                        }
                    }

                }

                // Find end from start
                for(endIndex = startIndex; endIndex < numEdges; endIndex++) {
                    MapEdge edge = edgeList.get(endIndex);

                    if(edge.getTrackRange().getEnd() > endOffset) {
                        // the "to" should *not* be included in the node list
                        break;
                    }
                }
            }
        }

        for(int i = startIndex; i < endIndex; i++) {
            MapEdge edge = edgeList.get(i);
            nodes.add(edge.getTo());
        }

        return nodes;
    }

    public Collection<MapNode> getNodes(int trackID, double start, double end) {
        return appendNodes(trackID, start, end, new ArrayList<MapNode>());
    }

    public Collection<? extends MapNode> getNodes() {
        return nodes;
    }

    public Collection<? extends MapEdge> getEdges() {
        List<MapEdge> edges = new ArrayList<MapEdge>();

        for(List<MapEdge> edgeList : edgeLists.values()) {
            edges.addAll(edgeList);
        }

        return edges;
    }

    public ConnectorGraph getPossibleDestinationsGraph(MapNode start) {
        return getPossibleDestinationsGraph(start, new HashMap<MapNode,MapNode>());
    }

    public ConnectorGraph getPossibleDestinationsGraph(
            MapNode startNode, 
            final Map<MapNode, MapNode> nodeMapping) {
        final ConnectorGraphBuilder cgb = new ConnectorGraphBuilder(network);

        nodeMapping.put(startNode, new MapNode());

        final Set<MapEdgeSet> visitedEdgeSets = new HashSet<MapEdgeSet>();

        new NodeProcessor() {
            @Override
            public void process(MapNode parentNode) {
                for(MapEdge edgeFromNode : parentNode.getFrom()) {

                    MapEdgeSet oldEdgeSet = edgeFromNode.getEdgeSet();

                    if(visitedEdgeSets.contains(oldEdgeSet)) {
                        continue;
                    }

                    visitedEdgeSets.add(oldEdgeSet);

                    MapEdgeSet newEdgeSet = new MapEdgeSet();

                    for(MapEdge laneEdge : oldEdgeSet) {

                        MapNode fromNode = nodeMapping.get(laneEdge.getFrom());

                        if(fromNode == null) {
                            fromNode = new MapNode();
                            nodeMapping.put(laneEdge.getFrom(), fromNode);
                        }

                        MapNode toNode = nodeMapping.get(laneEdge.getTo());

                        if(toNode == null) {
                            toNode = new MapNode();
                            nodeMapping.put(laneEdge.getTo(), toNode);
                        }

                        // Add the edge corresponding to the parent edge
                        MapEdge newLaneEdge = cgb.addMapEdge(fromNode, toNode, laneEdge.getTrackRange(), newEdgeSet);

                        newEdgeSet.addRight(newLaneEdge);

                        process(laneEdge.getTo());
                    }
                }

            }

        }.process(startNode);


        return cgb.toGraph();
    }

    public ConnectorGraph getPossibleSourcesGraph(final MapNode endNode) {
        return getPossibleSourcesGraph(endNode, new HashMap<MapNode,MapNode>());
    }

    ConnectorGraph getPossibleSourcesGraph(
            final MapNode endNode,
            final Map<MapNode, MapNode> nodeMapping) {
        final ConnectorGraphBuilder cgb = new ConnectorGraphBuilder(network);

        nodeMapping.put(endNode, new MapNode());

        final Set<MapEdgeSet> visitedEdgeSets = new HashSet<MapEdgeSet>();

        new NodeProcessor() {
            @Override
            public void process(MapNode parentNode) {
                for(MapEdge edgeToNode : parentNode.getTo()) {

                    MapEdgeSet oldEdgeSet = edgeToNode.getEdgeSet();

                    if(visitedEdgeSets.contains(oldEdgeSet)) {
                        continue;
                    }

                    visitedEdgeSets.add(oldEdgeSet);

                    MapEdgeSet newEdgeSet = new MapEdgeSet();

                    for(MapEdge laneEdge : oldEdgeSet) {

                        MapNode fromNode = nodeMapping.get(laneEdge.getFrom());

                        if(fromNode == null) {
                            fromNode = new MapNode();
                            nodeMapping.put(laneEdge.getFrom(), fromNode);
                        }

                        MapNode toNode = nodeMapping.get(laneEdge.getTo());

                        if(toNode == null) {
                            toNode = new MapNode();
                            nodeMapping.put(laneEdge.getTo(), toNode);
                        }

                        // Add the edge corresponding to the parent edge
                        MapEdge newLaneEdge = cgb.addMapEdge(fromNode, toNode, laneEdge.getTrackRange(), newEdgeSet);

                        newEdgeSet.addRight(newLaneEdge);

                        process(laneEdge.getFrom());
                    }
                }

            }

        }.process(endNode);


        return cgb.toGraph();
    }



    /**
     * @return the edgeSets
     */
    public Collection<MapEdgeSet> getEdgeSets() {
        return edgeSets;
    }



    public List<MapEdgeSet> getTerminatingEdgeSets() {
        final List<MapEdgeSet> terminatingEdgeSets = new ArrayList<MapEdgeSet>();

        for(MapEdgeSet edgeSet : edgeSets) {
            if(edgeSet.terminates()) {
                terminatingEdgeSets.add(edgeSet);
            }
        }

        return terminatingEdgeSets;
    }




}
