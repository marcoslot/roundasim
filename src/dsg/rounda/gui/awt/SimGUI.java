/**
 * 
 */
package dsg.rounda.gui.awt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JPanel;

import com.vividsolutions.jts.awt.PointTransformation;
import com.vividsolutions.jts.awt.ShapeWriter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import dsg.rounda.SimRun;
import dsg.rounda.gui.CircleFeature;
import dsg.rounda.gui.LineStringFeature;
import dsg.rounda.gui.PlayerView;
import dsg.rounda.gui.PolygonFeature;
import dsg.rounda.gui.SimView;
import dsg.rounda.gui.VehicleAttachedFeature;
import dsg.rounda.gui.VehicleConsoleView;
import dsg.rounda.gui.WorldScreenView;
import dsg.rounda.model.Clock;
import dsg.rounda.model.Pose2D;
import dsg.rounda.model.Track;
import dsg.rounda.model.VehicleState;
import dsg.rounda.model.WorldState;
import dsg.rounda.model.WorldView;

/**
 * AWT implementation of the simulator GUI
 */
public class SimGUI extends JPanel implements SimView {

    /**
     * 
     */
    private static final long serialVersionUID = 4702273250025771143L;
    private static final GeometryFactory GEOM = new GeometryFactory();
    private static final Coordinate ROOT = new Coordinate(0, 0);

    private static final Stroke roadStroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Color roadColor = Color.GRAY;
    private static final Stroke laserStroke = new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Color laserColor = Color.GREEN;
    private static final Stroke commStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Color commColor = Color.BLUE;
    private static final Color defaultCarColor = Color.RED;
    private static final Color chosenCarColor = Color.WHITE;
    private static final Color selectedCarColor = Color.YELLOW;
    private static final Color backgroundColor = Color.WHITE;

    WorldState world;
    Clock clock;

    Dimension windowSize;
    BufferedImage bimg;
    Image background;

    final ShapeWriter jtsToAWT;

    long roadsVersion;
    final Collection<Shape> roadSurfaces; 
    final Collection<Shape> roadPaths; 

    Collection<VehicleState> vehicles;

    private final Object lasersFeaturesLock;
    private List<FeatureAWT> laserFeatures;

    private final Object commFeaturesLock;
    private Set<VehicleAttachedFeature> commFeatures;

    boolean showRoadSurfaces;
    boolean showRoadPaths;
    Presenter presenter;

    final WorldScreenView initialWorldView;
    final WorldScreenView worldScreenView;
    final Thread picasso;

    WorldScreenView backgroundView;
    
    Coordinate mouseDragStartPosition;

    int selectedVehicle;
    int chosenVehicle;

    public SimGUI(WorldScreenView worldView) {
        initialWorldView = new WorldScreenView(worldView);
        worldScreenView = worldView;
        
        roadSurfaces = new ArrayList<Shape>();
        roadPaths = new ArrayList<Shape>();
        vehicles = new ArrayList<VehicleState>();
        
        jtsToAWT = new ShapeWriter(pointTransformer);
        background = null;
        picasso = new Thread(painter);

        lasersFeaturesLock = new Object();
        laserFeatures = new ArrayList<FeatureAWT>();

        commFeaturesLock = new Object();
        commFeatures = Collections.newSetFromMap(new ConcurrentHashMap<VehicleAttachedFeature,Boolean>());
        
        selectedVehicle = -1;
        chosenVehicle = -1;
        roadsVersion = -1;
        showRoadSurfaces = false;
        showRoadPaths = false;
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        addMouseWheelListener(mouseHandler);
        addComponentListener(componentHandler);
    }
    
    final ComponentAdapter componentHandler = new ComponentAdapter() {
    };

