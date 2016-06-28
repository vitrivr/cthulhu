package org.vitrivr.cthulhu.scheduler;

import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.jobs.JobFactory;
import org.vitrivr.cthulhu.jobs.JobQueue;
import org.vitrivr.cthulhu.worker.Worker;

import org.vitrivr.cthulhu.keeper.StatusKeeper;
import org.vitrivr.cthulhu.keeper.JsonKeeper;

import org.vitrivr.cthulhu.rest.CthulhuRESTConnector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.*;

import java.util.Properties;

import com.google.gson.annotations.Expose;

public abstract class CthulhuScheduler {
    transient protected StatusKeeper sk;
    transient protected CthulhuRESTConnector conn;
    transient protected JobQueue jq;
    transient private JobFactory jf;
    protected Map<String,Worker> wt; // Worker table
    protected Map<String,Job> jt; // Job table
    transient protected Logger lg;
    protected Properties props;
    public CthulhuScheduler(Properties props) {
        sk = new JsonKeeper(props);
        jq = new JobQueue();
        jf = new JobFactory();
        jt = new ConcurrentHashMap<String,Job>();
        conn = new CthulhuRESTConnector();
        this.props = props;
        lg = LogManager.getLogger("r.m.ms"); // master.masterscheduler
    }
    public Job updateJob(String jobDefinition) {
        Job job = jf.buildJob(jobDefinition);
        lg.info("Updating job {}",job.getName());
        Job oldJob = jt.get(job.getName());
        if(oldJob == null) {
            lg.warn("Job {} not existing - unable to update",job.getName());
            return null;
        }
        try {
            updateJobInt(job);
        } catch (Exception e) {
            lg.error("Internal job update failed: {}",e.toString());
        }
        jt.remove(job.getName());
        jt.put(job.getName(),job);
        return oldJob;
    }
    public Job registerJob(String jobDefinition) {
        Job job = null;
        try {
            job = jf.buildJob(jobDefinition);
        } catch (Exception e) {
            return job;
        }
        jq.push(job);
        jt.put(job.getName(),job);
        lg.info("Created job {}",job.getName());
        schedulerTick();
        return job;
    }
    public int registerWorker(String address, String ip, int port) {
        if(address == null || address.equals("")) address = ip;
        Worker w = new Worker(address,port);
        wt.put(w.getId(),w);
        lg.info("Registered worker in {}",w.getAddress());
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
        lg.info("Deleting job {} with force = {}",jobId, force == true ? "TRUE" : "FALSE");
        if(j == null || (j.isRunning() && force == false)) {
            lg.warn("Can not delete job {}. Job does not exist or is running and force is set to FALSE", jobId);
            throw new Exception("Can not delete job that is running. Wait until it finishes, or use force=True.");
        }
        deleteJobInt(j,force); 
        // If an exception is thrown, the job is not deleted
        return jt.remove(jobId);
    }
    protected void deleteJobInt(Job job, boolean force) throws Exception {
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
