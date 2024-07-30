/**
 * 
 */
package dsg.roundagwt.gui;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * @author slotm
 *
 */
public interface SimGUIResources extends ClientBundle {

    @Source("images/play-icon.png")
    ImageResource playIcon();

    @Source("images/save-icon.png")
    ImageResource saveIcon();

    @Source("images/document-open.png")
    ImageResource openIcon();

    @Source("images/grass-icon.png")
    ImageResource grassIcon();

    @Source("images/road-icon.png")
    ImageResource roadIcon();

    @Source("images/track-icon.png")
    ImageResource trackIcon();

    @Source("images/expand-road-icon.png")
    ImageResource expandRoadIcon();

    @Source("images/glue-icon.png")
    ImageResource glueIcon();

    @Source("images/building-icon.png")
    ImageResource buildingIcon();

    @Source("images/undo-icon.png")
    ImageResource undoIcon();

    @Source("images/world-icon.png")
    ImageResource worldIcon();

    @Source("images/text-icon.png")
    ImageResource textIcon();
    
    @Source("style/SimGUI.css")
    Style style();

    public interface Style extends CssResource {
        String hidden();
        String clear();
        String relative();
        String absolute();
        String border();
        String pointerCursor();
        String vehicleConsole();
        String consoleLabelHeader();
        String inline();
        String marginLeft();
        String toolBarButton();
        String outText();
        String markerPanel();
        String dropPanel();
        String widthBox();
        String heightBox();
        String buttonSeparator();
    }
}
