package org.vitrivr.cthulhu.scheduler;

import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cthulhu.runners.CthulhuRunner.RunnerType;

public class SchedulerFactory {

  private static final Logger LOGGER = LogManager.getLogger("r.sf");

  /**
   * Constructs and returns a {@link CthulhuScheduler} depending on the type of runner the method
   * executer requests as defined by the {@link RunnerType} parameter.
   *
   * @param type the type of scheduler to generate
   * @param props the properties to pass to the scheduler
   * @return a constructed scheduler or null if the given runner type is unsupported
   * @see Properties
   */
  public CthulhuScheduler createScheduler(RunnerType type, Properties props) {
    if (type == RunnerType.WORKER) {
      LOGGER.info("Creating a WorkerScheduler");
      return new WorkerScheduler(props);
    }
    if (type == RunnerType.COORDINATOR) {
      LOGGER.info("Creating a CoordinatorScheduler");
      return new CoordinatorScheduler(props);
    }
    return null;
  }
}
