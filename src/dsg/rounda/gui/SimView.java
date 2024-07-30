/**
 * 
 */
package dsg.rounda.gui;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Polygon;

import dsg.rounda.SimRun;
import dsg.rounda.model.WorldView;

/**
 * Interface of a GUI controlled by SimPresenter
 */
public interface SimView {
    
    interface Presenter {
        void onVehicleSelected(int vehicleID);

        void onZoom(int deltaY, Coordinate mousePosition);
        void onDrag(double dragX, double dragY);

        void onShowLasersClicked(boolean checked);
        void onShowSendClicked(boolean checked);
        void onShowReceiveClicked(boolean checked);
        void onShowMeasuredAreas2DClicked(boolean value);
        void onShowMeasuredAreas1DClicked(boolean value);
        void onShowMeasuredRoute1DClicked(boolean value);
        void onShowQueryAreasClicked(boolean value);
        void onShowMembershipClicked(boolean value);

        void onEmptyWeakConflicts(boolean value);


    }

    VehicleConsoleView getVehicleConsole();
    void setPresenter(Presenter presenter);
    void init(SimRun run);
    
    void setChosenVehicle(int vehicleID);
    
    void clearTemporaryFeatures();
    
    void showCommunication(VehicleAttachedFeature feature);
    void hideCommunication(VehicleAttachedFeature feature);
    
    
    void setWorldView(WorldView worldView);
    WorldView getWorldView();
    
    void showTemporaryLines(List<LineSegment> beams);
    void showTemporaryLineStrings(String key, List<LineStringFeature> features);
    void showTemporaryPolygons(List<PolygonFeature> features);
    void showTemporaryCircles(List<CircleFeature> features);
    PlayerView getPlayerView();
    
    void setShowLasers(boolean showLasers);
    void setShowReceives(boolean showReceives);
    void setShowSends(boolean showSends);
    void setShowMeasuredAreas2D(boolean checked);
    void setShowMeasuredAreas1D(boolean checked);
    void setShowQueryAreas(boolean checked);
    void setShowMeasuredRoute1D(boolean checked);
    void setShowMembership(boolean checked);
}
