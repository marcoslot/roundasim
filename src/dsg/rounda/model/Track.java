/**
 * 
 */
package dsg.rounda.model;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.math.Vector2D;
import com.vividsolutions.jts.operation.buffer.BufferParameters;
import com.vividsolutions.jts.operation.buffer.OffsetCurveBuilder;

import dsg.rounda.geometry.BezierCurve;
import dsg.rounda.geometry.Directionality;

/**
 * A track that vehicles can follow. 
 */
public class Track {
    
    private static final double LANE_WIDTH = 5.0;//m
    private static final int NUM_OFFSET_BUCKETS = 100;
    private static final GeometryFactory GEOM = new GeometryFactory();

    final int trackID;
    TrackType type;
    Connector from;
    Connector to;
    LineString path;
    Polygon area;
    BezierCurve curve;
    double laneWidth;
    
    // Cached path info
    double pathLength;
    double[] segmentLengths;
    double[] segmentLengthSums;
    int[] offsetBuckets;
    
    LineSegment[] pathSegments;
    
    Envelope boundingBox;
    Integer leftLane;
    Integer rightLane;
    
    Double laneChangeDistance;
	LaneChangeDirection laneChangeDirection;

    public Track(int id, TrackType type, LineString path) {
        this(
                id, 
                type, 
                path, 
                null, 
                null);
    }

    public Track(int id, TrackType type, BezierCurve curve) {
        this(
                id, 
                type, 
                curve, 
                null, 
                null);
    }
    
    /**
     * Create a lane change track
     * 
     * @param id
     * @param curve
     * @param direction
     * @param laneChangeDistance
     */
    public Track(int id, BezierCurve curve, LaneChangeDirection direction, Double laneChangeDistance) {
        this(
                id, 
                TrackType.LANE_CHANGE, 
                curve, 
                null, 
                null);
        
        this.laneChangeDirection = direction;
        this.laneChangeDistance = laneChangeDistance;
    }
    
    public Track(int id, TrackType type, BezierCurve curve, Connector from, Connector to) {
        this(
                id, 
                type,
                curve.getCurvePoints(curve.getNumControlPoints()*5),
                from,
                to);
        this.curve = curve;
    }

    public Track(int id, TrackType type, LineString path, Connector from, Connector to) {
        this.trackID = id;
        this.type = type;
        this.from = from;
        this.to = to;
        this.laneWidth = LANE_WIDTH;
        setPath(path);
    }

    public int getLineSegmentIndex(double offset) {
        int bucketIndex = (int) Math.min(NUM_OFFSET_BUCKETS * offset / pathLength, offsetBuckets.length-1);
        
        // Start searching at this line segment
        int segmentIndex = offsetBuckets[bucketIndex];
        
        // Subtract the length of the path up to the start of the first line segment in the bucket
        double remainingOffset = offset - getPathLengthBeforeSegment(segmentIndex);
        
        // Subtract segment lengths from remaining offset until we've reached the segment on which offset sits
        for(; segmentIndex < pathSegments.length && remainingOffset > segmentLengths[segmentIndex]; segmentIndex++) {
            remainingOffset -= segmentLengths[segmentIndex];
        }
        
        return Math.min(segmentIndex, pathSegments.length-1);
    }

    public LineSegment getLineSegmentByOffset(double offset) {
        return getLineSegmentByIndex(getLineSegmentIndex(offset));
    }
    
    public LineSegment getLineSegmentByIndex(int index) {
        return pathSegments[index];
    }
    
    public int getNumLineSegments() {
        return pathSegments.length;
    }

    public double getSegmentLength(int segmentIndex) {
        return segmentLengths[segmentIndex];
    }

    public double getPathLengthBeforeSegment(int segmentIndex) {
        return segmentIndex > 0 ? segmentLengthSums[segmentIndex-1] : 0.0;
    }

    public Pose2D getPose2D(double offset) {
        int segmentIndex = getLineSegmentIndex(offset);
        LineSegment currentSegment = pathSegments[segmentIndex];
        
        double pathLengthBeforeSegment = getPathLengthBeforeSegment(segmentIndex);
        double remainingOffset = offset - pathLengthBeforeSegment;
        Coordinate point = pathSegments[segmentIndex].pointAlong(remainingOffset / segmentLengths[segmentIndex]);
        
        double currentSegmentDX = currentSegment.p1.x-currentSegment.p0.x;
        double currentSegmentDY = currentSegment.p1.y-currentSegment.p0.y;
        Vector2D orientation = new Vector2D(currentSegmentDX, currentSegmentDY).normalize();
        
        return new Pose2D(point, orientation);
    }

    public void setType(TrackType type) {
        this.type = type;
    }

    /**
     * @return the area
     */
    public Polygon getArea() {
        return area;
    }

    /**
     * @return the pathLength
     */
    public double getPathLength() {
        return pathLength;
    }

    /**
     * @return the from
     */
    public Connector getFrom() {
        return from;
    }

