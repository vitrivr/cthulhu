package org.vitrivr.cthulhu.jobs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.core.config.IngestConfig;

public class CineastJob extends Job {

  private IngestConfig config;

  @JsonCreator
  public CineastJob(
      @JsonProperty("config") IngestConfig config,
      @JsonProperty("type") String type,
      @JsonProperty("name") String name,
      @JsonProperty(value = "priority", defaultValue = "2") int priority) {
    super();
    this.config = config;
    this.type = type;
    this.name = name;
    this.priority = priority;
  }

  @Override
  public int execute() {
    return 0;
  }
}
