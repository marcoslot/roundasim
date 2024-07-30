/**
 * 
 */
package dsg.rounda.gui.awt;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import dsg.rounda.gui.PlayerView;
import dsg.rounda.gui.PlayerView.Presenter;
import dsg.rounda.gui.images.Images;

/**
 * @author slotm
 *
 */
public class PlayerGUI extends JToolBar implements PlayerView {

    /**
     * 
     */
    private static final long serialVersionUID = -1930868226229255316L;

    private static final ImageIcon PLAY_ICON = Images.load("Play24.gif");
    private static final ImageIcon PAUSE_ICON = Images.load("Pause24.gif");
    private static final ImageIcon STOP_ICON = Images.load("Stop24.gif");
    private static final Hashtable<Integer, JComponent> SPEED_LABELS = createLabels();
    private static final String PLAY_ACTION = "play";
    private static final String PAUSE_ACTION = "pause";

    final JButton playPauseButton;
    final JButton stopButton;
    final JSlider playSpeedBar;
    
    Presenter presenter;

    public PlayerGUI() {
        this.stopButton = new JButton();
        this.stopButton.setIcon(STOP_ICON);
        this.stopButton.setToolTipText("Stop the simulation");
        this.stopButton.addActionListener(stopHandler);
        this.add(stopButton);
        this.playPauseButton = new JButton();
        this.playPauseButton.setIcon(PLAY_ICON);
        this.playPauseButton.setToolTipText("Start the simulation");
        this.playPauseButton.addActionListener(playPauseHandler);
        this.playPauseButton.setActionCommand(PLAY_ACTION);
        this.add(playPauseButton);
        this.playSpeedBar = new JSlider(0, 400, 100);
        this.playSpeedBar.setPaintTicks(false);
        this.playSpeedBar.setPaintLabels(true);
        this.playSpeedBar.addChangeListener(playSpeedChangeHandler);
        this.playSpeedBar.setLabelTable(SPEED_LABELS);
        this.add(playSpeedBar);
    }

    private static final Hashtable<Integer,JComponent> createLabels() {
        Hashtable<Integer,JComponent> result = new Hashtable<Integer,JComponent>();
        result.put(0, new JLabel("0x"));
        result.put(100, new JLabel("1x"));
        result.put(200, new JLabel("10x"));
        result.put(300, new JLabel("100x"));
        result.put(400, new JLabel("1000x"));
        return result;
    }

    private void setPlayButton(String altText, ImageIcon icon, String tooltipText) {
        playPauseButton.setIcon(icon);
        playPauseButton.setToolTipText(tooltipText);
    }
    
    final ActionListener playPauseHandler = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
                if(presenter != null) {
                    if(PAUSE_ACTION.equals(e.getActionCommand())) {
                        presenter.pause();
                    } else {
                        presenter.play();
                    }
                }
        }
    };
    
    final ActionListener stopHandler = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            if(presenter != null) {
                presenter.reset();
            }
        }
    };

    final ChangeListener playSpeedChangeHandler = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            if(presenter != null) {
                int barValue = playSpeedBar.getValue();
                double playSpeed = barValue == 0 ? 0 : Math.pow(10, barValue/100.)/10.;
                presenter.setPlaySpeed(playSpeed);
            }
        }
    };
    
    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override 
    public void showPauseButton() {
        setPlayButton("Pause", PAUSE_ICON, "Pause the simulation");
        playPauseButton.setActionCommand(PAUSE_ACTION);
    }

    @Override 
    public void showResumeButton() {
        setPlayButton("Resume", PLAY_ICON, "Resume the simulation");
        playPauseButton.setActionCommand(PLAY_ACTION);
    }

    @Override 
    public void showStartButton() {
        setPlayButton("Start", PLAY_ICON, "Start the simulation");
        playPauseButton.setActionCommand(PLAY_ACTION);
    }
    
    @Override
    public void setPlaySpeed(double value) {
        playSpeedBar.setValue(value <= 0 ? 0 : (int) (Math.log10(value*10)*100));
    }

    public void focusPlayButton() {
        playPauseButton.requestFocus();
    }

}
