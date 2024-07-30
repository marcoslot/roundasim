/**
 * 
 */
package dsg.rounda.swans;

import jist.swans.Constants;
import jist.swans.field.Fading;
import jist.swans.field.Field;
import jist.swans.field.Mobility;
import jist.swans.field.PathLoss;
import jist.swans.mac.Mac802_11;
import jist.swans.misc.Location;
import jist.swans.misc.Util;
import jist.swans.radio.RadioInfo;
import jist.swans.radio.RadioInfo.RadioInfoShared;
import jist.swans.radio.RadioNoiseAdditive;
import dsg.rounda.SimRun;
import dsg.rounda.config.SimulationParameters;
import dsg.rounda.logging.EventLog;
import dsg.rounda.model.Network;
import dsg.rounda.model.NetworkDeliveryInterface;
import dsg.rounda.model.NetworkTransmissionInterface;

/**
 * @author slotm
 *
 */
public class SwansNetwork implements Network, SimulationParameters {

    final RadioInfoShared radioInfo;
    final Field field;
    final SwansClock clock;
    final SwansScheduler scheduler;
    final EventLog log;
    
    /**
     * 
     */
    public SwansNetwork(SimRun run) {
        this.radioInfo = RadioInfo.createShared(
                5900000000., 
                6000000,
                run.getConfig().get(TRANSMISSION_POWER), 
                Constants.GAIN_DEFAULT,
                Util.fromDB(Constants.SENSITIVITY_DEFAULT), 
                Util.fromDB(Constants.THRESHOLD_DEFAULT),
                Constants.TEMPERATURE_DEFAULT, 
                Constants.TEMPERATURE_FACTOR_DEFAULT, 
                Constants.AMBIENT_NOISE_DEFAULT
        );
        this.log = run.getEventLog();
        this.clock = new SwansClock(run.getClock());
        this.scheduler = new SwansScheduler(run.getScheduler());
        this.field = new Field(
            new SwansWorldState(run.getWorld()),
            new Fading.Rayleigh(),
            new PathLoss.TwoRay(), //channel model
            new Mobility.Static(),
            Constants.PROPAGATION_LIMIT_DEFAULT,
            clock
        );
    }

    /* (non-Javadoc)
     * @see dsg.rounda.model.Network#addAdapter(int, dsg.rounda.model.NetworkDeliveryInterface)
     */
    @Override
    public NetworkTransmissionInterface addAdapter(NetworkDeliveryInterface adapter) {
        int id = adapter.getId();
        
        RadioNoiseAdditive radioInterface = new RadioNoiseAdditive(id, radioInfo, clock, scheduler);
        
        RadioInfo radioInfo = radioInterface.getRadioInfo();
        SwansNetInterface netInterface = new SwansNetInterface(adapter);
        
        Mac802_11 mac = new Mac802_11(id, clock, scheduler, radioInfo);
        mac.setNetEntity(netInterface, (byte) 0);
        mac.setRadioEntity(radioInterface);

        radioInterface.setFieldEntity(field);
        radioInterface.setMacEntity(mac);
        
        netInterface.setMacInterface(mac);
        
        field.addRadio(radioInfo, radioInterface, new Location.Location2D(0f,0f));
        
        return netInterface;
    }

    /* (non-Javadoc)
     * @see dsg.rounda.model.Network#removeAdapter(int)
     */
    @Override
    public void removeAdapter(int id) {
        field.delRadio(id);
    }

}
