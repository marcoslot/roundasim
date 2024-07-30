/**
 * 
 */
package dsg.roundagwt.gui;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.vectomatic.dnd.DataTransferExt;
import org.vectomatic.dnd.DropPanel;
import org.vectomatic.file.File;
import org.vectomatic.file.FileList;
import org.vectomatic.file.FileUploadExt;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

import dsg.rounda.geometry.BezierCurve;
import dsg.rounda.gui.Screen;
import dsg.rounda.gui.WorldScreenView;
import dsg.rounda.model.Building;
import dsg.rounda.model.Track;
import dsg.rounda.model.WorldView;

/**
 * @author slotm
 *
 */
public class BuilderPaletGUI extends Composite implements BuilderPaletView {

    private static final int NEW_ROAD_WIDTH = 2;
    private static final int ROAD_GUIDES_WIDTH = 1;
    private static final int MARGIN = 10;
    private static final int SCREEN_WIDTH = 600;
    private static final int DEFAULT_SCREEN_HEIGHT = 450;
    private static final Coordinate ROOT = new Coordinate(MARGIN, MARGIN);

    private static final NumberFormat METER_FORMAT = NumberFormat.getFormat("0.0m");

    private static final String MARKER_TEXT_COLOR = "white";
    private static final String MARKER_COLOR = "#FF0000";
    private static final int MARKER_HEIGHT = 18;
    private static final int MARKER_WIDTH = 8;

    private static final String NEW_ROAD_COLOR = "#E0E0E0";
    private static final String NEW_ROAD_GUIDES_COLOR = "#000000";
    private static final String ROAD_COLOR = "#A0A090";
    private static final String SELECTED_ROAD_COLOR = "#FFFF22";
    private static final double ROAD_STROKE_WIDTH = 1.0; // meters
    private static final double ROAD_POINT_RADIUS = 1.0; // meters

    private static final String BUILDING_STROKE_COLOR = "#101010";
    private static final String BUILDING_FILL_COLOR = "rgba(200,200,200,0.4)";
    private static final double BUILDING_WALL_THICKNESS = 0.5; // meters
    
    private static final String CONNECTOR_COLOR = "#F02222";
    private static final String NEW_CONNECTOR_COLOR = "#F06666";
    private static final double CONNECTOR_RADIUS = 1.0; // meters

    public static final int OPEN_BUTTON = 1 << 0;
    public static final int CREATE_ROAD_BUTTON = 1 <<1;
    public static final int GLUE_BUTTON = 1 <<2;
    public static final int CREATE_BUILDING_BUTTON = 1 <<3;
    public static final int UNDO_BUTTON = 1 << 4;
    public static final int WORLD_BUTTON = 1 << 5;
    public static final int TEXT_BUTTON = 1 << 6;
    public static final int EXPAND_ROAD_BUTTON = 1 << 7;
    public static final int ALL_BUTTONS = 0xffffffff;

    private static final int DISPLAY_INTERVAL = 10;

    private static BuilderPaletGUIUiBinder uiBinder = GWT
            .create(BuilderPaletGUIUiBinder.class);

    interface BuilderPaletGUIUiBinder extends UiBinder<Widget, BuilderPaletGUI> {
    }

    @UiField
    SimGUIResources res;

    @UiField
    Label errorLabel;

    @UiField(provided = true)
    Canvas canvas;

    @UiField
    TextBox marker1LatLong;

    @UiField
    TextBox marker2LatLong;

    @UiField
    Panel markerPanel1;

    @UiField
    Panel markerPanel2;

    @UiField
    FileUploadExt fileChooser;

    @UiField
    DropPanel dropPanel;

    @UiField
    TextBox widthBox;

    @UiField
    TextBox heightBox;

    ImageElement background;

    Presenter presenter;
    Mode mode;

    Coordinate marker1;
    Coordinate marker2;

    Coordinate mouseDragStart;
    Coordinate mouseScreenCoord;
    Coordinate mouseWorldCoord;
    
