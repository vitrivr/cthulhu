package org.vitrivr.cthulhu.worker;

import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.jobs.JobQueue;
import java.util.HashSet;

public class Worker{ 
    String state;
    String address;
    int port;
    int capacity; // Jobs that can be run simultaneously
    JobQueue jq;
    public Worker(String address, int port) {
        jq = new JobQueue();
        this.address = address;
        this.port = port;
        this.capacity = 1; // Jobs that can be run simultaneously
    }
    public Worker(String address, int port, int capacity) {
        jq = new JobQueue();
        this.address = address;
        this.port = port;
        this.capacity = capacity;
    }
    public int getCapacity() { return capacity; }
    public int getJQSize() { return jq.size(); }
    public String getAddress() { return address; }
    public String getId() { return address+":"+Integer.toString(port); }
}
