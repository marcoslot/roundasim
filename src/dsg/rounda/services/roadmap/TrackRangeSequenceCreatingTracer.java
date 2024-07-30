/**
 * 
 */
package dsg.rounda.services.roadmap;

import java.util.ArrayList;
import java.util.List;

import dsg.rounda.model.VehicleRouter;

/**
 * @author Niall
 *
 */
public class TrackRangeSequenceCreatingTracer implements VehicleRouter.Tracer {

    final List<TrackRange1D> result;
	final VehicleTrackMap trackMap;

    int startTrackID;
    double startOffset;

	/**
	 * 
	 */
	public TrackRangeSequenceCreatingTracer(VehicleTrackMap trackMap) {
		this.trackMap = trackMap;
		this.result = new ArrayList<TrackRange1D>();
	}

	@Override
	public void startOnTrack(int trackID, double startOffset) {
        this.startTrackID = trackID;
        this.startOffset = startOffset;
	}

	@Override
	public void endOnTrack(int trackID, double endOffset, boolean trackChange) {
        result.add(new TrackRange1D(startTrackID, startOffset, endOffset));
	}
	
	public TrackRangeSequence getTrackRangeSequence() {
		return new TrackRangeSequence(trackMap, result);
	}
}
