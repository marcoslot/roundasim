/**
 * 
 */
package dsg.rounda.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.math.Vector2D;

import dsg.rounda.Constants;
import dsg.rounda.SimController;
import dsg.rounda.SimRun;
import dsg.rounda.logging.Event;
import dsg.rounda.logging.EventHandler;
import dsg.rounda.logging.EventLog;
import dsg.rounda.model.Clock;
import dsg.rounda.model.Job;
import dsg.rounda.model.LidarSnapshot;
import dsg.rounda.model.LidarSpecification;
import dsg.rounda.model.Message;
import dsg.rounda.model.Pose2D;
import dsg.rounda.model.RangingSensorsSpecification;
import dsg.rounda.model.RangingSnapshot;
import dsg.rounda.model.Scheduler;
import dsg.rounda.model.SensorPose;
import dsg.rounda.model.SensorSnapshotAndConfig;
import dsg.rounda.model.Track;
import dsg.rounda.model.WorldState;
import dsg.rounda.model.WorldView;
import dsg.rounda.services.coordination.ConflictArea;
import dsg.rounda.services.coordination.ConflictTrajectory;
import dsg.rounda.services.coordination.ConflictTrajectoryPart;
import dsg.rounda.services.roadmap.TrackArea1D;
import dsg.rounda.services.roadmap.TrackBoundary1D;
import dsg.rounda.services.roadmap.TrackMapArea1D;
import dsg.rounda.services.roadmap.TrackMapBoundaries1D;
import dsg.rounda.services.roadmap.TrackRange1D;
import dsg.rounda.services.roadmap.TrackRangeSequence;
import dsg.rounda.stats.SimStats;
import dsg.rounda.stats.VehicleStats;


/**
 * Presents the simulator to the user using a GUI
 */
public class SimPresenter implements SimView.Presenter, VehicleConsoleView.Presenter, Constants {

    private static final GeometryFactory GEOM = new GeometryFactory();
    private static final int INITIAL_CHOSEN_VEHICLE = 0;
    private static final int MAX_ZOOM_FACTOR = 10;

    final SimView view;
    final SimController controller;
    
    final VehicleConsoleView vehicleConsole;

    WorldState world;
    Scheduler scheduler;
    Clock clock;
    SimStats stats;
    EventLog eventLog;
    
    final WorldView initialWorldView;

    int chosenVehicle;
    VehicleStats chosenVehicleStats;
    
    boolean showLasers;
    boolean showReceives;
    boolean showSends;
    boolean showMeasuredAreas2D;
    boolean showMeasuredAreas1D;
    boolean showMeasuredRoute1D;
    boolean showEmptyWeakConflicts;
    boolean showQueryAreas;
    boolean showMembership;

    double zoomFactor;

    /**
     * @param worldState 
     * 
     */
    public SimPresenter(
            SimView view,
            SimController controller) {
        this.zoomFactor = 1.0;
        this.controller = controller;
        this.view = view;
        this.view.setPresenter(this);
        this.initialWorldView = view.getWorldView();

        vehicleConsole = view.getVehicleConsole();
        
        reset();
    }

    public void reset() {
        SimRun run = controller.getRun();
        
        this.world = run.getWorld();
        this.scheduler = run.getScheduler();
        this.clock = run.getClock();
        this.eventLog = run.getEventLog();
        this.eventLog.addHandler(EventLog.acceptReset(), onReset);
        this.eventLog.addHandler(EventLog.acceptSourceType(Integer.class), onVehicleEvent);
        this.stats = run.getStats();
        
        view.init(run);
        view.setWorldView(initialWorldView);
        setChosenVehicle(INITIAL_CHOSEN_VEHICLE);

        if(vehicleConsole != null) {
            vehicleConsole.setPresenter(this);
            showChosenVehicleDetails();
        }
    }

    final EventHandler onReset = new EventHandler() {
        @Override
        public void event(Event event) {
            reset();
        }
    };

