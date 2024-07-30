/**
 * 
 */
package com.trosc.gwt;

import dsg.rounda.io.FileIO;

/**
 * @author slotm
 *
 */
public class FileIOTrosc implements FileIO {

    public FileIOTrosc() {
    }

    /* (non-Javadoc)
     * @see dsg.rounda.io.FileIO#readFile(java.lang.String, dsg.rounda.io.FileIO.Callback)
     */
    @Override
    public void readFile(
            final String filename, 
            final Callback callback) {
        nativeRead(getTroscResource(filename), new ResultHandler() {
            @Override
            public void onResult(String b64) {
                byte[] bytes = Base64.decode(b64);
                StringBuilder sb = new StringBuilder();
                
                for(int i = 0; i < bytes.length; i++) {
                    sb.append((char) bytes[i]);
                }
                
                callback.onSuccess(sb.toString().split("\n"));
            }
        });
    }
    
    interface ResultHandler {
        void onResult(String res);
    }
    
    native void nativeRead(String query, ResultHandler handler) /*-{
        var msg = {
            destination  : "http",
            type         : "http_request",
            payload      : {
                method   : "GET",
                protocol : "http",
                host     : "d3hijy4kpwrxwe.cloudfront.net",
                query    : query
            }
        };
        
        sys.send(msg, function(resp) {
            var data = resp.payload.data;
            handler.@com.trosc.gwt.FileIOTrosc.ResultHandler::onResult(Ljava/lang/String;)(data);
        });
    }-*/;
    
    native String getTroscResource(String filename) /*-{
        return trosc_resource(filename);         
    }-*/;
}
