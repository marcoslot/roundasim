/**
 * 
 */
package dsg.rounda.model;

import java.util.List;

/**
 * Generic interface for retrieving road information
 */
public interface TrackProvider {
    /**
     * Get all tracks
     * @return all track
     */
    List<Track> getRoads();
    /**
     * Get a specific track
     * @param trackID the identifier of the track
     * @return the track
     */
    Track getRoad(int trackID);
    
    /**
     * Get the set of lanes that a track is part of
     * 
     * @param trackID the identifier of the track
     * @return the set of lanes from left to right
     */
    List<Track> getLanes(int trackID);
}
