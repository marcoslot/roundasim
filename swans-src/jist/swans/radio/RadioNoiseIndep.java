//////////////////////////////////////////////////
// JIST (Java In Simulation Time) Project
// Timestamp: <RadioNoiseIndep.java Tue 2004/04/20 09:00:20 barr pompom.cs.cornell.edu>
//

// Copyright (C) 2004 by Cornell University
// All rights reserved.
// Refer to LICENSE for terms and conditions of use.

package jist.swans.radio;

import jist.swans.Clock;
import jist.swans.Constants;
import jist.swans.Scheduler;
import jist.swans.misc.Message;

/** 
 * <code>RadioNoiseIndep</code> implements a radio with an independent noise model.
 *
 * @author Rimon Barr &lt;barr+jist@cs.cornell.edu&rt;
 * @version $Id: RadioNoiseIndep.java,v 1.25 2004-11-05 03:38:34 barr Exp $
 * @since SWANS1.0
 */

public final class RadioNoiseIndep extends RadioNoise
{

	//////////////////////////////////////////////////
	// locals
	//

	/**
	 * threshold signal-to-noise ratio.
	 */
	protected double thresholdSNR;

	final Clock clock;
	final Scheduler scheduler;

	//////////////////////////////////////////////////
	// initialize
	//

	/**
	 * Create new radio with independent noise model.
	 *
	 * @param id radio identifier
	 * @param sharedInfo shared radio properties
	 */
	public RadioNoiseIndep(int id, RadioInfo.RadioInfoShared sharedInfo, Clock globalClock, Scheduler globalScheduler)
	{
		this(id, sharedInfo, Constants.SNR_THRESHOLD_DEFAULT, globalClock, globalScheduler);
	}

	/**
	 * Create new radio with independent noise model.
	 *
	 * @param id radio identifier
	 * @param sharedInfo shared radio properties
	 * @param thresholdSNR threshold signal-to-noise ratio
	 */
	public RadioNoiseIndep(int id, RadioInfo.RadioInfoShared sharedInfo, double thresholdSNR, Clock globalClock, Scheduler globalScheduler)
	{
		super(id, sharedInfo, globalClock, globalScheduler);
		setThresholdSNR(thresholdSNR);
		this.clock = globalClock;
		this.scheduler = globalScheduler;
	}

	//////////////////////////////////////////////////
	// accessors
	//

	/**
	 * Set signal-to-noise ratio.
	 *
	 * @param snrThreshold threshold signal-to-noise ratio
	 */
	public void setThresholdSNR(double snrThreshold)
	{
		this.thresholdSNR = snrThreshold;
	}

	//////////////////////////////////////////////////
	// reception
	//

	// RadioInterface interface
	/** {@inheritDoc} */
	public void receive(Message msg, Double powerObj_mW, Long durationObj)
	{
		final double power_mW = powerObj_mW.doubleValue();
		final long duration = durationObj.longValue();
		// ignore if below sensitivity
		if(power_mW < radioInfo.shared.sensitivity_mW) return;
		// discard message if below threshold
		if(power_mW < radioInfo.shared.threshold_mW 
				|| power_mW < radioInfo.shared.background_mW * thresholdSNR) msg = null;
		switch(mode)
		{
		case Constants.RADIO_MODE_IDLE:
			if(msg!=null) setMode(Constants.RADIO_MODE_RECEIVING);
			lockSignal(msg, power_mW, duration);
			break;
		case Constants.RADIO_MODE_RECEIVING:
			if(power_mW >= radioInfo.shared.threshold_mW
					&&  power_mW > signalPower_mW*thresholdSNR)
			{
				lockSignal(msg, power_mW, duration);
			}
			break;
		case Constants.RADIO_MODE_TRANSMITTING:
			break;
		case Constants.RADIO_MODE_SLEEP:
			break;
		default:
			throw new RuntimeException("invalid radio mode: "+mode);
		}
		// increment number of incoming signals
		signals++;
		// schedule an endReceive
	    scheduler.schedule(onEndReceive(powerObj_mW), clock.getNanos() + duration);
	}

	  private Runnable onEndReceive(final double powerObj_mW) {
		  return new Runnable() {
			  public void run() {
				  self.endReceive(powerObj_mW);
			  }
		  };
	  }
	// RadioInterface interface
	/** {@inheritDoc} */
	public void endReceive(final Double powerObj_mW)
	{
		if(mode==Constants.RADIO_MODE_SLEEP) return;
		signals--;
		if(mode==Constants.RADIO_MODE_RECEIVING)
		{
			if(signalBuffer!=null && clock.getNanos()==signalFinish)
			{
				this.macEntity.receive(signalBuffer);
				unlockSignal();
			}
			if(signals==0) setMode(Constants.RADIO_MODE_IDLE);
		}
	}

} // class: RadioNoiseIndep

