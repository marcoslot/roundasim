/**
 * 
 */
package dsg.rounda.scenarios;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import dsg.rounda.SimRun;
import dsg.rounda.config.LongParameter;
import dsg.rounda.config.RunConfig;
import dsg.rounda.config.StringParameter;
import dsg.rounda.geometry.BezierCurve;
import dsg.rounda.io.FileIO;
import dsg.rounda.model.Building;
import dsg.rounda.model.Clock;
import dsg.rounda.model.Connector;
import dsg.rounda.model.DefaultRangingSpecsFactory;
import dsg.rounda.model.Pose2D;
import dsg.rounda.model.Position1D;
import dsg.rounda.model.RangingSensorsSpecification;
import dsg.rounda.model.RangingSpecsFactory;
import dsg.rounda.model.Scheduler;
import dsg.rounda.model.Track;
import dsg.rounda.model.TrackType;
import dsg.rounda.model.VehicleFactory;
import dsg.rounda.model.VehicleState;
import dsg.rounda.model.Velocity1D;
import dsg.rounda.model.WorldState;
import dsg.rounda.model.WorldView;

/**
 * @author slotm
 *
 */
public class ArcDeTriompheScenario implements Scenario {
    
    public static final ScenarioFactory FACTORY = new ScenarioFactory() {
        @Override
        public Scenario create() {
            return new ArcDeTriompheScenario();
        }

        @Override
        public String getTitle() {
            return "Arc de Triomphe";
        }

        @Override
        public String getDescription() {
            return "A massive roundabout full of peril and adventure";
        }
    };

    public static final LongParameter NUM_RINGS_PARAM = new LongParameter("arc.num-rings", 8L);
    public static final StringParameter BUILDINGS_MAP_PARAM = new StringParameter("arc.buildings-map", "arc-buildings.txt");

    private static final double WORLD_CENTER_X = 132;
    private static final double WORLD_CENTER_Y = 103;
    private static final double WORLD_WEST_X = 0;
    private static final double WORLD_SOUTH_Y = 0;
    private static final double WORLD_WIDTH = 280;
    private static final double WORLD_HEIGHT = 210;

    private static final int NUM_POINTS_ON_RING = 360;
    private static final int NUM_LANE_CHANGE_POINTS = 7;
    private static final double INNER_RADIUS = 58; //meter
    private static final double RING_WIDTH = 5.2; //meter
    private static final double STARTING_VELOCITY = 20; //m/s
    private static final int NUM_VEHICLES_PER_RING_AT_START = 5;
    private static final double LANE_CHANGE_SPACING = 4 * Math.PI; // meter
    private static final double LANE_CHANGE_PROGRESS_BASE = 12 *(Math.PI / 180); // rad
    private static final double LANE_CHANGE_PROGRESS_END = 20 *(Math.PI / 180); // rad
    private static final double VEHICLE_WIDTH = 2; //m
    private static final double VEHICLE_LENGTH = 4; //m
    private static final double MAX_VELOCITY = 40; //m/s

    private static final GeometryFactory GEOM = new GeometryFactory();

    RangingSpecsFactory rangingSpecsFactory;
    
    RunConfig config;
    Clock clock;
    WorldState world;
    Scheduler scheduler;
    VehicleFactory factory;
    FileIO fileIO;

    final List<Track> rings;

    int nextRoadID;
    int nextVehicleID;

    public ArcDeTriompheScenario() {
        rings = new ArrayList<Track>();
    }

    /**
     * @param network 
     * 
     */
    public void init(
            final SimRun run,
            final VehicleFactory factory,
            final FileIO fileIO,
            final Runnable readyHandler) {
        this.config = run.getConfig();
        this.clock = run.getClock();
        this.world = run.getWorld();
        this.scheduler = run.getScheduler();
        this.factory = factory;
        this.rings.clear();
        this.nextRoadID = 0;
        this.nextVehicleID = 0;
        this.fileIO = fileIO;
        this.rangingSpecsFactory = new DefaultRangingSpecsFactory(config);

        generateRoads();
        generateVehicles();
        generateBuildings(readyHandler);
    }

