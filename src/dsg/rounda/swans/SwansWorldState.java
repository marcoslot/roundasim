/**
 * 
 */
package dsg.rounda.swans;

import java.util.HashMap;
import java.util.Map;

import jist.swans.field.Field.RadioData;
import jist.swans.field.Spatial;
import jist.swans.misc.Location;
import jist.swans.misc.Location.Location2D;
import jist.swans.misc.Message;
import jist.swans.radio.RadioInfo;
import dsg.rounda.model.Pose2D;
import dsg.rounda.model.VehicleState;
import dsg.rounda.model.WorldState;

/**
 * @author slotm
 *
 */
public class SwansWorldState extends Spatial {

    final WorldState world;
    final Map<Integer,RadioData> radios;
    
    /**
     */
    public SwansWorldState(WorldState world) {
    	// Fix these sizes
        super(new Location2D(-140, -105), new Location2D(140, 105));
        this.world = world;
        this.radios = new HashMap<Integer,RadioData>();
    }

    /**
     * @see jist.swans.field.Spatial#visitTransmit(jist.swans.field.Spatial.SpatialTransmitVisitor, jist.swans.radio.RadioInfo, jist.swans.misc.Location, jist.swans.misc.Message, java.lang.Long, double)
     */
    @Override
    public int visitTransmit(
            SpatialTransmitVisitor visitor, 
            RadioInfo srcInfo,
            Location dummySrcLoc, 
            Message msg, 
            Long durationObj, 
            double limit) {
        
        int numVisited = 0;
        
        int sourceID = srcInfo.getUnique().getID();
        VehicleState source = world.getVehicle(sourceID);
        
        if(source == null) {
            return 0;
        }
        
        Pose2D srcPose = source.getBackPosition().getPose2D();
        
        for(VehicleState vehicle : world.getVehicles()) {
            RadioData radio = radios.get(vehicle.getId());
            
            if(radio == null) {
                continue;
            }
            
            Location srcLoc = new Location2D((float) srcPose.getX(), (float) srcPose.getY());
            Pose2D dstPose = vehicle.getBackPosition().getPose2D();
            Location dstLoc = new Location2D((float) dstPose.getX(), (float) dstPose.getY());
            visitor.visitTransmit(srcInfo, srcLoc, radio.info, radio.entity, dstLoc, msg, durationObj);
            numVisited += 1;
        }
        
        return numVisited;
    }

    /**
     * @see jist.swans.field.Spatial#visit(jist.swans.field.Spatial.SpatialVisitor)
     */
    @Override
    public int visit(SpatialVisitor visitor) {
        int numVisited = 0;
        
        for(RadioData radio : radios.values()) {
            visitor.visit(radio);
            numVisited += 1;
        }
        return numVisited;
    }

    /**
     * @see jist.swans.field.Spatial#add(jist.swans.field.Field.RadioData)
     */
    @Override
    public void add(RadioData radioData) {
        radios.put(radioData.info.getUnique().getID(), radioData);
    }

    /**
     * @see jist.swans.field.Spatial#del(jist.swans.field.Field.RadioData)
     */
    @Override
    public void del(RadioData radioData) {
        radios.remove(radioData.info.getUnique().getID());
    }

    /**
     * @see jist.swans.field.Spatial#move(jist.swans.field.Field.RadioData, jist.swans.misc.Location)
     */
    @Override
    public RadioData move(RadioData radioData, Location newLoc) {
        // We don't need to move, we know where everyone is
        return radioData;
    }


}
