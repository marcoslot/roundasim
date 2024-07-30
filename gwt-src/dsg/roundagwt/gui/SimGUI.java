/**
 * 
 */
package dsg.roundagwt.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import dsg.rounda.SimRun;
import dsg.rounda.gui.CircleFeature;
import dsg.rounda.gui.LineStringFeature;
import dsg.rounda.gui.PlayerView;
import dsg.rounda.gui.PolygonFeature;
import dsg.rounda.gui.Screen;
import dsg.rounda.gui.SimView;
import dsg.rounda.gui.VehicleAttachedFeature;
import dsg.rounda.gui.VehicleConsoleView;
import dsg.rounda.gui.WorldScreenView;
import dsg.rounda.model.IndicatorState;
import dsg.rounda.model.Pose2D;
import dsg.rounda.model.VehicleState;
import dsg.rounda.model.WorldState;
import dsg.rounda.model.WorldView;

/**
 * GWT GUI for running simulations
 */
public class SimGUI extends Composite implements SimView {

    private static final int DISPLAY_INTERVAL = 25;
    private static final GeometryFactory GEOM = new GeometryFactory();
    private static final Coordinate ROOT = new Coordinate(0, 0);
    private static final NumberFormat NF = NumberFormat.getFormat("0.0");
    private static final String CHOSEN_VEHICLE_COLOR = "#FFFFFF";
    private static final String SELECTED_VEHICLE_COLOR = "#FFFF00";
    private static final String ORDINARY_VEHICLE_COLOR = "#FF0000";
    private static final String COMM_COLOR = "#0000FF";
	private static final String INDICATOR_COLOUR = "#FFA600";
	private static final double INDICATOR_RADIUS = 0.5; // m

    private static SimGUIUiBinder uiBinder = GWT.create(SimGUIUiBinder.class);

    interface SimGUIUiBinder extends UiBinder<Widget, SimGUI> {
    }

    @UiField
    SimGUIResources res;

    @UiField
    Panel container;

    @UiField(provided=true)
    Canvas canvas;

    @UiField
    VehicleConsoleGUI console;

    @UiField
    CheckBox showLasers;

    @UiField
    CheckBox showQueryAreas;

    @UiField
    CheckBox showMeasuredAreas2D;

    @UiField
    CheckBox showMeasuredAreas1D;

    @UiField
    CheckBox showMeasuredRoute1D;

    @UiField
    CheckBox showEmptyWeakConflicts;
    
    @UiField
    CheckBox showSends;

    @UiField
    CheckBox showReceives;

    @UiField
    CheckBox showMembership;
    
    @UiField
    PlayerGUI player;

    ImageElement background;

    WorldState world;
    Collection<VehicleState> vehicles;
    List<LineFeature> laserFeatures;
    List<PolygonFeature> areaFeatures;
    Map<String,List<LineStringFeature>> lineFeatureLists;
    List<CircleFeature> circleFeatures;
    Set<VehicleAttachedFeature> vehicleAttachedFeatures;

    Coordinate mousePosition;
    Coordinate mouseStartDragPosition;

    WorldScreenView initialScreenView;
    WorldScreenView screenView;
    WorldScreenView backgroundView;

    boolean mouseInVehicle;
    int selectedVehicle;
    int chosenVehicle;
    boolean mouseDown;
    
    String commColor; 

    Presenter presenter;

