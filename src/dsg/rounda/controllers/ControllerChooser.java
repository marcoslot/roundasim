/**
 * 
 */
package dsg.rounda.controllers;

import dsg.rounda.util.Chooser;

/**
 * This class allows you to select a controller.
 */
public class ControllerChooser extends Chooser<String,VehicleControllerFactory> {

    /**
     * A controller chooser containing the built-in scenarios.
     * Can be expanded.
     */
    public static ControllerChooser DEFAULT = new ControllerChooser();
    static {
        DEFAULT.add("default", HighwayController.FACTORY);
        DEFAULT.add("dizzy", DizzyController.FACTORY);
        DEFAULT.add("vertigo", AutonomousVertigoController.FACTORY);
        DEFAULT.add("highway", HighwayController.FACTORY);
        DEFAULT.add("traffic-lights", TrafficLightHumanController.FACTORY);
    }
    
}
