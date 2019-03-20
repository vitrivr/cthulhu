package org.vitrivr.cthulhu.scheduler;

import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.jobs.Job.Status;
import org.vitrivr.cthulhu.jobs.JobTools;
import org.vitrivr.cthulhu.worker.Worker;

public class WorkerScheduler extends CthulhuScheduler {

  private Worker coordinator;
  private Map<String, Thread> jobExecutors; // List of job executors
  private LinkedList<Job> doneJobQueue;
  private ScheduledFuture coordinatorPoller = null;

  public WorkerScheduler(Properties props) {
    this(props, false);
  }

  public WorkerScheduler(Properties props, boolean standAlone) {
    super(props);
    jobExecutors = new ConcurrentHashMap<>();
    doneJobQueue = new LinkedList<>();
    if (!standAlone) {
      coordinator = new Worker(
          props != null ? props.getProperty("hostAddress") : "127.0.0.1",
          props != null ? Integer.parseInt(props.getProperty("hostPort")) : 8082);
      int port = Integer.parseInt(props != null ? props.getProperty("port") : "8081");
      informCoordinator(props != null ? props.getProperty("address") : "127.0.0.1", port);
      jobTools = new JobTools(props, conn, coordinator); // Resetting the job tools
      jf.setTools(jobTools);
    }
  }

  /**
   * Stops the periodic polling of the coordinator
   * <p>
   */
  private void stopWaitingForCoord() {
    if (coordinatorPoller != null) {
      coordinatorPoller.cancel(false);
    }
    coordinatorPoller = null;
  }

  /**
   * Polls for the coordinator until it comes back, or the worker is terminated.
   * <p>
   */
  private void waitForCoord() {
    lg.info("Starting the polling for the coordinator");
    int pollDelay = 10;
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    coordinatorPoller = executor.scheduleAtFixedRate(() -> {
      try {
        returnFinalizedJobs();
      } catch (Exception e) {
        lg.warn("Coordinator still not available");
        return;
      }
      lg.info("Coordinator has been recovered!");
      schedulerTick();
      stopWaitingForCoord();
    }, pollDelay, pollDelay, TimeUnit.SECONDS);
  }

  /**
   * This function assumes that all the checks have been done, and it is safe to run a job in a new
   * thread. (i.e. assumes that the capacity allows to run one more job).
   */
  private void executeNextJob() {
    if (jq.size() == 0) {
      return;
    }
    Job nextJob = jq.pop();
    lg.info("Starting to execute job {}", nextJob.getName());

        /* Job is executed simply inside a thread. Unless we need 
           more sophisticaded execution logic, we'll use a  simple 
           lambda as the Runner interface passed to Thread t */
    Thread t = new Thread(() -> {
      int result = 0;
      result = nextJob.execute();
      System.out.println(result);
      String strResult = result == 0 ? "SUCCESS" : "FAILURE";
      strResult = nextJob.wasInterrupted() ? "INTERRUPTED" : strResult;
      lg.info("Job execution of job {} finalized with {}",
              nextJob.getName(), strResult);
      finalizeJobExecution(nextJob);
    });
    jobExecutors.put(nextJob.getName(), t);
    t.start();
  }

  private void returnFinalizedJobs() throws Exception {
    while (doneJobQueue.size() > 0) {
      Job j = doneJobQueue.poll();
      if (j == null) {
        continue; // This should never happen
      }
      if (coordinator != null) {
        conn.putJob(j, coordinator);
      }
      // After reporting the result of the job, we remove it from the job table
      // TODO: Pick up of job results
      jt.remove(j.getName());
      if (jobExecutors.containsKey(j.getName())) {
        jobExecutors.remove(j.getName());
      }
    }
  }

  private void finalizeJobExecution(Job j) {
    // TODO THIS MUST EXECUTE EVEN IF THE JOB WAS DELETED AND STOPPED.
    try {
      lg.info("Reporting result of job {} to coordinator.", j.getName());
      doneJobQueue.add(j);
      returnFinalizedJobs();
    } catch (Exception e) {
      lg.error(
          "Unable to report result of job {} to coordinator {}: {}",
          j.getName(),
          coordinator != null ? coordinator.getId() : "STANDALONE",
          e.toString());
      // In this case, we return without the next schedulerTick,
      // if we recover the coordinator, then waitForCoord will execute it.
      waitForCoord();
      return;
    }
    schedulerTick();
  }

  @Override
  protected void deleteJobInt(Job job, boolean force) throws Exception {
    if (!force) {
      return;
    }
    Thread exec = jobExecutors.get(job.getName());
    if (exec == null) {
      lg.warn("Job {} has finished running before it was deleted.", job.getName());
    }
    lg.info("Sending interruption to job executor of {}", job.getName());
    exec.interrupt();
    // TODO - can this fail?
    // If all went fine, then we remove it!
    jt.remove(job.getName());
    jobExecutors.remove(job.getName());
  }

  @Override
  protected void schedulerTick() {
    // Default capacity - to be changed later
    int capacity = 1;
    if (jobExecutors.size() < capacity) {
      executeNextJob();
    }
  }

  private void informCoordinator(String workerAddress, int workerPort) {
    lg.info("Registering worker with coordinator {}", coordinator.getId());
    try {
      conn.postWorker(coordinator, workerAddress, workerPort);
    } catch (Exception e) {
      lg.error("Failed to register with coordinator: {}", e.toString());
      System.exit(1); // Exiting. Need a coordinator or nothing can be done.
    }
  }
}
