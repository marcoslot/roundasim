/**
 * 
 */
package dsg.rounda.scenarios;

import dsg.rounda.util.Chooser;

/**
 * This class allows you to select a scenario.
 */
public class ScenarioChooser extends Chooser<String,ScenarioFactory> {

    /**
     * A scenario chooser containing the built-in scenarios.
     * Can be expanded.
     */
    public static ScenarioChooser DEFAULT = new ScenarioChooser();
    static {
        DEFAULT.add("default", ArcDeTriompheScenario.FACTORY);
        DEFAULT.add("arc", ArcDeTriompheScenario.FACTORY);
        DEFAULT.add("builder", BuilderScenario.FACTORY);
        DEFAULT.add("traffic-lights", TrafficLightJunctionScenario.FACTORY);
    }
    
}
