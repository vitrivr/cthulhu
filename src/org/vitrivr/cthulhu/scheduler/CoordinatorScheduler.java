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

public class CoordinatorScheduler extends CthulhuScheduler {
    public CoordinatorScheduler(Properties props) {
        super(props);
        wt = new Hashtable<String,Worker>();
        
        int dispatchDelay = 10;
        if(props != null && props.getProperty("dispatchDelay") != null) {
            dispatchDelay = Integer.parseInt(props.getProperty("dispatchDelay"));
        }
    
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    System.out.println("Running dispatch...");
                    runDispatch();
                }
            }, dispatchDelay, dispatchDelay, TimeUnit.SECONDS);
    }
    public void runDispatch() {
        // 1. The first stage of the dispatching cycle is to update jobs that have
        //    finished running

        // 2. The second stage of the dispatching cycle is to dispatch jobs.
        List<Worker> availableWks = getWorkers().stream()
            .filter(w -> w.getCapacity() > w.getJQSize()).collect(Collectors.toList());
        int freeCapacity = availableWks.stream().mapToInt(w-> w.getCapacity() - w.getJQSize()).sum();
        int wCount = 0;
        while(freeCapacity > 0 && jq.size() > 0) {
            Worker currWorker = availableWks.get(wCount % availableWks.size());
            wCount += 1; // Increase the worker round robin count

            // If the worker is at capacity, we skip this worker
            if(currWorker.getCapacity() <= currWorker.getJQSize()) continue;

            // We submit the job
            Job nextJob = jq.pop();
            conn.postJob(nextJob,currWorker);
            freeCapacity -= 1;
        }
    }
}
