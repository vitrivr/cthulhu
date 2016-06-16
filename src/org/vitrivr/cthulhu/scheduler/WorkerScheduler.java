package org.vitrivr.cthulhu.scheduler;

import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.jobs.JobFactory;
import org.vitrivr.cthulhu.jobs.JobQueue;

import org.vitrivr.cthulhu.worker.Worker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.util.List;
import java.util.Hashtable;
import java.util.stream.*;

import java.util.Properties;

public class WorkerScheduler extends CthulhuScheduler {
    Worker coordinator;
    public WorkerScheduler(Properties props) {
        super(props);
        coordinator = new Worker(props.getProperty("hostAddress"),
                                 Integer.parseInt(props.getProperty("hostPort")));
        int port = Integer.parseInt(props.getProperty("port"));
        informCoordinator(props.getProperty("address"),
                          Integer.parseInt(props.getProperty("port")));
    }
    void informCoordinator(String workerAddress, int workerPort) {
        lg.info("Registering worker with coordinator "+coordinator.getId());
        try {
            conn.postWorker(coordinator,workerAddress,workerPort);
        } catch (Exception e) {
            lg.error("Failed to register with coordinator: "+e.toString());
            System.exit(1); // Exiting. Need a coordinator or nothing can be done.
        }
    }
}
