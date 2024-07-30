/**
 * 
 */
package dsg.rounda.services.vertigo;

import java.util.ArrayList;
import java.util.Collection;

import dsg.rounda.services.comm.Footer;

/**
 * Footer containing a set of acknowledgements to vertigo queries
 */
public class AckFooter implements Footer {

    Collection<Long> acknowledgements;
    
    /**
     * 
     */
    public AckFooter() {
        acknowledgements = new ArrayList<Long>();
    }

    /**
     * @param acknowledgements
     */
    public AckFooter(Collection<Long> acknowledgements) {
        this.acknowledgements = acknowledgements;
    }

    /**
     * @return the acknowledgements
     */
    public Collection<Long> getAcknowledgements() {
        return acknowledgements;
    }

    /**
     * @param acknowledgements the acknowledgements to set
     */
    public void setAcknowledgements(Collection<Long> acknowledgements) {
        this.acknowledgements = acknowledgements;
    }

}
