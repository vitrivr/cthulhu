package org.vitrivr.cineast.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InputConfig {

  private final String name;
  private final String path;
  private final IdConfig id;
  private final int depth;
  private final int skip;
  private final int limit;

  @JsonCreator
  public InputConfig(
      @JsonProperty("name") String name,
      @JsonProperty("path") String path,
      @JsonProperty("id") IdConfig id,
      @JsonProperty("depth") int depth,
      @JsonProperty("skip") int skip,
      @JsonProperty("limit") int limit) {
    this.name = name;
    this.path = path;
    this.id = id;
    this.depth = depth;
    this.skip = skip;
    this.limit = limit;
  }

  public String getName() {
    return name;
  }

  public String getPath() {
    return path;
  }

  public IdConfig getId() {
    return id;
  }

  public int getDepth() {
    return depth;
  }

  public int getSkip() {
    return skip;
  }

  public int getLimit() {
    return limit;
  }
}
