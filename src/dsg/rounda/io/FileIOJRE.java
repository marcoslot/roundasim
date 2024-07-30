/**
 * 
 */
package dsg.rounda.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Read a file from disk
 */
public class FileIOJRE implements FileIO {

    final File baseDir;
    
    /**
     * 
     */
    public FileIOJRE(File baseDir) {
        this.baseDir = baseDir;
    }

    public FileIOJRE() {
        this(new File("."));
    }

    public FileIOJRE(String baseDirName) {
        this(new File(baseDirName));
    }

    /**
     * @see dsg.rounda.io.FileIO#readFile(java.lang.String, dsg.rounda.io.FileIO.Callback)
     */
    @Override
    public void readFile(String filename, Callback callback) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(new File(baseDir, filename)));
            List<String> lines = new ArrayList<String>();
            String line;
            
            while((line = in.readLine()) != null) {
                lines.add(line);
            }
            
            in.close();
            
            callback.onSuccess(lines.toArray(new String[lines.size()]));
        } catch (IOException e) {
            callback.onFailure(e);
        }
    }

}
