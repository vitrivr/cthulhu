package org.vitrivr.cthulhu.scheduler;

import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.jobs.JobFactory;
import org.vitrivr.cthulhu.jobs.JobQueue;
import org.vitrivr.cthulhu.worker.Worker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class MasterScheduler {
    private JobQueue jq;
    private JobFactory jf;
    private ArrayList<Worker> ws; // Worker list
    private Logger lg;
    public MasterScheduler() {
        jq = new JobQueue();
        jf = new JobFactory();
        ws = new ArrayList<Worker>();
        lg = LogManager.getLogger("m.ms"); // master.masterscheduler
    }

    public int RegisterJob(String jobDefinition) {
        Job job = jf.buildJob(jobDefinition);
        lg.info("Created job "+job.getName());
        return 0;
    }
    public int RegisterWorker(String address, String ip, int port) {
        Worker w = new Worker(address,port);
        ws.add(w);
        lg.info("Registered worker in "+w.getAddress());
        return 0;
    }
}
