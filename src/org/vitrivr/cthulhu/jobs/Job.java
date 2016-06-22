package org.vitrivr.cthulhu.jobs;

import com.google.gson.Gson;
import java.time.LocalDateTime;

/**
   The Job interface. Defines the basic methods for jobs to be manipulated.
 */
abstract public class Job implements Comparable<Job>{
    public static enum Status{
        SUCCEEDED(0),  // Failure in the job
        FAILED(1),   // Job failed while running
        WAITING(2), // Job has been created, but it has not been run.
        INTERRUPTED(3), // The job received an interruption while running
        UNEXPECTED_ERROR(4), // Failure from our program
        RUNNING(5);
        private final int value;
        Status (final int newValue) {
            value = newValue;
        }
        public int getValue() { return value; }
    }

    String type;
    String name;
    String action;
    Status status = Status.WAITING;
    LocalDateTime created_at;
    int priority = 2; // Default priority is 2
    /**
     * Executes the job.
     * <p>
     * It is the main routine called to execute a job. 
     * It returns an int with the status of the job (0:SUCCESS,1:FAILED)
     */
    abstract public int execute();

    /**
     * Creates a job without arguments
     */
    public Job() {
        created_at = LocalDateTime.now();
    }

    public boolean isRunning() {
        return status == Status.RUNNING;
    }
    public boolean wasInterrupted() {
        return status == Status.INTERRUPTED;
    }

    public int getStatus() {
        return status.getValue();
    }

    public void setRunning() {
        status = Status.RUNNING;
    }
    public void setWaiting() {
        status = Status.WAITING;
    }
    
    /**
     * Returns the type of the job.
     */
    public String getType(){
        return type;
    }

    public String getAction() {
        return action;
    }

    /**
     * Returns the priority of the job.
     */
    public int getPriority(){
        return priority;
    }
    public void setPriority(int priority){
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        Gson gson = new Gson();
        String json = gson.toJson(this);
        return json;
    }

    public LocalDateTime getCreatedAt() {
        return created_at;
    }

    public int compareTo(Job o) {
        if(priority > o.getPriority()) return 1;
        if(priority == o.getPriority() && created_at.compareTo(o.getCreatedAt()) == 1) return 1;
        if(priority == o.getPriority() && created_at.compareTo(o.getCreatedAt()) == 0) return 0;
        return -1;
    }
}
