package org.vitrivr.cthulhu.jobs;

import org.vitrivr.cthulhu.jobs.BashJob;
import com.google.gson.GsonBuilder;
import com.google.gson.Gson;

public class JobFactory {
    private Gson gson;

    /**
     * Builds a job based on a description received in a JSON-formatted string.
     * <p>
     *
     * Builds a job based on a JSON-formatted description. The job type must be present,
     * or it defaults to bash. Existing job types are:
     * * Bash script "BashJob"
     * * Feature extraction "FeatureExtractionJob"
     *
     * @param description The JSON definition of the job. It requires job type, action, name, status, and priority.
     * @return A Job object created using the JSON definition. If the definition is wrong, an exception will be thrown.
     */
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
