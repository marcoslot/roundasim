/**
 * 
 */
package dsg.rounda.config;

import static dsg.rounda.serialization.text.TextSerializationManager.deserialize;

import java.util.HashMap;
import java.util.Map;

import dsg.rounda.TimeMode;
import dsg.rounda.controllers.ControllerChooser;
import dsg.rounda.controllers.VehicleControllerFactory;
import dsg.rounda.scenarios.Scenario;
import dsg.rounda.scenarios.ScenarioChooser;

/**
 * Configuration of a simulation run
 */
public class RunConfig implements SimulationParameters, VehicleConfig {

    private final Map<String,Object> parameters;

    private Scenario scenario;
    private ScenarioChooser scenarioChooser;
    private ControllerChooser controllerChooser;
    
    /**
     * 
     */
    public RunConfig() {
        this.parameters = new HashMap<String,Object>();
        this.scenarioChooser = ScenarioChooser.DEFAULT;
        this.controllerChooser = ControllerChooser.DEFAULT;
        this.scenario = null;
        set(SCENARIO_NAME);
        set(CONTROLLER_NAME);
    }
    
    public RunConfig(Map properties) throws Exception {
        this();
        setProperties(properties);
    }

    public void setProperties(Map properties) throws Exception {
        for(Object entryObj : properties.entrySet()) {
            Map.Entry entry = (Map.Entry) entryObj;
            String key = (String) entry.getKey();
            String stringValue = (String) entry.getValue();
            Parameter parameter = ParameterManager.get(key);
            Class<?> type = parameter.getType();
            parameters.put(key, deserialize(type, stringValue));
        }
    }

    public <T> void set(Parameter<T> param) {
        this.parameters.put(param.getConfigKey(), param.getDefaultValue());
    }

    public <T> void set(Parameter<T> param, T value) {
        this.parameters.put(param.getConfigKey(), value);
    }

    public <T> void set(String key, T value) {
        this.parameters.put(key, value);
    }

    @Override
    public <T> T get(Parameter<T> param) {
        return (T) (parameters.containsKey(param.getConfigKey()) ? get(param.getConfigKey()) : param.getDefaultValue());
    }

    public <T> T get(String key) {
        return (T) parameters.get(key);
    }
    
    /**
     * 
     * @return
     */
    public Scenario getScenario() {
        if(scenario == null) {
            scenario = scenarioChooser.get(getScenarioName()).create();
        }
        return scenario;
    }
    
    /**
     * 
     * @return
     */
    public VehicleControllerFactory getControllerFactory() {
        return controllerChooser.get(getControllerName());
    }

    /**
     * @return the scenario
     */
    public String getScenarioName() {
        return get(SCENARIO_NAME);
    }

    /**
     * @param scenario the scenario to set
     */
    public void setScenarioName(String scenario) {
        set(SCENARIO_NAME, scenario);
    }

    /**
     * @return the controller
     */
    public String getControllerName() {
        return get(CONTROLLER_NAME);
    }

    /**
     * @param controller the controller to set
     */
    public void setControllerName(String controller) {
        set(CONTROLLER_NAME, controller);
    }

    /**
     * @return the seed
     */
    public Long getSeed() {
        return get(SEED);
    }

    /**
     * @param seed the seed to set
     */
    public void setSeed(Long seed) {
        set(SEED, seed);
    }

    /**
     * @return the duration
     */
    public Long getDuration() {
        return get(DURATION);
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(Long duration) {
        set(DURATION, duration);
    }

    public long getDuration(long def) {
        return getDuration() != null ? getDuration() : def;
    }

    /**
     * @return the scenarioChooser
     */
    public ScenarioChooser getScenarioChooser() {
        return scenarioChooser;
    }

    /**
     * @param scenarioChooser the scenarioChooser to set
     */
    public void setScenarioChooser(ScenarioChooser scenarioChooser) {
        this.scenarioChooser = scenarioChooser;
    }

    /**
     * @return the controllerChooser
     */
    public ControllerChooser getControllerChooser() {
        return controllerChooser;
    }

    /**
     * @param controllerChooser the controllerChooser to set
     */
    public void setControllerChooser(ControllerChooser controllerChooser) {
        this.controllerChooser = controllerChooser;
    }

    /**
     * @return the timeMode
     */
    public TimeMode getTimeMode() {
        return get(TimeMode.PARAM);
    }

    /**
     * @param timeMode the timeMode to set
     */
    public void setTimeMode(TimeMode timeMode) {
        set(TimeMode.PARAM, timeMode);
    }

    /**
     * @return the maxVelocity
     */
    public Double getMaxVelocity() {
        return get(MAX_VELOCITY);
    }

    /**
     * @return the maxVelocity
     */
    public double getMaxVelocity(double def) {
        return getMaxVelocity() != null ? getMaxVelocity() : def;
    }

    /**
     * @param maxVelocity the maxVelocity to set
     */
    public void setMaxVelocity(Double maxVelocity) {
        set(MAX_VELOCITY, maxVelocity);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "RunConfig [parameters=" + parameters + "]";
    }

    public <T> void set(Map<String,T> map) {
        for(Map.Entry<String,T> entry : map.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }


}