    final MouseAdapter mouseHandler = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent evt) {
            if(presenter != null && selectedVehicle != -1) {
                presenter.onVehicleSelected(selectedVehicle);
            }
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            mouseDragStartPosition = new Coordinate(evt.getX(), evt.getY());
        }

        @Override
        public void mouseDragged(MouseEvent evt) {
            if(presenter != null) {
                double dx = worldScreenView.toWorldWidth(evt.getX() - mouseDragStartPosition.x);
                double dy = worldScreenView.toWorldHeight(mouseDragStartPosition.y - evt.getY());
                presenter.onDrag(dx, dy);
                mouseDragStartPosition = new Coordinate(evt.getX(), evt.getY());
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            mouseDragStartPosition = null;
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent evt) {
            if(presenter != null) {
                presenter.onZoom(
                        evt.getScrollAmount() * evt.getWheelRotation(), 
                        worldScreenView.toWorldCoord(new Coordinate(evt.getX(), evt.getY())));
            }
        }
        
    };

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    final PointTransformation pointTransformer = new PointTransformation() {
        @Override
        public void transform(Coordinate src, Point2D dest) {
            Coordinate screenCoord = worldScreenView.toScreenCoord(src);
            dest.setLocation(screenCoord.x, screenCoord.y);
        }
    };


    @Override
    public void init(SimRun run) {
        this.clock = run.getClock();
        this.world = run.getWorld();
        this.vehicles = world.getVehicles();
        this.roadsVersion = -1;
        this.roadSurfaces.clear();
        clearTemporaryFeatures();

        if(!picasso.isAlive()) {
            picasso.start();
        }
    }

    public void setBackground(Image image, WorldScreenView backgroundView) {
        this.backgroundView = backgroundView;
        this.background = image;
    }

    final Runnable painter = new Runnable() {
        @Override
        public void run() {
            while(true) {
                repaint();

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    };

    public void paint(Graphics g) {
        Graphics2D g2 = createGraphics2D();
        draw(g2);
        g2.dispose();
        g.drawImage(bimg, 0, 0, this);
    }

    private void draw(Graphics2D g) {
        drawBackground(g);
        drawRoads(g);
        drawVehicles(g);
        drawStaticFeatures(g, getLaserFeatures());
        drawVehicleAttachedFeatures(g, commFeatures);
    }

    private void drawVehicleAttachedFeatures(
            Graphics2D g,
            Set<VehicleAttachedFeature> features) {
        for(VehicleAttachedFeature feature : features) {
            g.setStroke(commStroke);
            g.setColor(commColor);
            Coordinate srcPos = world.getVehicle(feature.getSourceID()).getPosition2D();
            Coordinate dstPos = world.getVehicle(feature.getDestinationID()).getPosition2D();
            Coordinate srcScreenPos = worldScreenView.toScreenCoord(srcPos);
            Coordinate dstScreenPos = worldScreenView.toScreenCoord(dstPos);
            g.drawLine(
                    (int) srcScreenPos.x,
                    (int) srcScreenPos.y,
                    (int) dstScreenPos.x,
                    (int) dstScreenPos.y
           );
        }
    }

    private void drawStaticFeatures(Graphics2D g, List<FeatureAWT> features) {
        for(FeatureAWT feature : features) {
            g.setStroke(feature.getStroke());
            g.setColor(feature.getColor());

            if(feature.getType() == FeatureAWT.Type.DRAW) {
                g.draw(feature.getShape());
            } else {
                g.fill(feature.getShape());
            }
        }
    }

    private void drawBackground(Graphics2D g) {
        if(background != null) {
            Coordinate cutXY = backgroundView.toScreenCoord(
                    worldScreenView.toWorldCoord(ROOT)        
            );
            double cutWidth = backgroundView.toScreenWidth(worldScreenView.getWorldWidth());
            double cutHeight = backgroundView.toScreenHeight(worldScreenView.getWorldHeight());
            
            g.drawImage(
                    background, 
                    (int) 0, 
                    (int) 0, 
                    (int) worldScreenView.getScreenWidth(), 
                    (int) worldScreenView.getScreenHeight(), 
                    (int) cutXY.x,
                    (int) cutXY.y,
                    (int) (cutXY.x + cutWidth),
                    (int) (cutXY.y + cutHeight), 
                    backgroundColor, 
                    this
                    );
        }
    }

    private void drawVehicles(Graphics2D g) {
        Coordinate mousePosition = null;
        Point mousePoint = null;

        if(getMousePosition() != null) {
            mousePosition = worldScreenView.toWorldCoord(new Coordinate(
                    getMousePosition().x,
                    getMousePosition().y
                    ));
            mousePoint = GEOM.createPoint(mousePosition);
        }

        boolean mouseInVehicle = false;

        for(VehicleState vehicle : vehicles) {
            Polygon vehicleGeometry = vehicle.getVehicleGeometry();

            if(!mouseInVehicle && mousePosition != null) {
                Pose2D vehiclePose = vehicle.getBackPosition().getPose2D();
                double diagonal2 = vehicle.getLength() * vehicle.getLength() + vehicle.getWidth() * vehicle.getWidth();

                if(distance2(mousePosition, vehiclePose.getPosition()) < 0.5*diagonal2
                        && vehicleGeometry.contains(mousePoint)) {
                    selectedVehicle = vehicle.getId();
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                    mouseInVehicle = true;
                }
            }

            if(vehicle.getId() == selectedVehicle) {
                g.setColor(selectedCarColor);
            } else if(vehicle.getId() == chosenVehicle) {
                g.setColor(chosenCarColor);
            } else {
                g.setColor(defaultCarColor);
            }
            g.fill(jtsToAWT.toShape(vehicleGeometry));
        }

        if(!mouseInVehicle) {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    double distance2(Coordinate a, Coordinate b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return dx*dx + dy*dy;
    }
    private void drawRoads(Graphics2D g) {
        if(roadsVersion != world.getRoadsVersion()) {
            updateRoads();
        }

        if(showRoadSurfaces) {
            for(Shape road : roadSurfaces) {
                g.setColor(roadColor);
                g.setStroke(roadStroke);
                g.fill(road);
            }
        }
        if(showRoadPaths) {
            for(Shape road : roadPaths) {
                g.setColor(roadColor);
                g.setStroke(roadStroke);
                g.draw(road);
            }
        }
    }

    private void updateRoads() {
        // Needs to be first, another update could happen
        // after retrieving the version number, but that
        // will only result in a redundant call to updateRoads
        roadsVersion = world.getRoadsVersion();

        roadSurfaces.clear();
        roadPaths.clear();

        for(Track road : world.getRoads()) {
            Polygon area = road.getArea();
            Shape areaShape = jtsToAWT.toShape(area);
            roadSurfaces.add(areaShape);

            LineString path = road.getPath();
            Shape pathShape = jtsToAWT.toShape(path);
            roadPaths.add(pathShape);
        }
    }

    private Graphics2D createGraphics2D() {
        Dimension windowSize = getSize();
        int windowWidth = (int) windowSize.getWidth();
        int windowHeight = (int) windowSize.getHeight();
        Graphics2D graphics = null;
        if (bimg == null || bimg.getWidth() != windowWidth || bimg.getHeight() != windowHeight) {
            worldScreenView.setScreenWidth(windowWidth);
            worldScreenView.setScreenHeight(windowHeight);
            updateRoads();
            bimg = (BufferedImage) createImage(windowWidth, windowHeight);
        } 
        graphics = bimg.createGraphics();
        return graphics;
    }

    @Override
    public void setChosenVehicle(int vehicleID) {
        this.chosenVehicle = vehicleID;
    }

    @Override
    public void clearTemporaryFeatures() {
        setLaserFeatures(new ArrayList<FeatureAWT>());
        commFeatures.clear();
    }
    
    @Override
    public VehicleConsoleView getVehicleConsole() {
        return null;
    }

    @Override
    public void showTemporaryLines(List<LineSegment> beams) {
        List<FeatureAWT> beamFeatures = new ArrayList<FeatureAWT>();

        // Convert line segments to GUI features
        for(LineSegment beam : beams) {
            Shape awtBeam = jtsToAWT.toShape(beam.toGeometry(GEOM));

            FeatureAWT beamFeature = new FeatureAWT(
                    awtBeam, 
                    laserStroke, 
                    laserColor, 
                    FeatureAWT.Type.DRAW,
                    0.0);

            beamFeatures.add(beamFeature);
        }

        setLaserFeatures(beamFeatures);

    }

    @Override
    public void showCommunication(VehicleAttachedFeature feature) {
        commFeatures.add(feature);
    }
    
    @Override
    public void hideCommunication(VehicleAttachedFeature feature) {
        commFeatures.remove(feature);
    }

    void setLaserFeatures(List<FeatureAWT> features) {
        synchronized(lasersFeaturesLock) {
            laserFeatures = features;
        }
    }

    List<FeatureAWT> getLaserFeatures() {
        synchronized(lasersFeaturesLock) {
            return laserFeatures;
        }
    }

    @Override
    public void setShowLasers(boolean showLasers) {
        
    }

    @Override
    public void setShowReceives(boolean showReceives) {
        
    }

    @Override
    public void setShowSends(boolean showSends) {
        
    }

    @Override
    public WorldView getWorldView() {
        return worldScreenView.getWorldView();
    }

    @Override
    public void setWorldView(WorldView worldView) {
        worldScreenView.setWorldView(worldView);
    }

    @Override
    public void showTemporaryPolygons(List<PolygonFeature> areas) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setShowMeasuredAreas1D(boolean showMeasuredAreas1D) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setShowMeasuredAreas2D(boolean checked) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showTemporaryCircles(List<CircleFeature> features) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public PlayerView getPlayerView() {
        return null;
    }

    @Override
    public void setShowQueryAreas(boolean checked) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void showTemporaryLineStrings(String key,
            List<LineStringFeature> features) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setShowMeasuredRoute1D(boolean checked) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setShowMembership(boolean checked) {
        // TODO Auto-generated method stub
        
    }

}
