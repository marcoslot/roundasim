/**
 * 
 */
package dsg.rounda.model;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.math.Vector2D;

import dsg.rounda.config.RunConfig;
import dsg.rounda.config.SimulationParameters;

/**
 * Default factory for ranging sensor specifications that has 180 degree
 * lasers in the front and back and 4 single-beam lasers on the sides
 */
public class DefaultRangingSpecsFactory implements RangingSpecsFactory, SimulationParameters {

    final RunConfig config;
    
    /**
     * 
     */
    public DefaultRangingSpecsFactory(RunConfig config) {
        this.config = config;
    }

    /**
     * @see dsg.rounda.model.RangingSpecsFactory#createRangingSpecs(dsg.rounda.model.VehicleState)
     */
    @Override
    public RangingSensorsSpecification createRangingSpecs(VehicleState vehicle) {
        // LIDARs specified on clock-wise direction
        // Some algorithms assume this order for simplicity (read: sanity).
        // Todo: use some kind of configuration file for this
        RangingSensorsSpecification rangingSpecs = new RangingSensorsSpecification();
        
        int numWideLidarBeams = 180;
        double maxRange = config.get(LIDAR_RANGE);
        
        // Main 180 degrees LIDAR front
        LidarSpecification frontLidarSpec = new LidarSpecification(
                // Relative pose in a coordinate frame where the 
                // car is driving along the x-axis
                new SensorPose(
                    // Front of the car
                    new Coordinate(vehicle.getLength(), 0),
                    // First beam points to the right (downwards in relative coordinate frame)
                    new Vector2D(0,-1)
                ),
                maxRange, // meter range
                numWideLidarBeams+1, // beams 0-180  (inclusive)
                Math.PI / numWideLidarBeams // 1 degree steps
        );
        rangingSpecs.add(frontLidarSpec);

        // Side LIDAR front 
        LidarSpecification leftFrontLidarSpec = new LidarSpecification(
                new SensorPose(
                    // quarter from front, left side of car
                    new Coordinate(vehicle.getLength()*2./3., vehicle.getWidth()/2),
                    //  beam points to the left
                    new Vector2D(0,1)
                ),
                maxRange, // meter range
                1, // 1 beam LIDAR
                Math.PI / numWideLidarBeams // irrelevant
        );
        rangingSpecs.add(leftFrontLidarSpec);

        // Side LIDAR back 
        LidarSpecification leftBackLidarSpec = new LidarSpecification(
                new SensorPose(
                    // quarter from back, left side of car
                    new Coordinate(vehicle.getLength()*1./3., vehicle.getWidth()/2),
                    //  beam points to the left
                    new Vector2D(0,1)
                ),
                maxRange, // meter range
                1, // 1 beam LIDAR
                Math.PI / numWideLidarBeams // irrelevant
        );
        rangingSpecs.add(leftBackLidarSpec);

        // Main 180 degrees LIDAR back
        LidarSpecification backLidarSpec = new LidarSpecification(
                new SensorPose(
                    // Back of the car
                    new Coordinate(0, 0),
                    // First beam points to the left (upwards in relative coordinate frame)
                    new Vector2D(0,1)
                ),
                maxRange, // meter range
                numWideLidarBeams+1, // beams 0-180  (inclusive)
                Math.PI / numWideLidarBeams 
        );

        rangingSpecs.add(backLidarSpec);
        
        // Side LIDAR back 
        LidarSpecification rightBackLidarSpec = new LidarSpecification(
                new SensorPose(
                    // right side of car, quarter from front
                    new Coordinate(vehicle.getLength()*1./3., -vehicle.getWidth()/2),
                    // First beam points to the left
                    new Vector2D(0,-1)
                ),
                maxRange, // meter range
                1, // 1 beam LIDAR
                Math.PI / numWideLidarBeams // irrelevant
        );
        rangingSpecs.add(rightBackLidarSpec);

        // Side LIDAR front 
        LidarSpecification rightFrontLidarSpec = new LidarSpecification(
                new SensorPose(
                    // quarter from front, right side of car
                    new Coordinate(vehicle.getLength()*2./3., -vehicle.getWidth()/2),
                    //  beam points to the right
                    new Vector2D(0,-1)
                ),
                maxRange, // meter range
                1, // 1 beam LIDAR
                Math.PI / numWideLidarBeams // irrelevant
        );
        rangingSpecs.add(rightFrontLidarSpec);
        
        return rangingSpecs;
    }

}