    public SimGUI(WorldScreenView worldView) {
        this.initialScreenView = new WorldScreenView(worldView);
        this.screenView = worldView;
        this.selectedVehicle = -1;
        this.chosenVehicle = -1;
        this.laserFeatures = new ArrayList<LineFeature>();
        this.areaFeatures = new ArrayList<PolygonFeature>();
        this.lineFeatureLists = new HashMap<String,List<LineStringFeature>>();
        this.vehicleAttachedFeatures = new HashSet<VehicleAttachedFeature>();
        this.commColor = COMM_COLOR;
        
        this.canvas = Canvas.createIfSupported();
        
        if(this.canvas == null) {
            throw new Error("Canvas not supported");
        }
        
        this.canvas.setPixelSize((int) worldView.getScreenWidth(), (int) worldView.getScreenHeight());
        this.canvas.setCoordinateSpaceWidth((int) worldView.getScreenWidth());
        this.canvas.setCoordinateSpaceHeight((int) worldView.getScreenHeight());
        
        initWidget(uiBinder.createAndBindUi(this));
        
        this.drawTimer.scheduleRepeating(DISPLAY_INTERVAL);
        
        draw();
    }

    @Override
    public void init(SimRun run) {
        this.world = run.getWorld();
        this.vehicles = world.getVehicles();
        clearTemporaryFeatures();
    }

    final Timer drawTimer = new Timer() {
        @Override
        public void run() {
            draw();
        }
    };

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    public void setBackground(String backgroundURL) {
        final Image backgroundImage = new Image(backgroundURL);
        backgroundImage.addLoadHandler(new LoadHandler() {

            @Override
            public void onLoad(LoadEvent event) {
                // Now the background size is available
                double backgroundWidth = backgroundImage.getWidth();
                double backgroundHeight = backgroundImage.getHeight();
                
                backgroundView = new WorldScreenView(
                        new Screen(backgroundWidth, backgroundHeight),
                        new WorldView(0, 0, initialScreenView.getWorldWidth(), initialScreenView.getWorldHeight())
                );

                background = backgroundImage.getElement().cast();
                
            }

        });

        // Trigger load
        backgroundImage.addStyleName(res.style().hidden());
        RootPanel.get().add(backgroundImage);
    }


    @Override
    public void clearTemporaryFeatures() {
        laserFeatures.clear();
        areaFeatures = null;
        circleFeatures = null;
        lineFeatureLists.clear();
        vehicleAttachedFeatures.clear();
    }

    void draw() {
        CanvasPainter g = new CanvasPainter(canvas.getContext2d());
        drawBackground(g);
        drawLasers(g);
        drawPolygons(g);
        drawVehicleAttachedFeatures(g);
        drawLineStrings(g);
        drawCircles(g);
        drawVehicles(g);
        drawMouseCoordinates(g);
    }

    private void drawCircles(CanvasPainter g) {
        if(circleFeatures == null || circleFeatures.isEmpty()) {
            return;
        }
        
        for(CircleFeature feature : circleFeatures) {
            g.drawCircle(
                    feature.getCoordinate(), 
                    feature.getColor().toString(), 
                    feature.getRadius(),
                    screenView);
        }
    }

    private void drawLineStrings(CanvasPainter g) {
        for(List<LineStringFeature> lineFeatures : lineFeatureLists.values()) {
            for(LineStringFeature feature : lineFeatures) {
                g.drawLineString(
                        feature.getLineString(), 
                        feature.getColor().toString(), 
                        feature.getLineWidth(),
                        screenView);
            }
        }
    }

    private void drawPolygons(CanvasPainter g) {
        if(areaFeatures == null || areaFeatures.isEmpty()) {
            return;
        }
        
        for(PolygonFeature feature : areaFeatures) {
            g.strokePolygon(
                    feature.getLine(), 
                    feature.getColor().toString(), 
                    feature.getLineWidth(), 
                    screenView);
        }
    }

    void drawBackground(CanvasPainter g) {
        g.setFillStyle("#FFFFFF");
        g.fillRect(0, 0, getScreenWidth(), getScreenHeight());

        if(background == null) {
            return;
        }

        Coordinate cutXY = backgroundView.toScreenCoord(screenView.toWorldCoord(ROOT));
        double cutWidth = backgroundView.toScreenWidth(screenView.getWorldWidth());
        double cutHeight = backgroundView.toScreenHeight(screenView.getWorldHeight());

        g.drawImage(
                background, 
                cutXY.x,
                cutXY.y,
                cutWidth,
                cutHeight, 
                0,
                0,
                screenView.getScreenWidth(),
                screenView.getScreenHeight()
        );
    }

