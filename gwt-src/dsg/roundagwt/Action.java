/**
 * 
 */
package dsg.roundagwt;

import dsg.roundagwt.Action.Type;

/**
 * @author slotm
 *
 */
public class Action {
    
    public enum Type {
        CREATE_ROAD,
        CREATE_ROADS,
        CREATE_BUILDING,
        GLUE_ROADS, REMOVE_ROAD
    }

    final Type type;
    final Object payload;
    
    /**
     * 
     */
    public Action(Type type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * @return the payload
     */
    public Object getPayload() {
        return payload;
    }

}
