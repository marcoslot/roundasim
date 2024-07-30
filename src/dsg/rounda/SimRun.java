/**
 * 
 */
package dsg.rounda;

import java.util.Random;

import dsg.rounda.config.RunConfig;
import dsg.rounda.controllers.VehicleControllerFactory;
import dsg.rounda.logging.EventLog;
import dsg.rounda.model.Clock;
import dsg.rounda.model.InfrastructureMap;
import dsg.rounda.model.Scheduler;
import dsg.rounda.model.WorldState;
import dsg.rounda.scenarios.Scenario;
import dsg.rounda.stats.SimStats;

/**
 * Objects associated with a single simulation run 
 */
public class SimRun {

    final RunConfig config;
    final Clock clock;
    final VehicleControllerFactory controllerFactory;
    final Scenario scenario;
    final WorldState world;
    final EventLog eventLog;
    final Scheduler scheduler;
    final Random random;
    final SimStats stats;
    final InfrastructureMap infrastructure;
    
    /**
     * @param config
     * @param controllerFactory
     * @param scenario
     * @param world
     * @param eventLog
     * @param scheduler
     * @param random
     */
    public SimRun(RunConfig config) {
        this.config = config;
        this.clock = new SimTimeClock();
        this.eventLog = new EventLog(clock);
        this.scheduler = new Scheduler(clock);
        this.controllerFactory = config.getControllerFactory();
        this.scenario = config.getScenario();
        this.world = new WorldState(eventLog);
        this.stats = new SimStats(config, eventLog);
        this.infrastructure = new InfrastructureMap();
        this.random = config.getSeed() != null ? new Random(config.getSeed()) : new Random();
    }

    /**
     * @return the config
     */
    public RunConfig getConfig() {
        return config;
    }

    /**
     * @return the clock
     */
    public Clock getClock() {
        return clock;
    }

    /**
     * @return the controllerFactory
     */
    public VehicleControllerFactory getControllerFactory() {
        return controllerFactory;
    }

    /**
     * @return the scenario
     */
    public Scenario getScenario() {
        return scenario;
    }

    /**
     * @return the world
     */
    public WorldState getWorld() {
        return world;
    }

    /**
     * @return the eventLog
     */
    public EventLog getEventLog() {
        return eventLog;
    }

    /**
     * @return the scheduler
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * @return the random
     */
    public Random getRandom() {
        return random;
    }

    /**
     * @return the stats
     */
    public SimStats getStats() {
        return stats;
    }

    public InfrastructureMap getInfrastructureMap() {
        return infrastructure;
    }

    

}
