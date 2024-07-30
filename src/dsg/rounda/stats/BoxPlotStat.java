/**
 * 
 */
package dsg.rounda.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dsg.rounda.logging.Event;

/**
 * @author slotm
 *
 */
public class BoxPlotStat implements Statistic {

    final List<Double> values;

    double p0;
    double p25;
    double p50;
    double p75;
    double p100;
    boolean upToDate;
    boolean insufficientData;
    
    /**
     * 
     */
    public BoxPlotStat() {
        this.values = new ArrayList<Double>();
    }
    
    void recompute() {
        if(upToDate) {
            return;
        }
        
        upToDate = true;
        
        if(values.size() < 4) {
            insufficientData = true;
            return;
        }
        
        Collections.sort(values);
        
        p0 = values.get(0);
        p25 = values.get(values.size()/4);
        p50 = values.get(values.size()/2);
        p75 = values.get(3*values.size()/4);
        p100 = values.get(values.size()-1);
        insufficientData = false;
    }

    /* (non-Javadoc)
     * @see dsg.rounda.stats.Statistic#event(dsg.rounda.logging.Event)
     */
    @Override
    public void event(Event evt) {
        Object msg = evt.getMessage();
        
        if(msg instanceof Double) {
            values.add((Double) msg);
        } else if(msg instanceof Integer) {
            values.add(((Integer) msg).doubleValue());
        }
        
        upToDate = false;
    }

    /**
     * @see dsg.rounda.stats.Statistic#doubleValue()
     */
    @Override
    public Double doubleValue() {
        return 0.;
    }

    /**
     * @see dsg.rounda.stats.Statistic#longValue()
     */
    @Override
    public Long longValue() {
        return 0L;
    }

    /**
     * @see dsg.rounda.stats.Statistic#stringValue()
     */
    @Override
    public String stringValue() {
        recompute();
        
        if(insufficientData) {
            return "insufficient data";
        }
        
        return p0 + " " + p25 + " " + p50 + " " + p75 + " " + p100;
    }

}
