/**
 * 
 */
package dsg.roundagwt;

import static dsg.rounda.serialization.text.TextSerializationManager.serialize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.me.jstott.jcoord.LatLng;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.operation.buffer.BufferParameters;
import com.vividsolutions.jts.operation.buffer.OffsetCurveBuilder;
import com.vividsolutions.jts.operation.distance.DistanceOp;

import dsg.rounda.SimController;
import dsg.rounda.config.RunConfig;
import dsg.rounda.config.SimulationParameters;
import dsg.rounda.geometry.BezierCurve;
import dsg.rounda.geometry.GeoUtil;
import dsg.rounda.gui.PlayerPresenter;
import dsg.rounda.gui.Screen;
import dsg.rounda.gui.SimPresenter;
import dsg.rounda.gui.SimView;
import dsg.rounda.gui.WorldScreenView;
import dsg.rounda.model.Building;
import dsg.rounda.model.Connector;
import dsg.rounda.model.Pose2D;
import dsg.rounda.model.Track;
import dsg.rounda.model.TrackNetwork;
import dsg.rounda.model.TrackType;
import dsg.rounda.model.WorldView;
import dsg.rounda.scenarios.BuilderScenario;
import dsg.roundagwt.Action.Type;
import dsg.roundagwt.gui.BackgroundLoadCallback;
import dsg.roundagwt.gui.BuilderButtonView;
import dsg.roundagwt.gui.BuilderPaletView;
import dsg.roundagwt.gui.BuilderView;
import dsg.roundagwt.io.FileIOGWT;


/**
 * Controls the whole builder GUI
 */
public class BuilderPresenter implements BuilderView.Presenter, SimulationParameters {
    
    private static final double LANE_CHANGE_LENGTH = 20.0;
    private static final double DISTANCE_BETWEEN_LANE_CHANGES = 60.0;
    private static final double SNAP_DISTANCE_METERS = 2;

    private static final GeometryFactory GEOM = new GeometryFactory();
    private static final double LANE_WIDTH = 5.0; //m
    
    final BuilderView view;
    final BuilderPaletView palet;
    final BuilderButtonView buttons;
    SimView simView;

    SimPlayerGWT runner;
    
    LatLng marker1LatLng;
    LatLng marker2LatLng;

    TrackNetwork pixelNetwork;
    int newRoadID;

    final List<Building> buildings;
    final List<Action> actions;

    double zoomFactor;
    double metersPerPixel;
    WorldView globalPixelView;
    
    String worldAsText;

    int worldVersion;
    int textVersion;
    int glueVersion;
    
    int selectedRoad;

    /**
     * 
     */
    public BuilderPresenter(
            BuilderView view) {
        this.view = view;
        this.view.setPresenter(this);
        this.palet = view.getPaletView();
        this.palet.setPresenter(paletPresenter);
        this.buttons = view.getButtonView();
        this.buttons.setPresenter(buttonPresenter);

        this.buildings = new ArrayList<Building>();
        this.pixelNetwork = new TrackNetwork();
        this.actions = new ArrayList<Action>();

        this.newRoadID = 0;
        this.zoomFactor = 1.0;
        this.textVersion = -1;
        this.glueVersion = -1;
        this.worldVersion = 0;
        this.selectedRoad = -1;

        this.metersPerPixel = BuilderPaletView.DEFAULT_METERS_PER_PIXEL;
        this.palet.setPixelsPerMeter(1.0 / metersPerPixel);

        this.globalPixelView = palet.getPixelView();
        
        this.worldAsText = view.loadFromStorage();
        
        if(worldAsText != null) {
            buttons.setOpenButtonEnabled(true);
            buttons.setButtonsEnabled(BuilderButtonView.OPEN_BUTTON | BuilderButtonView.BACKGROUND_BUTTON | BuilderButtonView.TEXT_BUTTON);
        } else {
            buttons.setButtonsEnabled(BuilderButtonView.BACKGROUND_BUTTON | BuilderButtonView.TEXT_BUTTON);
        }
    }

