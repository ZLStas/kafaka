package com.some.kafaka.service.kafka;//package com.diplome.demo.service.kafka;
//
//
//import lombok.NonNull;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.kafka.streams.KafkaStreams;
//import org.apache.kafka.streams.StreamsBuilder;
//import org.apache.kafka.streams.StreamsConfig;
//import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.PreDestroy;
//import java.util.Properties;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicReference;
//
//import static com.diplome.demo.config.KafkaConfig.KafkaWorkersProperties.KafkaWorkerProperties;
//import static com.diplome.demo.service.kafka.KafkaWorkerUtils.stepToString;
//
//@Slf4j
//public abstract class KafkaWorker {
//
//  private final static int DEBUG_LOG_WIDTH = 80;
//  private final static String DEBUG_LOG_SEPARATOR = "\n" + StringUtils.center("", DEBUG_LOG_WIDTH, "~") + "\n";
//
//  protected final String applicationId;
//
//  protected final KafkaProperties kafkaProperties;
//  protected final KafkaWorkerProperties workerProperties;
//
//  private final Properties streamsProperties;
//
//  private final Thread watchdogThread;
//
//  private final AtomicBoolean shutdownCalled = new AtomicBoolean(false);
//  private final AtomicBoolean restartNeeded = new AtomicBoolean(false);
//
//  private final AtomicReference<KafkaStreams> kafkaStreams = new AtomicReference<>();
//
//  public KafkaWorker(@NonNull KafkaProperties kafkaProperties, @NonNull KafkaWorkerProperties workerProperties) {
//    this.kafkaProperties = kafkaProperties;
//    this.workerProperties = workerProperties;
//
//    // get unique application identifier
//    this.applicationId = workerProperties.getApplicationId();
//
//    // initialize kafka properties form supplied configuration
//    Properties properties = new Properties();
//
//    // general kafka connection properties
//    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
//    properties.put(StreamsConfig.CLIENT_ID_CONFIG, applicationId + "-client");
//    properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, String.join(",", kafkaProperties.getBootstrapServers()));
//    properties.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, workerProperties.getReplicationFactor());
//    properties.put(StreamsConfig.STATE_DIR_CONFIG, workerProperties.getStateDir());
//    properties.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, workerProperties.getStreamThreads());
//
//    // disable record cache in debug mode
//    if (workerProperties.isDebug()) {
//      properties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
//    }
//
//    // save kafka properties for kafka streams initialization
//    this.streamsProperties = properties;
//
//    // initialize watchdog thread for restarting kafka streams instance on processing failures
//    this.watchdogThread = new Thread(this::restartKafkaStreams, applicationId + "-watchdog-thread");
//
//    // log brief summary for this worker
//    log.info("New kafka worker {} created using following configuration: {}", getClass().getSimpleName(),
//             workerProperties);
//  }
//
//  @PostConstruct
//  public void start() {
//    // start watchdog thread
//    startWatchdogThread();
//
//    // start kafka streams
//    startKafkaStreams();
//  }
//
//  @PreDestroy
//  public void stop() {
//    // stop watchdog thread
//    stopWatchdogThread();
//
//    // stop kafka streams
//    stopKafkaStreams();
//  }
//
//  protected abstract void createTopology(StreamsBuilder builder);
//
//  protected void logStep(String step, Object stepOutput, Object... stepInputs) {
//    // provide verbose summary for step execution if debug mode is on
//    if (workerProperties.isDebug()) {
//      log.info(stepToString(applicationId, step, stepOutput, stepInputs));
//    }
//  }
//
//  private void handleUncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
//    // print exception details
//    log.warn("Uncaught exception thrown in stream processing thread '{}', this will cause the thread to die. " +
//             "The event which was being processed when the thread died will be re-tried, no data will be lost. " +
//             "The KafkaStreams instance '{}' will be restarted.", thread.getName(), applicationId, ex);
//
//    // restart when any of the threads dies
//    restartNeeded.set(true);
//
//    // notify watchdog thread of restart request
//    synchronized (restartNeeded) {
//      restartNeeded.notifyAll();
//    }
//  }
//
//  private KafkaStreams createKafkaStreams() {
//    final StreamsBuilder builder = new StreamsBuilder();
//
//    createTopology(builder);
//
//    return new KafkaStreams(builder.build(), streamsProperties);
//  }
//
//  private void restartKafkaStreams() {
//    // loop until worker has been shut down by Spring
//    while (!shutdownCalled.get()) {
//      try {
//        // wait until this worker requires restart
//        while (!restartNeeded.get() && !shutdownCalled.get()) {
//          synchronized (restartNeeded) {
//            try {
//              restartNeeded.wait();
//            } catch (InterruptedException ignore) {
//              // ignore
//            }
//          }
//        }
//
//        // don't try to restart the processor if we've been shut down
//        if (!shutdownCalled.get()) {
//          log.info("Restarting KafkaStreams instance '{}'...", applicationId);
//
//          // eagerly shut down existing streams instance
//          stopKafkaStreams();
//
//          // clear restart flag as old threads might not interfere with it after instance shutdown
//          restartNeeded.set(false);
//
//          // start fresh Kafka Streams instance
//          startKafkaStreams();
//
//          log.info("Restarting KafkaStreams instance '{}'... DONE!", applicationId);
//        }
//      } catch (Exception ex) {
//        log.warn("Unexpected exception occurred in watchdog thread for worker '{}'", applicationId, ex);
//      }
//    }
//  }
//
//  private synchronized void startKafkaStreams() {
//    // perform clean-up up front just to be sure
//    stopKafkaStreams();
//
//    log.info("Starting KafkaStreams instance '{}'...", applicationId);
//
//    // create and initialize new kafka streams instance
//    KafkaStreams instance = createKafkaStreams();
//
//    // customize exception handling
//    instance.setUncaughtExceptionHandler(this::handleUncaughtException);
//
//    // start processing
//    instance.start();
//
//    // update local state
//    kafkaStreams.set(instance);
//
//    log.info("Starting KafkaStreams instance '{}'... DONE!", applicationId);
//  }
//
//  private synchronized void stopKafkaStreams() {
//    if (kafkaStreams.get() != null) {
//      log.info("Stopping KafkaStreams instance '{}'...", applicationId);
//
//      // close kafka streams instance
//      kafkaStreams.get().close(workerProperties.getCloseTimeoutMs(), TimeUnit.MILLISECONDS);
//
//      // clear local state
//      kafkaStreams.set(null);
//
//      log.info("Stopping KafkaStreams instance '{}'... DONE!", applicationId);
//    }
//  }
//
//  private void startWatchdogThread() {
//    log.info("Starting watchdog thread '{}' for KafkaStreams instance '{}'...", watchdogThread.getName(),
//             applicationId);
//
//    // call thread start explicitly
//    watchdogThread.start();
//
//    log.info("Starting watchdog thread '{}' for KafkaStreams instance '{}'... DONE!", watchdogThread.getName(),
//             applicationId);
//  }
//
//  private void stopWatchdogThread() {
//    log.info("Stopping watchdog thread '{}' for KafkaStreams instance '{}'...", watchdogThread.getName(),
//             applicationId);
//
//    // set shutdown flag to stop watchdog thread
//    shutdownCalled.set(true);
//
//    // interrupt watchdog thread to break out of loop
//    watchdogThread.interrupt();
//
//    // wait for watchdog thread termination
//    while (watchdogThread.isAlive()) {
//      try {
//        watchdogThread.join();
//      } catch (InterruptedException ignore) {
//        // ignore
//      }
//    }
//
//    log.info("Stopping watchdog thread '{}' for KafkaStreams instance '{}'... DONE!", watchdogThread.getName(),
//             applicationId);
//  }
//
//}
