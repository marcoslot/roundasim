/**
 * 
 */
package dsg.rounda.scenarios;

import com.vividsolutions.jts.io.ParseException;

import dsg.rounda.Constants;
import dsg.rounda.SimRun;
import dsg.rounda.io.FileIO;
import dsg.rounda.io.FileIO.Callback;
import dsg.rounda.model.VehicleFactory;
import dsg.rounda.model.WorldView;
import dsg.rounda.services.roadmap.TrackPoint1D;
import dsg.rounda.services.trafficcontrol.TrafficLight;
import dsg.rounda.services.trafficcontrol.TrafficLightController;
import dsg.rounda.services.trafficcontrol.TrafficLightGroup;

/**
 * @author slotm
 *
 */
public class TrafficLightJunctionScenario implements Scenario, Constants {

    public static final ScenarioFactory FACTORY = new ScenarioFactory() {
        @Override
        public Scenario create() {
            return new TrafficLightJunctionScenario();
        }

        @Override
        public String getTitle() {
            return "Traffic light junction";
        }

        @Override
        public String getDescription() {
            return "Scenario with invisible traffic lights on the Merrion Square junction";
        }
    };

    SimRun run;
    VehicleFactory vehicleFactory;
    FileIO fileIO;
    VehicleSpawner spawner;
    
    TrafficLightController trafficLights;
    
    WorldView worldView;
    String backgroundURL;

    private Runnable readyHandler;

    /**
     * 
     */
    public TrafficLightJunctionScenario() {

    }

    /**
     * @see dsg.rounda.scenarios.Scenario#init(dsg.rounda.SimRun, dsg.rounda.model.VehicleFactory, dsg.rounda.io.FileIO)
     */
    @Override
    public void init(
            SimRun run, 
            VehicleFactory vehicleFactory, 
            FileIO fileIO, 
            Runnable readyHandler) {
        this.run = run;
        this.vehicleFactory = vehicleFactory;
        this.fileIO = fileIO;
        this.readyHandler = readyHandler;
        this.trafficLights = trafficLightController();
        this.spawner = new VehicleSpawner(run, vehicleFactory);
        
        createTrafficLight();
        loadScenario();
    }

    void createTrafficLight() {
        TrafficLightGroup snGroup = new TrafficLightGroup(TrafficLight.Colour.RED);
        snGroup.addTrafficLightAt(new TrackPoint1D(3, 84.0));
        snGroup.addTrafficLightAt(new TrackPoint1D(4, 91.0));
        trafficLights.addGroup("SN", snGroup);
        run.getInfrastructureMap().addTrafficLights(snGroup.getTrafficLights());

        TrafficLightGroup ewGroup = new TrafficLightGroup(TrafficLight.Colour.RED);
        ewGroup.addTrafficLightAt(new TrackPoint1D(6, 123.0));
        ewGroup.addTrafficLightAt(new TrackPoint1D(7, 114.0));
        trafficLights.addGroup("EW", ewGroup);
        run.getInfrastructureMap().addTrafficLights(ewGroup.getTrafficLights());
    }

    void loadScenario() {
        fileIO.readFile("wayway.txt", new Callback() {
            @Override
            public void onFailure(Throwable e) {
                
            }
            @Override
            public void onSuccess(String[] contents) {
                try {
                    TextWorld worldText = new TextWorld();
                    worldText.parseFrom(contents);
                    backgroundURL = worldText.getBackgroundURL();
                    worldView = worldText.getWorldView();
                    run.getWorld().load(worldText);
                    start();
                    readyHandler.run();
                } catch (ParseException e) {
                    onFailure(e);
                }
            }
        });
    }

    void start() {
        trafficLights.start();
        spawner.start();
    }

    enum Direction {
        SN,
        EW;
    }
    
    TrafficLightController trafficLightController() {
        return new TrafficLightController(run.getClock(), run.getScheduler()) {
            
            TrafficLight.Colour currentState = TrafficLight.Colour.RED;
            Direction currentDirection = Direction.EW;
            
            @Override
            public long nextEvent() {
                long delay = 0;
                
                switch(currentState) {
                    case GREEN:
                        currentState = TrafficLight.Colour.ORANGE;
                        delay = 3 * SECONDS;
                        break;
                    case ORANGE:
                        currentState = TrafficLight.Colour.RED;
                        delay = 2 * SECONDS;
                        break;
                    case RED:
                        currentState = TrafficLight.Colour.GREEN;
                        currentDirection = other(currentDirection);
                        delay = 15 * SECONDS;
                        break;
                }

                TrafficLightGroup trafficLightGroup = getGroup(currentDirection.toString());
                trafficLightGroup.setState(currentState);
                
                return run.getClock().getTime() + delay;
            }
            
            Direction other(Direction dir) {
                return dir == Direction.SN ? Direction.EW : Direction.SN;
            }
        };
    }


    /**
     * @see dsg.rounda.scenarios.Scenario#getInitialWorldView()
     */
    @Override
    public WorldView getInitialWorldView() {
        return worldView;
    }

    /**
     * @see dsg.rounda.scenarios.Scenario#getBackground()
     */
    @Override
    public String getBackground() {
        return backgroundURL;
    }

}