    final BuilderButtonView.Presenter buttonPresenter = new BuilderButtonView.Presenter() {

        @Override
        public void onPlayClick(boolean pressed) {
            if(pressed) {
                buttons.setButtonsEnabled(BuilderButtonView.PLAY_BUTTON);
                play();
            } else {
                buttons.setButtonsEnabled(BuilderButtonView.ALL_BUTTONS);
                unplay();
            }
        }

        @Override
        public void onWorldClick(boolean pressed) {
            if(pressed) {
                buttons.setButtonsEnabled(BuilderButtonView.WORLD_BUTTON);
                palet.enterMeasuringMode();
            } else {
                computeWorldSize();
                buttons.setButtonsEnabled(BuilderButtonView.ALL_BUTTONS);
                palet.leaveMeasuringMode();
            }
        }

        @Override
        public void onCreateRoadClick(boolean pressed) {
            if(pressed) {
                buttons.setButtonsEnabled(BuilderButtonView.CREATE_ROAD_BUTTON);
                palet.enterCreateRoadMode();
            } else {
                buttons.setButtonsEnabled(BuilderButtonView.ALL_BUTTONS);
                palet.leaveCreateRoadMode();
            }
        }

        @Override
        public void onCreateBuildingClick(boolean pressed) {
            if(pressed) {
                buttons.setButtonsEnabled(BuilderButtonView.CREATE_BUILDING_BUTTON);
                palet.enterCreateBuildingMode();
            } else {
                buttons.setButtonsEnabled(BuilderButtonView.ALL_BUTTONS);
                palet.leaveCreateBuildingMode();
            }
        }

        @Override
        public void onGlueClick() {
            glue();
        }

        @Override
        public void onExpandRoadClick() {
            expandSelectedRoad();
        }

        @Override
        public void onBackgroundClick() {
            palet.openBackgroundDialog();
        }

        @Override
        public void onUndoClick() {
            undo();
        }

        @Override
        public void onTextClick() {
            export();
        }

        @Override
        public void onSaveClick() {
            view.saveToStorage(getWorldAsText());
            buttons.setOpenButtonEnabled(true);
        }

        @Override
        public void onOpenClick() {
            loadWorldFromText(view.loadFromStorage());
        }
        
    };
    protected int selectedRoadIndex;
    
