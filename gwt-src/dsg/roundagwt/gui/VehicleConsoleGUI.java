/**
 * 
 */
package dsg.roundagwt.gui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import dsg.rounda.gui.VehicleConsoleView;

/**
 * GWT implementation of the VehicleConsoleView
 */
public class VehicleConsoleGUI extends Composite implements VehicleConsoleView {

    private static VehicleConsoleGUIUiBinder uiBinder = GWT
            .create(VehicleConsoleGUIUiBinder.class);

    interface VehicleConsoleGUIUiBinder extends UiBinder<Widget, VehicleConsoleGUI> {
    }

    public VehicleConsoleGUI() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @UiField
    SimGUIResources res;

    @UiField
    Label idLabel;

    @UiField
    Label receivedMessagesLabel;

    @UiField
    Label sentMessagesLabel;

    @UiField
    Label allocatedDistanceLabel;

    @UiField
    Label availableDistanceLabel;

    @UiField
    Label trackLabel;

    @UiField
    Label offsetLabel;

    @UiField
    Label velocityLabel;

    Presenter presenter;

    public VehicleConsoleGUI(String firstName) {
        initWidget(uiBinder.createAndBindUi(this));
        res.style().ensureInjected();
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setID(Integer id) {
        if(id == null) {
            return;
        }
        idLabel.setText(Integer.toString(id));
    }

    @Override
    public void setReceivedMessageCount(Long count) {
        if(count == null) {
            return;
        }
        
        receivedMessagesLabel.setText(Long.toString(count));
    }

    @Override
    public void setSentMessageCount(Long count) {
        if(count == null) {
            return;
        }
        
        sentMessagesLabel.setText(Long.toString(count));
    }

    @Override
    public void setAllocatedDistance(Double distance) {
        if(distance == null) {
            return;
        }
        
        allocatedDistanceLabel.setText(Double.toString(distance));
    }

    @Override
    public void setAvailableDistance(Double distance) {
        if(distance == null) {
            return;
        }
        
        availableDistanceLabel.setText(Double.toString(distance));
    }

    @Override
    public void setVelocity(Double velocity) {
        if(velocity == null) {
            return;
        }
        
        velocityLabel.setText(Double.toString(velocity));
    }

    @Override
    public void setTrackID(Long trackID) {
        if(trackID == null) {
            return;
        }
        
        trackLabel.setText(Long.toString(trackID));
    }

    @Override
    public void setOffset(Double offset) {
        if(offset == null) {
            return;
        }
        
        offsetLabel.setText(Double.toString(offset));
    }

}
