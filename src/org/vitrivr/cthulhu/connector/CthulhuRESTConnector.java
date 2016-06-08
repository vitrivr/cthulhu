package org.vitrivr.cthulhu.connector;

import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.worker.Worker;

public class CthulhuRESTConnector {
    public void postJob(Job j, Worker w) {
        System.out.println("Sent job "+j.getName()+" to worker "+w.getId());
    }
    public void putJob(Job j, Worker w) {
    }
}
