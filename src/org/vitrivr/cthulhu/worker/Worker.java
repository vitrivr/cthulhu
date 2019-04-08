package org.vitrivr.cthulhu.worker;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.vitrivr.cthulhu.jobs.Job;

public class Worker {

  private String address;
  private int port;
  private int capacity; // Jobs that can be run simultaneously
  private Hashtable<String, Job> jobTable;

  /**
   * Constructor for a worker that doesn't declare the capacity, so the default capacity of 1 is
   * given.
   *
   * @param address an IPv4 address the worker is at, by default this is 127.0.0.1
   * @param port the port the worker is listening on
   */
  public Worker(String address, int port) {
    this(address, port, 1);
  }

  /**
   * Main constructor for a worker.
   *
   * @param address an IPv4 address the worker is at, by default this is 127.0.0.1
   * @param port the port the worker is listening on
   * @param capacity the number of jobs the worker can handle at once
   */
  public Worker(String address, int port, int capacity) {
    this.address = address;
    this.port = port;
    this.capacity = capacity;
    jobTable = new Hashtable<>();
  }

  public List<Job> getJobs() {
    return new ArrayList<>(jobTable.values());
  }

  public Job getJob(String jobName) {
    return jobTable.get(jobName);
  }

  public void addJob(Job job) {
    jobTable.put(job.getName(), job);
  }

  public Job removeJob(String jobName) {
    return jobTable.remove(jobName);
  }

  public boolean hasJob(String jobName) {
    return jobTable.containsKey(jobName);
  }

  public Job deleteJob(String jobId) {
    return jobTable.remove(jobId);
  }

  public int getCapacity() {
    return capacity;
  }

  public int getJobQueueSize() {
    return jobTable.size();
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
