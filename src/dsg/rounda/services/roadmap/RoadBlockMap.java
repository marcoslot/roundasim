/**
 * 
 */
package dsg.rounda.services.roadmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author slotm
 *
 */
public class RoadBlockMap {
    
    final Map<Integer,List<RoadBlock>> laneBlocks;

    public RoadBlockMap() {
        this.laneBlocks = new HashMap<Integer,List<RoadBlock>>();
    }
    
    List<RoadBlock> getOrCreateLaneBlockList(int trackID) {
        List<RoadBlock> laneBlockList = laneBlocks.get(trackID);
        
        if(laneBlockList == null) {
            laneBlockList = new ArrayList<RoadBlock>();
            laneBlocks.put(trackID, laneBlockList);
        }
        
        return laneBlockList;
    }
    
    public void add(RoadBlock laneBlock) {
        List<RoadBlock> laneBlockList = getOrCreateLaneBlockList(laneBlock.getTrackID());
        laneBlockList.add(laneBlock);
        Collections.sort(laneBlockList);
    }
    
    public RoadBlockDetection getFirstRoadBlock(TrackRangeSequence seq) {
        double distance = 0.0;
        
        for(TrackRange1D range : seq) {
            RoadBlock block = getFirstLaneBlock(range);
            
            if(block != null) {
                distance += block.getOffset() - range.getStart();
                return new RoadBlockDetection(block, distance);
            }
            
            distance += range.getLength();
        }
        return null;
    }
    
    public RoadBlock getFirstLaneBlock(TrackRange1D range) {
        List<RoadBlock> laneBlockList = laneBlocks.get(range.getTrackID());
        
        if(laneBlockList == null) {
            return null;
        }
        
        for(RoadBlock laneBlock : laneBlockList) {
            if(range.contains(laneBlock)) {
                return laneBlock;
            }
        }
        
        return null;
    }

}
