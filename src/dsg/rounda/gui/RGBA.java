/**
 * 
 */
package dsg.rounda.gui;

/**
 * Colour!
 */
public class RGBA {

    int r;
    int g;
    int b;
    int a;
    
    String toString;
    /**
     * @param r
     * @param g
     * @param b
     */
    public RGBA(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 255;
    }
    /**
     * @return the r
     */
    public int getR() {
        return r;
    }
    /**
     * @param r the r to set
     */
    public void setR(int r) {
        this.r = r;
    }
    /**
     * @return the g
     */
    public int getG() {
        return g;
    }
    /**
     * @param g the g to set
     */
    public void setG(int g) {
        this.g = g;
    }
    /**
     * @return the b
     */
    public int getB() {
        return b;
    }
    /**
     * @param b the b to set
     */
    public void setB(int b) {
        this.b = b;
    }
    /**
     * @return the a
     */
    public int getA() {
        return a;
    }
    /**
     * @param a the a to set
     */
    public void setA(int a) {
        this.a = a;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if(toString != null) {
            return toString;
        }
        return toString = "rgba(" + r + "," + g + "," + b + "," + a + ")";
    }
    
    
    
}
