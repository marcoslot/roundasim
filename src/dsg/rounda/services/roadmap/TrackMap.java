/**
 * 
 */
package dsg.rounda.services.roadmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;

import dsg.rounda.model.LaneChangeDirection;
import dsg.rounda.model.Position1D;
import dsg.rounda.model.Track;
import dsg.rounda.model.TrackNetwork;
import dsg.rounda.model.TrackProvider;
import dsg.rounda.model.TrackSegment;
import dsg.rounda.model.TrackType;
import dsg.rounda.model.Trajectory1D;
import dsg.rounda.services.coordination.ConflictAreaFinder;

/**
 * Representation of the road map used by controllers.
 */
public class TrackMap implements TrackProvider {

    private static final int MAX_NUM_X_CELLS = 100;
    private static final int MAX_NUM_Y_CELLS = 100; 

    final TrackNetwork network;
    final Map<Integer,List<Track>> fromIndex;
    
    
    Envelope mapBox;
    
    SpatialGrid2D<TrackSegment> pathSegmentGrid;
    ConnectorGraph connGraph;
    ConflictAreaFinder conflictFinder;
    
    long mapVersion;
    
    /**
     * Create a new road map
     * 
     * @param network the physical road network to base the road map on
     */
    public TrackMap(TrackNetwork network) {
        this.network = network;
        this.fromIndex = new HashMap<Integer,List<Track>>();
        this.mapVersion = -1;
    }
    
    private void update() {
        long worldVersion = network.getRoadsVersion();
        
        if(mapVersion != worldVersion) {
            buildFromIndex();
            buildBoundingBox();
            buildPathSegmentGrid();
            
            connGraph = new ConnectorGraph(this);
            conflictFinder = new ConflictAreaFinder(this);
            
            mapVersion = worldVersion;
        }
    }

    private int countNumSegments() {
        int numSegments = 0;
        
        for(Track track : network.getRoads()) {
            numSegments += track.getNumLineSegments();
        }
        
        return numSegments;
    }

    private void buildBoundingBox() {
        Double minX = null;
        Double minY = null;
        Double maxX = null;
        Double maxY = null;
        
        for(Track track : network.getRoads()) {
            if(minX == null || track.getBoundingBox().getMinX() < minX) {
                minX = track.getBoundingBox().getMinX();
            }
            if(maxX == null || track.getBoundingBox().getMaxX() > maxX) {
                maxX = track.getBoundingBox().getMaxX();
            }
            if(minY == null || track.getBoundingBox().getMinY() < minY) {
                minY = track.getBoundingBox().getMinY();
            }
            if(maxY == null || track.getBoundingBox().getMaxY() > maxY) {
                maxY = track.getBoundingBox().getMaxY();
            }
        }
        
        if(minX != null && minY != null && maxX != null && maxY != null) {
            mapBox = new Envelope(minX, maxX, minY, maxY);
        } else {
            mapBox = null;
        }
    }

    private void buildPathSegmentGrid() {     
        if(mapBox == null) {
            pathSegmentGrid = null;
            return;
        }

        int totalSegments = countNumSegments();
        double widthRatio = mapBox.getWidth() / mapBox.getHeight();
        int numXCells = (int) Math.min(Math.max(1, totalSegments * widthRatio), MAX_NUM_X_CELLS);
        int numYCells = (int) Math.min(Math.max(1, totalSegments / widthRatio), MAX_NUM_Y_CELLS);

        pathSegmentGrid = new SpatialGrid2D<TrackSegment>(mapBox, numXCells, numYCells);

        for(Track track : network.getRoads()) {
            addTrackSegmentsToGrid(
            		pathSegmentGrid, 
            		track, 
            		TrackSegment.Type.PATH, 
            		track.getPathSegments());
        }
    }
    
    void addTrackSegmentsToGrid(SpatialGrid2D<TrackSegment> pathSegmentGrid, Track track, TrackSegment.Type type, LineSegment[] segments) {
        for(int i = 0; i < segments.length; i++) {
            LineSegment pathSegment = track.getLineSegmentByIndex(i);
            TrackSegment trackSegment = new TrackSegment(track, type, pathSegment, i);
            pathSegmentGrid.addToGrid(trackSegment, pathSegment);
        }
    }

