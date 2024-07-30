/**
 * 
 */
package dsg.roundagwt;

import com.google.gwt.i18n.client.Dictionary;

import dsg.rounda.config.RunConfig;

/**
 * A RunConfig set using a JavaScript variable
 */
public class JSRunConfig extends RunConfig {
    
    public static final String DEFAULT_VARIABLE_NAME = "config"; 

    public JSRunConfig() {
        this(DEFAULT_VARIABLE_NAME);
    }
    
    public JSRunConfig(String variableName) {
        Dictionary pageConfig = Dictionary.getDictionary(variableName);
        for(String configKey : pageConfig.keySet()) {
            set(configKey, pageConfig.get(configKey));
        }
    }

}
