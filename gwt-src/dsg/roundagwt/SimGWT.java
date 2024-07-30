/**
 * 
 */
package dsg.roundagwt;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;

import dsg.rounda.SimController;
import dsg.rounda.TimeMode;
import dsg.rounda.config.RunConfig;
import dsg.rounda.config.SimulationParameters;
import dsg.rounda.gui.PlayerPresenter;
import dsg.rounda.gui.SimPresenter;
import dsg.rounda.gui.WorldScreenView;
import dsg.roundagwt.gui.PlayerGUI;
import dsg.roundagwt.gui.SimGUI;
import dsg.roundagwt.io.FileIOGWT;

/**
 * GWT implementation of round-a-sim
 */
public class SimGWT implements EntryPoint, SimulationParameters {

    /**
     * 
     * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {
        RunConfig simConfig = new JSRunConfig();

        SimController simController = new SimController(
                simConfig,
                new FileIOGWT());

        simController.init(handleReady(simController, simConfig));
    }

    Runnable handleReady(
            final SimController simController,
            final RunConfig simConfig) {
        return new Runnable() {
            public void run() {
                runSimulation(simController, simConfig);
            }
        };
    }

    void runSimulation(
            final SimController simController, 
            final RunConfig simConfig) {

        final SimGUI simPalet = new SimGUI(new WorldScreenView(
                600, 450, // screen size
                simConfig.getScenario().getInitialWorldView()        
                ));
        simPalet.setBackground(simConfig.getScenario().getBackground());

        final SimPresenter simPresenter = new SimPresenter(
                simPalet,
                simController
                );

        final SimPlayerGWT runner = new SimPlayerGWT(
                simController
                );
        runner.setTimeMode(GWT.isProdMode() ? TimeMode.REAL_TIME : TimeMode.SMOOTH_TIME);

        final Runnable onReset = new Runnable() {
            @Override
            public void run() {
                simPresenter.reset();
            }
        };

        final PlayerPresenter player = new PlayerPresenter(
                simPalet.getPlayerView(), 
                runner,
                onReset
                );

        RootPanel.get().add(simPalet);
    }

}
