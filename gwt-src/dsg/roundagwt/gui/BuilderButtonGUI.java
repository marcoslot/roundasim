/**
 * 
 */
package dsg.roundagwt.gui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author slotm
 *
 */
public class BuilderButtonGUI extends Composite implements BuilderButtonView {

    private static BuilderButtonGUIUiBinder uiBinder = GWT
            .create(BuilderButtonGUIUiBinder.class);

    interface BuilderButtonGUIUiBinder extends
            UiBinder<Widget, BuilderButtonGUI> {
    }

    @UiField
    SimGUIResources res;

    @UiField
    PushButton textButton;

    @UiField
    PushButton undoButton;

    @UiField
    PushButton openButton;

    @UiField
    PushButton backgroundButton;

    @UiField
    PushButton expandRoadButton;

    @UiField
    PushButton glueButton;

    @UiField
    ToggleButton createBuildingButton;

    @UiField
    ToggleButton createRoadButton;

    @UiField
    ToggleButton worldButton;

    @UiField
    ToggleButton playButton;

    @UiField
    PushButton saveButton;
    
    Presenter presenter;
    
    boolean roadSelected;
    boolean undoEnabled;
    boolean openEnabled;
    int buttonFlags;
    
    public BuilderButtonGUI() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setButtonsEnabled(int flags) {
        openButton.setEnabled(openEnabled && (flags & OPEN_BUTTON) > 0);
        backgroundButton.setEnabled((flags & BACKGROUND_BUTTON) > 0);
        createRoadButton.setEnabled((flags & CREATE_ROAD_BUTTON) > 0);
        glueButton.setEnabled((flags & GLUE_BUTTON) > 0);
        createBuildingButton.setEnabled((flags & CREATE_BUILDING_BUTTON) > 0);
        undoButton.setEnabled(undoEnabled && (flags & UNDO_BUTTON) > 0);
        worldButton.setEnabled((flags & WORLD_BUTTON) > 0);
        textButton.setEnabled((flags & TEXT_BUTTON) > 0);
        expandRoadButton.setEnabled(roadSelected && (flags & EXPAND_ROAD_BUTTON) > 0);
        playButton.setEnabled((flags & PLAY_BUTTON) > 0);
        saveButton.setEnabled((flags & SAVE_BUTTON) > 0);
        buttonFlags = flags;
    }

    @UiHandler("textButton")
    public void onTextClick(ClickEvent evt) {
        if(presenter != null) {
            presenter.onTextClick();
        }
    }

    @UiHandler("undoButton")
    public void onUndoClick(ClickEvent evt) {
        if(presenter != null) {
            presenter.onUndoClick();
        }
    }

    @UiHandler("saveButton")
    public void onSaveClick(ClickEvent evt) {
        if(presenter != null) {
            presenter.onSaveClick();
        }
    }

    @UiHandler("openButton")
    public void onOpenClick(ClickEvent evt) {
        if(presenter != null) {
            presenter.onOpenClick();
        }
    }

    @UiHandler("backgroundButton")
    public void onBackgroundClick(ClickEvent evt) {
        if(presenter != null) {
            presenter.onBackgroundClick();
        }
    }

    @UiHandler("expandRoadButton")
    public void onExpandRoadClick(ClickEvent evt) {
        if(presenter != null) {
            presenter.onExpandRoadClick();
        }
    }

    @UiHandler("glueButton")
    public void onGlueClick(ClickEvent evt) {
        if(presenter != null) {
            presenter.onGlueClick();
        }
    }

    @UiHandler("createBuildingButton")
    public void onCreateBuildingClick(ClickEvent evt) {
        if(presenter != null) {
            presenter.onCreateBuildingClick(createBuildingButton.getValue());
        }
    }

    @UiHandler("createRoadButton")
    public void onCreateRoadClick(ClickEvent evt) {
        if(presenter != null) {
            presenter.onCreateRoadClick(createRoadButton.getValue());
        }
    }

    @UiHandler("worldButton")
    public void onWorldClick(ClickEvent evt) {
        if(presenter != null) {
            presenter.onWorldClick(worldButton.getValue());
        }
    }

    @UiHandler("playButton")
    public void onPlayClick(ClickEvent evt) {
        if(presenter != null) {
            presenter.onPlayClick(playButton.getValue());
        }
    }

    @Override
    public void setUndoButtonEnabled(boolean enabled) {
        undoEnabled = enabled;
        
        if(enabled) {
            undoButton.setEnabled(undoEnabled && (buttonFlags & UNDO_BUTTON) > 0);
        }
    }

    @Override
    public void setOpenButtonEnabled(boolean enabled) {
        openEnabled = enabled;

        if(enabled) {
            openButton.setEnabled(openEnabled && (buttonFlags & OPEN_BUTTON) > 0);
        }
    }

    @Override
    public void setExpandButtonEnabled(boolean enabled) {
        this.roadSelected = enabled;

        if(enabled) {
            expandRoadButton.setEnabled(enabled && (buttonFlags & EXPAND_ROAD_BUTTON) > 0);
        }
    }

    @Override
    public void setCreateBuildingButtonPressed(boolean pressed) {
        createBuildingButton.setValue(pressed);
    }

    @Override
    public void setCreateRoadButtonPressed(boolean pressed) {
        createRoadButton.setValue(pressed);
    }

}
