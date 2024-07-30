/**
 * 
 */
package dsg.rounda.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author slotm
 *
 */
public class Trajectory1D {

    final LinkedList<Track> nextTracks;

    /**
     * 
     */
    public Trajectory1D() {
        this.nextTracks = new LinkedList<Track>();
    }

    /**
     * 
     */
    public Trajectory1D(Trajectory1D traj) {
        this.nextTracks = new LinkedList<Track>(traj.nextTracks);
    }

    /**
     * 
     */
    public Trajectory1D(List<Track> traj) {
        this.nextTracks = new LinkedList<Track>(traj);
    }
    
    List<Track> getTracks() {
        return Collections.unmodifiableList(this.nextTracks);
    }

    /**
     * 
     * @return
     */
    public Track getNextTrack() {
        return nextTracks.peek();
    }

    /**
     * 
     * @return
     */
    public Track popNextTrack() {
        return nextTracks.remove();
    }
    
    /**
     * @param track
     */
    public void addTrackToFollow(Track track) {
        nextTracks.add(track);
    }
    
    public void replace(List<Track> tracks) {
        clear();
        nextTracks.addAll(tracks);
    }
    
    public void clear() {
        nextTracks.clear();
    }

}
