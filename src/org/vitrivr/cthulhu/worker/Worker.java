package org.vitrivr.cthulhu.worker;

import org.vitrivr.cthulhu.jobs.Job;

import java.util.HashSet;
import java.util.Hashtable;

public class Worker{ 
    String state;
    String address;
    int port;
    int capacity; // Jobs that can be run simultaneously
    Hashtable<String,Job> jt; // Job table
    public Worker(String address, int port) {
        this.address = address;
        this.port = port;
        this.capacity = 1; // Jobs that can be run simultaneously
        jt = new Hashtable<String,Job>();
    }
    public Worker(String address, int port, int capacity) {
        this.address = address;
        this.port = port;
        this.capacity = capacity;
        jt = new Hashtable<String,Job>();
    }
    public void addJob(Job job) {
        jt.put(job.getName(),job);
    }
    public Job deleteJob(String jobId) {
        return jt.remove(jobId);
    }
    public int getCapacity() { return capacity; }
    public int getJQSize() { return jt.size(); }
    public String getAddress() { return address; }
    public int getPort(){ return port; }
    public String getId() { return address+":"+Integer.toString(port); }
}