    /**
     * @return the to
     */
    public Connector getTo() {
        return to;
    }

    /**
     * @return the path
     */
    public LineString getPath() {
        return path;
    }

    /**
     * @param from the from to set
     */
    public void setFrom(Connector from) {
        this.from = from;
    }

    /**
     * @param to the to to set
     */
    public void setTo(Connector to) {
        this.to = to;
    }

    /**
     * 
     * @return
     */
    public int getId() {
        return this.trackID;
    }
    
    /**
     * Returns the type of the track
     * @return the type of the track
     */
    public TrackType getType() {
        return type;
    }

    public Directionality getDirection() {
        return  Directionality.FORWARD;
    }

    public double getMaxSpeed() {
        return 40;
    }

    /**
     * Returns whether the path of this road is a bezier curve
     * @return whether the path of this road is a bezier curve
     */
    public boolean hasBezierCurve() {
        return curve != null;
    }

    /**
     * Returns the bezier curve of this road or null if it has no curve
     * @return the bezier curve of this road or null if it has no curve
     */
    public BezierCurve getBezierCurve() {
        return curve;
    }

    public double findOffset(Coordinate coord) {
        int closestIndex = findClosestLineSegmentIndex(coord);
        LineSegment closestSegment = pathSegments[closestIndex];
        double projectionFactor = closestSegment.projectionFactor(coord);
        
        if(projectionFactor < 0) {
            return 0.0;
        } else if(projectionFactor > 1.0) {
            return getPathLengthBeforeSegment(closestIndex+1);
        } else {
            return getPathLengthBeforeSegment(closestIndex) + projectionFactor * closestSegment.getLength();
        }
    }
    
    public int findClosestLineSegmentIndex(Coordinate coord) {
        int closestIndex = 0;
        double minDistance = pathSegments[0].distance(coord);
        
        for(int i = 1; i < pathSegments.length; i++) {
            double distanceToCoord = pathSegments[i].distance(coord);
            
            if(distanceToCoord < minDistance) {
                closestIndex = i;
                minDistance = distanceToCoord;
            }
        }
        
        return closestIndex;
    }

    public void setPath(LineString newPath) {
        this.path = newPath;
        this.pathLength = path.getLength();
        this.pathSegments = new LineSegment[path.getNumPoints()-1];
        this.segmentLengths = new double[path.getNumPoints()-1];
        this.segmentLengthSums = new double[path.getNumPoints()-1];
        this.offsetBuckets = new int[NUM_OFFSET_BUCKETS];
        
        double bucketSize = this.pathLength / NUM_OFFSET_BUCKETS;
        double segmentLengthSum = 0.0;

        // Set first and last bucket
        this.offsetBuckets[0] = 0;
        
        int currentBucket = 0;
        double nextBucketStart = bucketSize;
        
        for(int i = 0; i < pathSegments.length; i++) {
            pathSegments[i] = new LineSegment(
                    path.getCoordinateN(i),
                    path.getCoordinateN(i+1)
            );
            
            double segmentLength = pathSegments[i].getLength();
            segmentLengthSum += segmentLength;
            segmentLengths[i] = segmentLength;
            segmentLengthSums[i] = segmentLengthSum;
            
            while(segmentLengthSum > nextBucketStart && currentBucket < offsetBuckets.length-1) {
                currentBucket += 1;
                offsetBuckets[currentBucket] = i;
                nextBucketStart += bucketSize;
            }
        }
        
        this.area = (Polygon) path.buffer(LANE_WIDTH*0.5);
    }

    public void setCurve(BezierCurve newCurve) {
        setPath(newCurve.getCurvePoints(newCurve.getNumControlPoints()*5));
        this.curve = newCurve;
    }

    /**
     * @return the laneWidth
     */
    public double getLaneWidth() {
        return laneWidth;
    }

    public Envelope getBoundingBox() {
        if(boundingBox != null) {
            return boundingBox;
        }
        
        return boundingBox = area.getEnvelopeInternal();
    }

    public boolean isNear(Polygon shape) {
        return area.getEnvelopeInternal().intersects(shape.getEnvelopeInternal());
    }

    public LineSegment[] getPathSegments() {
        return pathSegments;
    }

    /**
     * @return the leftLane
     */
    public Integer getLeftLane() {
        return leftLane;
    }

    /**
     * @param leftLane the leftLane to set
     */
    public void setLeftLane(Integer leftLane) {
        this.leftLane = leftLane;
    }

    /**
     * @return the rightLane
     */
    public Integer getRightLane() {
        return rightLane;
    }

    /**
     * @param rightLane the rightLane to set
     */
    public void setRightLane(Integer rightLane) {
        this.rightLane = rightLane;
    }

	public Double getLaneChangeDistance() {
		return laneChangeDistance;
	}

	public boolean isLaneChange() {
		return type == TrackType.LANE_CHANGE;
	}

	public LaneChangeDirection getLaneChangeDirection() {
		return laneChangeDirection;
	}


}
