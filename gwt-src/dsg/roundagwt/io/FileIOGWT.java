/**
 * 
 */
package dsg.roundagwt.io;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;

import dsg.rounda.io.FileIO;

/**
 * Read a file from the web server
 */
public class FileIOGWT implements FileIO {

    final String prefix;
    
    /**
     * 
     */
    public FileIOGWT(String prefix) {
        this.prefix = prefix;
    }

    /**
     * 
     */
    public FileIOGWT() {
        this.prefix = "";
    }

    /**
     * @see dsg.rounda.io.FileIO#readFile(java.lang.String, dsg.rounda.io.FileIO.Callback)
     */
    @Override
    public void readFile(String filename, final Callback callback) {
        String url = URL.encode(prefix + filename);
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    callback.onFailure(exception);
                }

                public void onResponseReceived(Request request, Response response) {
                    if (200 == response.getStatusCode()) {
                        callback.onSuccess(response.getText().split("\r\n"));
                    } else {
                        callback.onFailure(new RequestException(response.getStatusText()));
                    }
                }
            });
        } catch (RequestException e) {
            callback.onFailure(e);
        }
    }

}
