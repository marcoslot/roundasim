/**
 * 
 */
package dsg.rounda.services.coordination;

import dsg.rounda.services.roadmap.TrackRange1D;

/**
 * A range in a conflict area
 */
public class ConflictRange {

    public enum Type {
        WEAK,
        STRONG;
    }
    
    public static Type type(boolean isStrong) {
        return isStrong ? Type.STRONG : Type.WEAK;
    }

    final Type type;
    final TrackRange1D range;

    public ConflictRange(boolean isStrong, TrackRange1D range) {
        this.type = type(isStrong);
        this.range = range;
    }

    public ConflictRange(Type type, TrackRange1D range) {
        this.type = type;
        this.range = range;
    }

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * @return the range
     */
    public TrackRange1D getRange() {
        return range;
    }

    public int getTrackID() {
        return range.getTrackID();
    }

    public boolean isWeak() {
        return getType() == Type.WEAK;
    }

    public boolean isStrong() {
        return getType() == Type.STRONG;
    }
    
}
