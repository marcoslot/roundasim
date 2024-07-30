/**
 * 
 */
package dsg.rounda.io;

/**
 * @author slotm
 *
 */
public interface FileIO {

    public interface Callback {
        void onFailure(Throwable e);
        void onSuccess(String[] contents);
    }
    
    void readFile(String filename, Callback callback);
}
