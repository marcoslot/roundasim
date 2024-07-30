/**
 * 
 */
package dsg.roundagwt.gui;

import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

import dsg.rounda.model.Building;
import dsg.rounda.model.Track;
import dsg.rounda.model.WorldView;

/**
 * @author slotm
 *
 */
public interface BuilderPaletView {

    public static final double DEFAULT_METERS_PER_PIXEL = 0.2;
    
    public enum Mode { 
        IDLE,
        CREATE_BUILDING,
        CREATE_ROAD_START,
        CREATE_ROAD_IN_PROGRESS,
        DRAG_MARKER_1, 
        DRAG_MARKER_2, 
        MEASURE_WORLD;
    }
    
    public interface Presenter extends BackgroundLoadCallback {
        void createRoad(List<Coordinate> newRoadControlPoints);
        void removeSelectedRoad();
        void addBuilding(List<Coordinate> newBuildingCorners);
        
        void onHeightUpdate(double heightMeters);
        void onWidthUpdate(double widthMeters);

        void onBackgroundLoad(double backgroundWidth, double backgroundHeight);
        void onTextFormChange(String userText);
        
        void onZoom(int deltaY, Coordinate centre);
        void onDrag(double dx, double dy);
        void cancelRoad();
        void cancelBuilding();
        void onSelectVehicle(int deltaY);
    }
    
    void setPresenter(Presenter presenter);

    void initializeCanvas(double backgroundWidth, double backgroundHeight);

    Coordinate getMarker1();
    Coordinate getMarker2();
    String getMarker1LatLon();
    String getMarker2LatLon();
    
    void setError(String err);

    int getScreenWidth();
    int getScreenHeight();

    void selectRoad(int id);
    void setRoads(Collection<Track> roads);
    void setBuildings(Collection<Building> buildings);

    void showText(String serialize);

    WorldView getPixelView();
    void setPixelView(WorldView currentWorldView);

    void setPixelsPerMeter(double zoomFactor);
    void setWorldWidthMeters(double widthMeters);
    void setWorldHeightMeters(double heightMeters);

    String getBackgroundDataURL();


    void draw();

    void openBackgroundDialog();

    void enterMeasuringMode();
    void leaveMeasuringMode();

    void enterCreateBuildingMode();
    void leaveCreateBuildingMode();

    void enterCreateRoadMode();
    void leaveCreateRoadMode();

    void setBackgroundURL(String backgroundURL, BackgroundLoadCallback callback);


}
