/**
 * 
 */
package dsg.rounda.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

import dsg.rounda.geometry.BezierCurve;
import dsg.rounda.serialization.text.TextSerializationManager;
import dsg.rounda.serialization.text.TextSerializer;
import dsg.rounda.services.roadmap.TrackPoint1D;

/**
 * Represents the physical road network
 */
public class TrackNetwork implements TrackProvider {

	private static final double RELATIVE_CURVE_START_OFFSET = 0.2;
	private static final double RELATIVE_CURVE_END_OFFSET = 0.8;

	long roadsVersion;
	final Map<Integer,Track> roadTable;
	final List<Track> roadList;
	final VehicleRouter router;

	int newRoadID;

	public TrackNetwork() {
		this.roadsVersion = 0;
		this.roadTable = new HashMap<Integer,Track>();
		this.roadList = new CopyOnWriteArrayList<Track>();
		this.router = new VehicleRouter(this);
		this.newRoadID = 0;
	}

	public TrackNetwork(TrackNetwork copy) {
		this.roadsVersion = copy.roadsVersion;
		this.roadTable = new HashMap<Integer,Track>(copy.roadTable);
		this.roadList = new CopyOnWriteArrayList<Track>(copy.roadList);
		this.router = new VehicleRouter(this);
		this.newRoadID = getMaxRoadID() + 1;
	}

	int getMaxRoadID() {
		int maxRoadID = -1;

		for(Track track : roadList) {
			if(track.getId() > maxRoadID) {
				maxRoadID = track.getId();
			}
		}

		return maxRoadID;
	}

	Track createLaneChange(TrackPoint1D laneChangeStartPoint, double laneChangeDistance, LaneChangeDirection direction) {

		Position1D laneChangeStartOnCurrentLane = new Position1D(
				getRoad(laneChangeStartPoint.getTrackID()), 
				laneChangeStartPoint.getOffset());
		
		Position1D laneChangeEndOnCurrentLane = router.route(laneChangeStartOnCurrentLane, new Trajectory1D(), laneChangeDistance);

		if(laneChangeEndOnCurrentLane.getTrack() == null) {
			// If you want to make a lane change of laneChangeDistance meters,
			// but your current trajectory does not have laneChangeDistance meters left.
			return null;
		}

		Position1D curveStartOnCurrentLane = router.route(laneChangeStartOnCurrentLane, new Trajectory1D(), laneChangeDistance * RELATIVE_CURVE_START_OFFSET);
		
		Position1D curveEndOnCurrentLane = router.route(laneChangeStartOnCurrentLane, new Trajectory1D(), laneChangeDistance * RELATIVE_CURVE_END_OFFSET);
		Position1D curveEndOnNewLane = translateOffset(curveEndOnCurrentLane, direction);
		Position1D laneChangeEndOnNewLane = translateOffset(laneChangeEndOnCurrentLane, direction);

		Pose2D changeStart = laneChangeStartOnCurrentLane.getPose2D();
		Pose2D changeDepart = curveStartOnCurrentLane.getPose2D();
		Pose2D changeArrive = curveEndOnNewLane.getPose2D();
		Pose2D changeEnd = laneChangeEndOnNewLane.getPose2D();

		BezierCurve laneChangeCurve = new BezierCurve(new Coordinate[] {
				changeStart.getPosition(),
				changeDepart.getPosition(),
				changeArrive.getPosition(),
				changeEnd.getPosition()
		});
		
		Track laneChange = new Track(newRoadID++, laneChangeCurve, direction, laneChangeDistance);
		laneChange.setFrom(new Connector(laneChangeStartOnCurrentLane.getTrackID(), laneChangeStartOnCurrentLane.getOffset()));
		laneChange.setTo(new Connector(laneChangeEndOnNewLane.getTrackID(), laneChangeEndOnNewLane.getOffset()));

		synchronized(this) {
			roadTable.put(laneChange.getId(), laneChange);
		}

		return laneChange;
	}

    public Position1D translateOffset(Position1D pos, LaneChangeDirection direction) {
    	Track reference = pos.getTrack();
    	
    	if(reference.getType() == TrackType.LANE_CHANGE) {

    		Position1D laneChangeStart = new Position1D(
    				getRoad(reference.getFrom().getRoad()), 
    				reference.getFrom().getOffset());
			
			Position1D startOfLaneChangeProjection = translateOffset(
					laneChangeStart, 
					direction);
			
			double relativeOffset = pos.getOffset() / reference.getPathLength();
			double translatedOffset = relativeOffset * reference.getLaneChangeDistance();

			return router.route(
					startOfLaneChangeProjection, 
					new Trajectory1D(), 
					translatedOffset);
    		
    	}
    	
    	Integer otherLaneID = direction == LaneChangeDirection.LEFT ? reference.getLeftLane() : reference.getRightLane();
    	
    	if(otherLaneID == null) {
    		return null;
    	}
    	
        Track other = getRoad(otherLaneID);

        return new Position1D(other, other.getPathLength() * pos.getOffset() / reference.getPathLength());
    }


