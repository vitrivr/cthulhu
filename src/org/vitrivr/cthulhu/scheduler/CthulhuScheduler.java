package org.vitrivr.cthulhu.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.jobs.JobFactory;
import org.vitrivr.cthulhu.jobs.JobQueue;
import org.vitrivr.cthulhu.jobs.JobTools;
import org.vitrivr.cthulhu.keeper.JsonKeeper;
import org.vitrivr.cthulhu.keeper.StatusKeeper;
import org.vitrivr.cthulhu.rest.CthulhuRestConnector;
import org.vitrivr.cthulhu.worker.Worker;

public abstract class CthulhuScheduler {

  static final transient Logger LOGGER = LogManager.getLogger("r.m.ms");
  /**
   * Runtime properties of this agent.
   */
  protected Properties props;
  transient StatusKeeper statusKeeper;
  transient CthulhuRestConnector conn;
  transient JobQueue jobQueue;
  transient JobTools jobTools;
  transient JobFactory jobFactory;
  /**
   * The worker table. It's a hash table of workers, mapped by their address:port string.
   */
  Map<String, Worker> workerTable; // Worker table
  /**
   * The job table. It's the table with all the jobs (waiting, running, failed or successful jobs)
   * in the system.
   */
  Map<String, Job> jobTable; // Job table

  public CthulhuScheduler() {
    this(null);
  }

  /**
   * Main constructor for the scheduler, instantiates the variables using the given properties.
   */
  public CthulhuScheduler(Properties props) {
    statusKeeper = new JsonKeeper(props);
    jobQueue = new JobQueue();
    jobFactory = new JobFactory();
    jobTable = new ConcurrentHashMap<>();
    conn = new CthulhuRestConnector();
    this.props = props;
    jobTools = new JobTools(props, conn);
    jobFactory.setTools(jobTools);
  }

  /**
   * Updates a job with a new definition.
   * <p>
   * Takes in a JSON job definition, creates a new job from it, and substitutes an existing job. If
   * the job does not exist already, the new job definition is not inserted, and the function
   * returns null.
   * </p>
   *
   * @param jobDefinition the JSON string defining the new job
   * @return the old job object
   */
  public Job updateJob(String jobDefinition) {
    Job job = jobFactory.buildJob(jobDefinition);
    LOGGER.info("Updating job {}", job.getName());
    Job oldJob = jobTable.get(job.getName());
    if (oldJob == null) {
      LOGGER.warn("Job {} not existing - unable to update", job.getName());
      return null;
    }
    try {
      updateJobInt(job);
    } catch (Exception e) {
      LOGGER.error("Internal job update failed: {}", e.toString());
    }
    jobTable.remove(job.getName());
    jobTable.put(job.getName(), job);
    return oldJob;
  }

  /**
   * Creates a new job and saves it in the job table.
   * <p>
   * Takes in a JSON job definition, creates a new job from it, and inserts it into the job table
   * (and job queue if the job is ready to run, and this is a coordinator).
   * </p>
   *
   * @param jobDefinition the JSON string defining the new job
   * @return the job object of the job that we created. If no job was created, it returns null.
   */
  public Job registerJob(String jobDefinition) {
    Job job = null;
    try {
      job = jobFactory.buildJob(jobDefinition);
    } catch (Exception e) {
      return job;
    }
    jobQueue.push(job);
    jobTable.put(job.getName(), job);
    LOGGER.info("Created job {}", job.getName());
    schedulerTick();
    return job;
  }

  /**
   * Adds a job to the job queue and triggers the scheduler to check for jobs.
   *
   * @param newJob the job to add to the current scheduler
   */
  public void registerJob(Job newJob) {
    jobQueue.push(newJob);
    jobTable.put(newJob.getName(), newJob);
    LOGGER.info("Created job {}", newJob.getName());
    schedulerTick();
  }

  /**
   * Registers a new worker with the coordinator.
   *
   * @param address The ip address or name of the remote host of the worker
   * @param port The port number where the worker is listening for communication with the
   *     coordinator
   * @return integer 0 if all went well
   */
  public int registerWorker(String address, int port) {
    Worker w = new Worker(address, port);
    workerTable.put(w.getId(), w);
    LOGGER.info("Registered worker in {}", w.getAddress());
    return 0;
  }

  /**
   * Obtains a list of all the jobs known to this agent.
   *
   * @return a list of all the jobs in the job table
   */
  public List<Job> getJobs() {
    return new ArrayList<>(jobTable.values());
  }

  /**
   * Obtains a job according to its name.
   * <p>
   * If the job does not exist, null will be returned.
   * </p>
   *
   * @param jobId the name of the job
   * @return Job object with name equal to the jobId parameter or null
   */
  public Job getJobs(String jobId) {
    return jobTable.get(jobId);
  }

  /**
   * Obtains a list of all the workers known to this agent.
   * <p>
   * If this is a WorkerScheduler, it just returns null.
   * </p>
   *
   * @return a list of all the workers in the worker table
   */
  public List<Worker> getWorkers() {
    if (workerTable == null) {
      return null;
    }
    return new ArrayList<>(workerTable.values());
  }

  /**
   * Obtains a host according to its address and port.
   * <p>
   * If there are no registered hosts under that address:port, null will be returned
   * </p>
   *
   * @param workerId of the host in hostname:port format
   * @return Worker object with said hostname:port id
   */
  public Worker getWorkers(String workerId) {
    return workerTable.get(workerId);
  }

  /**
   * Deletes a job with a specific job id. Force parameter set to false.
   * <p>
   * If the job is running, or does not exist, this call will throw an exception.
   * </p>
   *
   * @param jobId The name of the job to delete
   * @return The job object that was removed from the job table
   */
  Job deleteJob(String jobId) throws Exception {
    return deleteJob(jobId, false);
  }

  /**
   * Deletes a job with a specific job id.
   * <p>
   * If the job is running, and force is set to false this call will throw an exception. If the job
   * is running and force is set to true, the job will first be stopped, and then it will be
   * deleted
   * </p>
   *
   * @param jobId The name of the job to delete
   * @param force indicatess whether to force deletion by stopping the job
   * @return The job object that was removed from the job table
   */
  public Job deleteJob(String jobId, boolean force) throws Exception {
    Job j = jobTable.get(jobId);
    LOGGER.info("Deleting job {} with force = {}", jobId, force ? "TRUE" : "FALSE");
    if (j == null || (j.isRunning() && !force)) {
      LOGGER.warn(
          "Can not delete job {}. Job does not exist or is running and force is set to FALSE",
          jobId);
      throw new Exception(
          "Can not delete job that is running. Wait until it finishes, or use force=True.");
    }
    deleteJobInt(j, force);
    // If an exception is thrown, the job is not deleted
    return jobTable.remove(jobId);
  }

  /**
   * Internal routine for job deletion. To be reimplemented by subclasses.
   */
  protected void deleteJobInt(Job job, boolean force) throws Exception {
  }

  /**
   * Deletes a job with a specific address:port identifier.
   * <p>
   * If the worker is not registered, null is returned. Otherwise, the worker object is.
   * </p>
   *
   * @param workerId of the host in hostname:port format
   * @return Worker object that was deleted with said hostname:port id
   */
  public Worker deleteWorker(String workerId) {
    return workerTable.remove(workerId);
  }

  public void restoreStatus() {
  }

  void setConn(CthulhuRestConnector conn) {
    this.conn = conn;
  }

  public Properties getProperties() {
    return props;
  }

  protected void updateJobInt(Job job) throws Exception {
  }

  protected void schedulerTick() {
  }

  public void stop() {
  }

  public void init() {
  }
}
