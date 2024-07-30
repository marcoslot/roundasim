/**
 * 
 */
package dsg.rounda.model;

/**
 * The type of track.
 */
public enum TrackType {
    
    /**
     * A regular track
     */
    NORMAL,
    
    /**
     * A track representing a lane from which you
     * can change to a neighbouring lane
     */
    LANE,
    
    /**
     * A track representing a change from one lane
     * to another.
     */
    LANE_CHANGE;

}
