/**
 * 
 */
package dsg.rounda.services.comm.beaconing;

import dsg.rounda.services.comm.Footer;

/**
 * @author slotm
 *
 */
public class BeaconFooter implements Footer {

    double interval;
    
    /**
     * 
     */
    public BeaconFooter() {
    }

    /**
     * @param interval
     */
    public BeaconFooter(double interval) {
        super();
        this.interval = interval;
    }

    /**
     * @return the interval
     */
    public double getInterval() {
        return interval;
    }

    /**
     * @param interval the interval to set
     */
    public void setInterval(double interval) {
        this.interval = interval;
    }
    
}
