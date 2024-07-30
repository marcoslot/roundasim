/**
 * 
 */
package dsg.rounda;

import dsg.rounda.config.RunConfig;
import dsg.rounda.config.SimulationParameters;
import dsg.rounda.controllers.VehicleController;
import dsg.rounda.io.FileIO;
import dsg.rounda.logging.Event;
import dsg.rounda.logging.EventHandler;
import dsg.rounda.logging.EventLog;
import dsg.rounda.logging.VehicleEventLog;
import dsg.rounda.model.Actuators;
import dsg.rounda.model.IndicatorDetector;
import dsg.rounda.model.LocalizationSensors;
import dsg.rounda.model.Network;
import dsg.rounda.model.NetworkAdapter;
import dsg.rounda.model.RangingSensors;
import dsg.rounda.model.RangingSensorsSpecification;
import dsg.rounda.model.VehicleCapabilities;
import dsg.rounda.model.VehicleFactory;
import dsg.rounda.model.VehicleState;
import dsg.rounda.services.roadmap.TrackMap;
import dsg.rounda.services.roadmap.VehicleTrackMap;
import dsg.rounda.stats.SimStats;
import dsg.rounda.stats.VehicleStats;
import dsg.rounda.stats.WeightedDoubleValue;
import dsg.rounda.swans.SwansNetwork;
/**
 * Implements the main simulation logic
 */
public class SimController implements SimulationParameters {

    static final Runnable NOOP = new Runnable() {public void run() {}};

    final RunConfig config;
    SimRun run;

    TrackMap roadMap;
    Network network;

    final private Object activeLock;
    private boolean active;

    final private Object playSpeedLock;
    private double playSpeed;

    private FileIO fileIO;

    public SimController(
            RunConfig config,
            FileIO fileIO) {
        this.config = config;
        this.fileIO = fileIO;

        this.playSpeedLock = new Object();
        this.playSpeed = 1.0;

        this.activeLock = new Object();
        this.active = false;
    }

    public void init() {
        init(NOOP);
    }

    /**
     * Must be called before using SimController
     *  
     * @param readyHandler called when the SimController is ready to use
     */
    public void init(Runnable readyHandler) {
        setActive(false);

        this.run = new SimRun(config);
        this.network = new SwansNetwork(run);
        this.roadMap = new TrackMap(run.getWorld().getRoadNetwork());

        run.getEventLog().addHandler(EventLog.acceptTag("bye"), vehicleScraper);
        run.getStats().init();

        run.getScenario().init(
                run,
                vehicleFactory,
                fileIO,
                readyHandler
                );
    }


    /**
     * Vehicle factory interface for the scenario
     */
    VehicleFactory vehicleFactory = new VehicleFactory() {

        @Override
        public void createVehicle(VehicleState vehicleState, RangingSensorsSpecification rangingSensorSpec) {
            // Event log for vehicle
            VehicleEventLog vehicleEventLog = new VehicleEventLog(vehicleState.getId(), run.getEventLog());
            vehicleEventLog.log("vehicle-create", run.getClock().getTime());
            vehicleEventLog.log("vehicle-count", run.getWorld().getNumberOfVehicles());

            // Add vehicle to the world
            run.getWorld().addVehicle(vehicleState);

            VehicleTrackMap vehicleMap = new VehicleTrackMap(roadMap);
            
            // Build interface for the controller
            VehicleCapabilities capabilities = new VehicleCapabilities(
                    vehicleState.getId(),
                    // the vehicle state provides properties
                    vehicleState,
                    run.getConfig(),
                    run.getClock(),
                    run.getScheduler(),
                    new NetworkAdapter(vehicleState.getId(), network, run.getClock(), run.getScheduler(), vehicleEventLog),
                    new LocalizationSensors(run.getWorld(), vehicleState, vehicleMap),
                    new RangingSensors(run.getWorld(), run.getClock(), run.getScheduler(), vehicleState, rangingSensorSpec),
                    new IndicatorDetector(run.getWorld(), vehicleState, vehicleMap),
                    new Actuators(run.getWorld().getRoadNetwork(), vehicleState),
                    // Use the same road map for all vehicles for efficiency
                    vehicleMap,
                    run.getInfrastructureMap(),
                    run.getRandom(),
                    vehicleEventLog
                    );

            // Create and start the brains
            VehicleController controller = run.getControllerFactory().createController(capabilities);
            controller.start();

        }
    };


