/**
 * 
 */
package dsg.rounda.geometry;

/**
 * Cached factorial calculator
 * 
 * Adapted from bezier.c by David F. Rogers.  
 */
public class Factorial {

    int numFactorials;
    final double[] factorials;
    
    /**
     * Create a factorial calculator that can compute
     * at most maxNum!
     */
    public Factorial(int maxNum) {
        this.factorials = new double[maxNum];
        this.factorials[0] = 1.0f;
        this.factorials[1] = 1.0f;
        this.numFactorials = 1;
    }

    /**
     * Compute n!
     * @param n the value to compute the factorial for
     * @return n!
     */
    public double computeFactorial(int n) {
        if (n < 0)
            throw new IllegalArgumentException(
                    "Negative factorial");
        if (n > factorials.length)
            throw new IllegalArgumentException(
                    "Factorial value too large");
        
        while (numFactorials < n) {
            int oldNum = numFactorials;
            numFactorials += 1;
            factorials[numFactorials] = factorials[oldNum] * numFactorials;
        }
        return factorials[n]; 
    }
}
