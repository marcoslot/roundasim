/**
 * 
 */
package dsg.rounda.gui;

/**
 * @author slotm
 *
 */
public interface PlayerView {

    public interface Presenter {
        void play();
        void pause();
        void reset();
        void setPlaySpeed(double playSpeed);
    }
    
    void setPresenter(Presenter presenter);

    void showPauseButton();
    void showResumeButton();
    void showStartButton();
    void setPlaySpeed(double ratio);
}
