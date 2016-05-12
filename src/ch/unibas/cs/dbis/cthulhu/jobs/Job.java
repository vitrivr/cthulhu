package ch.unibas.cs.dbis.cthulhu.jobs;

/**
   The Job interface. Defines the basic methods for jobs to be manipulated.
 */
abstract public class Job {
    static final int PROGRAM_FAILURE = 5; // Failure from our program
    static final int JOB_INTERRUPTION = 3; // The job received an interruption while running
    static final int JOB_FAILURE = 1;     // Failure in the job
    String type;
    int priority;
    int res;
    /**
     * Executes the job.
     * <p>
     * It is the main routine called to execute a job. 
     * It returns an int with the status of the job (0:SUCCESS,1:FAILED)
     */
    abstract public int execute();
    
    /**
     * Returns the type of the job.
     */
    public String jobType(){
        return this.type;
    }

    /**
     * Returns the priority of the job.
     */
    public int jobPriority(){
        return this.priority;
    }
}
