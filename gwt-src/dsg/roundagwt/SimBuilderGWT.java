/**
 * 
 */
package dsg.roundagwt;

import java.util.MissingResourceException;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import dsg.rounda.io.FileIO.Callback;
import dsg.roundagwt.gui.BuilderButtonView;
import dsg.roundagwt.gui.BuilderGUI;
import dsg.roundagwt.io.FileIOGWT;

/**
 * GWT implementation of round-a-sim
 */
public class SimBuilderGWT implements EntryPoint {

    /**
     * 
     * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {
        final BuilderGUI builderGUI = new BuilderGUI();
        final BuilderButtonView palet = builderGUI.getButtonView();
        final BuilderPresenter presenter = new BuilderPresenter(builderGUI);
        RootPanel.get().add(builderGUI);
        
        try {
            Dictionary config = Dictionary.getDictionary("config");
            String url = config.get("scenario-url");
            
            final DialogBox loading = new DialogBox(false);
            loading.setGlassEnabled(true);
            loading.add(new Label("Loading..."));
            loading.center();
            loading.show();
            
            new FileIOGWT().readFile(url, new Callback() {
                @Override
                public void onFailure(Throwable e) {
                    loading.hide();
                    RootPanel.get().add(new Label(e.getMessage()));
                }

                @Override
                public void onSuccess(String[] contents) {
                    loading.hide();
                    presenter.loadWorldFromText(contents);
                }
            });
        } catch (MissingResourceException e) {
        }
    }
    
}