    final EventHandler onVehicleEvent = new EventHandler() {
        @Override
        public void event(Event event) {
            int sourceID = (Integer) event.getSource();

            if(event.getMessage() instanceof Message) {
                Message message = (Message) event.getMessage();

                if(showSends && message.getSource() == chosenVehicle && sourceID != chosenVehicle) {
                    // This is a message sent by chosen vehicle, received by another vehicle
                    final VehicleAttachedFeature commFeature = new VehicleAttachedFeature(message.getSource(), sourceID);
                    
                    view.showCommunication(commFeature);
                    
                    scheduler.schedule(new Job(new Runnable() {
                        @Override
                        public void run() {
                            view.hideCommunication(commFeature);
                        }
                    }, Job.GLOBAL_OWNER, clock.getTime() + 0.2*SECONDS));
                }
                
                if(showReceives && sourceID == chosenVehicle) {
                    // This is a message sent by chosen vehicle, received by another vehicle
                    final VehicleAttachedFeature commFeature = new VehicleAttachedFeature(message.getSource(), sourceID);
                    
                    view.showCommunication(commFeature);
                    
                    scheduler.schedule(new Job(new Runnable() {
                        @Override
                        public void run() {
                            view.hideCommunication(commFeature);
                        }
                    }, Job.GLOBAL_OWNER, clock.getTime() + (long) (0.2*SECONDS)));
                }

                if(sourceID == chosenVehicle && vehicleConsole != null) {
                    if("receive".equals(event.getTag())) {
                        vehicleConsole.setReceivedMessageCount(chosenVehicleStats.getLongValue(event.getTag(), "count"));
                    } else if("send".equals(event.getTag())) {
                        vehicleConsole.setSentMessageCount(chosenVehicleStats.getLongValue(event.getTag(), "count"));
                    } 
                }
                return;
            } 
            
            if(sourceID != chosenVehicle) {
                return;
            }
            
            if(vehicleConsole != null) {
                if("velocity".equals(event.getTag())) {
                    vehicleConsole.setVelocity(chosenVehicleStats.getDoubleValue(event.getTag(), "latest"));
                    return;
                } else if("track".equals(event.getTag())) {
                    vehicleConsole.setTrackID(chosenVehicleStats.getLongValue(event.getTag(), "latest"));
                    return;
                } else if("offset".equals(event.getTag())) {
                    vehicleConsole.setOffset(chosenVehicleStats.getDoubleValue(event.getTag(), "latest"));
                    return;
                } else if("allocated-distance".equals(event.getTag())) {
                    vehicleConsole.setAllocatedDistance(chosenVehicleStats.getDoubleValue(event.getTag(), "latest"));
                    return;
                } else if("available-distance".equals(event.getTag())) {
                    vehicleConsole.setAvailableDistance(chosenVehicleStats.getDoubleValue(event.getTag(), "latest"));
                    return;
                }
            }
            
            if("bye".equals(event.getTag())) {
                // our chosen vehicle disappeared
                chosenVehicle = -1;
                view.setChosenVehicle(-1);
                view.clearTemporaryFeatures();
            } else if(showMeasuredAreas2D && "emptiness".equals(event.getTag())) {
                RGBA color = new RGBA(255, 220, 0);

                List<PolygonFeature> features = new ArrayList<PolygonFeature>();
                
                if(event.getMessage() instanceof Polygon) {
                    Polygon polygon = (Polygon) event.getMessage();
                    features.add(new PolygonFeature(
                            polygon,
                            1.0,
                            color
                    ));
                } else {
                    MultiPolygon measuredAreas = (MultiPolygon) event.getMessage();

                    for(int i = 0, numPolygons = measuredAreas.getNumGeometries(); i < numPolygons; i++) {
                        Polygon polygon = (Polygon) measuredAreas.getGeometryN(i);
                        
                        features.add(new PolygonFeature(
                                polygon,
                                1.0,
                                color
                        ));
                    }
                }
                
                view.showTemporaryPolygons(features);

            } else if(showMeasuredAreas1D && "boundaries".equals(event.getTag())) {
                TrackMapBoundaries1D boundaries = (TrackMapBoundaries1D) event.getMessage();
                
                RGBA color = new RGBA(0,0,0);
                double radius = 5.0;

                List<CircleFeature> features = new ArrayList<CircleFeature>();
                
                for(TrackBoundary1D boundary : boundaries.getBoundaries()) {
                    Track track = world.getRoad(boundary.getTrackID());
                    Pose2D pose = track.getPose2D(boundary.getOffset());
                    
                    features.add(new CircleFeature(
                        pose.getPosition(),
                        radius,
                        color
                    ));
                }
                
                view.showTemporaryCircles(features);

            } else if(showMeasuredAreas1D && "emptiness1D".equals(event.getTag())) {
                TrackMapArea1D mapArea = (TrackMapArea1D) event.getMessage();
                
                RGBA color = new RGBA(100, 255, 0);
                double lineWidth = 2.0;

                List<LineStringFeature> lineStrings = mapAreaToLineStrings(mapArea, color, lineWidth);
                view.showTemporaryLineStrings("emptiness1D", lineStrings);
            } else if(showMeasuredRoute1D && "empty-route".equals(event.getTag())) {
                TrackRangeSequence emptyRoute = (TrackRangeSequence) event.getMessage();

                RGBA color = new RGBA(100, 255, 0);
                double lineWidth = 4.0;
                
                List<LineStringFeature> lineStrings = new ArrayList<LineStringFeature>();
                trackRangesToLineStringFeatures(emptyRoute, color, lineWidth, lineStrings);
                view.showTemporaryLineStrings("empty-route", lineStrings);
            } else if(showEmptyWeakConflicts && "weak-conflict-trajectory".equals(event.getTag())) {
                ConflictTrajectory weakConflictTrajectory = (ConflictTrajectory) event.getMessage();

                RGBA color = new RGBA(120, 60, 30);
                double lineWidth = 2.0;

                List<LineStringFeature> lineStrings = new ArrayList<LineStringFeature>();

                for(ConflictArea conflictArea : weakConflictTrajectory.getConflictAreas()) {
                    for(TrackRange1D trackRange : conflictArea.getRanges()) {
                        LineStringFeature feature = trackRangeToLineStringFeature(trackRange, color, lineWidth);
                        lineStrings.add(feature);
                    }
                }
                
                view.showTemporaryLineStrings("weak-conflict-trajectory", lineStrings);
                
            } else if(showMembership && "membership-success".equals(event.getTag())) {
                TrackMapArea1D mapArea = (TrackMapArea1D) event.getMessage();
                
                RGBA color = new RGBA(0, 150, 0);
                double lineWidth = 4.0;

                view.showTemporaryLineStrings("membership", mapAreaToLineStrings(mapArea, color, lineWidth));
            } else if(showMembership && "membership-fail".equals(event.getTag())) {
                TrackMapArea1D mapArea = (TrackMapArea1D) event.getMessage();
                
                RGBA color = new RGBA(150, 0, 0);
                double lineWidth = 4.0;

                view.showTemporaryLineStrings("membership", mapAreaToLineStrings(mapArea, color, lineWidth));
            } else if(showQueryAreas && "query-start".equals(event.getTag())) {
                TrackMapArea1D mapArea = (TrackMapArea1D) event.getMessage();

                RGBA color = new RGBA(150, 200, 255);
                double lineWidth = 7.0;
                
                view.showTemporaryLineStrings("query-area", mapAreaToLineStrings(mapArea, color, lineWidth));
            } else if("allocation-done".equals(event.getTag())) {
                view.showTemporaryLineStrings("query-area", null);
                view.showTemporaryLineStrings("membership", null);
            } else if(showLasers && "rangers".equals(event.getTag())) {

                SensorSnapshotAndConfig snapshot = (SensorSnapshotAndConfig) event.getMessage();

                RangingSnapshot ranges = snapshot.getRanges();
                RangingSensorsSpecification rangingSpecs = snapshot.getRangingSpecs();

                List<LidarSnapshot> lidarSnapshots = ranges.getLidarSnapshots();
                List<LidarSpecification> lidarSpecs = rangingSpecs.getLidarSpecs();

                Track track = world.getRoad(snapshot.getPosition().getTrackID());
                Pose2D vehiclePose = track.getPose2D(snapshot.getPosition().getOffset());
                Coordinate vehiclePosition = vehiclePose.getPosition();
                Vector2D vehicleOrientation = vehiclePose.getOrientation();

                List<LineSegment> beams = new ArrayList<LineSegment>();

                for(int lidarID = 0, numLidars = lidarSnapshots.size(); lidarID < numLidars; lidarID++) {
                    LidarSnapshot lidarSnapshot = lidarSnapshots.get(lidarID);
                    LidarSpecification lidarSpec = lidarSpecs.get(lidarID);
                    SensorPose relativeSensorPose = lidarSpec.getPose();

                    Coordinate sensorPosition = relativeSensorPose.getAbsolutePosition(vehiclePosition, vehicleOrientation);
                    Vector2D sensorStartOrientation = relativeSensorPose.getAbsoluteOrientation(vehicleOrientation);
                    double sensorStartAngle = sensorStartOrientation.angle(); 
                    double stepSize = lidarSpec.getStepSize();

                    for(int measIndex = 0, numMeas = lidarSnapshot.getNumSteps(); measIndex < numMeas; measIndex++) {
                        double angle = sensorStartAngle + stepSize * measIndex;
                        double distance = lidarSnapshot.getDistance(measIndex);

                        Coordinate beamEnd = new Coordinate(
                                sensorPosition.x + distance * Math.cos(angle),
                                sensorPosition.y + distance * Math.sin(angle)
                                );

                        LineSegment beam = new LineSegment(sensorPosition, beamEnd);

                        beams.add(beam);
                    }
                }

                view.showTemporaryLines(beams);
            }
        }
    };
    
