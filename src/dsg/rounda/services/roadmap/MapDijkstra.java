/**
 * 
 */
package dsg.rounda.services.roadmap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Adapted from
 * http://www.vogella.com/articles/JavaAlgorithmsDijkstra/article.html
 *
 */
public class MapDijkstra {
    
    public interface Measurer {
        double getLength(MapEdge edge);
    }
    
    public static final Measurer DEFAULT_MEASURER = new Measurer() {
        public double getLength(MapEdge edge) {
            return edge.getLength();
        }
    };

    private Set<MapNode> settledNodes;
    private Set<MapNode> unSettledNodes;
    private Set<MapNode> exits;
    private Map<MapNode, MapEdge> predecessors;
    private Map<MapNode, Double> distance;
    private Measurer measurer;

    public MapDijkstra(MapNode source) {
        this(source, DEFAULT_MEASURER);
    }
    
    public MapDijkstra(MapNode source, Measurer meas) {
        measurer = meas;
        settledNodes = new HashSet<MapNode>();
        unSettledNodes = new HashSet<MapNode>();
        distance = new HashMap<MapNode, Double>();
        predecessors = new HashMap<MapNode, MapEdge>();
        exits = new HashSet<MapNode>();
        distance.put(source, 0.0);
        unSettledNodes.add(source);
        while (unSettledNodes.size() > 0) {
            MapNode node = getMinimum(unSettledNodes);
            settledNodes.add(node);
            unSettledNodes.remove(node);
            
            if(node.getFrom().isEmpty()) {
                exits.add(node);
            } else {
                findMinimalDistances(node);
            };
        }
    }

    /**
     * @return the measurer
     */
    public Measurer getMeasurer() {
        return measurer;
    }

    /**
     * @param measurer the measurer to set
     */
    public void setMeasurer(Measurer measurer) {
        this.measurer = measurer;
    }

    private void findMinimalDistances(MapNode node) {
        for (MapEdge edge : node.getFrom()) {
            MapNode target = edge.getTo();
            
            if(isSettled(target)) {
                continue;
            }
            
            double distanceWithEdge = getShortestDistance(node) + measurer.getLength(edge);
            
            if (distanceWithEdge < getShortestDistance(target)) {
                distance.put(target, distanceWithEdge);
                predecessors.put(target, edge);
                unSettledNodes.add(target);
            }
        }
    }

    private MapNode getMinimum(Set<MapNode> vertexes) {
        MapNode minimum = null;
        for (MapNode node : vertexes) {
            if (minimum == null) {
                minimum = node;
            } else {
                if (getShortestDistance(node) < getShortestDistance(minimum)) {
                    minimum = node;
                }
            }
        }
        return minimum;
    }

    private boolean isSettled(MapNode MapNode) {
        return settledNodes.contains(MapNode);
    }

    private double getShortestDistance(MapNode destination) {
        Double d = distance.get(destination);
        if (d == null) {
            return Double.MAX_VALUE;
        } else {
            return d;
        }
    }
    
    public Collection<MapNode> getExits() {
        return exits;
    }

    /*
     * This method returns the path from the source to the selected target and
     * NULL if no path exists
     */
    public LinkedList<MapEdge> getPath(MapNode target) {
        LinkedList<MapEdge> path = new LinkedList<MapEdge>();
        MapNode step = target;
        // Check if a path exists
        if (predecessors.get(step) == null) {
            return null;
        }
        while (predecessors.get(step) != null) {
            MapEdge edge = predecessors.get(step);
            step = edge.getFrom();
            path.add(edge);
        }
        // Put it into the correct order
        Collections.reverse(path);
        return path;
    }

}