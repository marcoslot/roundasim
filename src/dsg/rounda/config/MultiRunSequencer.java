/**
 * 
 */
package dsg.rounda.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dsg.rounda.controllers.ControllerChooser;
import dsg.rounda.scenarios.ScenarioChooser;
import static dsg.rounda.serialization.text.TextSerializationManager.serialize;

/**
 * Sequences a MultiRunConfig to multiple run configs
 */
public class MultiRunSequencer implements Iterable<RunConfig>, SimulationParameters {

    final MultiRunConfig config;

    ScenarioChooser scenarioChooser;
    ControllerChooser controllerChooser;
    
    /**
     * 
     */
    public MultiRunSequencer(MultiRunConfig config) {
        this.config = config;
        this.scenarioChooser = ScenarioChooser.DEFAULT;
        this.controllerChooser = ControllerChooser.DEFAULT;
    }
    

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Iterator<RunConfig> iterator() {
        final List<RangeSequencer<?>> sequencers = new ArrayList<RangeSequencer<?>>();

        for(Map.Entry<String,Range<?>> entry : config.getRangeValues().entrySet()) {
            Parameter<?> parameter = ParameterManager.get(entry.getKey());
            RangeSequencer sequencer = new RangeSequencer(entry.getValue(), parameter);
            sequencers.add(sequencer);
        }

        final List<RunConfig> configs = new ArrayList<RunConfig>();
        
        return new Iterator<RunConfig>() {
            boolean isDone = descend(0, sequencers, configs);

            @Override
            public boolean hasNext() {
                return !(isDone && configs.size() == 0);
            }

            @Override
            public RunConfig next() {
                RunConfig config = configs.get(0);
                configs.clear();
                
                if(!isDone) {
                    isDone = descend(0, sequencers, configs);
                }
                return config;
            }

            @Override
            public void remove() {
            }
            
        };
    }

    
    private boolean descend(
            int index, 
            List<RangeSequencer<?>> sequencers,
            List<RunConfig> configs) {
        if(index >= sequencers.size()) {
            // We've reached a leaf of the tree
            // Create a new config now
            configs.add(createBaseConfig());
            return true;
        }
        
        // sequencers lower down the tree set their config first
        boolean treeDone = descend(index+1, sequencers, configs);

        // Get the sequencer for this range
        RangeSequencer<?> sequencer = sequencers.get(index);

        // Get the latest config
        RunConfig config = configs.get(configs.size()-1);
        
        // Update the config
        sequencer.set(config);

        if(sequencer.getNumValues() > 1) {
            String runName = config.get(RUN_ID);
            
            if(runName == null) {
                runName = "";
            } else {
                runName += ",";
            }
            
            runName += sequencer.getName() + "=";
            runName += serialize(sequencer.current());
            
            config.set(RUN_ID, runName);
        }

        if(!"repeat".equals(sequencer.getName()) && sequencer.getNumValues() > 1) {
            String runName = config.get(RUN_SET_ID);
            
            if(runName == null) {
                runName = "";
            } else {
                runName += ",";
            }
            
            runName += sequencer.getName() + "=";
            runName += serialize(sequencer.current());
            
            config.set(RUN_SET_ID, runName);
        }

        if(treeDone) {
            // All sequencers below us have finished
            if(sequencer.hasNext()) {
                // We still have work remaining, set it to current
                sequencer.next();
                return false;
            } else {
                // Sequencers below us are done, we're done
                // Prepare to start over for the next round
                // Will never happen if we're root
                sequencer.reset();
                return true;
            }
        } else {
            // Sequencers below us still have work to do
            return false;
        }
    }

    private RunConfig createBaseConfig() {
        RunConfig baseConfig = new RunConfig();
        baseConfig.set(config.getSingleValues());
        baseConfig.setControllerChooser(controllerChooser);
        baseConfig.setScenarioChooser(scenarioChooser);
        return baseConfig;
    }

    /**
     * @param scenarioChooser the scenarioChooser to set
     */
    public void setScenarioChooser(ScenarioChooser scenarioChooser) {
        this.scenarioChooser = scenarioChooser;
    }

    /**
     * @param controllerChooser the controllerChooser to set
     */
    public void setControllerChooser(ControllerChooser controllerChooser) {
        this.controllerChooser = controllerChooser;
    }

    public static void main(String[] args) {
        MultiRunConfig config = new MultiRunConfig();
        config.set(SEED, new LongRange(0L,1L));
        config.set(SCENARIO_NAME, new StringRange("ozz","wizz"));
        config.set(REPEAT, new RepeatRange(2));
        
        for(RunConfig rc : config.getSequencer()) {
            System.out.println(rc);
        }
    }

}
