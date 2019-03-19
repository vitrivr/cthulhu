package org.vitrivr.cineast.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;

public class IdConfig {

  private final String name;
  private final HashMap<String, String> properties;
  private final ExistenceCheck existenceCheckMode;

  public IdConfig(
      @JsonProperty("name") String name,
      @JsonProperty("properties") HashMap<String, String> properties,
      @JsonProperty("existenceCheckMode") ExistenceCheck existenceCheckMode) {
    this.name = name;
    this.properties = properties;
    this.existenceCheckMode = existenceCheckMode;
  }

  public String getName() {
    return name;
  }

  public HashMap<String, String> getProperties() {
    return properties;
  }

  public ExistenceCheck getExistenceCheckMode() {
    return existenceCheckMode;
  }

  public enum ExistenceCheck {
    CHECK_SKIP,
    CHECK_PROCEED
  }
}
