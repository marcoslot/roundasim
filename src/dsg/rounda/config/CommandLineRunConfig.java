/**
 * 
 */
package dsg.rounda.config;

import static dsg.rounda.serialization.text.TextSerializationManager.deserialize;
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

/**
 * Class for command-line arguments 
 */
public class CommandLineRunConfig extends RunConfig {

    @Option(name = "-h",
            aliases = "-help,-?",
            usage = "display help")
    private boolean help = false;

    @Option(name = "-p",
            aliases = "-properties",
            usage = "properties file with further configuration")
    private File propertiesFile;

    @Argument
    private List<String> arguments = new ArrayList<String>();
    
    public CommandLineRunConfig(Class<?> mainType, String[] args) {
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
                
                String key = arg.substring(9, equalsIndex);
                String value = arg.substring(equalsIndex+1);
                Parameter<?> param = ParameterManager.get(key);
                
                if(param != null) {
                    set(key, deserialize(param.getType(), value));
                } else {
                    set(key, value);
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


}