    private List<LineStringFeature> mapAreaToLineStrings(TrackMapArea1D mapArea, RGBA color, double lineWidth) {
        List<LineStringFeature> lineStrings = new ArrayList<LineStringFeature>();
        
        for(TrackArea1D trackArea : mapArea.getAreas()) {
            trackRangesToLineStringFeatures(new ArrayList<TrackRange1D>(trackArea.getRanges()), color, lineWidth, lineStrings);
        }
        
        return lineStrings;
    }

    private void trackRangesToLineStringFeatures(
            Collection<TrackRange1D> ranges,
            RGBA color, 
            double lineWidth,
            List<LineStringFeature> lineStrings) {
        for(TrackRange1D trackRange : ranges) {
            LineStringFeature feature = trackRangeToLineStringFeature(trackRange, color, lineWidth);
            lineStrings.add(feature);
        }
    }

    private LineStringFeature trackRangeToLineStringFeature(TrackRange1D trackRange, RGBA color, double lineWidth) {
        Track track = world.getRoad(trackRange.getTrackID());
        
        List<Coordinate> coordinates = new ArrayList<Coordinate>();

        Pose2D startPose = track.getPose2D(trackRange.getStart());
        Pose2D endPose = track.getPose2D(trackRange.getEnd());

        int firstIndex = track.getLineSegmentIndex(trackRange.getStart());
        int lastIndex = track.getLineSegmentIndex(trackRange.getEnd());

        if(!startPose.getPosition().equals(track.getLineSegmentByIndex(firstIndex).p1)) { 
            coordinates.add(startPose.getPosition());
        }

        for(int i = firstIndex; i < lastIndex-1; i++) {
            coordinates.add(track.getLineSegmentByIndex(i).p1);
        }

        coordinates.add(endPose.getPosition());

        Coordinate[] coordArray = coordinates.toArray(new Coordinate[coordinates.size()]);
        LineString lineString = GEOM.createLineString(coordArray);

        return new LineStringFeature(
                lineString,
                lineWidth,
                color
                );
    }