    final EventHandler vehicleScraper = new EventHandler() {
        @Override
        public void event(Event event) {
            int vehicleID = (Integer) event.getSource();

            // Prevent any pending events for this vehicle from being executed
            run.getScheduler().removeEventsWithOwner(vehicleID);

            // This will prevent in-flight receives from being scheduled
            run.getScheduler().blackListOwner(vehicleID);

            // Remove from the network
            network.removeAdapter(vehicleID);

            run.getEventLog().log(vehicleID, "vehicle-destroy", run.getClock().getTime());
            run.getEventLog().log(vehicleID, "vehicle-count", run.getWorld().getNumberOfVehicles());

            VehicleStats stats = run.getStats().getOrCreateVehicleStats(vehicleID);

            // After the destroy we can get the age from vehicle-create
            double ageNanos = stats.getDoubleValue("vehicle-create", "timer");
            double ageSeconds = ageNanos / Constants.SECONDS;
            double distanceDrivenMeters = stats.getDoubleValue("distance-driven", "sum");
            
            run.getEventLog().log(vehicleID, "vehicle-age", ageSeconds);
            run.getEventLog().log(vehicleID, "velocity-km", 3.6 * distanceDrivenMeters / ageSeconds);

            Long commitTime = stats.getLongValue("commit-time", "first");
            Long queryStartTime = stats.getLongValue("query-start-time", "first");

            if(commitTime != null && queryStartTime != null) {
                double vertigoTimeNanos = commitTime - queryStartTime;
                run.getEventLog().log(vehicleID, "vertigo-time", vertigoTimeNanos / Constants.SECONDS);
            }
        }

    };

    /**
     * Progress the simulation by elapsedRealTime * getPlaySpeed() simulation
     * nanoseconds. May exit early if setActive() or setPlaySpeed() is called.
     * 
     * @param elapsedRealTime time in nanoseconds
     */
    public void step(long elapsedRealTime) {
        // Get shared variables 
        double localPlaySpeed = getPlaySpeed();
        long localSimTime = run.getClock().getTime();

        // Translate elapsed real time to elapsed simulation time based on play speed
        long elapsedSimTime = (long) (elapsedRealTime * localPlaySpeed);

        // The time at the end of this round 
        long endOfRoundSimTime = localSimTime + elapsedSimTime;

        // Get the simulation time for the next event time.
        // Next event time is null if there are no more events to schedule,
        // in which case cars turn into mindless zombies.
        Long nextEventTime = run.getScheduler().getNextEventTime();

        while(nextEventTime != null && nextEventTime <= endOfRoundSimTime) {
            if(!isActive()) {
                // No longer active, stop immediately to avoid
                // having to wait for a long event loop.
                // Break out of main loop, because we do not
                // want to progress time to endOfRoundSimTime 
                // without executing events first.
                return;
            }
            if(getPlaySpeed() != localPlaySpeed) {
                // Play speed has changed, restart round
                return;
            }

            // Time the world needs to progress before executing next event
            long simTimeBeforeEvent = nextEventTime - localSimTime;

            // Progress continuous time world
            run.getWorld().progress(simTimeBeforeEvent);

            // Update the local and shared clock
            run.getClock().setTime(localSimTime = nextEventTime);

            // Run instant event
            run.getScheduler().runEvent();

            nextEventTime = run.getScheduler().getNextEventTime();
        }

        // Progress continuous time world until end of round
        run.getWorld().progress(endOfRoundSimTime - localSimTime);

        // Update the shared clock
        run.getClock().setTime(endOfRoundSimTime);

    }

    /**
     * Returns whether the controller is active 
     * 
     * @return whether the controller is active
     */
    public boolean isActive() {
        synchronized(activeLock) {
            return active;
        }
    }

    /**
     * Tell the controller thread whether to 
     * keep running (internal use only)
     * 
     * @param active whether to keep running
     */
    public void setActive(boolean active) {
        synchronized(activeLock) {
            this.active = active;
        }
    }

    /**
     * Get the simulation time / real time ratio.
     * 
     * @return the ratio
     */
    public double getPlaySpeed() {
        synchronized(playSpeedLock) {
            return playSpeed;
        }
    }

    /**
     * Set the simulation time / real time ratio.
     * 
     * @param playSpeed the ratio to set
     */
    public void setPlaySpeed(double playSpeed) {
        synchronized(playSpeedLock) {
            this.playSpeed = playSpeed;
        }
    }

    /**
     * @return the run
     */
    public synchronized SimRun getRun() {
        return run;
    }

    /**
     * Alias for init
     */
    public void reset() {
        init();
    }

    public void finish() {
    	SimStats simStats = run.getStats();
        run.getEventLog().log(this, "density", simStats.getDoubleValue("vehicle-count", "average") / (0.001 * simStats.getDoubleValue("scenario-length", "latest")));
        run.getEventLog().log(this, "done", "");
    }

    public String toString() {
        return "sim";
    }


}
