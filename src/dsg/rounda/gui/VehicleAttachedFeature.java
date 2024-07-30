/**
 * 
 */
package dsg.rounda.gui;

/**
 * A communication feature in the GUI
 */
public class VehicleAttachedFeature {

    final int sourceID;
    final int destinationID;
    /**
     * @param sourceID
     * @param destinationID
     * @param expires
     */
    public VehicleAttachedFeature(int sourceID, int destinationID) {
        this.sourceID = sourceID;
        this.destinationID = destinationID;
    }
    /**
     * @return the sourceID
     */
    public int getSourceID() {
        return sourceID;
    }
    /**
     * @return the destinationID
     */
    public int getDestinationID() {
        return destinationID;
    }
    
}
