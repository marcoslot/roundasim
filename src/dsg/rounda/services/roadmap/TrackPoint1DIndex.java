/**
 * 
 */
package dsg.rounda.services.roadmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author slotm
 *
 */
public class TrackPoint1DIndex {

    final List<TrackPoint1D> allPoints;
    final Map<Integer,List<TrackPoint1D>> pointIndex;
    
    public TrackPoint1DIndex() {
        this.allPoints = new ArrayList<TrackPoint1D>();
        this.pointIndex = new HashMap<Integer,List<TrackPoint1D>>();
    }
    
    public List<TrackPoint1D> getAllTrackPoints() {
        return new ArrayList<TrackPoint1D>(allPoints);
    }

    public void add(TrackPoint1D point) {
        int trackID = point.getTrackID();
        List<TrackPoint1D> trackPoints = getOrCreateTrackPoints(trackID);
        trackPoints.add(point);
        allPoints.add(point);
    }
    
    List<TrackPoint1D> getOrCreateTrackPoints(int trackID) {
        List<TrackPoint1D> trackPoints = pointIndex.get(trackID); 
        
        if(trackPoints == null) {
            trackPoints = new ArrayList<TrackPoint1D>();
            pointIndex.put(trackID, trackPoints);
        }
        
        return trackPoints;
    }

    public List<TrackPoint1D> getTrackPoints(int trackID) {
    	List<TrackPoint1D> result = pointIndex.get(trackID);
    	return result == null ? new ArrayList<TrackPoint1D>() : result;
    }
}
