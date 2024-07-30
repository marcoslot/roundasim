/**
 * 
 */
package dsg.rounda.services.roadmap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Road map edges ordered from left to right
 * (the left-most lane is at the top of the list)
 */
public class MapEdgeSet implements Iterable<MapEdge> {

    final List<MapEdge> edges;
    
    public MapEdgeSet() {
        this.edges = new LinkedList<MapEdge>();
    }

    public List<MapNode> getEndNodes() {
        final List<MapNode> endNodes = new ArrayList<MapNode>();
        
        for(MapEdge edge : edges) {
            endNodes.add(edge.getTo());
        }
        
        return endNodes;
    }
    public List<MapNode> getStartNodes() {
        final List<MapNode> startNodes = new ArrayList<MapNode>();
        
        for(MapEdge edge : edges) {
            startNodes.add(edge.getFrom());
        }
        
        return startNodes;
    }
    
    /**
     * Determine if this edge set is at the end
     * of the road map 
     * 
     * @return true if there are no edges leaving
     * the end nodes of this edge set, false otherwise.
     */
    public boolean terminates() {
        for(MapEdge edge : edges) {
            if(!edge.getTo().terminates()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Returns true if the edges in this set continue
     * straight from the edges of other.
     * 
     * @param other
     * @return
     */
    public boolean isContinuationOf(MapEdgeSet other) {
        if(edges.size() != other.edges.size()) {
            return false;
        }
        
        for(int i = 0, len = edges.size(); i < len; i++) {
            if(edges.get(i).getFrom() != other.edges.get(i).getTo()) {
                return false;
            }
        }
        
        return true;
    }

    public void addRight(MapEdge edge) {
        edges.add(edge);
    }

    public void addLeft(MapEdge edge) {
        edges.add(0, edge);
    }
    
    public List<MapEdge> getEdges() {
        return edges;
    }

    @Override
    public Iterator<MapEdge> iterator() {
        return edges.iterator();
    }

}
