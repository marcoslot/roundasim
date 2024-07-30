/**
 * 
 */
package dsg.roundagwt.gui;

/**
 * @author slotm
 *
 */
public interface BuilderButtonView {

    public static final int OPEN_BUTTON = 1 << 0;
    public static final int CREATE_ROAD_BUTTON = 1 << 1;
    public static final int EXPAND_ROAD_BUTTON = 1 << 2;
    public static final int GLUE_BUTTON = 1 << 3;
    public static final int CREATE_BUILDING_BUTTON = 1 << 4;
    public static final int UNDO_BUTTON = 1 << 5;
    public static final int WORLD_BUTTON = 1 << 6;
    public static final int TEXT_BUTTON = 1 << 7;
    public static final int PLAY_BUTTON = 1 << 8;
    public static final int SAVE_BUTTON = 1 << 9;
    public static final int BACKGROUND_BUTTON = 1 << 10;
    public static final int ALL_BUTTONS = 0xffffffff;
    public static final int NO_BUTTONS = 0;

    public interface Presenter {
        void onPlayClick(boolean pressed);
        void onWorldClick(boolean pressed);
        void onCreateRoadClick(boolean pressed);
        void onCreateBuildingClick(boolean pressed);
        void onGlueClick();
        void onExpandRoadClick();
        void onOpenClick();
        void onUndoClick();
        void onTextClick();
        void onSaveClick();
        void onBackgroundClick();
    }
    
    void setPresenter(Presenter presenter);
    void setButtonsEnabled(int flags);
    void setExpandButtonEnabled(boolean enabled);
    void setUndoButtonEnabled(boolean enabled);
    void setCreateRoadButtonPressed(boolean pressed);
    void setCreateBuildingButtonPressed(boolean pressed);
    void setOpenButtonEnabled(boolean enabled);

}