    private void buildFromIndex() {
        Map<Integer,List<Track>> unorderedFromIndex = new HashMap<Integer,List<Track>>();

        // Add a from-list for every track
        for(Track current : network.getRoads()) {
            unorderedFromIndex.put(current.getId(), new ArrayList<Track>());
        }
        
        // Add each track to the from-list of the track it starts on
        for(Track current : network.getRoads()) {
            if(current.getFrom() == null) {
                continue;
            }
            
            int baseID = current.getFrom().getRoad();
            List<Track> tracksFromBase = unorderedFromIndex.get(baseID);
            
            if(tracksFromBase == null) {
                // From points to non-existent road
                continue;
            }
            
            tracksFromBase.add(current);
        }

        // Convert to an unmodifiable, ordered index
        fromIndex.clear();
        
        for(Map.Entry<Integer, List<Track>> entry : unorderedFromIndex.entrySet()) {
            int roadID = entry.getKey();
            List<Track> tracks = entry.getValue();
            Collections.sort(tracks, new Comparator<Track>() {
                @Override
                public int compare(Track left, Track right) {
                    return Double.compare(left.getFrom().getOffset(), right.getFrom().getOffset());
                }
            });
            fromIndex.put(roadID, Collections.unmodifiableList(tracks));
        }
    }

    /**
     * Get a track by its identifier
     * 
     * @param roadID the track identifier
     * @return the road with identifier roadID, or null if it does not exist
     */
    public Track getRoad(int roadID) {
        return network.getRoad(roadID);
    }
    
    /**
     * Returns the list of tracks starting from the permanent
     * track with identifier trackID. Returns null if no such
     * track.
     * 
     * @param trackID the identifier of the starting track
     * @return list of tracks ordered by offset
     */
    public List<Track> getTracksStartingFrom(int trackID) {
        update();
        return fromIndex.get(trackID);
    }

    /**
     * Returns the list of tracks starting from the permanent
     * track with identifier trackID and whose offset is >=
     * startingOffset. Returns null if no such track.
     * 
     * @param trackID the identifier of the starting track
     * @return list of tracks ordered by offset
     */
    public List<Track> getTracksStartingFrom(int trackID, double startingOffset) {
        update();
        List<Track> tracks = fromIndex.get(trackID);
        
        if(tracks == null) {
            return null;
        }
        
        for(int i = 0, len = tracks.size(); i < len; i++) {
            if(tracks.get(i).getFrom().getOffset() >= startingOffset) {
                return tracks.subList(i, len);
            }
        }
        
        return Collections.emptyList();
    }

    /**
     * Get track segments near the line p0 to p1
     * 
     * @param p0 the start of the line
     * @param p1 the end of the line
     * @return a superset of the track segments that intersect with the line
     */
    public Collection<TrackSegment> getTracksByPathSegment(Coordinate p0, Coordinate p1) {
        update();
        return pathSegmentGrid.getNearbyObjects(p0, p1);
    }

    /**
     * Get track segments near the envelope
     * 
     * @param envelope the envelope
     * @return a superset of the track segments that intersect with the line
     */
    public Collection<TrackSegment> getTracksByPathSegment(Envelope envelope) {
        update();
        return pathSegmentGrid.getNearbyObjects(envelope);
    }

    /**
     * @see dsg.rounda.model.TrackNetwork#getRoads()
     */
    public List<Track> getRoads() {
        return network.getRoads();
    }

    /**
     * Returns the connector graph for this road map
     * 
     * @return the connector graph for this road map
     */
    public ConnectorGraph getConnectorGraph() {
        update();
        return connGraph;
    }

    public long getVersion() {
        update();
        return this.mapVersion;
    }

    public ConflictAreaFinder getConflictFinder() {
        update();
        return conflictFinder;
    }

    public Position1D asPosition(TrackPoint1D vehiclePoint) {
        update();
        return new Position1D(getRoad(vehiclePoint.getTrackID()), vehiclePoint.getOffset());
    }

    public List<Track> getLanes(int trackID) {
        Track track = getRoad(trackID);
        List<Track> lanes = new LinkedList<Track>();
        
        lanes.add(track);

        for(Track currentTrack = track; 
                currentTrack.getLeftLane() != null; 
                currentTrack = getRoad(currentTrack.getLeftLane())) {
            
            lanes.add(0, getRoad(currentTrack.getLeftLane()));
        }

        for(Track currentTrack = track; 
                currentTrack.getRightLane() != null; 
                currentTrack = getRoad(currentTrack.getRightLane())) {
            
            lanes.add(getRoad(currentTrack.getRightLane()));
        }
        
        return lanes;
    }

    public TrackPoint1D translateToOtherLane(TrackPoint1D pos, LaneChangeDirection direction) {
    	Track track = getRoad(pos.getTrackID());
    	Position1D projectedPosition = network.translateOffset(new Position1D(track, pos.getOffset()), direction);
    	
    	if(projectedPosition == null) {
    		return null;
    	}
    	
    	return new TrackPoint1D(projectedPosition.getTrackID(), projectedPosition.getOffset());
    }

	public double getScenarioLength() {
		double sum = 0.0;

        for(Track track : network.getRoads()) {
        	sum = track.getPathLength();
        }
        
        return sum;
	}

}
