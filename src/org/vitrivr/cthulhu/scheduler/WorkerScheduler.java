package org.vitrivr.cthulhu.scheduler;

import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.jobs.JobFactory;
import org.vitrivr.cthulhu.jobs.JobQueue;
import org.vitrivr.cthulhu.jobs.JobExecutor;

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
    int capacity = 1; // Default capacity - to be changed later
    Hashtable<String,JobExecutor> jobExecutors; // List of job executors
    public WorkerScheduler(Properties props) {
        super(props);
        coordinator = new Worker(props.getProperty("hostAddress"),
                                 Integer.parseInt(props.getProperty("hostPort")));
        int port = Integer.parseInt(props.getProperty("port"));
        informCoordinator(props.getProperty("address"),
                          Integer.parseInt(props.getProperty("port")));
    }
    /*
     * This function assumes that all the checks have been done, and it is
     * safe to run a job in a new thread. (i.e. assumes that the capacity allows
     * to run one more job).
     */
    void executeNextJob() {
        if(jq.size() == 0) return;
        Job nextJob = jq.pop();
        JobExecutor je = new JobExecutor((job)-> finalizeJobExecution(job), nextJob);
        jobExecutors.put(nextJob.getName(), je);
        Thread t = new Thread(je);
        t.run();
    }
    void finalizeJobExecution(Job j) {
        try {
            conn.putJob(j, coordinator);
            // After reporting the result of the job, we remove it from the job table
            // TODO: Pick up of job results
            jt.remove(j.getName());
        } catch (Exception e) {
            // TODO: Need a way to deal with failure to contact the coordinator. Should perhaps suicide.
            lg.error("Unable to report result of job "+j.getName()+" to coordinator "+coordinator.getId());
        }
        jobExecutors.remove(j.getName());
        schedulerTick();
    }
    @Override
    protected void schedulerTick() {
        if(jobExecutors.size() < capacity) executeNextJob();
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
