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

import java.lang.InterruptedException;

import java.util.List;
import java.util.Hashtable;
import java.util.stream.*;

import java.util.Properties;

public class WorkerScheduler extends CthulhuScheduler {
    Worker coordinator;
    int capacity = 1; // Default capacity - to be changed later
    Hashtable<String,Thread> jobExecutors; // List of job executors
    public WorkerScheduler(Properties props) {
        this(props,false);
    }
    public WorkerScheduler(Properties props, boolean standAlone) {
        super(props);
        jobExecutors = new Hashtable<String,Thread>();
        if(!standAlone) {
            coordinator = new Worker(props != null ? props.getProperty("hostAddress") : "127.0.0.1",
                                     props != null ? Integer.parseInt(props.getProperty("hostPort")) : 8082);
            int port = Integer.parseInt(props != null ? props.getProperty("port") : "8081");
            informCoordinator(props != null ? props.getProperty("address") : "127.0.0.1", port);
        }
    }
    /*
     * This function assumes that all the checks have been done, and it is
     * safe to run a job in a new thread. (i.e. assumes that the capacity allows
     * to run one more job).
     */
    void executeNextJob() {
        if(jq.size() == 0) return;
        Job nextJob = jq.pop();
        lg.info("Starting to execute job {}",nextJob.getName());

        /* Job is executed simply inside a thread. Unless we need 
           more sophisticaded execution logic, we'll use a  simple 
           lambda as the Runner interface passed to Thread t */
        Thread t = new Thread(()-> {
                int result = 0;
                result = nextJob.execute();
                String strResult = result == 0 ? "SUCCESS" : "FAILURE";
                strResult = nextJob.wasInterrupted() ? "INTERRUPTED" : strResult;
                lg.info("Job execution of job {} finalized with {}",
                        nextJob.getName(),strResult);
                finalizeJobExecution(nextJob);
            });
        jobExecutors.put(nextJob.getName(), t);
        t.start();
    }
    void finalizeJobExecution(Job j) {
        // TODO THIS MUST EXECUTE EVEN IF THE JOB WAS DELETED AND STOPPED.
        try {
            lg.info("Reporting result of job {} to coordinator.",j.getName());
            if(coordinator != null) conn.putJob(j, coordinator);
            // After reporting the result of the job, we remove it from the job table
            // TODO: Pick up of job results
            jt.remove(j.getName());
        } catch (Exception e) {
            // TODO: Need a way to deal with failure to contact the coordinator. Should perhaps suicide.
            lg.error("Unable to report result of job {} to coordinator {}: {}",
                     j.getName(),
                     coordinator != null ? coordinator.getId() : "STANDALONE",
                     e.toString());
        }
        jobExecutors.remove(j.getName());
        schedulerTick();
    }
    @Override
    protected void deleteJobInt(Job job, boolean force) throws Exception {
        if(force == false) return ;
        Thread exec = jobExecutors.get(job.getName());
        if(exec == null) {
            lg.warn("Job {} has finished running before it was deleted.",job.getName());
        }
        lg.info("Sending interruption to job executor of {}",job.getName());
        exec.interrupt();
        // TODO - can this fail?
        // If all went fine, then we remove it!
        jt.remove(job.getName());
        jobExecutors.remove(job.getName());
    }
    @Override
    protected void schedulerTick() {
        if(jobExecutors.size() < capacity) executeNextJob();
    }
    void informCoordinator(String workerAddress, int workerPort) {
        lg.info("Registering worker with coordinator {}",coordinator.getId());
        try {
            conn.postWorker(coordinator,workerAddress,workerPort);
        } catch (Exception e) {
            lg.error("Failed to register with coordinator: {}",e.toString());
            System.exit(1); // Exiting. Need a coordinator or nothing can be done.
        }
    }
}
