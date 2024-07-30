/**
 * 
 */
package dsg.rounda.services.sensing.distance;

import dsg.rounda.services.roadmap.TrackPoint1D;
import dsg.rounda.services.roadmap.TrackRange1D;
import dsg.rounda.services.roadmap.TrackRangeSequence;

/**
 * @author Niall
 * 
 */
public class LaneGap {
	boolean isValid;
	TrackRangeSequence gap;
	Double successorVelocity;
	Double predecessorVelocity;

	public LaneGap(TrackRangeSequence gap) {
		super();
		this.gap = gap;
	}

	public boolean updateGap(TrackRangeSequence update, double delay) {
		if (movesTheGapForward(update)) {
			successorVelocity = gap.getDistanceFromStart(update.getStart()) / delay;
			predecessorVelocity = update.getDistanceFromEnd(gap.getEnd()) / delay;
			isValid = true;
		} else {
			isValid = false;
		}
		gap = update;
		return isValid;
	}

	public boolean movesTheGapForward(TrackRangeSequence update) {
		if(!gap.contains(update.getStart())) {
			return false;
		}
		if(!update.contains(gap.getEnd())) {
			return false;
		}
		return true;
	}

	public Double getSuccessorVelocity() {
		return successorVelocity;
	}

	public Double getPredecessorVelocity() {
		return predecessorVelocity;
	}

	public boolean isValid() {
		return isValid;
	}

	public Double getSuccessorDistance(TrackPoint1D reference) {
		return gap.getDistanceFromStart(reference);
	}

	public Double getPredecessorDistance(TrackPoint1D reference) {
		Double succDistance = getSuccessorDistance(reference);
		return succDistance != null ? gap.getLength() - succDistance : null;
	}

	public double getLength() {
		return gap.getLength();
	}

	public TrackPoint1D getPredecessorPosition() {
		return gap.getEnd();
	}

}
