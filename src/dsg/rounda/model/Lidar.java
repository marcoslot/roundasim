/**
 * 
 */
package dsg.rounda.model;

import java.util.Collection;

import com.vividsolutions.jts.algorithm.LineIntersector;
import com.vividsolutions.jts.algorithm.RobustLineIntersector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.math.Vector2D;

import dsg.rounda.Constants;
import dsg.rounda.geometry.BeamCalc;

/**
 * Simulates LIDAR hardware by computing line-polygon intersections
 */
public class Lidar {
    
    final VehicleState self;
    final WorldState world;
    final Clock clock;
    final LidarSpecification config;
    final BeamCalc beamCalc;
    
    /**
     * @param world the state of the world
     * @param clock the clock
     * @param vehicle the vehicle this LIDAR is mounted on
     * @param config the configuration of the LIDAR sensor
     */
    public Lidar(
            WorldState world, 
            Clock localClock, 
            VehicleState vehicle, 
            LidarSpecification config) {
        this.self = vehicle;
        this.world = world;
        this.config = config;
        this.clock = localClock;
        this.beamCalc = new BeamCalc(config);
    }
    
    /**
     * Get a single LIDAR snapshot. Sweeping time is currently not
     * considered.
     * 
     * @return a LIDAR snapshot.
     */
    public LidarSnapshot takeSnapshot() {
        // General result objects 
        final Double[] squaredDistances = new Double[config.getNumSteps()];
        final LineIntersector intersector = new RobustLineIntersector();
        
        // Vehicle properties
        final Pose2D vehiclePose = self.getBackPosition().getPose2D();
        final Coordinate vehiclePosition = vehiclePose.getPosition();
        final Vector2D vehicleOrientation = vehiclePose.getOrientation();
        
        // Sensor properties
        final SensorPose relativeSensorPose = config.getPose();
        final double angularRange = config.getAngularRange();
        final double stepAngle = config.getStepSize();
        final Coordinate sensorPosition = relativeSensorPose.getAbsolutePosition(vehiclePosition, vehicleOrientation);
        final Vector2D sensorStartOrientation = relativeSensorPose.getAbsoluteOrientation(vehicleOrientation);
        final Vector2D sensorEndOrientation = sensorStartOrientation.rotate(angularRange);
        
        double sensorStartAngle = sensorStartOrientation.angle();
        double sensorEndAngle = sensorEndOrientation.angle();
        
        if(sensorEndAngle < sensorStartAngle) {
            // Ensure that end is always greater than start to simplify math
            sensorEndAngle += 2*Math.PI;
        }

        Collection<VehicleState> vehiclesInRange = world.getVehiclesInCircle(sensorPosition, config.getRange());

        for(VehicleState observedVehicle : vehiclesInRange) {
            if(observedVehicle.id == self.id) {
                // We don't observe ourselves
                continue;
            }
            
            // Compute the geometry of the vehicle
            Polygon observedGeometry = observedVehicle.getVehicleGeometry();
            
            processObservedPolygon(
                    observedGeometry,
                    sensorPosition,
                    sensorStartOrientation,
                    sensorEndOrientation,
                    sensorStartAngle,
                    stepAngle,
                    intersector,
                    squaredDistances
            );
        }

        for(Building building : world.getBuildings()) {
            if(building.minDistance(sensorPosition) > config.getRange()) {
                continue;
            }
            
            processObservedPolygon(
                    building.getPolygon(),
                    sensorPosition,
                    sensorStartOrientation,
                    sensorEndOrientation,
                    sensorStartAngle,
                    stepAngle,
                    intersector,
                    squaredDistances
            );
        }
        
        // Finally, compute the square roots of the distances
        // we've deferred this to optimize performance
        LidarSnapshot lastLidarSnapshot = new LidarSnapshot(clock.getTime(), config.getNumSteps(), config.getRange());
        
        for(int i = 0; i < squaredDistances.length; i++) {
            if(squaredDistances[i] == null){ 
                continue;
            }
            lastLidarSnapshot.setDistance(i, Math.sqrt(squaredDistances[i]));
        }
        
        return lastLidarSnapshot;
    }
    
