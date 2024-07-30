/**
 * 
 */
package dsg.rounda;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import dsg.rounda.config.CommandLineRunConfig;
import dsg.rounda.config.RunConfig;
import dsg.rounda.gui.PlayerPresenter;
import dsg.rounda.gui.SimPresenter;
import dsg.rounda.gui.WorldScreenView;
import dsg.rounda.gui.awt.PlayerGUI;
import dsg.rounda.gui.awt.SimGUI;
import dsg.rounda.gui.images.Images;
import dsg.rounda.io.FileIOJRE;
import dsg.rounda.scenarios.Scenario;

/**
 * Main class of Round-A-sim with AWT GUI
 */
public class SimMain {

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                UIManager.put("swing.boldMetal", Boolean.FALSE);        
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                createAndShowGUI(args);
            }
        });
    }

    public static void createAndShowGUI(String[] args) {
        final CommandLineRunConfig config = new CommandLineRunConfig(SimMain.class, args);
        
        final SimController simController = new SimController(
                config,
                new FileIOJRE("war")
        );
        simController.init(run(simController, config));
    }


    static Runnable run(final SimController simController, final RunConfig config) {
        return new Runnable() {
            public void run() {

                final Scenario scenario = config.getScenario();
                final Image background = Images.load(scenario.getBackground()).getImage();
                
                final SimGUI palet = new SimGUI(new WorldScreenView(
                    600, 450, // screen size
                    scenario.getInitialWorldView()        
                ));
                palet.setBackground(Color.WHITE);
                palet.setBackground(
                        background,
                        new WorldScreenView(
                                background.getWidth(null), 
                                background.getHeight(null),
                                scenario.getInitialWorldView())
                );
                
                final SimPresenter simPresenter = new SimPresenter(
                        palet,
                        simController
                );
                
                final SimPlayer runner = new SimPlayerJRE(
                        simController,
                        simPresenter
                );

                final PlayerGUI playerToolbar = new PlayerGUI();
                
                final Runnable onReset = new Runnable() {
                    @Override
                    public void run() {
                        simController.reset();
                        simPresenter.reset();
                    }
                };
                
                final PlayerPresenter player = new PlayerPresenter(
                        playerToolbar, 
                        runner,
                        onReset
                );

                JFrame frame = new JFrame("Round-A-Sim: Paris edition");
                frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {System.exit(0);}
                    public void windowDeiconified(WindowEvent e) { player.unfreeze(); }
                    public void windowIconified(WindowEvent e) { player.freeze(); }
                });
                frame.setLayout(new BorderLayout());
                frame.add(palet, BorderLayout.CENTER);
                frame.add(playerToolbar, BorderLayout.SOUTH);
                frame.setSize(new Dimension(800,600));
                frame.setVisible(true);
                playerToolbar.focusPlayButton();
            }
        };
    }
}
