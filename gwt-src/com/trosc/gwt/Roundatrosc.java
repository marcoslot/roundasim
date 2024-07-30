/**
 * 
 */
package com.trosc.gwt;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;

import dsg.rounda.Constants;
import dsg.rounda.SimController;
import dsg.rounda.config.RunConfig;
import dsg.rounda.config.SimulationParameters;
import dsg.rounda.logging.Event;
import dsg.rounda.logging.EventHandler;
import dsg.rounda.logging.EventLog;

/**
 * @author slotm
 *
 */
public class Roundatrosc implements EntryPoint, SimulationParameters {

    static final int STEP_SIZE = 1; // seconds
    
    @Override
    public native void onModuleLoad() /*-{
        sys.main($entry(this.@com.trosc.gwt.Roundatrosc::start()));
        sys.ready();
    }-*/;

    void start() {
        RunConfig config = new RunConfig();
        config.set(SCENARIO_FILE_PARAM, "wayway.txt");
        config.set(CONTROLLER_NAME, "vertigo");
        config.set(SCENARIO_NAME, "builder");

        final SimController controller = new SimController(
                config,
                new FileIOTrosc());

        controller.init(sim(controller));
    }
    
    private Runnable sim(
            final SimController controller) {
        
        return new Runnable() {
            public void run() {
                controller.getRun().getEventLog().addHandler(EventLog.acceptTag("vehicle-stats"), printEvent());
                controller.getRun().getEventLog().addHandler(EventLog.acceptTag("sim-stats"), printEvent());
                controller.setActive(true);
                
                Scheduler.get().scheduleFixedDelay(step(controller), 10);
            }
        };
    }
    
    protected RepeatingCommand step(final SimController controller) {
        return new RepeatingCommand() {
            int clock = 0;
            
            @Override
            public boolean execute() {
                clock += STEP_SIZE;
                controller.step(STEP_SIZE*Constants.SECONDS);
                
                if(clock >= 300) {
                    controller.finish();
                    exit();
                    return false;
                } else {
                    return true;
                }
            }
        };
    }

    private EventHandler printEvent() {
        return new EventHandler() {
            public void event(Event event) {
                printf("%s %s: %s %s\r\n", event.getSimTime(), event.getSource(), event.getTag(), event.getMessage());
            }
        };
    }

    public static void printf(Object... args) {
        JSArray jsArgs = JSArray.create();
        
        for(int i = 0; i < args.length; i++) {
            Object arg = args[i];

            if(arg instanceof Byte) {
                jsArgs.pushByte((Byte) arg);
            } else if(arg instanceof Character) {
                jsArgs.pushChar((Character) arg);
            } else if(arg instanceof Short) {
                jsArgs.pushShort((Short) arg);
            } else if(arg instanceof Integer) {
                jsArgs.pushInt((Integer) arg);
            } else if(arg instanceof String) {
                jsArgs.pushString((String) arg);
            } else if(arg instanceof Boolean) {
                jsArgs.pushBoolean((Boolean) arg);
            } else {
                jsArgs.pushString(arg.toString());
            }
        }

        _printf(jsArgs);
    }
    
    static native void _printf(JSArray args) /*-{
        sys.printf.apply(this, args); 
    }-*/;

    native void exit() /*-{
        sys.exit(0);
    }-*/;
    
}
