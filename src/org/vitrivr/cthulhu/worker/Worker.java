package org.vitrivr.cthulhu.worker;

import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.jobs.JobQueue;
import java.util.HashSet;

public class Worker{ 
    String state;
    String address;
    int port;
    JobQueue jq;
    public Worker(String address, int port) {
        jq = new JobQueue();
        this.address = address;
        this.port = port;
    }
    public String getAddress() { return address; }
    public String getId() { return address+":"+Integer.toString(port); }
}