    private void showChosenVehicleDetails() {
        if(vehicleConsole == null) {
            return;
        }
        vehicleConsole.setID(chosenVehicle);
        vehicleConsole.setReceivedMessageCount(chosenVehicleStats.getLongValue("receive", "count"));
        vehicleConsole.setSentMessageCount(chosenVehicleStats.getLongValue("send", "count"));
        vehicleConsole.setAllocatedDistance(chosenVehicleStats.getDoubleValue("allocated-distance", "latest"));
        vehicleConsole.setAvailableDistance(chosenVehicleStats.getDoubleValue("available-distance", "latest"));
        vehicleConsole.setVelocity(chosenVehicleStats.getDoubleValue("velocity", "latest"));
        vehicleConsole.setTrackID(chosenVehicleStats.getLongValue("track", "latest"));
        vehicleConsole.setOffset(chosenVehicleStats.getDoubleValue("offset", "latest"));
    }

    /**
     * @see dsg.rounda.gui.SimView.Presenter#onVehicleSelected(int)
     */
     @Override
     public void onVehicleSelected(int vehicleID) {
        setChosenVehicle(vehicleID);
        view.setChosenVehicle(vehicleID);
        view.clearTemporaryFeatures();
        showChosenVehicleDetails();
     }

     private void setChosenVehicle(int vehicleID) {
         chosenVehicle = vehicleID;

         if(vehicleID != -1) {
             chosenVehicleStats = stats.getOrCreateVehicleStats(vehicleID);
         }

         view.setChosenVehicle(chosenVehicle);
     }

