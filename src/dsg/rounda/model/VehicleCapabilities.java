/**
 * 
 */
package dsg.rounda.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import dsg.rounda.config.VehicleConfig;
import dsg.rounda.logging.VehicleEventLog;
import dsg.rounda.services.ServiceFactory;
import dsg.rounda.services.roadmap.VehicleTrackMap;

/**
 * The capabilities of a vehicle exposed to controllers
 */
public class VehicleCapabilities {

    final int id;
    final VehicleProperties properties;
    final Clock clock;
    final Scheduler scheduler;
    final NetworkAdapter network;
    final LocalizationSensors localizationSensors;
    final RangingSensors rangingSensors;
    final IndicatorDetector indicatorDetector;
    final Actuators actuators;
    final VehicleTrackMap roadMap;
    final InfrastructureMap infrastructure;
    final Random random;
    final VehicleEventLog eventLog;
    final VehicleConfig config;
    
    final Map<Class<?>,Object> services;
    
    public VehicleCapabilities(
            int id, 
            VehicleProperties properties,
            VehicleConfig config,
            Clock clock, 
            Scheduler scheduler,
            NetworkAdapter network, 
            LocalizationSensors localizationSensors, 
            RangingSensors rangingSensors,
            IndicatorDetector indicatorDetector,
            Actuators actuators,
            VehicleTrackMap roadMap,
            InfrastructureMap infrastructure,
            Random random,
            VehicleEventLog eventLog) {
        this.id = id;
        this.properties = properties;
        this.config = config;
        this.clock = clock;
        this.scheduler = scheduler;
        this.network = network;
        this.localizationSensors = localizationSensors;
        this.rangingSensors = rangingSensors;
        this.indicatorDetector = indicatorDetector;
        this.actuators = actuators;
        this.roadMap = roadMap;
        this.infrastructure = infrastructure;
        this.random = random;
        this.eventLog = eventLog;
        this.services = new HashMap<Class<?>,Object>();
    }
    
    /**
     * @return the properties
     */
    public VehicleProperties getProperties() {
        return properties;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
    /**
     * @return the clock
     */
    public Clock getClock() {
        return clock;
    }
    /**
     * @return the scheduler
     */
    public Scheduler getScheduler() {
        return scheduler;
    }
    /**
     * @return the network
     */
    public NetworkAdapter getNetwork() {
        return network;
    }
    /**
     * @return the sensors
     */
    public LocalizationSensors getLocalizationSensors() {
        return localizationSensors;
    }
    /**
     * @return the actuators
     */
    public Actuators getActuators() {
        return actuators;
    }
    /**
     * @return the roadMap
     */
    public VehicleTrackMap getRoadMap() {
        return roadMap;
    }
    /**
     * 
     * @return
     */
    public Random getRandom() {
        return random;
    }
    /**
     * @return the rangingSensors
     */
    public RangingSensors getRangingSensors() {
        return rangingSensors;
    }
    /**
     * @return the eventLog
     */
    public VehicleEventLog getEventLog() {
        return eventLog;
    }
    /**
     * 
     * @return the config
     */
    public VehicleConfig getConfig() {
        return config;
    }
    
    public IndicatorDetector getIndicatorDetector() {
		return indicatorDetector;
	}

	/**
     * Get a registered service instance by its type
     * 
     * @param serviceType the service type
     * @return the service instance
     * @throws RuntimeException if the service instance is not registered
     */
    public <T> T getService(Class<T> serviceType) {
        T result = (T) services.get(serviceType);
        
        if(result == null) {
            ServiceFactory factory = ServiceFactory.CHOOSER.get(serviceType);
            
            if(factory == null) {
                throw new IllegalArgumentException("No factory defined for class " + serviceType.getName());
            }
            
            result = (T) factory.create(this);
            services.put(serviceType, result);
        }
        
        return result;
    }

    public InfrastructureMap getInfrastructureMap() {
        return infrastructure;
    }

}