    final BuilderPaletView.Presenter paletPresenter = new BuilderPaletView.Presenter() {

        @Override
        public void cancelRoad() {
            buttons.setCreateRoadButtonPressed(false);
            buttons.setButtonsEnabled(BuilderButtonView.ALL_BUTTONS);
            palet.leaveCreateRoadMode();
        }

        @Override
        public void cancelBuilding() {
            buttons.setCreateBuildingButtonPressed(false);
            buttons.setButtonsEnabled(BuilderButtonView.ALL_BUTTONS);
            palet.leaveCreateBuildingMode();
        }

        @Override
        public void addBuilding(List<Coordinate> coordinates) {
            Building building = new Building(coordinates);
            buildings.add(building);

            action(Action.Type.CREATE_BUILDING, building);
            worldVersion++;

            palet.leaveCreateBuildingMode();
            palet.setBuildings(buildings);

            buttons.setCreateBuildingButtonPressed(false);
            buttons.setButtonsEnabled(BuilderButtonView.ALL_BUTTONS);
        }

        @Override
        public void createRoad(List<Coordinate> controlPoints) {
            int segmentID = newRoadID++;
            Track newRoad;

            if(controlPoints.size() == 2) {
                LineString path = GEOM.createLineString(controlPoints.toArray(new Coordinate[controlPoints.size()]));
                newRoad = new Track(segmentID, TrackType.NORMAL, path);
            } else {
                BezierCurve curve = new BezierCurve(controlPoints);
                newRoad = new Track(segmentID, TrackType.NORMAL, curve);
            }

            pixelNetwork.addRoad(newRoad);

            selectedRoad = newRoad.getId();
            
            action(Action.Type.CREATE_ROAD, newRoad);
            worldVersion++;
            
            buttons.setExpandButtonEnabled(true);
            buttons.setCreateRoadButtonPressed(false);
            buttons.setButtonsEnabled(BuilderButtonView.ALL_BUTTONS);

            palet.leaveCreateRoadMode();
            palet.setRoads(pixelNetwork.getRoads());
            palet.selectRoad(newRoad.getId());
        }

        @Override
        public void removeSelectedRoad() {
            Track road = pixelNetwork.getRoad(selectedRoad);
            
            if(road == null) {
                return;
            }
            
            pixelNetwork.removeRoad(road.getId());
            worldVersion++;
            
            selectedRoad = -1;
            buttons.setExpandButtonEnabled(false);
            
            action(Action.Type.REMOVE_ROAD, road);
            
            palet.setRoads(pixelNetwork.getRoads());
            onSelectVehicle(0);
        }

        @Override
        public void onHeightUpdate(double heightMeters) {
            WorldView pixelView = palet.getPixelView();
            metersPerPixel = heightMeters / pixelView.getHeight();
            palet.setPixelsPerMeter(1.0 / metersPerPixel);
            double widthMeters = globalPixelView.getWidth() * metersPerPixel;
            palet.setWorldWidthMeters(widthMeters);
            worldVersion++;
        }

        @Override
        public void onWidthUpdate(double widthMeters) {
            WorldView pixelView = palet.getPixelView();
            metersPerPixel = widthMeters / pixelView.getWidth(); 
            double heightMeters = globalPixelView.getHeight() * metersPerPixel;
            palet.setPixelsPerMeter(1.0 / metersPerPixel);
            palet.setWorldHeightMeters(heightMeters);
            worldVersion++;
        }

        @Override
        public void onTextFormChange(String userText) {
            loadWorldFromText(userText);
        }

        @Override
        public void onZoom(int deltaY, Coordinate centre) {
            if(deltaY < 0) {
                zoomFactor = Math.min(globalPixelView.getWidth()/100., zoomFactor + 0.2*zoomFactor);
            } else {
                zoomFactor = Math.max(1.0, zoomFactor - 0.2*zoomFactor);
            }

            zoom(centre);
        }

        @Override
        public void onDrag(double dx, double dy) {
            WorldView currentPixelView = palet.getPixelView();

            double newWestX = currentPixelView.getWestX()-dx;
            newWestX = Math.max(newWestX, globalPixelView.getWestX());
            newWestX = Math.min(newWestX, globalPixelView.getEastX() - currentPixelView.getWidth());

            double newSouthY = currentPixelView.getSouthY()-dy;
            newSouthY = Math.max(newSouthY, globalPixelView.getSouthY());
            newSouthY = Math.min(newSouthY, globalPixelView.getNorthY() - currentPixelView.getHeight());

            currentPixelView.setWestSouth(newWestX, newSouthY);

            palet.setPixelView(currentPixelView);
        }

        @Override
        public void onBackgroundLoad(
                double backgroundWidth,
                double backgroundHeight) {
            palet.initializeCanvas(backgroundWidth, backgroundHeight);
            buttons.setButtonsEnabled(BuilderButtonView.ALL_BUTTONS);

            globalPixelView = palet.getPixelView();
            worldVersion++;
        }

        @Override
        public void onSelectVehicle(int deltaY) {
            List<Track> roads = pixelNetwork.getRoads();
            
            if(roads.isEmpty()) {
                return;
            }
            
            int direction = deltaY == 0 ? 0 : deltaY / Math.abs(deltaY);
            
            selectedRoadIndex += direction;
            
            // Wrap around
            if(selectedRoadIndex < 0) {
                selectedRoadIndex = roads.size()-1;
            } else if(selectedRoadIndex >= roads.size()){
                selectedRoadIndex = 0;
            }
            
            selectedRoad = roads.get(selectedRoadIndex).getId();

            buttons.setExpandButtonEnabled(true);
            palet.selectRoad(selectedRoad);
        }

    };
    
    void zoom(Coordinate centre) {
        WorldView currentPixelView = palet.getPixelView();
        WorldView newPixelView = globalPixelView.zoomed(currentPixelView, centre, zoomFactor);

        palet.setPixelsPerMeter(1.0 / metersPerPixel);
        palet.setPixelView(newPixelView);

        double widthMeters = newPixelView.getWidth()*metersPerPixel;
        double heightMeters = newPixelView.getHeight()*metersPerPixel;

        palet.setWorldWidthMeters(widthMeters);
        palet.setWorldHeightMeters(heightMeters);
    }
    
