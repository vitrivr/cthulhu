package org.vitrivr.cthulhu.scheduler;

import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.jobs.JobFactory;
import org.vitrivr.cthulhu.jobs.JobQueue;
import org.vitrivr.cthulhu.worker.Worker;

import org.vitrivr.cthulhu.rest.CthulhuRESTConnector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Hashtable;
import java.util.stream.*;

import java.util.Properties;

public abstract class CthulhuScheduler {
    protected CthulhuRESTConnector conn;
    protected JobQueue jq;
    private JobFactory jf;
    protected Hashtable<String,Worker> wt; // Worker table
    protected Hashtable<String,Job> jt; // Job table
    protected Logger lg;
    protected Properties props;
    public CthulhuScheduler(Properties props) {
        jq = new JobQueue();
        jf = new JobFactory();
        jt = new Hashtable<String,Job>();
        conn = new CthulhuRESTConnector();
        this.props = props;
        lg = LogManager.getLogger("r.m.ms"); // master.masterscheduler
    }
    public int registerJob(String jobDefinition) {
        Job job = jf.buildJob(jobDefinition);
        jq.push(job);
        jt.put(job.getName(),job);
        lg.info("Created job "+job.getName());
        return 0;
    }
    public int registerWorker(String address, String ip, int port) {
        if(address == null || address.equals("")) address = ip;
        Worker w = new Worker(address,port);
        wt.put(w.getId(),w);
        lg.info("Registered worker in "+w.getAddress());
        return 0;
    }
    public List<Job> getJobs() {
        return jt.entrySet()
            .stream()
            .map(entry -> entry.getValue())
            .collect(Collectors.toList());
    }
    public Job getJobs(String jobId) {
        return jt.get(jobId);
    }
    public List<Worker> getWorkers() {
        return wt.entrySet()
            .stream()
            .map(entry -> entry.getValue())
            .collect(Collectors.toList());
    }
    public Worker getWorkers(String workerId) {
        return wt.get(workerId);
    }
    public Job deleteJob(String jobId) throws Exception {
        return deleteJob(jobId, false);
    }
    public Job deleteJob(String jobId, boolean force) throws Exception {
        Job j = jt.get(jobId);
        if(j.isRunning() && force == false) {
            throw new Exception("Can not delete job that is running. Wait until it finishes, or use force=True.");
        }
        if(j.isRunning()) {
            // Stop the job in the worker.
        }
        return jt.remove(jobId);
    }
    public Worker deleteWorker(String workerId) {
        return wt.remove(workerId);
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
