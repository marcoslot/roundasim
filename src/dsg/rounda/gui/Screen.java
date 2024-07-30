/**
 * 
 */
package dsg.rounda.gui;

/**
 * @author slotm
 *
 */
public class Screen {

    private double leftX;
    private double topY;
    private double width;
    private double height;
    /**
     * @param screenWidth
     * @param screenHeight
     */
    public Screen(double screenWidth, double screenHeight) {
        this(0.0, 0.0, screenWidth, screenHeight);
    }
    
    /**
     * @param leftX
     * @param topY
     * @param width
     * @param height
     */
    public Screen(double leftX, double topY, double width, double height) {
        this.leftX = leftX;
        this.topY = topY;
        this.width = width;
        this.height = height;
    }

    /**
     * @return the leftX
     */
    public double getLeftX() {
        return leftX;
    }

    /**
     * @param leftX the leftX to set
     */
    public void setLeftX(double leftX) {
        this.leftX = leftX;
    }

    /**
     * @return the topY
     */
    public double getTopY() {
        return topY;
    }

    /**
     * @param topY the topY to set
     */
    public void setTopY(double topY) {
        this.topY = topY;
    }

    /**
     * 
     * @param screenView
     */
    public Screen(Screen screenView) {
        this(screenView.leftX, screenView.topY, screenView.width, screenView.height);
    }
    /**
     * @return the width
     */
    public double getWidth() {
        return width;
    }
    /**
     * @param width the width to set
     */
    public void setWidth(double width) {
        this.width = width;
    }
    /**
     * @return the height
     */
    public double getHeight() {
        return height;
    }
    /**
     * @param height the height to set
     */
    public void setHeight(double height) {
        this.height = height;
    }


}
