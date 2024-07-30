/**
 * 
 */
package dsg.rounda.services.roadmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dsg.rounda.model.TrackProvider;

/**
 * @author slotm
 *
 */
public class ConnectorGraphBuilder {

    final TrackProvider network;
    final Map<Integer,List<MapEdge>> edgeLists;
    final Collection<MapNode> nodes;
    final Set<MapEdgeSet> edgeSets;

    /**
     * 
     */
    public ConnectorGraphBuilder(TrackProvider network) {
        this.network = network;
        this.edgeLists = new HashMap<Integer,List<MapEdge>>();
        this.nodes = new HashSet<MapNode>();
        this.edgeSets = new HashSet<MapEdgeSet>();
    }

    private List<MapEdge> getOrCreateEdgeList(int trackID) {
        List<MapEdge> edges = edgeLists.get(trackID);
        
        if(edges == null) {
            edges = new ArrayList<MapEdge>();
            edgeLists.put(trackID, edges);
        }
        
        return edges;
    }
    
    public MapEdge addMapEdge(MapNode fromNode, MapNode toNode, TrackRange1D trackRange, MapEdgeSet edgeSet) {
        MapEdge edge = new MapEdge(fromNode, toNode, trackRange, edgeSet);

        edgeSets.add(edgeSet);
        nodes.add(fromNode);
        nodes.add(toNode);
        getOrCreateEdgeList(trackRange.getTrackID()).add(edge);
        return edge;
    }
    
    public ConnectorGraph toGraph() {
        return new ConnectorGraph(network, edgeLists, nodes, edgeSets);
    }

}
