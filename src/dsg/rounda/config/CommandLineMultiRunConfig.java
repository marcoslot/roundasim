/**
 * 
 */
package dsg.rounda.config;

import static org.kohsuke.args4j.ExampleMode.ALL;

import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import static dsg.rounda.serialization.text.TextSerializationManager.deserialize;

/**
 * Class for command-line arguments 
 */
public class CommandLineMultiRunConfig extends MultiRunConfig implements SimulationParameters {

    /**
     * 
     */
    private static final long serialVersionUID = 1489785578552139044L;

    @Option(name = "-h",
            aliases = "-help,-?",
            usage = "display help")
    private boolean help = false;

    @Option(name = "-m",
            aliases = "-multi",
            usage = "log multi run statistics")
    boolean printMultiRunStats = false;

    @Option(name = "-p",
            aliases = "-properties",
            usage = "properties file with further configuration")
    private File propertiesFile;

    @Argument
    private List<String> arguments = new ArrayList<String>();
    
    public CommandLineMultiRunConfig(Class<?> mainType, String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        
        try {
            parser.parseArgument(args);
            
            if(help) {
                printHelp(parser, mainType, System.out);
                System.exit(0);
            }
            
            if(propertiesFile != null) {
                Properties properties = new Properties();
                properties.load(new FileReader(propertiesFile));
                setProperties(properties);
            }
            
            for(String arg : arguments) {
                int equalsIndex = arg.indexOf('=');
                
                if(equalsIndex < 0) {
                    continue;
                }
                
                String key = arg.substring(0, equalsIndex);
                String value = arg.substring(equalsIndex+1);
                Parameter<?> param = ParameterManager.get(key);
                
                if(param != null) {
                    if(param.getRangeType() == null) {
                        setSingle(key, deserialize(param.getType(), value));
                    } else {
                        set(key, (Range<?>) deserialize(param.getRangeType(), value));
                    }
                } else {
                    set(key, new StringRange(value));
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            printHelp(parser, mainType, System.err);
            System.exit(1);
        }
    }
    
    private void printHelp(CmdLineParser parser, Class<?> mainType, PrintStream out) {
        out.println("java " + mainType.getName() + " [options...] arguments...");
        parser.printUsage(out);
        out.println();
        out.println("  Example: java " + mainType.getName() + parser.printExample(ALL));
    }

	public boolean isPrintMultiRunStats() {
		return printMultiRunStats;
	}

}
