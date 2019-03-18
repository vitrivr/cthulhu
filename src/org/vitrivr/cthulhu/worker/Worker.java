package org.vitrivr.cthulhu.worker;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.vitrivr.cthulhu.jobs.Job;

public class Worker {

  private String address;
  private int port;
  private int capacity; // Jobs that can be run simultaneously
  private Hashtable<String, Job> jt; // Job table

  public Worker(String address, int port) {
    this.address = address;
    this.port = port;
    this.capacity = 1; // Jobs that can be run simultaneously
    jt = new Hashtable<>();
  }

  public Worker(String address, int port, int capacity) {
    this.address = address;
    this.port = port;
    this.capacity = capacity;
    jt = new Hashtable<>();
  }

  public List<Job> getJobs() {
    return new ArrayList<>(jt.values());
  }

  public Job getJob(String jobName) {
    return jt.get(jobName);
  }

  public void addJob(Job job) {
    jt.put(job.getName(), job);
  }

  public Job removeJob(String jobName) {
    return jt.remove(jobName);
  }

  public boolean hasJob(String jobName) {
    return jt.containsKey(jobName);
  }

  public Job deleteJob(String jobId) {
    return jt.remove(jobId);
  }

  public int getCapacity() {
    return capacity;
  }

  public int getJQSize() {
    return jt.size();
  }

  public String getAddress() {
    return address;
  }

  public int getPort() {
    return port;
  }

  public String getId() {
    return address + ":" + port;
  }
}
