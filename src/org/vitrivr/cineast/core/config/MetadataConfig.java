package org.vitrivr.cineast.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;

public class MetadataConfig {

  private final String name;
  private final HashMap<String, String> properties;

  @JsonCreator
  public MetadataConfig(
      @JsonProperty("name") String name,
      @JsonProperty("properties") HashMap<String, String> properties) {
    this.name = name;
    this.properties = properties;
  }

  public String getName() {
    return name;
  }

  public HashMap<String, String> getProperties() {
    return properties;
  }
}
