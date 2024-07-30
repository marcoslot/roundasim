/**
 * 
 */
package dsg.rounda;

import dsg.rounda.config.EnumParameter;

/**
 * @author slotm
 *
 */
public enum TimeMode {
    /* Always take steps of the same size, but speed of simulation depends on speed of computation */
    SMOOTH_TIME,
    
    /* Speed of simulation always matches real time, but may take bigger steps when computation is slow.
     * Could fall behind */
    REAL_TIME;
    

    public static final EnumParameter<TimeMode> PARAM = new EnumParameter<TimeMode>(
            TimeMode.class, 
            "time-mode", 
            TimeMode.REAL_TIME);
    
}
