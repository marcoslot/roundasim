//////////////////////////////////////////////////
// JIST (Java In Simulation Time) Project
// Timestamp: <RadioNoiseAdditive.java Tue 2004/04/13 18:16:53 barr glenlivet.cs.cornell.edu>
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
 * <code>RadioNoiseAdditive</code> implements a radio with an additive noise
 * model.
 * 
 * @author Rimon Barr &lt;barr+jist@cs.cornell.edu&rt;
 * @version $Id: RadioNoiseAdditive.java,v 1.26 2004-11-19 15:55:34 barr Exp $
 * @since SWANS1.0
 */

public class RadioNoiseAdditive extends RadioNoise {
	// ////////////////////////////////////////////////
	// constants
	//

	/** signal-to-noise error model constant. */
	public static final byte SNR = 0;

	/** bit-error-rate error model constant. */
	public static final byte BER = 1;

	// ////////////////////////////////////////////////
	// locals
	//

	//
	// properties
	//

	/**
	 * radio type: SNR or BER.
	 */
	protected byte type;

	/**
	 * threshold signal-to-noise ratio.
	 */
	protected float thresholdSNR;

	/**
	 * bit-error-rate table.
	 */
	protected BERTable ber;

	//
	// state
	//

	/**
	 * total signal power.
	 */
	protected double totalPower_mW;

	final Clock clock;
	final Scheduler scheduler;

	// ////////////////////////////////////////////////
	// initialize
	//

	/**
	 * Create new radio with additive noise model.
	 * 
	 * @param id
	 *            radio identifier
	 * @param shared
	 *            shared radio properties
	 */
	public RadioNoiseAdditive(int id, RadioInfo.RadioInfoShared shared,
			Clock globalClock, Scheduler globalScheduler) {
		this(id, shared, (float) Constants.SNR_THRESHOLD_DEFAULT, globalClock,
				globalScheduler);
	}

	/**
	 * Create a new radio with additive noise model.
	 * 
	 * @param id
	 *            radio identifier
	 * @param shared
	 *            shared radio properties
	 * @param snrThreshold_mW
	 *            threshold signal-to-noise ratio
	 */
	public RadioNoiseAdditive(int id, RadioInfo.RadioInfoShared shared,
			float snrThreshold_mW, Clock globalClock, Scheduler globalScheduler) {
		super(id, shared, globalClock, globalScheduler);
		this.type = SNR;
		this.thresholdSNR = snrThreshold_mW;
		totalPower_mW = radioInfo.shared.background_mW;
		if (totalPower_mW > radioInfo.shared.sensitivity_mW)
			mode = Constants.RADIO_MODE_SENSING;
		this.clock = globalClock;
		this.scheduler = globalScheduler;
	}

	/**
	 * Create a new radio with additive noise model.
	 * 
	 * @param id
	 *            radio identifier
	 * @param shared
	 *            shared radio properties
	 * @param ber
	 *            bit-error-rate table
	 */
	public RadioNoiseAdditive(int id, RadioInfo.RadioInfoShared shared,
			BERTable ber, Clock globalClock, Scheduler globalScheduler) {
		super(id, shared, globalClock, globalScheduler);
		this.type = BER;
		this.ber = ber;
		totalPower_mW = radioInfo.shared.background_mW;
		if (totalPower_mW > radioInfo.shared.sensitivity_mW)
			mode = Constants.RADIO_MODE_SENSING;
		this.clock = globalClock;
		this.scheduler = globalScheduler;
	}

	// ////////////////////////////////////////////////
	// accessors
	//

	/**
	 * Register a bit-error-rate table.
	 * 
	 * @param ber
	 *            bit-error-rate table
	 */
	public void setBERTable(BERTable ber) {
		this.ber = ber;
	}

	// ////////////////////////////////////////////////
	// reception
	//

	// RadioInterface interface
	/** {@inheritDoc} */
	public void receive(final Message msg, final Double powerObj_mW,
			final Long durationObj) {
		final double power_mW = powerObj_mW.doubleValue();
		final long duration = durationObj.longValue();
		switch (mode) {
		case Constants.RADIO_MODE_IDLE:
			if (power_mW >= radioInfo.shared.threshold_mW
					&& power_mW >= totalPower_mW * thresholdSNR) {
				lockSignal(msg, power_mW, duration);
				setMode(Constants.RADIO_MODE_RECEIVING);
			} else if (totalPower_mW + power_mW > radioInfo.shared.sensitivity_mW) {
				setMode(Constants.RADIO_MODE_SENSING);
			}
			break;
		case Constants.RADIO_MODE_SENSING:
			if (power_mW >= radioInfo.shared.threshold_mW
					&& power_mW >= totalPower_mW * thresholdSNR) {
				lockSignal(msg, power_mW, duration);
				setMode(Constants.RADIO_MODE_RECEIVING);
			}
			break;
		case Constants.RADIO_MODE_RECEIVING:
			if (power_mW > signalPower_mW
					&& power_mW >= totalPower_mW * thresholdSNR) {
				lockSignal(msg, power_mW, duration);
				setMode(Constants.RADIO_MODE_RECEIVING);
			} else if (type == SNR
					&& signalPower_mW < (totalPower_mW - signalPower_mW + power_mW)
							* thresholdSNR) {
				unlockSignal();
				setMode(Constants.RADIO_MODE_SENSING);
			}
			break;
		case Constants.RADIO_MODE_TRANSMITTING:
			break;
		case Constants.RADIO_MODE_SLEEP:
			break;
		default:
			throw new RuntimeException("unknown radio mode");
		}
		// cumulative signal
		signals++;
		totalPower_mW += power_mW;
		// schedule an endReceive
		scheduler.schedule(onEndReceive(powerObj_mW), clock.getNanos()
				+ duration);
	} // function: receive

	private Runnable onEndReceive(final double powerObj_mW) {
		return new Runnable() {
			public void run() {
				self.endReceive(powerObj_mW);
			}
		};
	}

	// RadioInterface interface
	/** {@inheritDoc} */
	public void endReceive(Double powerObj_mW) {
		final double power_mW = powerObj_mW.doubleValue();
		// cumulative signal
		signals--;
		totalPower_mW = signals == 0 ? radioInfo.shared.background_mW
				: totalPower_mW - power_mW;
		switch (mode) {
		case Constants.RADIO_MODE_RECEIVING:
			if (clock.getNanos() == signalFinish) {
				boolean dropped = false;
				dropped |= type == BER
						&& totalPower_mW > 0
						&& ber.shouldDrop(signalPower_mW / totalPower_mW,
								8 * signalBuffer.getSize());
				if (!dropped) {
					this.macEntity.receive(signalBuffer);
				}
				unlockSignal();
				setMode(totalPower_mW >= radioInfo.shared.sensitivity_mW ? Constants.RADIO_MODE_SENSING
						: Constants.RADIO_MODE_IDLE);
			}
			break;
		case Constants.RADIO_MODE_SENSING:
			if (totalPower_mW < radioInfo.shared.sensitivity_mW)
				setMode(Constants.RADIO_MODE_IDLE);
			break;
		case Constants.RADIO_MODE_TRANSMITTING:
			break;
		case Constants.RADIO_MODE_IDLE:
			break;
		case Constants.RADIO_MODE_SLEEP:
			break;
		default:
			throw new RuntimeException("unknown radio mode");
		}
	} // function: endReceive

} // class: RadioNoiseAdditive

