package org.vitrivr.cthulhu.scheduler;

import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.jobs.JobFactory;
import org.vitrivr.cthulhu.jobs.JobQueue;
import org.vitrivr.cthulhu.worker.Worker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Hashtable;

public class MasterScheduler {
    private JobQueue jq;
    private JobFactory jf;
    private ArrayList<Worker> ws; // Worker list
    private Hashtable<String,Job> jt; // Job table
    private Logger lg;
    public MasterScheduler() {
        jq = new JobQueue();
        jf = new JobFactory();
        ws = new ArrayList<Worker>();
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
        lg.info("Registering worker in "+address);
        Worker w = new Worker(address,port);
        ws.add(w);
        lg.info("Registered worker in "+w.getAddress());
        return 0;
    }
    public String getJobs() {
        return "";
    }
    public String getJobs(String jobId) {
        Job job = jt.get(jobId);
        if(job == null) return "";
        return job.toString();
    }
}
