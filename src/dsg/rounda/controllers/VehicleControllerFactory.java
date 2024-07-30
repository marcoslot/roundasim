/**
 * 
 */
package dsg.rounda.controllers;

import dsg.rounda.model.VehicleCapabilities;

/**
 * @author slotm
 *
 */
public interface VehicleControllerFactory {
    VehicleController createController(VehicleCapabilities capabilities);
}
