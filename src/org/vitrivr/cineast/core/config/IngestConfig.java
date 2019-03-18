package org.vitrivr.cineast.core.config;

import static org.apache.logging.log4j.LogManager.getLogger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.MediaType;

public class IngestConfig {

  private static final Logger LOGGER = getLogger();

  private final MediaType type;
  private final InputConfig input;
  private final List<MetadataConfig> extractors;
  private final List<MetadataConfig> exporters;
  private final List<MetadataConfig> metadata;
  private final DatabaseConfig database;
  private final ExtractionPipelineConfig pipeline;

  @JsonCreator
  public IngestConfig(
      @JsonProperty("type") MediaType type,
      @JsonProperty("input") InputConfig input,
      @JsonProperty("extractors") List<MetadataConfig> extractors,
      @JsonProperty("exporters") List<MetadataConfig> exporters,
      @JsonProperty("metadata") List<MetadataConfig> metadata,
      @JsonProperty("database") DatabaseConfig database,
      @JsonProperty("pipeline") ExtractionPipelineConfig pipeline) {
    this.type = type;
    this.input = input;
    this.extractors = extractors;
    this.exporters = exporters;
    this.metadata = metadata;
    this.database = database;
    this.pipeline = pipeline;
  }

  public static Logger getLOGGER() {
    return LOGGER;
  }

  public MediaType getType() {
    return type;
  }

  public InputConfig getInput() {
    return input;
  }

  public List<MetadataConfig> getExtractors() {
    return extractors;
  }

  public List<MetadataConfig> getExporters() {
    return exporters;
  }

  public List<MetadataConfig> getMetadata() {
    return metadata;
  }

  public DatabaseConfig getDatabase() {
    return database;
  }

  public ExtractionPipelineConfig getPipeline() {
    return pipeline;
  }
}