    private void generateBuildings(final Runnable readyHandler) {
        if(fileIO == null) {
            // No file I/O available
            // That's alright, we'll do without buildings
            readyHandler.run();
            return;
        }
        
        fileIO.readFile(config.get(BUILDINGS_MAP_PARAM), new FileIO.Callback() {            
            @Override
            public void onSuccess(String[] contents) {
                generateBuildingsFromMap(contents);
                readyHandler.run();
            }
            
            @Override
            public void onFailure(Throwable e) {
                // Continue without buildings
                System.err.println(e);
            }
        });
    }

    void generateBuildingsFromMap(String[] lines) {
        for(String line : lines) {
            String[] coordTexts = line.split(",");
            Coordinate[] coordinates = new Coordinate[coordTexts.length];
            
            for(int i = 0; i < coordTexts.length; i++) {
                String coordText = coordTexts[i].trim();
                int sepIndex = coordText.indexOf(' ');
                double x = WORLD_CENTER_X + Double.parseDouble(coordText.substring(0, sepIndex));
                double y = WORLD_CENTER_Y + Double.parseDouble(coordText.substring(sepIndex+1));
                coordinates[i] = new Coordinate(x, y);
            }
            
            CoordinateSequence coordinateSeq = new CoordinateArraySequence(coordinates);
            LinearRing shell = new LinearRing(coordinateSeq, GEOM);
            Polygon shape = new Polygon(shell, new LinearRing[0], GEOM);
            
            world.addBuilding(new Building(shape));
        }
    }

    private void generateRoads() {
        generateRings();
        generateLaneChangeTracks();
    }
    
    private void generateLaneChangeTracks() {
        for(int ringIndex = 0, numRings = rings.size(); ringIndex < numRings-1; ringIndex++) {
            Track innerRing = rings.get(ringIndex);
            Track outerRing = rings.get(ringIndex+1);
            double innerRingLength = innerRing.getPathLength();
            double innerRingRadius = ringIndex * RING_WIDTH + INNER_RADIUS;
            double outerRingRadius = (ringIndex+1) * RING_WIDTH + INNER_RADIUS;
            int numLaneChanges = (int) Math.ceil(innerRingLength / LANE_CHANGE_SPACING);
            
            for(int laneChangeIndex = 0; laneChangeIndex < numLaneChanges; laneChangeIndex++) {
                double innerRingOffset = laneChangeIndex * LANE_CHANGE_SPACING;
                double startAngle = innerRingOffset / innerRingRadius;

                Pose2D innerRingPoint = innerRing.getPose2D(innerRingOffset);

                // Inwards lane change track from innerRingOffset
                double baseAngleOutwards = (startAngle + LANE_CHANGE_PROGRESS_BASE) % (2*Math.PI);
                double endAngleOutwards = (startAngle + LANE_CHANGE_PROGRESS_END) % (2*Math.PI);
                Pose2D outerRingPointOutwardsBase = outerRing.getPose2D(baseAngleOutwards * outerRingRadius);
                Pose2D outerRingPointOutwardsEnd = outerRing.getPose2D(endAngleOutwards * outerRingRadius);
                
                Coordinate[] outwardsPoints = new Coordinate[] { 
                        innerRingPoint.getPosition(), 
                        outerRingPointOutwardsBase.getPosition(), 
                        outerRingPointOutwardsEnd.getPosition()
                };
                LineString outwardsPath = new BezierCurve(outwardsPoints).getCurvePoints(NUM_LANE_CHANGE_POINTS);
                Track outwardsLaneChange = new Track(nextRoadID++, TrackType.LANE_CHANGE, outwardsPath);
                Connector outwardsFrom = new Connector(innerRing.getId(), innerRingOffset);
                Connector outwardsTo = new Connector(outerRing.getId(), endAngleOutwards * outerRingRadius);
                outwardsLaneChange.setFrom(outwardsFrom);
                outwardsLaneChange.setTo(outwardsTo);
                world.addRoad(outwardsLaneChange);
                
                // Outward lane change track to innerRingOffset
                double baseAngleInwards = (startAngle - LANE_CHANGE_PROGRESS_BASE + 2*Math.PI) % (2*Math.PI);
                double startAngleInwards = (startAngle - LANE_CHANGE_PROGRESS_END + 2*Math.PI) % (2*Math.PI); 
                Pose2D outerRingPointInwardsBase = outerRing.getPose2D(baseAngleInwards * outerRingRadius); 
                Pose2D outerRingPointInwardsStart = outerRing.getPose2D(startAngleInwards * outerRingRadius);
                
                Coordinate[] inwardsPoints = new Coordinate[] { 
                        outerRingPointInwardsStart.getPosition(),
                        outerRingPointInwardsBase.getPosition(), 
                        innerRingPoint.getPosition() 
                };
                LineString inwardsPath = new BezierCurve(inwardsPoints).getCurvePoints(NUM_LANE_CHANGE_POINTS);
                Track inwardsLaneChange = new Track(nextRoadID++, TrackType.LANE_CHANGE, inwardsPath);
                Connector inwardsFrom = new Connector(outerRing.getId(), startAngleInwards * outerRingRadius);
                Connector inwardsTo = new Connector(innerRing.getId(), innerRingOffset);
                inwardsLaneChange.setFrom(inwardsFrom);
                inwardsLaneChange.setTo(inwardsTo);
                world.addRoad(inwardsLaneChange);
            }
        }
    }

