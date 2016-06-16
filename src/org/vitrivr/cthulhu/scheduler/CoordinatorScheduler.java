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
        lg.info("Starting the dispatch of " + Integer.toString(jq.size()) + 
                " jobs to " + Integer.toString(freeCapacity) + " spots in "+
                Integer.toString(availableWks.size())+ "workers.");
        while(freeCapacity > 0 && jq.size() > 0) {
            Worker currWorker = availableWks.get(wCount % availableWks.size());
            wCount += 1; // Increase the worker round robin count

            // If the worker is at capacity, we skip this worker
            if(currWorker.getCapacity() <= currWorker.getJQSize()) continue;

            // We submit the job
            if(nextJob == null) nextJob = jq.pop();
            lg.trace("Posting job "+nextJob.getName()+" to worker "+
                    currWorker.getId()+".");
            try {
                conn.postJob(nextJob,currWorker);
                nextJob = null;
            } catch (Exception e) {
                lg.warn("Problems posting job "+nextJob.getName()+" to worker "+
                        currWorker.getId()+": "+e.toString());
                freeCapacity -= 1;
                continue;
            }
            freeCapacity -= 1;
            jobsDispatched += 1;
        }
        lg.info("Dispatched "+jobsDispatched+" jobs");
    }
}
