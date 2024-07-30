/**
 * 
 */
package dsg.rounda.scenarios;

import static dsg.rounda.serialization.text.TextSerializationManager.serialize;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

import dsg.rounda.model.Building;
import dsg.rounda.model.Track;
import dsg.rounda.model.TrackNetwork;
import dsg.rounda.model.WorldView;

/**
 * A text-based scenario
 */
public class TextWorld {

    List<Building> buildings;
    TrackNetwork network;
    
    WorldView worldView;
    String backgroundURL;
    
    public TextWorld() {
        this.buildings = new ArrayList<Building>();
    }
    
    public void init() {
        buildings.clear();
        network = new TrackNetwork();
        worldView = null;
        backgroundURL = null;
    }
    
    public void parseFrom(String scenarioText) throws ParseException {
        parseFrom(scenarioText.split("\n"));
    }

    public void parseFrom(String[] scenarioLines) throws ParseException {
        init();
        
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
                buildings.add(new Building(buildingPolygon));
            }
        }

        return i;
    }

    private int readWorldView(String[] scenarioLines, int i) {
        String[] parts = scenarioLines[i].split(" ");
        double width = Double.parseDouble(parts[0]);
        double height = Double.parseDouble(parts[1]);
        worldView = new WorldView(0., 0., width, height);
        return i+1;
    }

    public String asText() {
        WKTWriter geoToText = new WKTWriter();
        StringBuilder sb = new StringBuilder();
        
        sb.append("WORLD\r\n");
        sb.append(worldView.getWidth());
        sb.append(" ");
        sb.append(worldView.getHeight());
        sb.append("\r\n");
        sb.append("\r\n");
        
        sb.append("BUILDINGS\r\n");

        for(Building building : buildings) {
            sb.append(geoToText.write(building.getPolygon()));
            sb.append("\r\n");
        }

        sb.append("\r\n");
        
        sb.append("ROADS\r\n");
        sb.append(serialize(network));
        sb.append("\r\n");
        
        sb.append("BACKGROUND\r\n");
        sb.append(backgroundURL);
        
        return  sb.toString();
    }

    public List<Building> getBuildings() {
        return buildings;
    }
    
    public List<Track> getTracks() {
        return network.getRoads();
    }

    public String getBackgroundURL() {
        return backgroundURL;
    }
    
    public WorldView getWorldView() {
        return worldView;
    }

}
