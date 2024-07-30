/**
 * 
 */
package dsg.roundagwt.gui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import dsg.rounda.gui.SimView;
import dsg.rounda.gui.WorldScreenView;

/**
 * @author slotm
 *
 */
public class BuilderGUI extends Composite implements BuilderView {

    private static BuilderGUIUiBinder uiBinder = GWT
            .create(BuilderGUIUiBinder.class);

    interface BuilderGUIUiBinder extends UiBinder<Widget, BuilderGUI> {
    }

    @UiField
    SimGUIResources res;

    @UiField
    BuilderButtonGUI buttons;

    @UiField
    BuilderPaletGUI palet;
    
    @UiField
    SimplePanel paletContainer;
    
    Presenter presenter;
    
    public BuilderGUI() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public BuilderButtonView getButtonView() {
        return buttons;
    }

    @Override
    public BuilderPaletView getPaletView() {
        return palet;
    }

    @Override
    public SimView showSimView(
            WorldScreenView worldScreenView, 
            String backgroundURL) {
        SimGUI simView = new SimGUI(worldScreenView);
        simView.setBackground(backgroundURL);
        
        paletContainer.setWidget(simView);
        
        return simView;
    }

    @Override
    public void showPalet() {
        paletContainer.setWidget(palet);
    }

    @Override
    public void saveToStorage(String worldAsText) {
        Storage storage = Storage.getLocalStorageIfSupported();
        
        if(storage == null) {
            return;
        }
        
        storage.setItem("builder-world", worldAsText);
    }

    @Override
    public String loadFromStorage() {
        Storage storage = Storage.getLocalStorageIfSupported();
        
        if(storage == null) {
            return null;
        }
        
        return storage.getItem("builder-world");
    }

    
}
