/**
 * 
 */
package dsg.rounda.gui;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Polygon;

import dsg.rounda.model.WorldView;

/**
 * Represents the relationship between the screen and
 * the part of the world that is displayed.
 */
public class WorldScreenView {

    private final Screen screenView;
    private final WorldView worldView;

    private double widthRatio;
    private double heightRatio;
    
    public WorldScreenView(
            Screen screenView, 
            WorldView worldView) {
        this.screenView = new Screen(screenView);
        this.worldView = new WorldView(worldView);
        updateRatios();
    }
    
    public WorldScreenView(
            WorldScreenView copy) {
        this.screenView = new Screen(copy.screenView);
        this.worldView = new WorldView(copy.worldView);
        updateRatios();
    }

    public WorldScreenView(int screenWidth, int screenHeight, WorldView initialWorldView) {
        this.screenView = new Screen(screenWidth, screenHeight);
        this.worldView = new WorldView(initialWorldView);
        updateRatios();
    }

    void updateRatios() {
        updateWidthRatio();
        updateHeightRatio();
    }

    void updateWidthRatio() {
        widthRatio = getScreenWidth() / worldView.getWidth();
    }

    void updateHeightRatio() {
        heightRatio = getScreenHeight() / worldView.getHeight();
    }

    public Coordinate toWorldCoord(Coordinate screenCoord) {
        double x = toWorldX(screenCoord.x);
        double y = toWorldY(screenCoord.y);
        return new Coordinate(x, y);
    }

    public void makeScreenCoord(Coordinate coord) {
        coord.x = screenView.getLeftX() + (coord.x - worldView.getWestX()) * widthRatio;
        coord.y = screenView.getTopY() + (worldView.getNorthY() - coord.y) * heightRatio;
    }

    public Coordinate toScreenCoord(Coordinate simCoord) {
        double x = screenView.getLeftX() + (simCoord.x - worldView.getWestX()) * widthRatio;
        double y = screenView.getTopY() + (worldView.getNorthY() - simCoord.y) * heightRatio;
        return new Coordinate(x, y);
    }

    public double toWorldX(double screenX) {
        return (screenX - screenView.getLeftX()) / widthRatio  + worldView.getWestX();
    }

    public double toWorldY(double screenY) {
        return worldView.getNorthY() - (screenY - screenView.getTopY()) / heightRatio;
    }

    public double toScreenWidth(double simWidth) {
        return simWidth * widthRatio;
    }
    
    public double toScreenHeight(double simHeight) {
        return simHeight * heightRatio;
    }
    
    public double toWorldHeight(double screenHeight) {
        return screenHeight / heightRatio;
    }
    
    public double toWorldWidth(double screenWidth) {
        return screenWidth / widthRatio;
    }

    /**
     * @return the screenWidth
     */
    public double getScreenWidth() {
        return screenView.getWidth();
    }

    /**
     * @param screenWidth the screenWidth to set
     */
    public void setScreenWidth(double screenWidth) {
        this.screenView.setWidth(screenWidth);
        updateWidthRatio();
    }

    /**
     * @return the screenHeight
     */
    public double getScreenHeight() {
        return screenView.getHeight();
    }

    /**
     * @param screenHeight the screenHeight to set
     */
    public void setScreenHeight(double screenHeight) {
        this.screenView.setHeight(screenHeight);
        updateHeightRatio();
    }

    /**
     * @return
     * @see dsg.rounda.model.WorldView#getWorldViewX()
     */
    public double getWorldViewX() {
        return worldView.getWestX();
    }

    /**
     * @return
     * @see dsg.rounda.model.WorldView#getWorldViewY()
     */
    public double getWorldViewY() {
        return worldView.getSouthY();
    }

    /**
     * @return
     * @see dsg.rounda.model.WorldView#getWorldWidth()
     */
    public double getWorldWidth() {
        return worldView.getWidth();
    }

    /**
     * @param worldWidth
     * @see dsg.rounda.model.WorldView#setWorldWidth(double)
     */
    public void setWorldWidth(double worldWidth) {
        worldView.setWidth(worldWidth);
        updateWidthRatio();
    }

    /**
     * @return
     * @see dsg.rounda.model.WorldView#getWorldHeight()
     */
    public double getWorldHeight() {
        return worldView.getHeight();
    }

    /**
     * @param worldHeight
     * @see dsg.rounda.model.WorldView#setWorldHeight(double)
     */
    public void setWorldHeight(double worldHeight) {
        worldView.setHeight(worldHeight);
        updateHeightRatio();
    }

    /**
     * @return the widthRatio
     */
    public double getWidthRatio() {
        return widthRatio;
    }

    /**
     * @return the heightRatio
     */
    public double getHeightRatio() {
        return heightRatio;
    }

    public Coordinate getWorldViewXY() {
        return new Coordinate(getWorldViewX(), getWorldViewY());
    }

    public void setWorldView(WorldView worldView) {
        this.worldView.set(worldView);
        updateRatios();
    }

    public WorldView getWorldView() {
        return new WorldView(this.worldView);
    }

}