    void drawLasers(CanvasPainter g) {
        if(laserFeatures == null || laserFeatures.isEmpty()) {
            return;
        }
        for(LineFeature feature : laserFeatures) {
            Coordinate startCoord = feature.getLine().p0;
            Coordinate endCoord = feature.getLine().p1;

            g.setStrokeStyle(feature.getColor());
            g.setLineWidth(0.5);
            g.beginPath();
            g.moveTo(startCoord.x, startCoord.y);
            g.lineTo(endCoord.x, endCoord.y);
            g.closePath();
            g.stroke();
        }
    }

    private void drawVehicleAttachedFeatures(CanvasPainter g) {
        if(vehicleAttachedFeatures == null || vehicleAttachedFeatures.isEmpty()) {
            return;
        }
        
        for(VehicleAttachedFeature feature : vehicleAttachedFeatures) {
            VehicleState srcVehicle = world.getVehicle(feature.getSourceID());
            VehicleState dstVehicle = world.getVehicle(feature.getDestinationID());
            
            if(srcVehicle == null || dstVehicle == null) {
                continue;
            }
            
            Coordinate srcPos = srcVehicle.getPosition2D();
            Coordinate dstPos = dstVehicle.getPosition2D();
            Coordinate srcScreenPos = screenView.toScreenCoord(srcPos);
            Coordinate dstScreenPos = screenView.toScreenCoord(dstPos);
            
            g.setLineWidth(1.0);
            g.setStrokeStyle(COMM_COLOR);
            g.beginPath();
            g.moveTo(srcScreenPos.x, srcScreenPos.y);
            g.lineTo(dstScreenPos.x, dstScreenPos.y);
            g.closePath();
            g.stroke();
        }
    }

    void drawMouseCoordinates(CanvasPainter g) {
        if(mousePosition == null) {
            return;
        }
        g.setFillStyle("#FFFFFF");
        g.fillText(NF.format(mousePosition.x) + " " + NF.format(mousePosition.y), 10, 440);
    }

    void drawVehicles(CanvasPainter g) {
        if(vehicles == null) {
            return;
        }

        Point mousePoint = GEOM.createPoint(mousePosition);
        mouseInVehicle = false;

        for(VehicleState vehicle : vehicles) {
            Polygon vehicleGeometry = vehicle.getVehicleGeometry();

            if(!mouseInVehicle && mousePosition != null) {
                Pose2D vehiclePose = vehicle.getBackPosition().getPose2D();
                double diagonal2 = vehicle.getLength() * vehicle.getLength() + vehicle.getWidth() * vehicle.getWidth();

                if(distance2(mousePosition, vehiclePose.getPosition()) < 0.5*diagonal2
                        && vehicleGeometry.contains(mousePoint)) {
                    usePointerCursor();
                    mouseInVehicle = true;
                    selectedVehicle = vehicle.getId();
                }
            }
            
            Coordinate[] coordinates = vehicleGeometry.getCoordinates();
            
            IndicatorState indicator = vehicle.getIndicatorState();
            
            if(indicator != IndicatorState.NONE && (System.currentTimeMillis() / 200) % 2 == 0) {
            	boolean[] drawIndicator;
            	
            	switch(indicator) {
            	case LEFT:
            		drawIndicator = new boolean[] { true, true, false, false};
            		break;
            	case RIGHT:
            		drawIndicator = new boolean[] { false, false, true, true};
            		break;
            	case HAZARD:
            		drawIndicator = new boolean[] { true, true, true, true};
            		break;
            	default:
            		drawIndicator = new boolean[] { false, false, false, false};
            		break;
            	}
            	
            	for(int i = 0; i < 4; i++) {
            		if(!drawIndicator[i]) {
            			continue;
            		}
                	Coordinate indicatorCoordinate = coordinates[i];

                    g.drawCircle(
                    		indicatorCoordinate, 
                    		INDICATOR_COLOUR, 
                    		INDICATOR_RADIUS,
                            screenView);
            	}
            }

            if(vehicle.getId() == chosenVehicle) {
                g.setFillStyle(CHOSEN_VEHICLE_COLOR);
            } else if(vehicle.getId() == selectedVehicle) {
                g.setFillStyle(SELECTED_VEHICLE_COLOR);
            } else {
                g.setFillStyle(ORDINARY_VEHICLE_COLOR);
            }
            g.beginPath();

            Coordinate firstCoord = screenView.toScreenCoord(coordinates[0]);
            g.moveTo(firstCoord.x, firstCoord.y);

            for(int i = 1; i < coordinates.length; i++) {
                Coordinate displayCoord = screenView.toScreenCoord(coordinates[i]);
                g.lineTo(displayCoord.x, displayCoord.y);
            }

            g.closePath();
            g.fill();
        }

        if(!mouseInVehicle) {
            useRegularCursor();
        }
    }

