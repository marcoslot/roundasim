/**
 * 
 */
package dsg.rounda.services.roadmap;


/**
 * Edge in the graph of track ranges
 */
public class MapEdge {

    final MapNode from;
    final MapNode to;
    final TrackRange1D trackRange;
    final MapEdgeSet edgeSet;


    public MapEdge(MapNode from, MapNode to, TrackRange1D trackRange, MapEdgeSet edgeSet) {
        this.from = from;
        this.from.addFrom(this);
        this.to = to;
        this.to.addTo(this);
        this.trackRange = trackRange;
        this.edgeSet = edgeSet;
    }
    /**
     * @return the from
     */
    public MapNode getFrom() {
        return from;
    }
    /**
     * @return the to
     */
    public MapNode getTo() {
        return to;
    }
    /**
     * @return the trackRange
     */
    public TrackRange1D getTrackRange() {
        return trackRange;
    }
    /**
     * @return
     * @see dsg.rounda.services.roadmap.TrackRange1D#getStart()
     */
    public double getStart() {
        return trackRange.getStart();
    }
    /**
     * @return
     * @see dsg.rounda.services.roadmap.TrackRange1D#getEnd()
     */
    public double getEnd() {
        return trackRange.getEnd();
    }
    public int getTrackID() {
        return trackRange.getTrackID();
    }
    public double getLength() {
        return trackRange.getLength();
    }
    public MapNode other(MapNode node) {
        if(node == from) {
            return to;
        } 
        if(node == to) {
            return from;
        }
        return null;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "MapEdge[from=" + from + ", to=" + to + ", trackRange=" + trackRange + "]";
    }


    /**
     * @return the edgeSet
     */
    public MapEdgeSet getEdgeSet() {
        return edgeSet;
    }
    
}