    void computeWorldSize() {
        String marker1LatLong = palet.getMarker1LatLon();
        String marker2LatLong = palet.getMarker2LatLon();

        if(marker1LatLong != null
                &&!marker1LatLong.trim().isEmpty()) {
            marker1LatLng = parseLatLon(marker1LatLong);
        }

        if(marker2LatLong != null
                &&!marker2LatLong.trim().isEmpty()) {
            marker2LatLng = parseLatLon(marker2LatLong);
        }

        if(marker1LatLng != null && marker2LatLng != null) {
            Coordinate marker1Pix = palet.getMarker1();
            Coordinate marker2Pix = palet.getMarker2();

            metersPerPixel = 1000*marker1LatLng.distance(marker2LatLng) / marker1Pix.distance(marker2Pix);

            palet.setPixelsPerMeter(1.0 / metersPerPixel);

            double widthMeters = globalPixelView.getWidth() * metersPerPixel;
            double heightMeters = globalPixelView.getHeight() * metersPerPixel;

            palet.setWorldHeightMeters(heightMeters);
            palet.setWorldWidthMeters(widthMeters);
        }
    }

    void action(Type type, Object object) {
        actions.add(new Action(type, object));
        buttons.setUndoButtonEnabled(true);
    }

    LatLng parseLatLon(String latLonStr) {
        String[] parts = latLonStr.split(",");

        if(parts.length != 2) {
            return null;
        }

        double lat = Double.parseDouble(parts[0]);
        double lon = Double.parseDouble(parts[1]);

        return new LatLng(lat, lon);
    }

    public void undo() {
        if(actions.isEmpty()) {
            return;
        }

        Action action = actions.remove(actions.size()-1);

        switch(action.getType()) {
            case CREATE_ROAD: {
                Track road = (Track) action.getPayload();
                
                pixelNetwork.removeRoad(road.getId());

                if(road.getId() == selectedRoad) {
                    buttons.setExpandButtonEnabled(false);
                    palet.selectRoad(-1);
                    selectedRoad = -1;
                }
                
                palet.setRoads(pixelNetwork.getRoads());
                break;
            }
            case CREATE_ROADS: {
                Collection<Track> roads = (Collection<Track>) action.getPayload();
                
                for(Track road : roads) {
                    pixelNetwork.removeRoad(road.getId());
                    
                    if(road.getId() == selectedRoad) {
                        buttons.setExpandButtonEnabled(false);
                        palet.selectRoad(-1);
                        selectedRoad = -1;
                    }
                }
                
                palet.setRoads(pixelNetwork.getRoads());
                break;
            }
            case REMOVE_ROAD: {
                Track removedRoad = (Track) action.getPayload();
                
                // Should be safe to add it back since we don't reuse identifiers
                // This will also immediately restore broken connectors, which can
                // otherwise be restored by gluing.
                pixelNetwork.addRoad(removedRoad);

                buttons.setExpandButtonEnabled(true);
                palet.setRoads(pixelNetwork.getRoads());
                palet.selectRoad(removedRoad.getId());
                break;
            }
            case CREATE_BUILDING: {
                buildings.remove((Building) action.getPayload());
                break;
            }
            default:
                break;
        }
        
        if(actions.isEmpty()) {
            buttons.setUndoButtonEnabled(false);
        }
        
        worldVersion++;
    }

    public void export() {
        palet.showText(getWorldAsText());
    }

    private TrackNetwork toWorldCoords(TrackNetwork pixelNetwork) {
        TrackNetwork worldNetwork = new TrackNetwork();

        for(Track road : pixelNetwork.getRoads()) {
            Coordinate[] screenCoords = road.getPath().getCoordinates();
            Coordinate[] worldCoords = toWorldCoords(screenCoords);

            Connector from = road.getFrom() != null ? new Connector(road.getFrom().getRoad(), road.getFrom().getOffset() * metersPerPixel) : null;
            Connector to = road.getTo() != null ? new Connector(road.getTo().getRoad(), road.getTo().getOffset() * metersPerPixel) : null;
            Track worldRoad = new Track(road.getId(), road.getType(), GEOM.createLineString(worldCoords));
            worldRoad.setFrom(from);
            worldRoad.setTo(to);
            worldRoad.setLeftLane(road.getLeftLane());
            worldRoad.setRightLane(road.getRightLane());
            worldNetwork.addRoad(worldRoad);
        }

        return worldNetwork;
    }


