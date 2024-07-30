/**
 * 
 */
package dsg.rounda.config;

import java.util.Collection;

import dsg.rounda.serialization.text.StringSet;

/**
 * @author slotm
 *
 */
public class StringSetParameter extends
        AbstractSimulationParameter<StringSet> {

    public StringSetParameter(String key, StringSet defaultValue) {
        super(key, defaultValue);
    }

    public StringSetParameter(String key) {
        super(key, new StringSet());
    }

    @Override
    public Range<?> getDefaultRange() {
        return null;
    }

    @Override
    public Class<?> getType() {
        return StringSet.class;
    }

}