    boolean shiftDown;
    boolean mouseDown;
    boolean dragging;

    WorldScreenView screenView;
    WorldScreenView backgroundView;
    double pixelsPerMeter;

    List<Coordinate> newRoadControlPoints;
    LineString pendingRoadCurve;
    List<Coordinate> newBuildingCorners;

    Collection<Track> roads;
    Collection<Building> buildings;
    protected String backgroundType;

    int selectedRoad;
    private boolean controlDown;

    public BuilderPaletGUI() {
        mode = Mode.IDLE;
        roads = new ArrayList<Track>();
        buildings = new ArrayList<Building>();
        mouseScreenCoord = new Coordinate();
        mouseWorldCoord = new Coordinate();
        pixelsPerMeter = 1.0;
        selectedRoad = -1;
        
        // Initialize the screen view with an arbitrary setting of
        // 1px = 1 meter, and the world is 600*450 meters
        screenView = new WorldScreenView(
                new Screen(MARGIN, MARGIN, SCREEN_WIDTH, DEFAULT_SCREEN_HEIGHT), 
                new WorldView(0, 0, SCREEN_WIDTH, DEFAULT_SCREEN_HEIGHT)
        );

        canvas = Canvas.createIfSupported();

        if(canvas == null) {
            throw new RuntimeException("Canvas is not supported");
        }

        canvas.setPixelSize((int) screenView.getScreenWidth()+2*MARGIN, (int) screenView.getScreenHeight()+2*MARGIN);
        canvas.setCoordinateSpaceWidth((int) screenView.getScreenWidth()+2*MARGIN);
        canvas.setCoordinateSpaceHeight((int) screenView.getScreenHeight()+2*MARGIN);

        initWidget(uiBinder.createAndBindUi(this));
        this.res.style().ensureInjected();
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void draw() {
        CanvasPainter g = new CanvasPainter(canvas.getContext2d());
        drawBackground(g);
        drawArrows(g);
        drawBuildings(g);
        drawRoads(g);
        drawMarkers(g);
        drawNewRoad(g);
        drawNewBuilding(g);
    }

    private void drawBuildings(CanvasPainter g) {
        for(Building building : buildings) {
            g.fillPolygon(
                    building.getPolygon(),
                    BUILDING_FILL_COLOR,
                    screenView);
        }
    }

    private void drawNewBuilding(CanvasPainter g) {
        if(newBuildingCorners == null || newBuildingCorners.size() < 2) {
            return;
        }

        g.drawCoordinates(
                newBuildingCorners,
                BUILDING_STROKE_COLOR,
                2,
                screenView);
        
        if(newBuildingCorners.size() > 3) {
            g.fillPolygon(
                    newBuildingCorners, 
                    BUILDING_FILL_COLOR, 
                    screenView);
        }
        
    }

    void drawBackground(CanvasPainter g) {
        g.setFillStyle("#FFFFFF");
        g.fillRect(0, 0, getScreenWidth()+2*MARGIN, getScreenHeight()+2*MARGIN);

        if(backgroundView == null) {
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
                MARGIN,
                MARGIN,
                getScreenWidth(),
                getScreenHeight());

    }

    private void drawArrows(CanvasPainter g) {

        double verticalLineX = getScreenWidth() + 1.5 * MARGIN;
        double horizontalLineY = getScreenHeight() + 1.5 * MARGIN;

        g.setLineWidth(1);

        g.drawLine(
                new Coordinate(MARGIN, horizontalLineY), 
                new Coordinate(getScreenWidth() + MARGIN, horizontalLineY), 
                "#000000");

        g.drawLine(
                new Coordinate(MARGIN, horizontalLineY), 
                new Coordinate(MARGIN+7, horizontalLineY-3), 
                "#000000");

        g.drawLine(
                new Coordinate(MARGIN, horizontalLineY), 
                new Coordinate(MARGIN+7, horizontalLineY+3), 
                "#000000");

        g.drawLine(
                new Coordinate(getScreenWidth() + MARGIN, horizontalLineY), 
                new Coordinate(getScreenWidth() + MARGIN - 7, horizontalLineY-3), 
                "#000000");

        g.drawLine(
                new Coordinate(getScreenWidth() + MARGIN, horizontalLineY), 
                new Coordinate(getScreenWidth() + MARGIN - 7, horizontalLineY+3), 
                "#000000");

        g.drawLine(
                new Coordinate(MARGIN, horizontalLineY), 
                new Coordinate(MARGIN+7, horizontalLineY-3), 
                "#000000");

        g.drawLine(
                new Coordinate(MARGIN, horizontalLineY), 
                new Coordinate(MARGIN+7, horizontalLineY+3), 
                "#000000");

        g.drawLine(
                new Coordinate(getScreenWidth() + MARGIN, horizontalLineY), 
                new Coordinate(getScreenWidth() + MARGIN - 7, horizontalLineY-3), 
                "#000000");

        g.drawLine(
                new Coordinate(getScreenWidth() + MARGIN, horizontalLineY), 
                new Coordinate(getScreenWidth() + MARGIN - 7, horizontalLineY+3), 
                "#000000");


        // Vertical line

        g.drawLine(
                new Coordinate(verticalLineX, MARGIN), 
                new Coordinate(verticalLineX, getScreenHeight() + MARGIN), 
                "#000000");

        g.drawLine(
                new Coordinate(verticalLineX, MARGIN), 
                new Coordinate(verticalLineX+3,  MARGIN + 7), 
                "#000000");

        g.drawLine(
                new Coordinate(verticalLineX, MARGIN), 
                new Coordinate(verticalLineX-3, MARGIN + 7), 
                "#000000");


        g.drawLine(
                new Coordinate(verticalLineX, getScreenHeight() + MARGIN), 
                new Coordinate(verticalLineX+3, getScreenHeight() + MARGIN - 7), 
                "#000000");

        g.drawLine(
                new Coordinate(verticalLineX, getScreenHeight() + MARGIN), 
                new Coordinate(verticalLineX-3, getScreenHeight() + MARGIN - 7), 
                "#000000");

    }

    private void drawRoads(CanvasPainter g) {
        for(Track road : roads) {
            LineString path = road.getPath();
            
            String color = road.getId() == selectedRoad ? SELECTED_ROAD_COLOR : ROAD_COLOR;
            
            g.drawCircle(
                    path.getCoordinateN(0), 
                    color, 
                    ROAD_POINT_RADIUS*pixelsPerMeter, 
                    screenView);
            
            g.drawLineString(
                    path, 
                    color, 
                    screenView.toScreenWidth(ROAD_STROKE_WIDTH*pixelsPerMeter), 
                    screenView);
            
            g.drawCircle(
                    path.getCoordinateN(path.getNumPoints()-1), 
                    color, 
                    ROAD_POINT_RADIUS*pixelsPerMeter, 
                    screenView);
        }
    }

    private void drawNewRoad(CanvasPainter g) {
        switch(mode) {
            case CREATE_ROAD_IN_PROGRESS:
                g.drawLineString(pendingRoadCurve, NEW_ROAD_COLOR, NEW_ROAD_WIDTH, screenView);
                g.drawCoordinates(newRoadControlPoints, NEW_ROAD_GUIDES_COLOR, ROAD_GUIDES_WIDTH, screenView);
                break;
            default:
                break;
        }
    }

    private void drawMarkers(CanvasPainter g) {
        switch(mode) {
            case DRAG_MARKER_1:
            case DRAG_MARKER_2:
            case MEASURE_WORLD:
                // Draw shades showing where to put the marker
                g.setFillStyle("rgba(0,0,0,0.5)");
                g.fillRect(MARGIN, MARGIN, getScreenWidth(), MARGIN);
                g.fillRect(MARGIN, MARGIN, MARGIN, getScreenHeight());
                g.fillRect(MARGIN, getScreenHeight(), getScreenWidth(), MARGIN);
                g.fillRect(getScreenWidth(), MARGIN, MARGIN, getScreenHeight());
                g.fillRect(2*MARGIN, 0.5*getScreenHeight(), getScreenWidth()-20, MARGIN);
                g.fillRect(0.5*getScreenWidth(), 2*MARGIN, 0.5*getScreenWidth(), 0.5*getScreenHeight()-2*MARGIN);
                g.fillRect(2*MARGIN, 0.5*getScreenHeight()+MARGIN, 0.5*getScreenWidth(), 0.5*getScreenHeight()-MARGIN);
                
                // Draw markers
                drawMarker(g, marker2, "2");
                drawMarker(g, marker1, "1");
                break;
            default:
                break;
        }
    }

    private void drawMarker(CanvasPainter g, Coordinate marker, String label) {
        g.setFillStyle(MARKER_COLOR);
        g.beginPath();
        g.moveTo(marker.x, marker.y);
        g.lineTo(marker.x + MARKER_WIDTH, marker.y + MARKER_HEIGHT);
        g.lineTo(marker.x, marker.y + 15);
        g.lineTo(marker.x - MARKER_WIDTH, marker.y + MARKER_HEIGHT);
        g.lineTo(marker.x, marker.y);
        g.closePath();
        g.fill();
        g.setFillStyle(MARKER_TEXT_COLOR);
        g.fillText(label, marker.x-3, marker.y+13);
    }

    void updateMouseCoord(MouseEvent evt) {
        mouseScreenCoord.x = evt.getX();
        mouseScreenCoord.y = evt.getY();
        mouseWorldCoord.x = screenView.toWorldX(evt.getX());
        mouseWorldCoord.y = screenView.toWorldY(evt.getY());
    }

    void usePointerCursor() {
        canvas.addStyleName(res.style().pointerCursor());
    }

    void useRegularCursor() {
        canvas.removeStyleName(res.style().pointerCursor());
    }

    @UiHandler("canvas")
    public void onMouseMove(MouseMoveEvent evt) {
        updateMouseCoord(evt);

        switch(mode) {
            case DRAG_MARKER_1:
                if(mouseScreenCoord.x > 20 && mouseScreenCoord.x < 0.5 * getScreenWidth()
                && mouseScreenCoord.y > 20 && mouseScreenCoord.y < 0.5 * getScreenHeight()) {
                    marker1.x += mouseScreenCoord.x - mouseDragStart.x;
                    marker1.y += mouseScreenCoord.y - mouseDragStart.y;
                    mouseDragStart = new Coordinate(mouseScreenCoord);
                }
                draw();
                break;
            case DRAG_MARKER_2:
                if(mouseScreenCoord.x > 0.5 * getScreenWidth() + 20 && mouseScreenCoord.x < getScreenWidth()
                && mouseScreenCoord.y > 0.5 * getScreenHeight() + 20 && mouseScreenCoord.y <getScreenHeight()) {
                    marker2.x += mouseScreenCoord.x - mouseDragStart.x;
                    marker2.y += mouseScreenCoord.y - mouseDragStart.y;
                    mouseDragStart = new Coordinate(mouseScreenCoord);
                }
                draw();
                break;
            case CREATE_ROAD_IN_PROGRESS:
                updateNewRoadCurve();
            case CREATE_ROAD_START:
            case CREATE_BUILDING:
                if(mouseDown && !mouseDragStart.equals(mouseScreenCoord)) {
                    drag();
                }
                draw();
                break;
            case IDLE:
                if(mouseDown && !mouseDragStart.equals(mouseScreenCoord)) {
                    drag();
                    draw();
                }
                break;
            case MEASURE_WORLD: 
                if(marker1 != null && touchesMarker(mouseScreenCoord, marker1)) {
                    usePointerCursor();
                } else if(marker2 != null && touchesMarker(mouseScreenCoord, marker2)) {
                    usePointerCursor();
                } else {
                    useRegularCursor();
                }
                break;
            default:
                break;
        }
    }

    @UiHandler("canvas")
    public void onMouseDown(MouseDownEvent evt) {
        updateMouseCoord(evt);
        mouseDragStart = new Coordinate(mouseScreenCoord);

        switch(mode) {
            case MEASURE_WORLD: 
                if(marker1 != null && touchesMarker(mouseScreenCoord, marker1)) {
                    mode = Mode.DRAG_MARKER_1;
                } else if(marker2 != null && touchesMarker(mouseScreenCoord, marker2)) {
                    mode = Mode.DRAG_MARKER_2;
                } 
                break;
            default:
                break;
        }
        
        mouseDown = true;
        draw();
    }

    @UiHandler("canvas")
    public void onMouseUp(MouseUpEvent evt) {
        updateMouseCoord(evt);

        switch(mode) {
            case DRAG_MARKER_1:
                mode = Mode.MEASURE_WORLD;
                break;
            case DRAG_MARKER_2:
                mode = Mode.MEASURE_WORLD;
                break;
            default:
                break;
        }

        mouseDown = false;
        draw();
    }

    @UiHandler("canvas")
    void onMouseWheel(MouseWheelEvent evt) {
        updateMouseCoord(evt);
        shiftDown = evt.isShiftKeyDown();
        canvas.setFocus(true);

        switch(mode) {
            case CREATE_BUILDING:
            case CREATE_ROAD_START:
            case CREATE_ROAD_IN_PROGRESS:
            case IDLE:
                if(presenter != null) {
                    if(shiftDown) {
                        presenter.onSelectVehicle(evt.getDeltaY());
                    } else {
                        presenter.onZoom(evt.getDeltaY(), screenView.toWorldCoord(mouseScreenCoord));
                    }
                }
                break;
            default:
                break;
        }
        draw();
    }

    @UiHandler("canvas")
    public void onCanvasClick(ClickEvent evt) {
        updateMouseCoord(evt);
        
        if(dragging) {
            // Ignore dragged clicks
            dragging = false;
            return;
        }

        switch(mode) {
            case CREATE_BUILDING:
                if(shiftDown || newBuildingCorners.size() < 4) {
                    // Insert static mouse position before dynamic mouse position
                    newBuildingCorners.add(newBuildingCorners.size()-1, new Coordinate(mouseWorldCoord));
                    break;
                }

                // Freeze dynamic mouse position by copying it
                newBuildingCorners.set(newBuildingCorners.size()-1, new Coordinate(mouseWorldCoord));
                
                // Complete polygon 
                newBuildingCorners.add(newBuildingCorners.get(0));
                
                if(presenter != null) {
                    presenter.addBuilding(newBuildingCorners);
                }
                
                break;
            case CREATE_ROAD_START:
                // Insert before active mouse position
                newRoadControlPoints.add(newRoadControlPoints.size()-1, new Coordinate(mouseWorldCoord));
                
                updateNewRoadCurve();
                
                mode = Mode.CREATE_ROAD_IN_PROGRESS;
                break;
            case CREATE_ROAD_IN_PROGRESS:
                if(shiftDown && newRoadControlPoints.size() < 32) {

                    // Insert before active mouse position
                    newRoadControlPoints.add(newRoadControlPoints.size()-1, new Coordinate(mouseWorldCoord));
                    updateNewRoadCurve();
                    break;
                } 

                // Freeze mouse position
                newRoadControlPoints.set(newRoadControlPoints.size()-1, new Coordinate(mouseWorldCoord));

                createRoadDone();
                break;
        }
        draw();
    }

    void drag() {
        if(presenter != null) {
            double dragX = screenView.toWorldWidth(mouseScreenCoord.x - mouseDragStart.x);
            double dragY = screenView.toWorldHeight(mouseDragStart.y - mouseScreenCoord.y);
            presenter.onDrag(dragX, dragY);
        }
        mouseDragStart = new Coordinate(mouseScreenCoord);
        dragging = true;
    }

    private boolean touchesMarker(Coordinate mouse, Coordinate marker) {
        return marker.x - MARKER_WIDTH <= mouse.x && mouse.x <= marker.x + MARKER_WIDTH
            && marker.y <= mouse.y && mouse.y <= marker.y + MARKER_HEIGHT;
    }

    @UiHandler("canvas")
    public void onKeyDown(KeyDownEvent evt) {
        shiftDown = evt.isShiftKeyDown();
        controlDown = evt.isControlKeyDown();
    }

    @UiHandler("canvas")
    public void onKeyUp(KeyUpEvent evt) {
        int keyCode = evt.getNativeKeyCode();
        shiftDown = evt.isShiftKeyDown();
        controlDown = evt.isControlKeyDown();

        switch(keyCode) {
            case KeyCodes.KEY_DELETE:
                switch(mode) {
                    case CREATE_ROAD_IN_PROGRESS:
                        if(newRoadControlPoints.size() > 1) {
                            newRoadControlPoints.remove(newRoadControlPoints.size()-2);
                            updateNewRoadCurve();
                        }
                        break;
                    case IDLE:
                        if(presenter != null && selectedRoad != -1) {
                            presenter.removeSelectedRoad();
                        }
                        break;
                    default:
                        break;
                }

                evt.preventDefault();
                break;
            case KeyCodes.KEY_ENTER:
                switch(mode) {
                    case CREATE_ROAD_IN_PROGRESS:
                        // remove dynamic mouse position, only use fixed ones 
                        newRoadControlPoints.remove(newRoadControlPoints.size()-1);

                        createRoadDone();
                        break;
                }
                evt.preventDefault();
                break;
            case KeyCodes.KEY_ESCAPE:
                switch(mode) {
                    case CREATE_ROAD_START:
                    case CREATE_ROAD_IN_PROGRESS:
                        if(presenter != null) {
                            presenter.cancelRoad();
                        }
                        break;
                    case CREATE_BUILDING:
                        if(presenter != null) {
                            presenter.cancelBuilding();
                        }
                }
                evt.preventDefault();
                break;
                
        }
        draw();
    }

    void createRoadDone() {
        if(presenter != null && newRoadControlPoints.size() > 1) {
            presenter.createRoad(newRoadControlPoints);
        }
    }

    void updateNewRoadCurve() {
        pendingRoadCurve = new BezierCurve(newRoadControlPoints).getCurvePoints(5*newRoadControlPoints.size());
    }

    @Override
    public void enterMeasuringMode() {
        marker1 = new Coordinate(screenView.getScreenWidth()*0.25+MARGIN, screenView.getScreenHeight()*0.25+MARGIN);
        marker2 = new Coordinate(screenView.getScreenWidth()*0.75+MARGIN, screenView.getScreenHeight()*0.75+MARGIN);

        markerPanel1.removeStyleName(res.style().hidden());
        markerPanel1.getElement().getStyle().setTop(getScreenHeight()*0.25-2*MARGIN, Unit.PX);
        markerPanel1.getElement().getStyle().setLeft(getScreenWidth()*0.5+2*MARGIN, Unit.PX);
        
        markerPanel2.removeStyleName(res.style().hidden());
        markerPanel2.getElement().getStyle().setTop(getScreenHeight()*0.75-2*MARGIN, Unit.PX);
        markerPanel2.getElement().getStyle().setLeft(10+2*MARGIN, Unit.PX);
        
        mode = Mode.MEASURE_WORLD;
        draw();
    }
    
    @Override
    public void leaveMeasuringMode() {
        markerPanel1.addStyleName(res.style().hidden());
        markerPanel2.addStyleName(res.style().hidden());
        mode = Mode.IDLE;
        draw();
    }

    @Override
    public void enterCreateRoadMode() {
        newRoadControlPoints = new ArrayList<Coordinate>();
        // Add dynamically changing mouse coordinate
        newRoadControlPoints.add(mouseWorldCoord);
        mode = Mode.CREATE_ROAD_START;
    }

    @Override
    public void leaveCreateRoadMode() {
        newRoadControlPoints = null;
        pendingRoadCurve = null;
        mode = Mode.IDLE;
    }

    @Override
    public void enterCreateBuildingMode() {
        newBuildingCorners = new ArrayList<Coordinate>();
        newBuildingCorners.add(mouseWorldCoord);
        mode = Mode.CREATE_BUILDING;
    }

    @Override
    public void leaveCreateBuildingMode() {
        newBuildingCorners = null;
        mode = Mode.IDLE;
    }

    @UiHandler("fileChooser")
    public void onFileChooserSelect(ChangeEvent evt) {
        processFiles(fileChooser.getFiles());
    }

    @UiHandler("dropPanel")
    public void onDrop(DropEvent evt) {
        evt.stopPropagation();
        evt.preventDefault();
        processFiles(evt.getDataTransfer().<DataTransferExt>cast().getFiles());
    }

    private void processFiles(FileList files) {
        for(File file : files) {
            final String dataURL = createObjectURL(file);
            
            setBackgroundURL(dataURL, presenter);
            break;
        }
    }

    @Override
    public void initializeCanvas(double backgroundWidth, double backgroundHeight) {
        double screenWidth = screenView.getScreenWidth();
        double screenHeight = (backgroundHeight*screenWidth/backgroundWidth);

        // update the screen height
        screenView.setScreenHeight(screenHeight);
        
        // Show the full background
        screenView.setWorldWidth(backgroundWidth);
        screenView.setWorldHeight(backgroundHeight);

        // Set up view that does simply Y inversion for the background
        backgroundView = new WorldScreenView(
                new Screen(backgroundWidth, backgroundHeight),
                new WorldView(0, 0, backgroundWidth, backgroundHeight));

        dropPanel.addStyleName(res.style().hidden());

        canvas.removeStyleName(res.style().hidden());
        canvas.setPixelSize((int) screenWidth+2*MARGIN, (int) screenHeight+2*MARGIN);
        canvas.setCoordinateSpaceHeight((int) screenHeight+2*MARGIN);
        canvas.setHeight((int) (screenHeight+2*MARGIN) + "px");

        widthBox.setText(METER_FORMAT.format(DEFAULT_METERS_PER_PIXEL*screenView.getWorldWidth()));
        widthBox.getElement().getStyle().setTop(screenHeight + 2*MARGIN + 5, Unit.PX);
        widthBox.getElement().getStyle().setLeft(0.5 * screenWidth - 40, Unit.PX);
        widthBox.removeStyleName(res.style().hidden());

        heightBox.setText(METER_FORMAT.format(DEFAULT_METERS_PER_PIXEL*screenView.getWorldHeight()));
        heightBox.getElement().getStyle().setTop(0.5 * screenHeight - 10, Unit.PX);
        heightBox.getElement().getStyle().setLeft(screenWidth + 2*MARGIN + 5, Unit.PX);
        heightBox.removeStyleName(res.style().hidden());

        draw();
    }

    public final native String createObjectURL(File file) /*-{
        $wnd.URL = $wnd.URL || $wnd.webkitURL;
        return $wnd.URL.createObjectURL(file);
    }-*/;

    @UiHandler("dropPanel")
    public void onDragOver(DragOverEvent event) {
        event.stopPropagation();
        event.preventDefault();
    }

    @UiHandler("dropPanel")
    public void onDragEnter(DragEnterEvent event) {
        event.stopPropagation();
        event.preventDefault();
    }

    @UiHandler("dropPanel")
    public void onDragLeave(DragLeaveEvent event) {
        event.stopPropagation();
        event.preventDefault();
    }

    @UiHandler("widthBox")
    public void onWidthUpdate(ValueChangeEvent<String> event) {
        if(presenter != null) {
            try {
                double widthMeters = METER_FORMAT.parse(event.getValue());
                presenter.onWidthUpdate(widthMeters);
                setError("");
            } catch (NumberFormatException e) {
                setError(e.getMessage());
            }
        }
    }

    @UiHandler("heightBox")
    public void onHeightUpdate(ValueChangeEvent<String> event) {
        if(presenter != null) {
            try {
                double heightMeters = METER_FORMAT.parse(event.getValue());
                presenter.onHeightUpdate(heightMeters);
                setError("");
            } catch (NumberFormatException e) {
                setError(e.getMessage());
            }
        }
    }

    @Override
    public void setPixelView(WorldView worldView) {
        screenView.setWorldView(worldView);
        draw();
    }

    @Override
    public WorldView getPixelView() {
        return screenView.getWorldView();
    }

    @Override
    public int getScreenHeight() {
        return (int) screenView.getScreenHeight();
    }

    @Override
    public int getScreenWidth() {
        return (int) screenView.getScreenWidth();
    }

    @Override
    public Coordinate getMarker1() {
        return screenView.toWorldCoord(marker1);
    }

    @Override
    public Coordinate getMarker2() {
        return screenView.toWorldCoord(marker2);
    }

    @Override
    public String getMarker1LatLon() {
        return marker1LatLong.getText();
    }

    @Override
    public String getMarker2LatLon() {
        return marker2LatLong.getText();
    }

    @Override
    public void setRoads(Collection<Track> roads) {
        this.roads = roads;
        draw();
    }

    @Override
    public void setBuildings(Collection<Building> buildings) {
        this.buildings = buildings;
        draw();
    }
    
    @Override
    public void setError(String err) {
        errorLabel.setText(err);
    }

    @Override
    public void showText(String networkText) {
        final TextDialog dialog = new TextDialog();
        dialog.setText(networkText);
        dialog.center();
        dialog.show();
        
        // Get text back from dialog to anticipate EOL changes
        final String normalizedText = dialog.getText();
        
        dialog.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                String userText = dialog.getText();

                if(presenter != null && !userText.equals(normalizedText)) {
                    presenter.onTextFormChange(userText);
                }
            }
        });
    }

    @Override
    public void setPixelsPerMeter(double pixelsPerMeter) {
        this.pixelsPerMeter = pixelsPerMeter;
    }

    @Override
    public void setWorldWidthMeters(double widthMeters) {
        widthBox.setValue(METER_FORMAT.format(widthMeters), false);
    }

    @Override
    public void setWorldHeightMeters(double heightMeters) {
        heightBox.setValue(METER_FORMAT.format(heightMeters), false);
    }

    
    @Override
    public String getBackgroundDataURL() {
        if(background == null) {
            return "";
        }
        CanvasElement el = Document.get().createCanvasElement();
        el.setWidth(background.getWidth());
        el.setHeight(background.getHeight());
        Context2d g = el.getContext2d();
        g.drawImage(background, 0, 0);
        return getDataURL(el, "image/png");
    }

    native String getDataURL(CanvasElement el, String mimeType) /*-{
        return el.toDataURL(mimeType);
    }-*/;

    @Override
    public void selectRoad(int id) {
        this.selectedRoad = id;
        draw();
    }

    @Override
    public void openBackgroundDialog() {
        fileChooser.click();
    }

    @Override
    public void setBackgroundURL(
            final String backgroundURL,
            final BackgroundLoadCallback callback) {
        final Image backgroundImage = new Image(backgroundURL);
        backgroundImage.addLoadHandler(new LoadHandler() {

            @Override
            public void onLoad(LoadEvent event) {
                background = backgroundImage.getElement().cast();

                // Now the background size is available
                double backgroundWidth = backgroundImage.getWidth();
                double backgroundHeight = backgroundImage.getHeight();
                
                if(callback != null) {
                    callback.onBackgroundLoad(backgroundWidth, backgroundHeight);
                }
            }

        });

        // Trigger load
        backgroundImage.addStyleName(res.style().hidden());
        RootPanel.get().add(backgroundImage);
    }

}
