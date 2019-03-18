package org.vitrivr.cthulhu.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.worker.Worker;

public class CoordinatorScheduler extends CthulhuScheduler {

  private transient ScheduledExecutorService executor;
  private transient ScheduledFuture dispatchFuture;
  private int dispatchDelay = 10;

  protected CoordinatorScheduler() {
    this(null);
  }

  public CoordinatorScheduler(Properties props) {
    super(props);
    wt = new ConcurrentHashMap<>();

    if (props != null && props.getProperty("dispatchDelay") != null) {
      dispatchDelay = Integer.parseInt(props.getProperty("dispatchDelay"));
    }
  }

  public void init() {
    executor = Executors.newScheduledThreadPool(1);
    dispatchFuture = executor
        .scheduleAtFixedRate(this::runDispatch, dispatchDelay, dispatchDelay, TimeUnit.SECONDS);
  }

  public void stop() {
    if (dispatchFuture != null) {
      dispatchFuture.cancel(true);
    }
    if (executor != null) {
      executor.shutdown();
    }
  }

  public Worker getRunningWorker(Job job) {
    return wt.entrySet()
        .stream()
        .filter(entry -> entry.getValue().hasJob(job.getName()))
        .map(Map.Entry::getValue)
        .findFirst().orElse(null);
  }

  @Override
  protected void updateJobInt(Job job) throws Exception {
    lg.info("Internal update of job {}", job.getName());
    Worker w = getRunningWorker(job);
    if (w == null) {
      throw new Exception("Updated job not in any worker yet");
    }
    w.removeJob(job.getName());
  }

  @Override
  protected void deleteJobInt(Job job, boolean force) throws Exception {
    if (job.isRunning()) {
      // Stop the job in the worker.
      Worker w = getRunningWorker(job);
      if (w == null) {
        // This should never happen
        lg.error("Could not find worker executing job {}. This is an error.", job.getName());
        throw new Exception(
            "Could not find worker executing job " + job.getName() + ". This is an error.");
      }
      try {
        conn.deleteJob(job, w, true);
      } catch (Exception e) {
        lg.error("Failed to stop job in the worker: {}", e.toString());
        throw e;
        // This should not happen unless the worker became lost or inaccessible.
      }
    }
  }

  /**
   *
   */
  public void restoreStatus() {
    // 1. First we check with each worker if they are still up
    List<String> goneWorkers = new ArrayList<>();
    // We ask each worker to report on their status, and if they do not respond,
    // we blacklist them (to re assign any jobs they might have been running)
    HashMap<String, List<Job>> recoveredJobs = new HashMap<>();
    List<Worker> recoveredWorkers = wt.entrySet().stream()
        .filter(entry -> {
          List<Job> wjobs;
          try {
            wjobs = jf.buildJobs(conn.getJobs(entry.getValue()));
            lg.info("Worker {} has been found. It reported {} jobs.", entry.getKey(), wjobs.size());
            recoveredJobs.put(entry.getKey(), wjobs);
          } catch (Exception e) {
            lg.info("Worker {} has been lost ({}).", entry.getKey(), e.toString());
            goneWorkers.add(entry.getKey());
            return false;
          }
          return true;
        }).map(Entry::getValue).collect(Collectors.toList());
    lg.info("Recovered {} workers. Lost {} workers.", recoveredWorkers.size(), goneWorkers.size());

    // 2. Now we need to check each of the lost workers, and set their jobs as WAITING again
    goneWorkers.forEach(wid -> {
      Worker w = wt.get(wid);
      // All the jobs in w are set to waiting
      w.getJobs().forEach(j -> jt.get(j.getName()).setWaiting());
      // Remove the worker from the worker table
      wt.remove(wid);
    });

    // 3. Now we need to update the job status of jobs from recovered workers
    recoveredWorkers.forEach(w -> {
      String wId = w.getId();
      // For each job that we recovered from the worker
      recoveredJobs.get(wId).forEach(j -> {
        // If it finished running (by failing, succeeding or being interrupted),
        // we need only replace it on the job hash map
        if (j.getStatus() == Job.Status.SUCCEEDED.getValue() ||
            j.getStatus() == Job.Status.FAILED.getValue() ||
            j.getStatus() == Job.Status.INTERRUPTED.getValue() ||
            j.getStatus() == Job.Status.UNEXPECTED_ERROR.getValue()) {
          jt.put(j.getName(), j);
          // We mark it as removed from the worker (because it's done)
          w.removeJob(j.getName());
          return;
        }
                        /* If a job is running in the worker, then it must be running in
                           the restored coordinator. If that's the case, then all is consistent */
        if (j.isRunning() && w.getJob(j.getName()).isRunning()) {
          return;
        }

        /* Otherwise, the state of the job is inconsistent, and it should be reviewed */
        lg.error("Inconsistent state for job {}. Coord status: {}. Worker status: {}",
                 j.getName(), w.getJob(j.getName()).getStatus(), j.getStatus());
      });

    });
    // 4. Finally, we add all waiting jobs to the job queue.
    jt.entrySet().stream()
        .map(Entry::getValue)
        .filter(j -> j.getStatus() == Job.Status.WAITING.getValue())
        .forEach(j -> {
          jq.push(j);
          lg.trace("Inserting job {} to job queue", j.getName());
        });
    lg.info("Job queue size after restore is {}", jq.size());
    lg.info("Done restoring the coordinator status");
  }

  public void runDispatch() {
    // 1. The first stage of the dispatching cycle is to update jobs that have
    //    finished running

    // 2. The second stage of the dispatching cycle is to dispatch jobs.
    List<Worker> availableWks = getWorkers().stream()
        .filter(w -> w.getCapacity() > w.getJQSize()).collect(Collectors.toList());
    int freeCapacity = availableWks.stream().mapToInt(w -> w.getCapacity() - w.getJQSize()).sum();
    int wCount = 0;
    int jobsDispatched = 0;
    Job nextJob = null;
    if (freeCapacity > 0) {
      lg.info("Starting the dispatch of {} jobs to {} spots in {} workers.",
              Integer.toString(jq.size()), Integer.toString(freeCapacity),
              Integer.toString(availableWks.size()));
    } else {
      lg.trace("Unable to dispatch. Jobs waiting: {}. Available capacity: {}",
               Integer.toString(jq.size()), Integer.toString(freeCapacity));
    }
    while (freeCapacity > 0 && jq.size() > 0) {
      Worker currWorker = availableWks.get(wCount % availableWks.size());
      wCount += 1; // Increase the worker round robin count

      // If the worker is at capacity, we skip this worker
      if (currWorker.getCapacity() <= currWorker.getJQSize()) {
        continue;
      }

      // We submit the job
      if (nextJob == null) {
        nextJob = jq.pop();
      }
      lg.trace("Posting job {} to worker {}.",
               nextJob.getName(), currWorker.getId());
      try {
        currWorker.addJob(nextJob);
        nextJob.setRunning();
        conn.postJob(nextJob, currWorker);
        nextJob = null;
      } catch (Exception e) {
        lg.warn("Problems posting job {} to worker {}: {}",
                nextJob.getName(), currWorker.getId(), e.toString());
        currWorker.removeJob(nextJob.getName());
        nextJob.setWaiting();
        freeCapacity -= 1;
        continue;
      }
      freeCapacity -= 1;
      jobsDispatched += 1;
    }
    lg.info("Dispatched {} jobs", jobsDispatched);
    lg.info("Writing status to disk.");
    sk.saveStatus(this);
  }
}
