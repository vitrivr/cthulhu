package org.vitrivr.cthulhu.jobs;

import com.google.gson.Gson;
import java.time.LocalDateTime;

/**
 * The Job interface. Defines the basic methods for jobs to be manipulated.
 */
abstract public class Job implements Comparable<Job> {

  String stdOut;
  String stdErr;
  transient JobTools tools;
  String type;
  String name;
  String action;
  /**
   * Documents the status of the job. Possible statuses are:
   * <b>SUCCEEDED, FAILED, WAITING, INTERRUPTED, UNEXPECTED_ERROR, RUNNING</b>;
   * where each one should be self-explanatory.
   */
  Status status = Status.WAITING;
  private LocalDateTime created_at;
  int priority = 2; // Default priority is 2
  /**
   * Creates a job without arguments
   */
  public Job() {
    created_at = LocalDateTime.now();
  }

  protected void setTools(JobTools tools) {
    this.tools = tools;
  }

  /**
   * Executes the job.
   * <p>
   * It is the main routine called to execute a job. It returns an int with the status of the job
   * (0:SUCCESS,1:FAILED)
   */
  abstract public int execute();

  /**
   * Checks if the job is running
   * <p>
   *
   * @return True if the job is running, false otherwise.
   */
  public boolean isRunning() {
    return status == Status.RUNNING;
  }

  /**
   * Checks if the job was interrupted
   * <p>
   *
   * @return True if the job was interrupted, false otherwise.
   */
  public boolean wasInterrupted() {
    return status == Status.INTERRUPTED;
  }

  /**
   * Checks the status of the job
   * <p>
   *
   * @return An integer representing the value of the job status see {@link Status Status}.
   */
  public int getStatus() {
    return status.getValue();
  }

  /**
   * Sets the job status as RUNNING
   * <p>
   */
  public void setRunning() {
    status = Status.RUNNING;
  }

  /**
   * Sets the job status as WAITING
   * <p>
   */
  public void setWaiting() {
    status = Status.WAITING;
  }

  /**
   * Returns the type of the job.
   * <p>
   *
   * @return The job type (BashJob, FeatureExtractionJob,...)
   */
  public String getType() {
    return type;
  }

  /**
   * Returns the action property of the job. The main action of it.
   * <p>
   *
   * @return the action property of the job
   */
  public String getAction() {
    return action;
  }

  /**
   * Returns the priority of the job.
   * <p>
   *
   * @return the priority of the job (smaller is better)
   */
  public int getPriority() {
    return priority;
  }

  /**
   * Modifies the priority of the job
   * <p>
   *
   * @param priority the new priority of the job
   */
  public void setPriority(int priority) {
    this.priority = priority;
  }

  /**
   * Returns whether the job is waiting to run.
   * <p>
   *
   * @return true if the job status is WAITING, false otherwise
   */
  public boolean isWaiting() {
    return this.status == Status.WAITING;
  }

  /**
   * Returns the name of the job
   * <p>
   *
   * @return the name of the job
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the JSON representation of the job
   * <p>
   *
   * @return the JSON representation of the job
   */
  public String toString() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }

  /**
   * Returns the time a job was created
   * <p>
   *
   * @return the time a job was created
   */
  private LocalDateTime getCreatedAt() {
    return created_at;
  }

  /**
   * Compares two jobs for priority and creation time. If priority is smaller, then the job is
   * considered 'smaller'. If priority is the same, then jobs created earlier are considered
   * 'smaller'.
   * <p>
   *
   * @return 1 if our job is bigger, -1 if it's smaller. 0 if it is equal.
   */
  public int compareTo(Job o) {
    if (priority > o.getPriority()) {
      return 1;
    }
    if (priority == o.getPriority() && created_at.compareTo(o.getCreatedAt()) > 0) {
      return 1;
    }
    if (priority == o.getPriority() && created_at.compareTo(o.getCreatedAt()) == 0) {
      return 0;
    }
    return -1;
  }

  public enum Status {
    SUCCEEDED(0),  // Failure in the job
    FAILED(1),   // Job failed while running
    WAITING(2), // Job has been created, but it has not been run.
    INTERRUPTED(3), // The job received an interruption while running
    UNEXPECTED_ERROR(4), // Failure from our program
    RUNNING(5);
    private final int value;

    Status(final int newValue) {
      value = newValue;
    }

    public int getValue() {
      return value;
    }
  }

  /**
   * Returns the standard output of a job after it ran
   */
  String getStdOut() {
    return stdOut;
  }

  /**
   * Returns the standard error stream contents of a job after it ran
   */
  String getStdErr() {
    return stdErr;
  }
}