    private void usePointerCursor() {
        canvas.addStyleName(res.style().pointerCursor());
    }

    private void useRegularCursor() {
        canvas.removeStyleName(res.style().pointerCursor());
    }
    
    double distance2(Coordinate a, Coordinate b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return dx*dx + dy*dy;
    }

    @Override
    public void setChosenVehicle(int vehicleID) {
        this.chosenVehicle = vehicleID;
    }

    @UiHandler("showSends")
    void onShowSendChange(ValueChangeEvent<Boolean> evt) {
        if(presenter != null) {
            presenter.onShowSendClicked(evt.getValue());
        }
    }

    @UiHandler("showReceives")
    void onShowReceiveChange(ValueChangeEvent<Boolean> evt) {
        if(presenter != null) {
            presenter.onShowReceiveClicked(evt.getValue());
        }
    }

    @UiHandler("showLasers")
    void onShowLasersChange(ValueChangeEvent<Boolean> evt) {
        if(presenter != null) {
            presenter.onShowLasersClicked(evt.getValue());
        }
    }

    @UiHandler("showMeasuredAreas2D")
    void onShowMeasuredAreas2D(ValueChangeEvent<Boolean> evt) {
        if(presenter != null) {
            presenter.onShowMeasuredAreas2DClicked(evt.getValue());
        }
    }

    @UiHandler("showMeasuredAreas1D")
    void onShowMeasuredAreas1D(ValueChangeEvent<Boolean> evt) {
        if(presenter != null) {
            presenter.onShowMeasuredAreas1DClicked(evt.getValue());
        }
    }

    @UiHandler("showMeasuredRoute1D")
    void onShowMeasuredRoute1D(ValueChangeEvent<Boolean> evt) {
        if(presenter != null) {
            presenter.onShowMeasuredRoute1DClicked(evt.getValue());
        }
    }

    @UiHandler("showEmptyWeakConflicts")
    void onShowEmptyWeakConflicts(ValueChangeEvent<Boolean> evt) {
        if(presenter != null) {
            presenter.onEmptyWeakConflicts(evt.getValue());
        }
    }

    @UiHandler("showQueryAreas")
    void onShowQueryAreas(ValueChangeEvent<Boolean> evt) {
        if(presenter != null) {
            presenter.onShowQueryAreasClicked(evt.getValue());
        }
    }

    @UiHandler("showMembership")
    void onShowMembership(ValueChangeEvent<Boolean> evt) {
        if(presenter != null) {
            presenter.onShowMembershipClicked(evt.getValue());
        }
    }

    @UiHandler("canvas")
    void onMouseMove(MouseMoveEvent evt) {
        Coordinate screenCoord = new Coordinate(
                evt.getX(),
                evt.getY());
        
        mousePosition = screenView.toWorldCoord(screenCoord);
        
        if(presenter != null && mouseDown) {
            double dragX = screenView.toWorldWidth(evt.getX() - mouseStartDragPosition.x);
            double dragY = screenView.toWorldHeight(mouseStartDragPosition.y - evt.getY());
            presenter.onDrag(dragX, dragY);
            mouseStartDragPosition = screenCoord;
        }
    }