    private TrackNetwork toPixelCoords(TrackNetwork worldNetwork) {
        TrackNetwork pixelNetwork = new TrackNetwork();

        for(Track road : worldNetwork.getRoads()) {
            Coordinate[] worldCoords = road.getPath().getCoordinates();
            Coordinate[] pixelCoords = toPixelCoords(worldCoords);

            Connector from = road.getFrom() != null ? new Connector(road.getFrom().getRoad(), road.getFrom().getOffset()/ metersPerPixel) : null;
            Connector to = road.getTo() != null ? new Connector(road.getTo().getRoad(), road.getTo().getOffset() / metersPerPixel) : null;
            Track pixelRoad = new Track(road.getId(), road.getType(), GEOM.createLineString(pixelCoords));
            pixelRoad.setFrom(from);
            pixelRoad.setTo(to);
            pixelRoad.setLeftLane(road.getLeftLane());
            pixelRoad.setRightLane(road.getRightLane());
            pixelNetwork.addRoad(pixelRoad);
        }

        return pixelNetwork;
    }


    private Coordinate[] toWorldCoords(Coordinate[] screenCoords) {
        Coordinate[] worldCoords = new Coordinate[screenCoords.length];

        for(int i = 0; i < screenCoords.length; i++) {
            // Convert background pixels to meters
            worldCoords[i] = new Coordinate(
                    screenCoords[i].x * metersPerPixel,
                    screenCoords[i].y * metersPerPixel);
        }
        
        return worldCoords;
    }

    private Coordinate[] toPixelCoords(Coordinate[] worldCoords) {
        Coordinate[] pixelCoords = new Coordinate[worldCoords.length];

        for(int i = 0; i < worldCoords.length; i++) {
            // Convert meters to background pixels 
            pixelCoords[i] = new Coordinate(
                    worldCoords[i].x / metersPerPixel,
                    worldCoords[i].y / metersPerPixel);
        }
        
        return pixelCoords;
    }

    public String getWorldAsText() {
        if(textVersion == worldVersion) {
            return worldAsText;
        }
        glue();

        WKTWriter geoToText = new WKTWriter();
        StringBuilder sb = new StringBuilder();
        
        sb.append("WORLD\r\n");
        sb.append(metersPerPixel*globalPixelView.getWidth());
        sb.append(" ");
        sb.append(metersPerPixel*globalPixelView.getHeight());
        sb.append("\r\n");
        sb.append("\r\n");
        
        sb.append("BUILDINGS\r\n");

        for(Building building : buildings) {
            Polygon pixelPolygon = building.getPolygon();
            Coordinate[] pixelCoords = pixelPolygon.getExteriorRing().getCoordinates();
            Coordinate[] worldCoords = toWorldCoords(pixelCoords);
            Polygon worldPolygon = GEOM.createPolygon(GEOM.createLinearRing(worldCoords), null);
            
            sb.append(geoToText.write(worldPolygon));
            sb.append("\r\n");
        }

        sb.append("\r\n");
        
        sb.append("ROADS\r\n");
        TrackNetwork worldNetwork = toWorldCoords(pixelNetwork);
        sb.append(serialize(worldNetwork));
        sb.append("\r\n");
        
        sb.append("BACKGROUND\r\n");
        sb.append(palet.getBackgroundDataURL());
        
        worldAsText = sb.toString();
        textVersion = worldVersion;
        
        return worldAsText;
    }

    public void loadWorldFromText(String worldAsText) {
        String[] scenarioLines = worldAsText.split("\n");
        loadWorldFromText(scenarioLines);
    }
     
