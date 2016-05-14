package org.vitrivr.cthulhu.jobs;

import org.vitrivr.cthulhu.jobs.BashJob;

public class JobFactory {
    /**
     * Builds a job based on a description received in a JSON-formatted string.
     *
     * Builds a job based on a JSON-formatted description. The job type must be present,
     * or it defaults to bash. Existing job types are:
     * * Bash script "BASH"
     * * Feature extraction "FEATURE_EXTRACTION"
     */
    public Job buildJob(String description, String type) {
        return buildJob(description,type,2); // Default priority: 2
    }
    public Job buildJob(String description, String type, int priority) {
        switch (type) {
        case "BashJob":
            return new BashJob(description,priority);
        default:
            throw new IllegalArgumentException("Invalid job type: "+type);
        }
    }

    public JobFactory() {
    }
}
