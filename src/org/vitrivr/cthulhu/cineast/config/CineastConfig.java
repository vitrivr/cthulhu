package org.vitrivr.cthulhu.cineast.config;

import java.util.List;

public class CineastConfig {

  public CineastInput input;
  public List<CineastFeature> features;
  public List<CineastFeature> exporters;
  public CineastDBConfig database;
  public String retriever;
  public String decoder;
  public CineastExtractorConfig extractor;
  public String imagecache;
}
