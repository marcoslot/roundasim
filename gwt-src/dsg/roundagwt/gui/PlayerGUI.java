/**
 * 
 */
package dsg.roundagwt.gui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import dsg.rounda.gui.PlayerView;

/**
 * @author slotm
 *
 */
public class PlayerGUI extends Composite implements PlayerView {

    private static PlayerGUIUiBinder uiBinder = GWT
            .create(PlayerGUIUiBinder.class);

    interface PlayerGUIUiBinder extends UiBinder<Widget, PlayerGUI> {
    }
    
    @UiField
    SimGUIResources res;

    @UiField
    Button startButton;

    @UiField
    Button pauseButton;
    
    @UiField
    Button stopButton;
    
    Presenter presenter;

    public PlayerGUI() {
        initWidget(uiBinder.createAndBindUi(this));
        res.style().ensureInjected();
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @UiHandler("stopButton")
    public void onStopClick(ClickEvent event) {
        if(presenter != null) {
            presenter.reset();
        }
    }

    @UiHandler("startButton")
    public void onStartClick(ClickEvent event) {
        if(presenter != null) {
            presenter.play();
        }
    }

    @UiHandler("pauseButton")
    public void onPauseClick(ClickEvent event) {
        if(presenter != null) {
            presenter.pause();
        }
    }

    @Override
    public void showPauseButton() {
        startButton.addStyleName(res.style().hidden());
        pauseButton.removeStyleName(res.style().hidden());
    }

    @Override
    public void showResumeButton() {
        showStartButton();
    }

    @Override
    public void showStartButton() {
        startButton.removeStyleName(res.style().hidden());
        pauseButton.addStyleName(res.style().hidden());
    }

    @Override
    public void setPlaySpeed(double ratio) {
        // TODO Auto-generated method stub
        
    }


}
