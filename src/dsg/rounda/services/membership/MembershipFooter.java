/**
 * 
 */
package dsg.rounda.services.membership;

import dsg.rounda.services.comm.Footer;
import dsg.rounda.services.roadmap.TrackArea1D;

/**
 * Footer containing a membership tuple
 */
public class MembershipFooter implements Footer {

    private MembershipTuple tuple;
    
    /**
     * 
     */
    public MembershipFooter() {
    }

    /**
     * @param tuple
     */
    public MembershipFooter(MembershipTuple tuple) {
        this.tuple = tuple;
    }

    /**
     * @return the tuple
     */
    public MembershipTuple getTuple() {
        return tuple;
    }

    /**
     * @param tuple the tuple to set
     */
    public void setTuple(MembershipTuple tuple) {
        this.tuple = tuple;
    }

}
