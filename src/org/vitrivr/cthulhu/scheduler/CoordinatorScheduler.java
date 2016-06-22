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
import java.util.concurrent.ScheduledFuture;

import java.util.List;
import java.util.Map;
import java.util.Hashtable;
import java.util.stream.*;

import java.util.Properties;

public class CoordinatorScheduler extends CthulhuScheduler {
    ScheduledExecutorService executor;
    ScheduledFuture dispatchFuture;
    public CoordinatorScheduler(Properties props) {
        super(props);
        wt = new Hashtable<String,Worker>();
        
        int dispatchDelay = 10;
        if(props != null && props.getProperty("dispatchDelay") != null) {
            dispatchDelay = Integer.parseInt(props.getProperty("dispatchDelay"));
        }
    
        executor = Executors.newScheduledThreadPool(1);
        dispatchFuture = executor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    runDispatch();
                }
            }, dispatchDelay, dispatchDelay, TimeUnit.SECONDS);
    }

    public void stop() {
        if(dispatchFuture != null) dispatchFuture.cancel(true);
        if(executor != null) executor.shutdown();
    }
    public Worker getRunningWorker(Job job) {
        Worker w = wt.entrySet()
            .stream()
            .filter(entry -> entry.getValue().hasJob(job.getName()))
            .map(Map.Entry::getValue)
            .findFirst().orElse(null);
        return w;
    }
    @Override
    protected void updateJobInt(Job job) throws Exception {
        lg.info("Internal update of job {}",job.getName());
        Worker w = getRunningWorker(job);
        if(w == null) throw new Exception("Updated job not in any worker yet");
        w.removeJob(job.getName());
    }
    @Override
    protected void deleteJobInt(Job job, boolean force) throws Exception {
        if(job.isRunning()) {
            // Stop the job in the worker.
            Worker w = getRunningWorker(job);
            if(w == null) {
                // This should never happen
                lg.error("Could not find worker executing job {}. This is an error.",job.getName());
                throw new Exception("Could not find worker executing job "+job.getName()+". This is an error.");
            }
            try {
                conn.deleteJob(job,w,"TRUE");
            } catch (Exception e) {
                lg.error("Failed to stop job in the worker: {}",e.toString());
                throw e;
                // This should not happen unless the worker became lost or unaccessible.
            }
        }
    }
    public void runDispatch() {
        // 1. The first stage of the dispatching cycle is to update jobs that have
        //    finished running

        // 2. The second stage of the dispatching cycle is to dispatch jobs.
        List<Worker> availableWks = getWorkers().stream()
            .filter(w -> w.getCapacity() > w.getJQSize()).collect(Collectors.toList());
        int freeCapacity = availableWks.stream().mapToInt(w-> w.getCapacity() - w.getJQSize()).sum();
        int wCount = 0;
        int jobsDispatched = 0;
        Job nextJob = null;
        if(jq.size() > 0 || freeCapacity > 0) {
            lg.info("Starting the dispatch of {} jobs to {} spots in {} workers.",
                    Integer.toString(jq.size()),Integer.toString(freeCapacity),
                    Integer.toString(availableWks.size()));
        } else {
            lg.trace("Unable to dispatch. Jobs waiting: {}. Available capacity: {}",
                     Integer.toString(jq.size()),Integer.toString(freeCapacity));
        }
        while(freeCapacity > 0 && jq.size() > 0) {
            Worker currWorker = availableWks.get(wCount % availableWks.size());
            wCount += 1; // Increase the worker round robin count

            // If the worker is at capacity, we skip this worker
            if(currWorker.getCapacity() <= currWorker.getJQSize()) continue;

            // We submit the job
            if(nextJob == null) nextJob = jq.pop();
            lg.trace("Posting job {} to worker {}.",
                     nextJob.getName(),currWorker.getId());
            try {
                currWorker.addJob(nextJob);
                nextJob.setRunning();
                conn.postJob(nextJob,currWorker);
                nextJob = null;
            } catch (Exception e) {
                lg.warn("Problems posting job {} to worker {}: {}",
                        nextJob.getName(),currWorker.getId(),e.toString());
                currWorker.removeJob(nextJob.getName());
                nextJob.setWaiting();
                freeCapacity -= 1;
                continue;
            }
            freeCapacity -= 1;
            jobsDispatched += 1;
        }
        lg.info("Dispatched {} jobs",jobsDispatched);
    }
}
