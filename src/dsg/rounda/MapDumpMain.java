/**
 * 
 */
package dsg.rounda;

import dsg.rounda.config.CommandLineRunConfig;
import dsg.rounda.io.FileIOJRE;
import dsg.rounda.model.TrackNetwork;
import dsg.rounda.serialization.text.TextSerializationManager;

/**
 * Prints the map created by the scenario to stdout
 */
public class MapDumpMain {

    public static void main(String[] args) {
        CommandLineRunConfig config = new CommandLineRunConfig(MapDumpMain.class, args);
        
        final SimController controller = new SimController(
                config,
                new FileIOJRE("war")
        );
        controller.init(run(controller));
        
    }
    
    static final Runnable run(final SimController controller) {
        return new Runnable() {
            public void run() {
                try {
                    TrackNetwork roads = controller.getRun().getWorld().getRoadNetwork();
                    String serialized = TextSerializationManager.serialize(roads);
                    roads = TextSerializationManager.deserialize(TrackNetwork.class, serialized);
                    
                    System.out.print(TextSerializationManager.serialize(roads));
                } catch (Exception e) {
                    System.err.println(e);
                }
            }
        };
    }
}
