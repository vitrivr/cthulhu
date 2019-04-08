package org.vitrivr.cineast.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.File;

public class ExtractionPipelineConfig {

  private final Integer shotQueueSize;
  private final Integer threadPoolSize;
  private final Integer taskQueueSize;
  private final File outputLocation;

  /**
   * Base constructor for the job, can be used by Jackson.
   */
  @JsonCreator
  public ExtractionPipelineConfig(
      @JsonProperty("shotQueueSize") Integer shotQueueSize,
      @JsonProperty("threadPoolSize") Integer threadPoolSize,
      @JsonProperty("taskQueueSize") Integer taskQueueSize,
      @JsonProperty("outputLocation") File outputLocation
  ) {
    this.shotQueueSize = shotQueueSize;
    this.threadPoolSize = threadPoolSize;
    this.taskQueueSize = taskQueueSize;
    this.outputLocation = outputLocation;
  }

  public Integer getShotQueueSize() {
    return shotQueueSize;
  }

  public Integer getThreadPoolSize() {
    return threadPoolSize;
  }

  public Integer getTaskQueueSize() {
    return taskQueueSize;
  }

  public File getOutputLocation() {
    return outputLocation;
  }
}
