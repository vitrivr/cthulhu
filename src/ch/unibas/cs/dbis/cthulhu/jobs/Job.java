package ch.unibas.cs.dbis.cthulhu.jobs;

/**
   The Job interface. Defines the basic methods for jobs to be manipulated.
 */
interface Job {
    /**
     * Executes the job.
     * <p>
     * It is the main routine called to execute a job. 
     * It returns an int with the status of the job (0:SUCCESS,1:FAILED)
     */
    public int execute();
    
    /**
     * Returns the type of the job.
     */
    default public String jobType() {
        return this.type;
    }
}
