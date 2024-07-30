/**
 * 
 */
package dsg.rounda.gui;

import dsg.rounda.SimPlayer;

/**
 * This class coordinates interactions between the GUI (PlayerView)
 * and the logic (SimPlayer). 
 */
public class PlayerPresenter implements PlayerView.Presenter {

    final PlayerView view;
    final SimPlayer runner;
    final Runnable onReset;
    
    boolean playing;
    
    /**
     * 
     */
    public PlayerPresenter(PlayerView view, SimPlayer runner, Runnable onReset) {
        this.runner = runner;
        this.view = view;
        this.playing = false;
        this.view.setPresenter(this);
        this.view.setPlaySpeed(1.0);
        this.onReset = onReset;
    }

    /**
     * @see dsg.rounda.gui.PlayerView.Presenter#play()
     */
    @Override
    public synchronized void play() {
        this.runner.start();
        this.view.showPauseButton();
        this.playing = true;
    }

    /**
     * @see dsg.rounda.gui.PlayerView.Presenter#pause()
     */
    @Override
    public synchronized void pause() {
        this.runner.stop(false);
        this.view.showResumeButton();
        this.playing = false;
    }

    /**
     * @see dsg.rounda.gui.PlayerView.Presenter#reset()
     */
    @Override
    public synchronized void reset() {
        this.runner.stop();
        this.view.setPlaySpeed(1.0);
        this.view.showStartButton();
        this.playing = false;
        
        if(onReset != null) {
            onReset.run();
        }
    }

    /**
     * @see dsg.rounda.gui.PlayerView.Presenter#setPlaySpeed(double)
     */
    @Override
    public synchronized void setPlaySpeed(double playSpeed) {
        this.runner.setPlaySpeed(playSpeed);
    }

    public synchronized void freeze() {
        if(playing) {
            this.runner.stop(false);
        }
    }

    public synchronized void unfreeze() {
        if(playing) {
            this.runner.start();
        }
    }

}
