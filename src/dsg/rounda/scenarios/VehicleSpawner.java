/**
 * 
 */
package dsg.rounda.scenarios;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dsg.rounda.Constants;
import dsg.rounda.SimRun;
import dsg.rounda.config.RunConfig;
import dsg.rounda.config.SimulationParameters;
import dsg.rounda.model.Clock;
import dsg.rounda.model.Connector;
import dsg.rounda.model.DefaultRangingSpecsFactory;
import dsg.rounda.model.Job;
import dsg.rounda.model.Position1D;
import dsg.rounda.model.RangingSensorsSpecification;
import dsg.rounda.model.RangingSpecsFactory;
import dsg.rounda.model.Scheduler;
import dsg.rounda.model.Track;
import dsg.rounda.model.TrackProvider;
import dsg.rounda.model.VehicleFactory;
import dsg.rounda.model.VehicleState;
import dsg.rounda.model.Velocity1D;
import dsg.rounda.model.WorldState;

/**
 * Spawns vehicles at random entries
 */
public class VehicleSpawner implements Constants, SimulationParameters {

    private static final double STARTING_VELOCITY = 25.; // m/s
    private static final double VEHICLE_WIDTH = 1.83; // m
    private static final double VEHICLE_LENGTH = 4.12; //m

    private static final double MIN_SPAWNING_DISTANCE = 30.0;

    final RunConfig config;
    final Clock clock;
    final Scheduler scheduler;
    final WorldState world;
    final TrackProvider network;
    final Random random;
    final List<Connector> vehicleEntries;
    final VehicleFactory vehicleFactory;
    final RangingSpecsFactory rangingSpecsFactory;
    
    int newVehicleID;
    
    /**
     * 
     */
    public VehicleSpawner(SimRun run, VehicleFactory factory) {
        this.config = run.getConfig();
        this.clock = run.getClock();
        this.scheduler = run.getScheduler();
        this.world = run.getWorld();
        this.random = run.getRandom();
        this.vehicleFactory = factory;
        this.network = run.getWorld().getRoadNetwork();
        this.rangingSpecsFactory = new DefaultRangingSpecsFactory(config);
        this.vehicleEntries = new ArrayList<Connector>();
    }

    public void start() {
        for(Track road : network.getRoads()) {
            if(road.getFrom() == null
            || network.getRoad(road.getFrom().getRoad()) == null) {
                // Road starts from nowhere, make this a vehicle spawn point
                vehicleEntries.add(new Connector(road.getId(), 0.0));
            }
        }
        
        scheduleVehicleEntries();
    }

    void scheduleVehicleEntries() {
        double delay = poissonRandomInterarrivalDelay(config.get(SPAWN_RATE) / 60);
        long startTime = clock.getTime() + (long) (delay * SECONDS);
        scheduler.schedule(new Job(spawnVehicle, Job.GLOBAL_OWNER, startTime));
    }

    final Runnable spawnVehicle = new Runnable() {
        public void run() {
            if(vehicleEntries.isEmpty()) {
                return;
            }
            int randomVehicleEntry = random.nextInt(vehicleEntries.size());
            Connector vehicleEntry = vehicleEntries.get(randomVehicleEntry);
            spawnVehicleAt(vehicleEntry);
            
            scheduleVehicleEntries();
        }
    };
	

    private void spawnVehicleAt(Connector vehicleEntry) {
        int roadID = vehicleEntry.getRoad();
        Set<Integer> vehiclesOnTrack = world.getVehiclesOnTrack(roadID);

        double minSpawnOffset = vehicleEntry.getOffset() + MIN_SPAWNING_DISTANCE;
        double maxSpawnOffset = vehicleEntry.getOffset() - MIN_SPAWNING_DISTANCE;
        double safeEntrySpeed = config.get(DESIRED_VELOCITY);
        
        // Make sure no vehicle is very close to the spawning point
        for(Integer vehicleID : vehiclesOnTrack) {
            VehicleState vehicle = world.getVehicle(vehicleID);
            
            if(vehicle.getBackPosition().getOffset() < minSpawnOffset
                    && vehicle.getBackPosition().getOffset() > maxSpawnOffset) {
                // Vehicle is too close, don't spawn
                // TODO: flag the spawn point as having a vehicle waiting
                return;
            }
            if (vehicle.getVelocity().getRoadVelocity() < safeEntrySpeed) {
            	safeEntrySpeed = vehicle.getVelocity().getRoadVelocity();
            }
        }

        // No vehicle nearby, spawn a vehicle at the entry point
        Track road = world.getRoad(roadID);

        VehicleState initialState = new VehicleState(
                ++newVehicleID,
                new Position1D(road, vehicleEntry.getOffset()),
                new Velocity1D(safeEntrySpeed),
                VEHICLE_WIDTH,
                VEHICLE_LENGTH,
                config.getMaxVelocity(35));

        RangingSensorsSpecification rangingSpecs = rangingSpecsFactory.createRangingSpecs(initialState);

        vehicleFactory.createVehicle(initialState, rangingSpecs);
    }

    public double poissonRandomInterarrivalDelay(double lambda) {
        return Math.log(1.0 - random.nextDouble())/-lambda;
    }
}
