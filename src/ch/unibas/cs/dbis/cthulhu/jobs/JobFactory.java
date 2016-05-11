package ch.unibas.cs.dbis.cthulhu.jobs;

class JobFactory {
    /**
     * Builds a job based on a description received in a JSON-formatted string.
     *
     * Builds a job based on a JSON-formatted description. The job type must be present,
     * or it defaults to bash. Existing job types are:
     * * Bash script "BASH"
     * * Feature extraction "FEATURE_EXTRACTION"
     */
    public Job buildJob(String description) {
    }

    public JobFactory() {
    }
}
