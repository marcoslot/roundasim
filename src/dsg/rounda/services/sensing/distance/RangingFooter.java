/**
 * 
 */
package dsg.rounda.services.sensing.distance;

import dsg.rounda.model.RangingSensorsSpecification;
import dsg.rounda.model.RangingSnapshot;
import dsg.rounda.services.comm.Footer;

/**
 * @author slotm
 *
 */
public class RangingFooter implements Footer {

    final RangingSensorsSpecification specs;
    final RangingSnapshot snapshot;
    /**
     * @param specs
     * @param snapshot
     */
    public RangingFooter(RangingSensorsSpecification specs,
            RangingSnapshot snapshot) {
        this.specs = specs;
        this.snapshot = snapshot;
    }
    /**
     * @return the specs
     */
    public RangingSensorsSpecification getSpecs() {
        return specs;
    }
    /**
     * @return the snapshot
     */
    public RangingSnapshot getSnapshot() {
        return snapshot;
    }
    
}
