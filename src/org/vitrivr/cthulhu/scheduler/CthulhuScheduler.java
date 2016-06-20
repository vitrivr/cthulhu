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
    public Job updateJob(String jobDefinition) {
        Job job = jf.buildJob(jobDefinition);
        lg.info("Updating job "+job.getName());
        Job oldJob = jt.get(job.getName());
        if(oldJob == null) {
            lg.warn("Job "+job.getName()+" not existing - unable to update");
            return null;
        }
        try {
            updateJobInt(job);
        } catch (Exception e) {
            lg.error("Internal job update failed: "+e.toString());
        }
        jt.remove(job.getName());
        jt.put(job.getName(),job);
        return oldJob;
    }
    public Job registerJob(String jobDefinition) {
        Job job = jf.buildJob(jobDefinition);
        jq.push(job);
        jt.put(job.getName(),job);
        lg.info("Created job "+job.getName());
        schedulerTick();
        return job;
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
        if(wt == null) return null;
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
    protected void updateJobInt(Job job) throws Exception {
    }
    protected void schedulerTick() {
    }
    public void stop() {
    }
}
