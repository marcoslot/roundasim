/**
 * 
 */
package dsg.rounda.gui;

/**
 * Interface to a GUI display details for an individual vehicle
 */
public interface VehicleConsoleView {
    
    public interface Presenter {
        
    }
    
    void setPresenter(Presenter presenter);
    void setID(Integer id);
    void setReceivedMessageCount(Long tagCount);
    void setSentMessageCount(Long tagCount);
    void setAllocatedDistance(Double distance);
    void setAvailableDistance(Double distance);
    void setVelocity(Double velocity);
    void setTrackID(Long trackID);
    void setOffset(Double offset);

}
