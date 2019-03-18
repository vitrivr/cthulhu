package org.vitrivr.cthulhu.jobs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JobFactory {

  private Gson gson;
  private JobTools tools = null;

  public JobFactory() {
    this.gson = new GsonBuilder()
        .registerTypeAdapter(Job.class, new JobAdapter())
        .create();
  }

  /**
   * Builds a job based on a description received in a JSON-formatted string.
   * <p>
   *
   * Builds a job based on a JSON-formatted description. The job type must be present, or it
   * defaults to bash. Existing job types are: * Bash script "BashJob" * Feature extraction
   * "FeatureExtractionJob"
   *
   * @param description The JSON definition of the job. It requires job type, action, name,
   *     status, and priority.
   * @return A Job object created using the JSON definition. If the definition is wrong, an
   *     exception will be thrown.
   */
  public Job buildJob(String description) {
    Job jb = gson.fromJson(description, Job.class);
    if (tools != null) {
      jb.setTools(tools);
    }
    return jb;
  }

  public Job buildJob(String description, String type, int priority) {
    if (!"BashJob".equals(type)) {
      throw new IllegalArgumentException("Invalid job type: " + type);
    }
    Job jb = new BashJob(description, priority);
    if (tools != null) {
      jb.setTools(tools);
    }
    return jb;
  }

  public List<Job> buildJobs(String description) {
    Type listType = new TypeToken<ArrayList<Job>>() {
    }.getType();
    List<Job> jobs = gson.fromJson(description, listType);
    if (tools != null) {
      jobs.forEach(j -> j.setTools(tools));
    }
    return jobs;
  }

  public void setTools(JobTools tools) {
    this.tools = tools;
  }
}