    public void loadWorldFromText(final String[] scenarioLines) {
        // zoom out first
        zoomFactor = 1.0;
        zoom(new Coordinate());
        
        for(int i = 0; i < scenarioLines.length; i++) {
            String line = scenarioLines[i].trim();
            
            if("BACKGROUND".equals(line)) {
                palet.setBackgroundURL(scenarioLines[i+1], new BackgroundLoadCallback() {
                    @Override
                    public void onBackgroundLoad(
                            double backgroundWidth,
                            double backgroundHeight) {
                        paletPresenter.onBackgroundLoad(backgroundWidth, backgroundHeight);
                        loadScenario(scenarioLines);
                    }
                });
                break;
            }
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
                } else if("".equals(line)) {
                    i++;
                } else {
                    i = readGarbage(scenarioLines, i+1);
                }
            }
        } catch (Exception e) {
            palet.setError(e.toString());
        }
    }

    private int readGarbage(String[] scenarioLines, int i) {
        // Discard lines until an empty line is found
        for(; i < scenarioLines.length && !scenarioLines[i].trim().isEmpty(); i++);
        return i;
    }


    private int readRoads(String[] scenarioLines, int i) throws ParseException {
        int start = i;

        for(; i < scenarioLines.length && !scenarioLines[i].trim().isEmpty(); i++);

        int numLines = i - start;

        TrackNetwork worldNetwork = new TrackNetwork();
        worldNetwork.addRoadsFromLines(scenarioLines, start, numLines);
        pixelNetwork = toPixelCoords(worldNetwork);
        palet.setRoads(pixelNetwork.getRoads());
        
        for(Track road : pixelNetwork.getRoads()) {
            if(road.getId() >= newRoadID) {
                newRoadID = road.getId() + 1;
            }
        }

        return i;
    }

    private int readBuildings(String[] scenarioLines, int i) throws ParseException {
        WKTReader reader = new WKTReader();

        List<Building> newBuildings = new ArrayList<Building>();
        
        for(; i < scenarioLines.length && !scenarioLines[i].trim().isEmpty(); i++) {
            Geometry geom = reader.read(scenarioLines[i]);

            if(geom instanceof Polygon) {
                Polygon worldPolygon = (Polygon) geom;
                Coordinate[] worldCoords = worldPolygon.getExteriorRing().getCoordinates();
                Coordinate[] pixelCoords = toPixelCoords(worldCoords);
                Polygon pixelPolygon = GEOM.createPolygon(GEOM.createLinearRing(pixelCoords), null);
                
                newBuildings.add(new Building(pixelPolygon));
            }
        }
        
        buildings.clear();
        buildings.addAll(newBuildings);
        palet.setBuildings(buildings);

        return i;
    }

    private int readWorldView(String[] scenarioLines, int i) {
        String[] parts = scenarioLines[i].split(" ");
        double width = Double.parseDouble(parts[0]);
        double height = Double.parseDouble(parts[1]);
        palet.setWorldWidthMeters(width);
        palet.setWorldHeightMeters(height);
        paletPresenter.onWidthUpdate(width);
        paletPresenter.onHeightUpdate(height);
        return i+1;
    }

    void glue() {
        if(glueVersion == worldVersion) {
            return;
        }

        List<Track> roadList = new ArrayList<Track>(pixelNetwork.getRoads());
        List<Track> roadsToRemove = new ArrayList<Track>();
        
        // Construct a set of identifiers
        Set<Integer> roadSet = new HashSet<Integer>();
        
        for(Track road : roadList) {
            roadSet.add(road.getId());
        }
        
        // Clean dead pointers before glueing
        for(Track road : roadList) {
            if(road.getFrom() != null
            &&!roadSet.contains(road.getFrom().getRoad())) {
                road.setFrom(null);
            }
            if(road.getTo() != null
            &&!roadSet.contains(road.getTo().getRoad())) {
                road.setTo(null);
            }
        }

        for(int i = 0; i < roadList.size(); i++) {
            Track roadA = roadList.get(i);

            // Iterate through other roads to find one to snap to A
            for(int j = 0; j < roadList.size(); j++) {
                Track roadB = roadList.get(j);
                
                // Snap road B to road A
                glue(roadA, roadB);
                
                if(roadB.getPathLength() == 0) {
                    roadsToRemove.add(roadB);
                }
            }
        }
        
        for(Track roadToRemove : roadsToRemove) {
            pixelNetwork.removeRoad(roadToRemove.getId());
        }
        
        glueVersion = worldVersion;
        palet.setRoads(pixelNetwork.getRoads());
    }
    
    void glue(
            Track roadA, 
            Track roadB) {
        boolean isSelf = roadA == roadB;
        
        LineString aPath = roadA.getPath();
        Coordinate[] aCoordinates = aPath.getCoordinates();
        Coordinate aStart = aCoordinates[0];
        Coordinate aEnd = aCoordinates[aCoordinates.length-1];

        if(roadB.getFrom() != null && roadB.getTo() != null) {
            // Road B is already connected. Since roads
            // are immutable, we won't find anything
            // new to glue
            return;
        }

        double snapDistancePixels = SNAP_DISTANCE_METERS / metersPerPixel;
        double snapDistancePixels2 = snapDistancePixels*snapDistancePixels;

        boolean snappedToAStart = false;
        boolean snappedToAEnd = false;
        boolean bStartSnapped = false;
        boolean bEndSnapped = false;

        LineString bPath = roadB.getPath();
        Coordinate[] bCoordinates = bPath.getCoordinates();
        Coordinate bStart = bCoordinates[0];
        Coordinate bEnd = bCoordinates[bCoordinates.length-1];

        Connector bFrom = roadB.getFrom();
        Connector bTo = roadB.getTo();

        if(GeoUtil.distance2(bStart, aStart) < snapDistancePixels2 && !isSelf) {
            // Snap b start to a start
            bStart = aStart;
            bStartSnapped = true;
            snappedToAStart = true;
        } else if(GeoUtil.distance2(bStart, aEnd) < snapDistancePixels2) {
            // Snap b start to a end
            bStart = aEnd;
            bStartSnapped = true;
            bFrom = new Connector(roadA.getId(), roadA.getPathLength());
            snappedToAEnd = true;
        }

        if(GeoUtil.distance2(bEnd, aStart) < snapDistancePixels2) {
            // Snap b end to a start
            bEnd = aStart;
            bEndSnapped = true;
            bTo = new Connector(roadA.getId(), 0.0);
            snappedToAStart = true;
        } else if(GeoUtil.distance2(bEnd, aEnd) < snapDistancePixels2 && !isSelf) {
            // Snap b end to a end
            bEnd = aEnd;
            bEndSnapped = true;
            snappedToAEnd = true;
        }

        if(!bStartSnapped && !isSelf) {
            DistanceOp dop = new DistanceOp(aPath, GEOM.createPoint(bStart));
            double distanceToA = dop.distance();

            if(distanceToA < snapDistancePixels) {
                bStart = dop.nearestPoints()[0];
                bStartSnapped = true;
                bFrom = new Connector(roadA.getId(), roadA.findOffset(bStart));
            }
        }

        if(!bEndSnapped && !isSelf) {
            DistanceOp dop = new DistanceOp(aPath, GEOM.createPoint(bEnd));
            double distanceToA = dop.distance();

            if(distanceToA < snapDistancePixels) {
                bEnd = dop.nearestPoints()[0];
                bEndSnapped = true;
                bTo = new Connector(roadA.getId(), roadA.findOffset(bEnd));
            }
        }

        if(bStartSnapped || bEndSnapped) {
            if(roadB.hasBezierCurve()) {
                BezierCurve oldCurve = roadB.getBezierCurve();
                Coordinate[] controlPoints = oldCurve.getControlPoints();

                controlPoints[0] = bStart;
                controlPoints[controlPoints.length-1] = bEnd;
                
                BezierCurve newCurve = new BezierCurve(controlPoints);
                
                roadB.setCurve(newCurve);
            } else {
                bCoordinates[0] = bStart;
                bCoordinates[bCoordinates.length-1] = bEnd;
                
                LineString newPath = GEOM.createLineString(bCoordinates);
                
                roadB.setPath(newPath);
            }

            
            if(!bStart.equals(bEnd)) {

                // Update B connectors
                roadB.setFrom(bFrom);
                roadB.setTo(bTo);
                
                // Update A connectors
                if(snappedToAStart && roadA.getFrom() == null) {
                    if(bEndSnapped) {
                        roadA.setFrom(new Connector(roadB.getId(), roadB.getPathLength()));
                    }
                }
                if(snappedToAEnd && roadA.getTo() == null) {
                    if(bStartSnapped) {
                        roadA.setTo(new Connector(roadB.getId(), 0.0));
                    }
                }
                
            }
        }
    }

    void expandSelectedRoad() {
        Track road = pixelNetwork.getRoad(selectedRoad);
        
        if(road == null) {
            return;
        }
        
        road.setType(TrackType.LANE);
        LineString path = road.getPath();
        Coordinate[] coordinates = path.getCoordinates();
        
        BufferParameters params = new BufferParameters(BufferParameters.CAP_FLAT, BufferParameters.JOIN_ROUND);
        params.setSingleSided(true);
        OffsetCurveBuilder ocv = new OffsetCurveBuilder(GEOM.getPrecisionModel(), params);
        Coordinate[] ring = ocv.getLineCurve(path.getCoordinates(), LANE_WIDTH / metersPerPixel);
        Coordinate[] lane = new Coordinate[ring.length - coordinates.length - 1];
        
        System.arraycopy(ring, ring.length - lane.length - 1, lane, 0, lane.length);

        List<Track> newRoads = new ArrayList<Track>();
        Track newLane = new Track(newRoadID++, TrackType.LANE, GEOM.createLineString(lane));
        
        newLane.setRightLane(road.getId());
        road.setLeftLane(newLane.getId());

        newRoads.add(newLane);
        //newRoads.addAll(createLaneChanges(road, newLane, 1.5*DISTANCE_BETWEEN_LANE_CHANGES / metersPerPixel));
        //newRoads.addAll(createLaneChanges(newLane, road, 1.5*DISTANCE_BETWEEN_LANE_CHANGES / metersPerPixel));

        worldVersion++;
        
        for(Track newRoad : newRoads) {
            pixelNetwork.addRoad(newRoad);
        }
        palet.setRoads(pixelNetwork.getRoads());
        palet.selectRoad(newLane.getId());
        selectedRoad = newLane.getId();
        
        actions.add(new Action(Action.Type.CREATE_ROADS, newRoads));
    }

    List<Track> createLaneChanges(Track laneA, Track laneB, double highwayStart) {
        List<Track> laneChanges = new ArrayList<Track>();

        double laneChangeInterval = DISTANCE_BETWEEN_LANE_CHANGES / metersPerPixel;
        double laneChangeLength = LANE_CHANGE_LENGTH / metersPerPixel;
        double minLength = Math.min(laneA.getPathLength(), laneB.getPathLength()-laneChangeLength);
        int numChanges = (int) ((minLength-highwayStart) / laneChangeInterval)+1;
        
        for(int i = 0; i < numChanges; i++) {
            double laneChangeStart = highwayStart + i*laneChangeInterval;
            Pose2D changeStart = laneA.getPose2D(laneChangeStart);
            Pose2D changeDepart = laneA.getPose2D(laneChangeStart+0.2*laneChangeLength);
            Pose2D changeArrive = laneB.getPose2D(laneChangeStart+0.8*laneChangeLength);
            Pose2D changeEnd = laneB.getPose2D(laneChangeStart+1.0*laneChangeLength);
            BezierCurve laneChangeCurve = new BezierCurve(new Coordinate[] {
                    changeStart.getPosition(),
                    changeDepart.getPosition(),
                    changeArrive.getPosition(),
                    changeEnd.getPosition()
            });
            Track laneChange = new Track(newRoadID++, TrackType.LANE_CHANGE, laneChangeCurve);
            laneChange.setFrom(new Connector(laneA.getId(), laneChangeStart));
            laneChange.setTo(new Connector(laneB.getId(), laneChangeStart+1.0*laneChangeLength));
            laneChanges.add(laneChange);
        }
        
        return laneChanges;
    }

    public void play() {
        glue();
        
        WorldScreenView simWorldView = new WorldScreenView(
                new Screen(palet.getScreenWidth(), palet.getScreenHeight()),
                new WorldView(0, 0, metersPerPixel*globalPixelView.getWidth(), metersPerPixel*globalPixelView.getHeight())
        );
        
        SimView simView = view.showSimView(
                simWorldView, 
                palet.getBackgroundDataURL()
        );
        
        RunConfig config = new RunConfig();
        config.set(SimulationParameters.SCENARIO_NAME, "builder");
        config.set(BuilderScenario.SCENARIO_TEXT_PARAM, getWorldAsText());

        final SimController simController = new SimController(
                config,
                new FileIOGWT()
        );
        
        // Initialization of builder scenario has no asynchronous steps 
        simController.init();

        final SimPresenter simPresenter = new SimPresenter(
                simView, 
                simController
        );

        runner = new SimPlayerGWT(
                simController
        );

        final Runnable onReset = new Runnable() {
            @Override
            public void run() {
                simPresenter.reset();
            }
        };
        
        PlayerPresenter player = new PlayerPresenter(
                simView.getPlayerView(), 
                runner,
                onReset
        );
        
        player.play();

    }

    void unplay() {
        if(runner == null) {
            return;
        }
        
        runner.stop();
        
        // Free up some memory
        runner = null;
        
        view.showPalet();
    }


}