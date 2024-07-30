/**
 * 
 */
package dsg.rounda.scenarios;


/**
 * Create scenarios
 */
public interface ScenarioFactory {

    /**
     * Create a scenario instance
     * 
     * @return a new scenario instance
     */
    Scenario create();
    
    /**
     * Get the title of this scenario
     * 
     * @return the title
     */
    String getTitle();
    
    /**
     * Get a textual description of this scenario
     * 
     * @return the description
     */
    String getDescription();
}
