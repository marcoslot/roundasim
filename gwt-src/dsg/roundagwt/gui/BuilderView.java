/**
 * 
 */
package dsg.roundagwt.gui;

import dsg.rounda.gui.SimView;
import dsg.rounda.gui.WorldScreenView;

/**
 * @author slotm
 *
 */
public interface BuilderView {

    public interface Presenter {
        
    }
    
    void setPresenter(Presenter presenter);
    
    BuilderButtonView getButtonView();
    BuilderPaletView getPaletView();

    SimView showSimView(WorldScreenView simView, String backgroundURL);

    void showPalet();

    void saveToStorage(String worldAsText);
    String loadFromStorage();
}
