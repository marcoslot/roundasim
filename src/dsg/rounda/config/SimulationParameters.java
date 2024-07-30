/**
 * 
 */
package dsg.rounda.config;


/**
 * Constants for standard simulation parameters
 */
public interface SimulationParameters {

    public static final StringParameter OUTPUT_PREFIX = new StringParameter("output-prefix", "");
    public static final StringParameter SCENARIO_NAME = new StringParameter("scenario", "default");
    public static final StringParameter CONTROLLER_NAME = new StringParameter("controller", "default");
    public static final StringParameter RUN_ID = new StringParameter("runid");

    public static final StringSetParameter LOG_TAGS = new StringSetParameter("log-tags");
    
    public static final LongParameter SEED = new LongParameter("seed");
    public static final LongParameter DURATION = new LongParameter("duration");
    public static final RepeatParameter REPEAT = new RepeatParameter("repeat", 1);

    public static final DoubleParameter MAX_VELOCITY = new DoubleParameter("max-velocity", 35.); // m/s 
    public static final DoubleParameter DESIRED_VELOCITY = new DoubleParameter("desired-velocity", 20.5); // m/s 
    
    public static final DoubleParameter SPAWN_RATE = new DoubleParameter("spawn-rate", 400.); //  vehicles per minute
    public static final DoubleParameter TRANSMISSION_POWER = new DoubleParameter("transmission-power", 10.); // mW
    public static final DoubleParameter LIDAR_RANGE = new DoubleParameter("lidar-range", 80.); // meter
    public static final DoubleParameter POSITION_INACCURACY = new DoubleParameter("position-inaccuracy", 0.); // meter
    public static final StringParameter SCENARIO_TEXT_PARAM = new StringParameter("scenario-text");
    public static final StringParameter SCENARIO_FILE_PARAM = new StringParameter("scenario-file");

	public static final DoubleParameter DESIRED_TIME_HEADWAY = new DoubleParameter("desired-headway", 1.1); // seconds
	public static final DoubleParameter SLOW_VEHICLE_FRACTION = new DoubleParameter("slow-vehicle-fraction", 0.0); // fraction of vehicles that are slow
	public static final DoubleParameter SLOW_VEHICLE_FACTOR = new DoubleParameter("slow-vehicle-factor", 0.7); // * desired-velocity
	public static final DoubleParameter MIN_LANE_CHANGE_TIME_HEADWAY = new DoubleParameter("lane-change-headway", 1.5); // seconds

    public static final String RUN_SET_ID = "run-set-id";

    public static final LongParameter STATS_GRACE_PERIOD = new LongParameter("stats-grace-period", 0L); // seconds
}
