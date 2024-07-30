/**
 * 
 */
package dsg.rounda.scenarios;

import java.util.Random;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import dsg.rounda.Constants;
import dsg.rounda.SimRun;
import dsg.rounda.config.RunConfig;
import dsg.rounda.config.SimulationParameters;
import dsg.rounda.io.FileIO;
import dsg.rounda.logging.EventLog;
import dsg.rounda.model.Building;
import dsg.rounda.model.Clock;
import dsg.rounda.model.Scheduler;
import dsg.rounda.model.TrackNetwork;
import dsg.rounda.model.VehicleFactory;
import dsg.rounda.model.WorldState;
import dsg.rounda.model.WorldView;

/**
 * Scenario defined using the builder tool
 */
public class BuilderScenario implements Scenario, Constants, SimulationParameters {


    public static final ScenarioFactory FACTORY = new ScenarioFactory() {
        @Override
        public Scenario create() {
            return new BuilderScenario();
        }

        @Override
        public String getTitle() {
            return "Custom scenario";
        }

        @Override
        public String getDescription() {
            return "Scenario built using Round-a-Sim Builder";
        }
    };

    RunConfig config;
    VehicleSpawner spawner;
    WorldState world;
    TrackNetwork network;
    Clock clock;
    Scheduler scheduler;
    EventLog eventLog;
    Random random;
    FileIO fileIO;
    String backgroundURL;
    WorldView globalWorldView;

    /**
     * 
     */
    public BuilderScenario() {
    }

    @Override
    public void init(
            SimRun run, 
            VehicleFactory vehicleFactory, 
            FileIO fileIO,
            Runnable readyHandler) {
        this.config = run.getConfig();
        this.world = run.getWorld();
        this.network = world.getRoadNetwork();
        this.clock =  run.getClock();
        this.scheduler = run.getScheduler();
        this.random = run.getRandom();
        this.fileIO = fileIO;
        this.eventLog = run.getEventLog();
        this.spawner = new VehicleSpawner(run, vehicleFactory);
        loadScenario(readyHandler);
    }

    private void loadScenario(final Runnable readyHandler) {
        String scenarioText = config.get(SCENARIO_TEXT_PARAM);

        if(scenarioText == null) {
            String scenarioFile = config.get(SCENARIO_FILE_PARAM);

            if(scenarioFile == null) {
                throw new RuntimeException("No scenario-file or scenario-text parameter present");
            }

            fileIO.readFile(scenarioFile, new FileIO.Callback() {

                @Override
                public void onSuccess(String[] scenarioLines) {
                    loadScenario(scenarioLines);
                    readyHandler.run();
                }

                @Override
                public void onFailure(Throwable e) {
                    System.err.println(e);
                }

            });
        } else {
            String[] scenarioLines = scenarioText.split("\r\n");
            loadScenario(scenarioLines);
            readyHandler.run();
        }
    }

    private void loadScenario(String[] scenarioLines) {
        try {
            for(int i = 0; i < scenarioLines.length; i++) {
                String line = scenarioLines[i].trim();

                if("WORLD".equals(line)) {
                    i = readWorldView(scenarioLines, i+1);
                } else if("BUILDINGS".equals(line)) {
                    i = readBuildings(scenarioLines, i+1);
                } else if("ROADS".equals(line)) {
                    i = readRoads(scenarioLines, i+1);
                } else if("BACKGROUND".equals(line)) {
                    i = readBackground(scenarioLines, i+1);
                } else if("".equals(line)) {
                    i++;
                } else {
                    i = readGarbage(scenarioLines, i+1);
                }
            }

            double scenarioLength = network.getScenarioLength();
            eventLog.log(this, "scenario-length", scenarioLength);
            
            spawner.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int readGarbage(String[] scenarioLines, int i) {
        // Discard lines until an empty line is found
        for(; i < scenarioLines.length && !scenarioLines[i].trim().isEmpty(); i++);
        return i;
    }

    private int readBackground(String[] scenarioLines, int i) {
        backgroundURL = scenarioLines[i];
        return i+1;
    }

    private int readRoads(String[] scenarioLines, int i) throws ParseException {
        int start = i;

        for(; i < scenarioLines.length && !scenarioLines[i].trim().isEmpty(); i++);

        int numLines = i - start;

        network.addRoadsFromLines(scenarioLines, start, numLines);

        return i;
    }

    private int readBuildings(String[] scenarioLines, int i) throws ParseException {
        WKTReader reader = new WKTReader();

        for(; i < scenarioLines.length && !scenarioLines[i].trim().isEmpty(); i++) {
            Geometry geom = reader.read(scenarioLines[i]);

            if(geom instanceof Polygon) {
                Polygon buildingPolygon = (Polygon) geom;

                world.addBuilding(new Building(buildingPolygon));
            }
        }

        return i;
    }

    private int readWorldView(String[] scenarioLines, int i) {
        String[] parts = scenarioLines[i].split(" ");
        double width = Double.parseDouble(parts[0]);
        double height = Double.parseDouble(parts[1]);
        globalWorldView = new WorldView(0., 0., width, height);
        return i+1;
    }

    @Override
    public WorldView getInitialWorldView() {
        return globalWorldView;
    }

    @Override
    public String getBackground() {
        return backgroundURL;
    }

}
