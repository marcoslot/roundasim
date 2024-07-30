/**
 * 
 */
package dsg.rounda.model;

/**
 * Connection point to a track
 */
public class Connector implements Comparable<Connector> {

    public static final double CONNECTOR_EQUIVALENCE_DISTANCE = 0.001; // 1mm
    
    final int road;
    final double offset;
    
    /**
     * 
     */
    public Connector(int road, double offset) {
        this.road = road;
        this.offset = offset;
    }

    /**
     * @return the road
     */
    public int getRoad() {
        return road;
    }

    /**
     * @return the offset
     */
    public double getOffset() {
        return offset;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 31 * road + (int) (offset / CONNECTOR_EQUIVALENCE_DISTANCE);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Connector)) {
            return false;
        }
        Connector other = (Connector) obj;
        if (Math.abs(offset - other.offset) > CONNECTOR_EQUIVALENCE_DISTANCE) {
            return false;
        }
        if (road != other.road) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Connector o) {
        if(road != o.road) {
            throw new IllegalArgumentException("Cannot compare connectors from different roads");
        }
        
        if(equals(o)) {
            return 0;
        } else {
            return Double.compare(getOffset(), o.getOffset());
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Connector[track=" + road + ", offset=" + offset + "]";
    }
    

}
