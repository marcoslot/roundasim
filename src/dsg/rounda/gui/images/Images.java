/**
 * 
 */
package dsg.rounda.gui.images;

import java.net.URL;

import javax.swing.ImageIcon;


/**
 * @author slotm
 *
 */
public class Images {

    private Images() {
    }

    public static final ImageIcon load(String name) {
        URL imageURL = Images.class.getResource(name);
        return imageURL == null ? null : new ImageIcon(imageURL);
    }
    
}
