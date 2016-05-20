package org.vitrivr.cthulhu.jobs;

import org.vitrivr.cthulhu.jobs.BashJob;
import com.google.gson.GsonBuilder;
import com.google.gson.Gson;

public class JobFactory {
    /**
     * Builds a job based on a description received in a JSON-formatted string.
     *
     * Builds a job based on a JSON-formatted description. The job type must be present,
     * or it defaults to bash. Existing job types are:
     * * Bash script "BASH"
     * * Feature extraction "FEATURE_EXTRACTION"
     */
    private Gson gson;
    public Job buildJob(String description) {
        return gson.fromJson(description,Job.class);
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
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(Job.class, new JobAdapter())
            .create();
        this.gson = gson;
    }
}