    private void processObservedPolygon(
            Polygon observedGeometry,
            Coordinate sensorPosition,
            Vector2D sensorStartOrientation,
            Vector2D sensorEndOrientation,
            double sensorStartAngle,
            double stepAngle,
            LineIntersector intersector,
            Double[] squaredDistances) {
        Coordinate[] observedCorners = observedGeometry.getCoordinates();

        // Find the angular range in terms of vectors of the
        // vehicle geometry relative to the sensor position
        Vector2D maxObservationVector = new Vector2D(
                observedCorners[0].x - sensorPosition.x,
                observedCorners[0].y - sensorPosition.y
        );
        Vector2D minObservationVector = maxObservationVector;
        
        for(int i = 1; i < observedCorners.length; i++) {
            Vector2D observation =  new Vector2D(
                    observedCorners[i].x - sensorPosition.x,
                    observedCorners[i].y - sensorPosition.y
            );

            if(liesOnLeftHandSide(maxObservationVector, observation)) {
                // Moving max observation vector counter clock-wise
                maxObservationVector = observation;
            } else if(liesOnRightHandSide(minObservationVector, observation)) {
                // Moving max observation vector clock-wise
                minObservationVector = observation;
            }
        }
        

        // Compute which beams we need to measure
        int minBeamIndex;
        int maxBeamIndex;
        
        if(config.getNumSteps() == 1) {
            // This is a single-beam radar
            // Measure if the single beam lies in the angular range
            // of the vehicle geometry
            if(!liesBetween(
                    minObservationVector, 
                    maxObservationVector, 
                    sensorStartOrientation)) {
                return;
            }
            
            minBeamIndex = 0;
            maxBeamIndex = 0;
        } else {
            // This is a multi-beam radar
            // See if some part of the vehicle geometry lies
            // within the angular range of the sensor
            if(!rangesOverlap(
                    minObservationVector, 
                    maxObservationVector, 
                    sensorStartOrientation, 
                    sensorEndOrientation)) {
                return;
            }

            // the ranges overlap
            // See which part of the angular range we need to measure
            minBeamIndex = computeMinBeamForRange(
                sensorStartOrientation,
                minObservationVector,
                sensorStartAngle,
                stepAngle
            );
            maxBeamIndex = computeMaxBeamForRange(
                sensorEndOrientation,
                maxObservationVector,
                sensorStartAngle,
                stepAngle
            );
        }
        

        // Using the magic that is vector calculus, we now know the range of 
        // beams that intersect the vehicle. Construct actual line segments
        // to compute polygon intersections.
        for(int beamIndex = minBeamIndex; beamIndex <= maxBeamIndex; beamIndex++) {
            // Beam calc caches the answer to avoid calling sin/cos,
            // so this will only hurt once per sensor per vehicle
            // this could be improved to once globally, but this 
            // is a slightly cleaner model
            Vector2D beamVector = beamCalc.getVector(beamIndex);
            
            beamVector = new Vector2D( 
                    sensorStartOrientation.getX() * beamVector.getX() - sensorStartOrientation.getY() * beamVector.getY(),
                    sensorStartOrientation.getY() * beamVector.getX() + sensorStartOrientation.getX() * beamVector.getY()
            );
            
            Coordinate beamEnd = new Coordinate(
                    sensorPosition.x + beamVector.getX() * config.getRange(),
                    sensorPosition.y + beamVector.getY() * config.getRange()
            );
            
            // We will look for the minimum squared distance to 
            // avoid redundant calls to Math.sqrt()
            double minDistance2 = config.getRange() * config.getRange();
            
            // Compute intersection for each of the sides of the vehicles
            for(int i = 0; i < observedCorners.length; i++) {
                
                // This is where it hurts even more in terms of performance
                intersector.computeIntersection(
                        sensorPosition, beamEnd,
                        observedCorners[i], observedCorners[(i+1) % observedCorners.length]
                );
                
                if(!intersector.hasIntersection()) {
                    continue;
                }
                
                Coordinate intersection = intersector.getIntersection(0);
                
                // This beam intersects, compute the distance
                // and keep it if it smaller than the distance
                // to previous intersections and within range
                double distance2 = computeDistance2(intersection, sensorPosition);
                
                if(distance2 < minDistance2) {
                    minDistance2 = distance2;
                }
            }
            
            // We have completed a distance measurement!
            if(squaredDistances[beamIndex] == null || minDistance2 < squaredDistances[beamIndex]) {
                squaredDistances[beamIndex] = minDistance2;
            }
        }
    }

    private double computeDistance2(
            Coordinate p1,
            Coordinate p2) {
        double dX = p1.x - p2.x;
        double dY = p1.y - p2.y;
        return dX * dX + dY * dY;
    }

    private int computeMaxBeamForRange(
            Vector2D sensorEndOrientation, 
            Vector2D maxObservationVector, 
            double sensorStartAngle, 
            double stepAngle) {
        if(liesOnLeftHandSide(sensorEndOrientation, maxObservationVector)) {
            // the minObservationVector lies left of the last beam
            // Include the last beam
            return config.getNumSteps()-1;
        } else {
            // Compute the last beam index less than the maximum angle
            double maxObservationAngle = maxObservationVector.angle();
            
            if(maxObservationAngle < sensorStartAngle) {
                maxObservationAngle += Math.PI * 2;
            }
            
            double maxRelativeAngle = maxObservationAngle - sensorStartAngle;
            
            return (int) Math.floor(maxRelativeAngle / stepAngle);
        }
    }

    private int computeMinBeamForRange(
            Vector2D sensorStartOrientation, 
            Vector2D minObservationVector, 
            double sensorStartAngle, 
            double stepAngle) {
        if(liesOnRightHandSide(sensorStartOrientation, minObservationVector)) {
            // the minObservationVector lies right of the first beam
            // Include the first beam
            return 0;
        } else {
            // Compute the first beam index greater than the minimum angle
            double minObservationAngle = minObservationVector.angle();
            
            if(minObservationAngle < sensorStartAngle) {
                minObservationAngle += Math.PI * 2;
            }
            
            double minRelativeAngle = minObservationAngle - sensorStartAngle;
            
            return (int) Math.ceil(minRelativeAngle / stepAngle);
        }

    }

    private boolean rangesOverlap(
            Vector2D minObservationVector,
            Vector2D maxObservationVector, 
            Vector2D sensorStartOrientation,
            Vector2D sensorEndOrientation) {
        return liesOnLeftHandSide(sensorStartOrientation, maxObservationVector)
            || liesOnRightHandSide(sensorEndOrientation, minObservationVector);
    }

    boolean liesBetween(Vector2D start, Vector2D end, Vector2D test) {
        return liesOnLeftHandSide(start, test) && liesOnRightHandSide(end, test); 
    }

    boolean liesOnLeftHandSide(Vector2D base, Vector2D test) {
        return base.getX() * test.getY() - base.getY() * test.getX() >= 0;
    }

    boolean liesOnRightHandSide(Vector2D base, Vector2D test) {
        return base.getX() * test.getY() - base.getY() * test.getX() <= 0;
    }
    
}
