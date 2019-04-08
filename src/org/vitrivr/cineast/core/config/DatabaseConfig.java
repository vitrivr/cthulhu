package org.vitrivr.cineast.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DatabaseConfig {

  private final String host;
  private final int port;
  private final boolean plaintext;
  private final int batchsize;
  private final Writer writer;
  private final Selector selector;

  /**
   * Base constructor for the job, can be used by Jackson.
   */
  @JsonCreator
  public DatabaseConfig(
      @JsonProperty("host") String host,
      @JsonProperty("port") int port,
      @JsonProperty("plaintext") boolean plaintext,
      @JsonProperty("batchsize") int batchsize,
      @JsonProperty("writer") Writer writer,
      @JsonProperty("selector") Selector selector) {
    this.host = host;
    this.port = port;
    this.plaintext = plaintext;
    this.batchsize = batchsize;
    this.writer = writer;
    this.selector = selector;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public boolean isPlaintext() {
    return plaintext;
  }

  public int getBatchsize() {
    return batchsize;
  }

  public Writer getWriter() {
    return writer;
  }

  public Selector getSelector() {
    return selector;
  }

  public enum Writer {
    NONE,
    PROTO,
    JSON,
    ADAMPRO,
    COTTONTAIL
  }

  public enum Selector {
    NONE,
    JSON,
    PROTO,
    ADAMPRO,
    ADAMPROSTREAM,
    COTTONTAIL
  }
}
