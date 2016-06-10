package org.vitrivr.cthulhu.scheduler;

import org.vitrivr.cthulhu.runners.CthulhuRunner.RunnerType;

import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SchedulerFactory {
    private Logger LOGGER;
    public SchedulerFactory() {
        LOGGER = LogManager.getLogger("r.sf");
    }
    public CthulhuScheduler createScheduler(RunnerType type, Properties props) {
        if(type == RunnerType.WORKER) {
            LOGGER.info("Creating a WorkerScheduler");
            return new WorkerScheduler(props);
        }
        if(type == RunnerType.COORDINATOR) {
            LOGGER.info("Creating a CoordinatorScheduler");
            return new CoordinatorScheduler(props);
        }
        return null;
    }
}
