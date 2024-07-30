/**
 * 
 */
package dsg.rounda.services.roadmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Node in a graph of track ranges
 */
public class MapNode {

    final Collection<MapEdge> fromEdges;
    final Collection<MapEdge> toEdges;
    final Collection<MapEdge> allEdges;

    /**
     * 
     */
    public MapNode() {
        this.fromEdges = new ArrayList<MapEdge>();
        this.toEdges = new ArrayList<MapEdge>();
        this.allEdges = new ArrayList<MapEdge>();
    }
    
    public MapNode(MapNode... mapNodes) {
        this();
        
        for(MapNode mapNode : mapNodes) {
            fromEdges.addAll(mapNode.fromEdges);
            toEdges.addAll(mapNode.toEdges);
            allEdges.addAll(mapNode.allEdges);
        }
    }
    
    public boolean terminates() {
        return fromEdges.isEmpty();
    }
    
    public Set<MapEdgeSet> getIncomingEdgeSets() {
        Set<MapEdgeSet> incomingEdgeSets = new HashSet<MapEdgeSet>();
        
        for(MapEdge edgeToThis : toEdges) {
            incomingEdgeSets.add(edgeToThis.getEdgeSet());
        }
        
        return incomingEdgeSets;
    }

    public void addFrom(MapEdge mapEdge) {
        fromEdges.add(mapEdge);
        allEdges.add(mapEdge);
    }

    public void addTo(MapEdge mapEdge) {
        toEdges.add(mapEdge);
        allEdges.add(mapEdge);
    }
    
    public Collection<MapEdge> getFrom() {
        return fromEdges;
    }
    
    public Collection<MapEdge> getTo() {
        return toEdges;
    }
    
    public Collection<MapEdge> getEdges() {
        return allEdges;
    }

    public MapEdge getFromEdge(int trackID) {
        for(MapEdge edge : fromEdges) {
            if(edge.getTrackID() == trackID) {
                return edge;
            }
        }
        return null;
    }

    public MapEdge getToEdge(int trackID) {
        for(MapEdge edge : toEdges) {
            if(edge.getTrackID() == trackID) {
                return edge;
            }
        }
        return null;
    }

    public Collection<MapEdge> getAllIncomingEdges() {
        Set<MapEdge> allIncomingEdges = new HashSet<MapEdge>(); 
        
        for(MapEdgeSet edgeSet : getIncomingEdgeSets()) {
            for(MapEdge edge : edgeSet) {
                allIncomingEdges.add(edge);
            }
        }
        
        return allIncomingEdges;
    }

    public Collection<MapNode> getAllIncomingNodes() {
        Set<MapNode> allIncomingNodes = new HashSet<MapNode>(); 
        
        for(MapEdgeSet edgeSet : getIncomingEdgeSets()) {
            for(MapEdge edge : edgeSet) {
                allIncomingNodes.add(edge.getFrom());
            }
        }
        
        return allIncomingNodes;
    }

	public boolean isStart() {
		return this.toEdges.isEmpty();
	}

}
