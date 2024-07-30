/**
 * 
 */
package dsg.rounda;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import dsg.rounda.config.CommandLineMultiRunConfig;
import dsg.rounda.config.MultiRunConfig;
import dsg.rounda.config.RunConfig;
import dsg.rounda.config.SimulationParameters;
import dsg.rounda.io.FileIOJRE;
import dsg.rounda.logging.Event;
import dsg.rounda.logging.EventFilter;
import dsg.rounda.logging.EventHandler;
import dsg.rounda.logging.EventLog;
import dsg.rounda.stats.RunSetStats;
import dsg.rounda.stats.Statistic;

/**
 * Run round-a-sim without a GUI
 */
public class SimHeadless implements SimulationParameters {

	public static final void main(String[] args) throws IOException, InterruptedException {
		CommandLineMultiRunConfig configs = new CommandLineMultiRunConfig(SimHeadless.class, args);
		new SimHeadless(configs).run();
	}

	private static final int NUM_WORKERS =  Runtime.getRuntime().availableProcessors();;
	private static final long DEFAULT_RUNTIME = 3600*Constants.SECONDS;
	private static final DateFormat DF =
			new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_");

	final MultiRunConfig configs;
	final Map<String,RunSetStats> multiRunStats;
	final EventLog globalLog;
	final boolean printMultiRunStats;

	public SimHeadless(CommandLineMultiRunConfig configs) {
		this.configs = configs;
		this.multiRunStats = new LinkedHashMap<String,RunSetStats>();
		this.globalLog = new EventLog();
		this.printMultiRunStats = configs.isPrintMultiRunStats();
	}

	public void run() throws IOException, InterruptedException {
		String prefix = DF.format(new Date());

		BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
		ExecutorService workers = new ThreadPoolExecutor(NUM_WORKERS, NUM_WORKERS, 60, TimeUnit.SECONDS, workQueue);

		PrintWriter multiRunWriter = null;
		
		if(printMultiRunStats) {
			final String logFileName = prefix + "overall.log";
			final PrintWriter writer = multiRunWriter = new PrintWriter(new FileWriter(logFileName));

			globalLog.addHandler(
					EventLog.acceptSourceType(RunSetStats.class),
					new EventHandler() {
						public void event(Event event) {
							Map<String,Statistic> stats = (Map<String,Statistic>) event.getMessage();
							List<String> statKeys = new ArrayList<String>(stats.keySet());
							Collections.sort(statKeys);

							if(event.getSource().toString() != null) {
							    writer.printf("%s ", event.getSource().toString().replace('=', ' ').replace(',', ' '));
							}
							
							for(String statKey : statKeys) {
								Statistic stat = stats.get(statKey);
								writer.printf("%s %s ", statKey, stat.stringValue());
							}
							
							writer.println();
						}
					}
			);
		}

		for(RunConfig config : configs.getSequencer()) {
			config.set(OUTPUT_PREFIX, prefix);
			workers.execute(runSimulation(config));
		}

		workers.shutdown();
		workers.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

		// Print multi run stats
		globalLog.log(this, "done", "");
		
		if(multiRunWriter != null) {
			multiRunWriter.close();
		}
	}

	public Runnable runSimulation(final RunConfig config) {
		return new Runnable() {
			public void run() {
				try {
					final SimController controller = new SimController(
							config,
							new FileIOJRE("war"));

					controller.init();

					final String logFileName = config.get(OUTPUT_PREFIX) + config.get(RUN_ID) + ".log";
					final PrintWriter runLogWriter = new PrintWriter(new FileWriter(logFileName));
					final Set<String> logTags = config.get(LOG_TAGS);

					if(printMultiRunStats) {
						RunSetStats runSetStats = getRunSetStats(config.<String>get(RUN_SET_ID));
						runSetStats.addEventLog(controller.getRun().getEventLog());
					}

					controller.getRun().getEventLog().addHandler(
							new EventFilter() {
								public boolean accept(Event evt) {
									return logTags.contains(evt.getTag());
								}
							},
							new EventHandler() {
								public void event(Event event) {
									runLogWriter.printf("%d %s: %s %s\r\n", event.getSimTime(), event.getSource(), event.getTag(), event.getMessage());
								}
							}
					);

					long startTime = System.currentTimeMillis();

					// Simulate an hour        
					controller.setActive(true);
					controller.step(config.getDuration(DEFAULT_RUNTIME));
					controller.finish();

					long endTime = System.currentTimeMillis();

					runLogWriter.printf("%d global: runtime %.1f seconds\n", controller.getRun().getClock().getTime(), (endTime - startTime) / 1000.);
					runLogWriter.close();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	synchronized RunSetStats getRunSetStats(String key) {
		RunSetStats runStats = this.multiRunStats.get(key);

		if(runStats == null) {
			runStats = new RunSetStats(key, globalLog);
			runStats.init();
			multiRunStats.put(key, runStats);
		}

		return runStats;
	}

}