    @UiHandler("canvas")
    void onMouseDown(MouseDownEvent evt) {
        mouseStartDragPosition = new Coordinate(
                evt.getX(),
                evt.getY());
        mouseDown = true;
    }

    @UiHandler("canvas")
    void onMouseUp(MouseUpEvent evt) {
        mouseDown = false;
    }

    @UiHandler("canvas")
    void onClick(ClickEvent evt) {
        if(selectedVehicle != -1 && presenter != null) {
            presenter.onVehicleSelected(selectedVehicle);
        }
    }

    @UiHandler("canvas")
    void onMouseWheel(MouseWheelEvent evt) {
        if(presenter != null) {
            mousePosition = screenView.toWorldCoord(new Coordinate(evt.getX(),evt.getY()));
            presenter.onZoom(evt.getDeltaY(), mousePosition);
        }
    }

    @Override
    public VehicleConsoleView getVehicleConsole() {
        return console;
    }

    @Override
    public void showTemporaryLines(List<LineSegment> beams) {
        List<LineFeature> beamFeatures = new ArrayList<LineFeature>();

        for(LineSegment beam : beams) {
            LineSegment displayBeam = new LineSegment(
                    screenView.toScreenCoord(beam.p0), 
                    screenView.toScreenCoord(beam.p1));

            LineFeature beamFeature = new LineFeature(
                    displayBeam,  
                    "#00FF00");

            beamFeatures.add(beamFeature);
        }

        laserFeatures = beamFeatures;
    }

    @Override
    public void showTemporaryPolygons(List<PolygonFeature> features) {
        this.areaFeatures = features;
    }

    @Override
    public void showTemporaryLineStrings(String key, List<LineStringFeature> features) {
        if(features == null) {
            this.lineFeatureLists.remove(key);
        } else {
            this.lineFeatureLists.put(key, features);
        }
    }

    @Override
    public void showTemporaryCircles(List<CircleFeature> features) {
        this.circleFeatures = features;
    }

    @Override
    public void showCommunication(VehicleAttachedFeature feature) {
        this.vehicleAttachedFeatures.add(feature);
    }

    @Override
    public void hideCommunication(VehicleAttachedFeature feature) {
        this.vehicleAttachedFeatures.remove(feature);
    }

    @Override
    public void setShowLasers(boolean checked) {
        this.showLasers.setValue(checked, false);
    }

    @Override
    public void setShowReceives(boolean checked) {
        this.showReceives.setValue(checked, false);
    }

    @Override
    public void setShowSends(boolean checked) {
        this.showSends.setValue(checked, false);
    }

    @Override
    public void setShowMeasuredAreas1D(boolean checked) {
        this.showMeasuredAreas1D.setValue(checked, false);
    }

    @Override
    public void setShowMeasuredAreas2D(boolean checked) {
        this.showMeasuredAreas2D.setValue(checked, false);
    }

    @Override
    public void setShowQueryAreas(boolean checked) {
        this.showQueryAreas.setValue(checked, false);
    }


    @Override
    public void setShowMeasuredRoute1D(boolean checked) {
        this.showMeasuredRoute1D.setValue(checked, false);
    }


    @Override
    public void setShowMembership(boolean checked) {
        this.showMembership.setValue(checked, false);
    }


    @Override
    public WorldView getWorldView() {
        return screenView.getWorldView();
    }

    @Override
    public void setWorldView(WorldView worldView) {
        this.screenView.setWorldView(worldView);
    }

    private double getScreenHeight() {
        return screenView.getScreenWidth();
    }

    private double getScreenWidth() {
        return screenView.getScreenHeight();
    }
    
    @Override
    public PlayerView getPlayerView() {
        return player;
    }

}
