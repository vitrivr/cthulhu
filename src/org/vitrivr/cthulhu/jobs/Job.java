package org.vitrivr.cthulhu.jobs;

import com.google.gson.Gson;

/**
   The Job interface. Defines the basic methods for jobs to be manipulated.
 */
abstract public class Job implements Comparable<Job>{
    static final int PROGRAM_FAILURE = 5; // Failure from our program
    static final int JOB_INTERRUPTION = 3; // The job received an interruption while running
    static final int JOB_FAILURE = 1;     // Failure in the job

    String type;
    String name;
    int priority = 2; // Default priority is 2
    int res;
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
    }
    
    /**
     * Returns the type of the job.
     */
    public String getType(){
        return this.type;
    }

    /**
     * Returns the priority of the job.
     */
    public int getPriority(){
        return this.priority;
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

    public int compareTo(Job o) {
        if(priority > o.getPriority()) return 1;
        if(priority == o.getPriority()) return 0;
        return -1;
    }
}
