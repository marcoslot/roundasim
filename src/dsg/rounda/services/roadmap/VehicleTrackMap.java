/**
 * 
 */
package dsg.rounda.services.roadmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import dsg.rounda.model.LaneChangeDirection;
import dsg.rounda.model.Position1D;
import dsg.rounda.model.Track;
import dsg.rounda.model.TrackProvider;
import dsg.rounda.model.TrackSegment;
import dsg.rounda.services.coordination.ConflictAreaFinder;

/**
 * @author Niall
 *
 */
public class VehicleTrackMap implements TrackProvider {

	final TrackMap wrapped;
	final List<Track> laneChanges;

	SpatialGrid2D<TrackSegment> laneChangeGrid;
	ConnectorGraph vehicleConnectorGraph;

	public VehicleTrackMap(TrackMap wrapped) {
		super();
		this.wrapped = wrapped;
		this.laneChanges = new ArrayList<Track>();
	}

	public void registerLaneChange(Track track) {
		if(laneChangeGrid == null) {
			laneChangeGrid = new SpatialGrid2D<TrackSegment>(
					wrapped.pathSegmentGrid.getGridBox(),
					wrapped.pathSegmentGrid.getNumXCells(),
					wrapped.pathSegmentGrid.getNumYCells());;
		}

		wrapped.addTrackSegmentsToGrid(
				laneChangeGrid, 
				track, 
				TrackSegment.Type.PATH, 
				track.getPathSegments());
		
		this.laneChanges.add(track);
		buildVehicleConnectorGraph();
	}

	private void buildVehicleConnectorGraph() {
		vehicleConnectorGraph = new ConnectorGraph(this);
	}

	public Track getRoad(int roadID) {
		return wrapped.getRoad(roadID);
	}

	public List<Track> getTracksStartingFrom(int trackID) {
		return wrapped.getTracksStartingFrom(trackID);
	}

	public List<Track> getTracksStartingFrom(int trackID, double startingOffset) {
		return wrapped.getTracksStartingFrom(trackID, startingOffset);
	}

	public Collection<TrackSegment> getTracksByPathSegment(Coordinate p0,
			Coordinate p1) {
		Collection<TrackSegment> trackSegments = wrapped.getTracksByPathSegment(p0, p1);

		if(laneChangeGrid != null) {
			trackSegments.addAll(laneChangeGrid.getNearbyObjects(p0, p1));
		}
		
		return trackSegments;
	}

	public Collection<TrackSegment> getTracksByPathSegment(Envelope envelope) {
		Collection<TrackSegment> trackSegments = wrapped.getTracksByPathSegment(envelope);

		if(laneChangeGrid != null) {
			trackSegments.addAll(laneChangeGrid.getNearbyObjects(envelope));
		}
		
		return trackSegments;
	}

	public List<Track> getRoads() {
		List<Track> roads = new ArrayList<Track>();
		roads.addAll(wrapped.getRoads());
		roads.addAll(laneChanges);
		return roads;
	}

	public ConnectorGraph getConnectorGraph() {
		if(vehicleConnectorGraph != null) {
			return vehicleConnectorGraph;
		}
		
		return wrapped.getConnectorGraph();
	}

	public long getVersion() {
		return wrapped.getVersion();
	}

	public Position1D asPosition(TrackPoint1D vehiclePoint) {
		return wrapped.asPosition(vehiclePoint);
	}

	public boolean equals(Object obj) {
		return wrapped.equals(obj);
	}

	public ConflictAreaFinder getConflictFinder() {
		return wrapped.getConflictFinder();
	}

	public List<Track> getLanes(int trackID) {
		return wrapped.getLanes(trackID);
	}

	public TrackPoint1D translateToOtherLane(
			TrackPoint1D pos,
			LaneChangeDirection direction) {
		return wrapped.translateToOtherLane(pos, direction);
	}

	public int hashCode() {
		return wrapped.hashCode();
	}

	public String toString() {
		return wrapped.toString();
	}

}