	@Override
	public List<Track> getLanes(int trackID) {
		Track track = getRoad(trackID);
		List<Track> lanes = new LinkedList<Track>();

		lanes.add(track);

		for(Track currentTrack = track; 
				currentTrack.getLeftLane() != null; 
				currentTrack = getRoad(track.getLeftLane())) {

			lanes.add(0, getRoad(currentTrack.getLeftLane()));
		}

		for(Track currentTrack = track; 
				currentTrack.getRightLane() != null; 
				currentTrack = getRoad(track.getRightLane())) {

			lanes.add(getRoad(currentTrack.getRightLane()));
		}

		return lanes;
	}

	public synchronized void clear() {
		this.roadTable.clear();
		this.roadList.clear();
		this.roadsVersion = 0;
	}

	public synchronized void addRoad(Track road) {
		roadList.add(road);
		roadTable.put(road.getId(), road);
		roadsVersion++;

		if(road.getId() >= newRoadID) {
			newRoadID = road.getId()+1; 
		}
	}

	public synchronized Track getRoad(int roadID) {
		return this.roadTable.get(roadID);
	}

	public List<Track> getRoads() {
		return this.roadList;
	}

	public double getScenarioLength() {
		double sum = 0.0;

        for(Track track : roadList) {
        	sum += track.getPathLength();
        }
        
        return sum;
	}

	/**
	 * @return the roadsVersion
	 */
	public synchronized long getRoadsVersion() {
		return roadsVersion;
	}

	public synchronized void removeRoad(int segmentID) {
		Track road = roadTable.remove(segmentID);
		roadList.remove(road);
		roadsVersion++;
	}

	public void removeRoadWithoutVersionUpdate(int trackID) {
		roadTable.remove(trackID);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		WKTWriter wktWriter = new WKTWriter();

		for(Track road : roadList) {
			sb.append(road.getId());
			sb.append(' ');
			sb.append(road.getType());
			sb.append(' ');
			sb.append(road.getFrom() != null ? road.getFrom().getRoad() : -1);
			sb.append(' ');
			sb.append(road.getFrom() != null ? road.getFrom().getOffset() : 0.0);
			sb.append(' ');
			sb.append(road.getTo() != null ? road.getTo().getRoad() : -1);
			sb.append(' ');
			sb.append(road.getTo() != null ? road.getTo().getOffset() : 0.0);
			sb.append(' ');
			sb.append(road.getLeftLane() != null ? road.getLeftLane() : -1);
			sb.append(' ');
			sb.append(road.getRightLane() != null ? road.getRightLane() : -1);
			sb.append(' ');
			sb.append(wktWriter.write(road.getPath()));
			sb.append("\r\n");
		}

		return sb.toString();
	}

	public void addRoadsFromString(String text) throws ParseException {
		addRoadsFromLines(text.split("\n"));
	}

	public void addRoadsFromLines(String[] lines) throws ParseException {
		addRoadsFromLines(lines, 0, lines.length);
	}

	public void addRoadsFromLines(String[] lines, int start, int numLines) throws ParseException {
		Map<Integer,Connectors> conns = new HashMap<Integer,Connectors>();
		WKTReader geomParser = new WKTReader();

		// Add unconnected roads first
		for(int i = start; i < start + numLines; i++) {
			StringTokenizer st = new StringTokenizer(lines[i]);
			Connectors conn = new Connectors();
			int id = Integer.parseInt(st.nextToken());
			TrackType type = TrackType.valueOf(st.nextToken());
			conn.fromID = Integer.parseInt(st.nextToken());
			conn.fromOffset = Double.parseDouble(st.nextToken());
			conn.toID = Integer.parseInt(st.nextToken());
			conn.toOffset = Double.parseDouble(st.nextToken());
			Integer leftLane = Integer.parseInt(st.nextToken());
			Integer rightLane = Integer.parseInt(st.nextToken());
			String pathString = st.nextToken("\n");
			LineString path = (LineString) geomParser.read(pathString);

			Track newTrack = new Track(id,type,path);

			if(leftLane >= 0) {
				newTrack.setLeftLane(leftLane);
			}
			if(rightLane >= 0) {
				newTrack.setRightLane(rightLane);
			}

			addRoad(newTrack);

			conns.put(id, conn);
		}

		// Now that we know all roads, figure out connectors
		for(Map.Entry<Integer,Connectors> entry : conns.entrySet()) {
			Track road = getRoad(entry.getKey());
			Connectors conn = entry.getValue();

			if(conn.fromID != -1) {
				road.setFrom(new Connector(conn.fromID, conn.fromOffset));
			}
			if(conn.toID != -1) {
				road.setTo(new Connector(conn.toID, conn.toOffset));
			}
		}
	}

	static {
		TextSerializationManager.register(TrackNetwork.class, new TextSerializer<TrackNetwork>() {

			public String serialize(TrackNetwork network) {
				return network.toString();
			}

			@Override
			public TrackNetwork deserialize(String text) throws ParseException {
				TrackNetwork network = new TrackNetwork();
				network.addRoadsFromString(text);
				return network;
			}

		});

	}

	static class Connectors {
		int fromID;
		double fromOffset;
		int toID;
		double toOffset;
	}

}