    private void generateRings() {
        for(int i = 0; i < config.get(NUM_RINGS_PARAM); i++) {
            LineString ring = generateRing(i * RING_WIDTH + INNER_RADIUS);
            Track ringRoad = new Track(nextRoadID++, TrackType.LANE, ring);
            Connector from = new Connector(ringRoad.getId(), ring.getLength());
            Connector to = new Connector(ringRoad.getId(), 0);
            ringRoad.setFrom(from);
            ringRoad.setTo(to);
            world.addRoad(ringRoad);
            rings.add(ringRoad);
        }
    }

    private LineString generateRing(double radius) {
        Coordinate[] ringPoints = new Coordinate[NUM_POINTS_ON_RING+1];

        for(int i = 0; i < NUM_POINTS_ON_RING; i++) {
            double angleRad = i * 2 * Math.PI / NUM_POINTS_ON_RING;
            double x = WORLD_CENTER_X + radius * Math.cos(angleRad);
            double y = WORLD_CENTER_Y + radius * Math.sin(angleRad);
            ringPoints[i] = new Coordinate(x, y);
        }

        // Connect the tips
        ringPoints[NUM_POINTS_ON_RING] = ringPoints[0];

        return new LinearRing(new CoordinateArraySequence(ringPoints), GEOM);
    }

    private void generateVehicles() {
        for(int ringIndex = 0; ringIndex < rings.size(); ringIndex++) {
            Track ring = rings.get(ringIndex);
            
            for(int i = 0; i < NUM_VEHICLES_PER_RING_AT_START; i++) {
                int id = nextVehicleID++;
                double offset = i * ring.getPathLength() / NUM_VEHICLES_PER_RING_AT_START;
                Position1D position = new Position1D(ring, offset);
                Velocity1D velocity = new Velocity1D(STARTING_VELOCITY);
                VehicleState initialState = new VehicleState(id, position, velocity, VEHICLE_WIDTH, VEHICLE_LENGTH, MAX_VELOCITY);
                RangingSensorsSpecification rangingSpecs = rangingSpecsFactory.createRangingSpecs(initialState);
                
                factory.createVehicle(initialState, rangingSpecs);
            }
        }
    }

    @Override
    public WorldView getInitialWorldView() {
        return new WorldView(
                WORLD_WEST_X, WORLD_SOUTH_Y,
                WORLD_WIDTH, WORLD_HEIGHT
        );
    }

    @Override
    public String getBackground() {
        return "arc600x450.jpg";
    }

}