     @Override
     public void onZoom(int deltaY, Coordinate centre) {
         if(deltaY < 0) {
             zoomFactor = Math.min(MAX_ZOOM_FACTOR, zoomFactor + 0.3);
         } else {
             zoomFactor = Math.max(1.0, zoomFactor - 0.3);
         }

         WorldView currentWorldView = view.getWorldView();
         WorldView newWorldView = initialWorldView.zoomed(currentWorldView, centre, zoomFactor);
         
         view.setWorldView(newWorldView);
     }

     @Override
     public void onDrag(double dx, double dy) {
         WorldView currentWorldView = view.getWorldView();

         double newWestX = currentWorldView.getWestX()-dx;
         newWestX = Math.max(newWestX, initialWorldView.getWestX());
         newWestX = Math.min(newWestX, initialWorldView.getEastX() - currentWorldView.getWidth());
         
         double newSouthY = currentWorldView.getSouthY()-dy;
         newSouthY = Math.max(newSouthY, initialWorldView.getSouthY());
         newSouthY = Math.min(newSouthY, initialWorldView.getNorthY() - currentWorldView.getHeight());
         
         currentWorldView.setWestSouth(newWestX, newSouthY);
         
         view.setWorldView(currentWorldView);
     }

    @Override
    public void onShowLasersClicked(boolean checked) {
        showLasers = checked;
        
        if(!checked) {
            view.clearTemporaryFeatures();
        }
    }

    @Override
    public void onShowSendClicked(boolean checked) {
        showSends = checked;

        if(!checked) {
            view.clearTemporaryFeatures();
        }
    }

    @Override
    public void onShowReceiveClicked(boolean checked) {
        showReceives = checked;

        if(!checked) {
            view.clearTemporaryFeatures();
        }
    }

    @Override
    public void onShowMeasuredAreas2DClicked(boolean checked) {
        showMeasuredAreas2D = checked;

        if(!checked) {
            view.clearTemporaryFeatures();
        }
    }

    @Override
    public void onShowMeasuredAreas1DClicked(boolean checked) {
        showMeasuredAreas1D = checked;

        if(!checked) {
            view.clearTemporaryFeatures();
        }
    }

    @Override
    public void onShowQueryAreasClicked(boolean checked) {
        showQueryAreas = checked;

        if(!checked) {
            view.clearTemporaryFeatures();
        }
    }

    @Override
    public void onShowMeasuredRoute1DClicked(boolean value) {
        showMeasuredRoute1D = value;

        if(!value) {
            view.clearTemporaryFeatures();
        }
    }

    @Override
    public void onShowMembershipClicked(boolean value) {
        showMembership = value;

        if(!value) {
            view.clearTemporaryFeatures();
        }
    }

    @Override
    public void onEmptyWeakConflicts(boolean value) {
        showEmptyWeakConflicts = value;
        
        if(!value) {
            view.clearTemporaryFeatures();
        }
    }

}
