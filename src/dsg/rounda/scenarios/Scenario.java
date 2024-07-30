/**
 * 
 */
package dsg.rounda.scenarios;

import dsg.rounda.SimRun;
import dsg.rounda.config.RunConfig;
import dsg.rounda.io.FileIO;
import dsg.rounda.model.Clock;
import dsg.rounda.model.Scheduler;
import dsg.rounda.model.VehicleFactory;
import dsg.rounda.model.WorldState;
import dsg.rounda.model.WorldView;

/**
 * A class implementing this interface manages the creation
 * and destruction of buildings, roads, vehicles, and 
 * infrastructure. It can also generate global events. 
 */
public interface Scenario {
    
    /**
     * Initialize a scenario with the given components
     * 
     * @param run
     * @param vehicleFactory
     * @param fileIO
     */
    void init(SimRun run, VehicleFactory vehicleFactory, FileIO fileIO, Runnable readyHandler);
    
    /**
     * Returns the initial world view to display for the scenario
     * 
     * @return the initial world view to display for the scenario
     */
    WorldView getInitialWorldView();

    /**
     * Returns the name of a background image
     * 
     * @return a filename of a png or null
     */
    String getBackground();


}
