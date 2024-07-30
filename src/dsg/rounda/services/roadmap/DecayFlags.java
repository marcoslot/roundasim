/**
 * 
 */
package dsg.rounda.services.roadmap;

/**
 * Flags for decay algorithm
 */
public class DecayFlags {

    public static final int GROW = 1 << 0;
    public static final int SHRINK = 1 << 1;
    public static final int DISTANCE = 1 << 2;
    public static final int TIME = 1 << 3;
    public static final int CONSIDER_DIRECTION = 1 << 4;

    private int value;

    public DecayFlags()
    {
        setValue(SHRINK | TIME | CONSIDER_DIRECTION);
    }

    public DecayFlags(int value)
    {
        setValue(value);
    }

    public int getValue() {
        return this.value;
    }

    public void setValue(int value) {
        if (((value & GROW) ^ (value & SHRINK)) == 0)
        {
            throw new IllegalArgumentException(
                    "Either GROW or SHRINK needs to be set");
        }
        if (((value & DISTANCE) ^ (value & TIME)) == 0)
        {
            throw new IllegalArgumentException(
                    "Either DISTANCE or TIME needs to be set");
        }
        this.value = value;
    }

    public boolean is(int query) {
        return (this.value & query) > 0;
    }

}
