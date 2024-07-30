/**
 * 
 */
package dsg.rounda.model;

import dsg.rounda.services.roadmap.TrackPoint1D;

/**
 * A snapshot of sensor data at a particular time.
 */
public class SensorSnapshotAndConfig {

    final long time;
    final TrackPoint1D position;
    final RangingSnapshot ranges;
    final RangingSensorsSpecification rangingSpecs;
    /**
     * @param position
     * @param velocity
     * @param ranges
     * @param rangingSpecs
     */
    public SensorSnapshotAndConfig(
            TrackPoint1D position, 
            RangingSnapshot ranges, 
            RangingSensorsSpecification rangingSpecs) {
        this.time = ranges.getTime();
        this.position = position;
        this.ranges = ranges;
        this.rangingSpecs = rangingSpecs;
    }
    /**
     * @return the position
     */
    public TrackPoint1D getPosition() {
        return position;
    }
    /**
     * @return the ranges
     */
    public RangingSnapshot getRanges() {
        return ranges;
    }
    /**
     * @return the rangingSpecs
     */
    public RangingSensorsSpecification getRangingSpecs() {
        return rangingSpecs;
    }
}
