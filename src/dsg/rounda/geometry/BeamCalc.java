/**
 * 
 */
package dsg.rounda.geometry;

import com.vividsolutions.jts.math.Vector2D;

import dsg.rounda.model.LidarSpecification;

/**
 * Calculates vectors from beam indices for a given LIDAR spec
 */
public class BeamCalc {
    
    final Vector2D[] cache;
    final double stepSize;
    
    /**
     * 
     * @param spec the lidar specification
     */
    public BeamCalc(LidarSpecification spec) {
        this.cache = new Vector2D[spec.getNumSteps()];
        this.stepSize = spec.getStepSize();
    }
    
    /**
     * Get the vector for a given beam index. May be 
     * a cached result.
     * 
     * @param beamIndex the beam index
     * @return the vector
     */
    public Vector2D getVector(int beamIndex) {
        Vector2D vector = cache[beamIndex];
        
        if(vector != null) {
            return vector;
        } else {
            double beamAngle = beamIndex * stepSize;
            
            return cache[beamIndex] = computeVector(beamAngle);
        }
    }

    /**
     * Compute the vector for a given angle
     * 
     * @param angle the angle
     * @return the vector
     */
    public Vector2D computeVector(double angle) {
        return new Vector2D(
                Math.cos(angle),
                Math.sin(angle));
    }

}
