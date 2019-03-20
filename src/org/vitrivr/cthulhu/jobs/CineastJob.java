package org.vitrivr.cthulhu.jobs;

import static org.vitrivr.cthulhu.jobs.Job.Status.FAILED;
import static org.vitrivr.cthulhu.jobs.Job.Status.INTERRUPTED;
import static org.vitrivr.cthulhu.jobs.Job.Status.SUCCEEDED;
import static org.vitrivr.cthulhu.jobs.Job.Status.UNEXPECTED_ERROR;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.vitrivr.cineast.core.config.IngestConfig;

public class CineastJob extends Job {

  private static transient int what = 0;
  private final UUID id;
  private IngestConfig config;
  private String workDir;

  @JsonCreator
  public CineastJob(
      @JsonProperty("config") IngestConfig config,
      @JsonProperty("type") String type,
      @JsonProperty("name") String name,
      @JsonProperty(value = "priority", defaultValue = "2") int priority) {
    super();
    this.id = UUID.randomUUID();
    this.config = config;
    this.type = type;
    this.name = name;
    this.priority = priority;
  }

  private static void saveConfigurationFile(IngestConfig config, String outputPath, UUID id)
      throws IOException {
    String destination = getConfigPath(outputPath, id);
    ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module())
        .registerModule(new JavaTimeModule());
    mapper.writeValue(new File(destination), config);
  }

  private static String getConfigPath(String outputPath, UUID id) {
    return outputPath + id + "_config.json";
  }

  private static void boop() {
    System.out.println("" + what);
    what++;
  }

  @Override
  public int execute() {
    tools.lg.info("Cineast job started");
    if (!getTools().isPresent() || config == null) {
      setStatus(UNEXPECTED_ERROR);
      return getStatusValue();
    }

    String dir = getOrSetWorkDir();
    try {
      saveConfigurationFile(config, dir, this.id);
    } catch (IOException e) {
      setStatus(UNEXPECTED_ERROR);
      return getStatusValue();
    }

    String cineastDir = tools.getCineastLocation();
    String cineastConfig = tools.getCineastConfigLocation();
    Status executionStatus = executeCineast(cineastDir, cineastConfig, getConfigPath(dir, id));

    setStatus(executionStatus);
    if (!this.stdErr.isEmpty()) {
      setStatus(FAILED);
    }
    return getStatusValue();

  }

  private String getOrSetWorkDir() {
    if (workDir == null || workDir.isEmpty()) {
      tools.lg.info("Setting directory for Cineast job");
      workDir = tools.setWorkingDirectory(this);
    }
    return workDir;
  }

  private Status executeCineast(String cineastDir, String cineastConf, String configFile) {
    tools.lg.info("{} - Preparing to execute cineast", name);
    if (cineastDir == null || cineastDir.isEmpty()) {
      return UNEXPECTED_ERROR;
    }
    String javaFlags = tools.getJavaFlags();
    String command = String
        .format("java %s -jar %s --job %s --config %s",
                javaFlags, cineastDir, configFile, cineastConf);
    try {
      Process p = Runtime.getRuntime().exec(command);
      InputStream is = p.getInputStream();
      InputStream es = p.getErrorStream();
      this.stdOut = IOUtils.toString(is, "UTF-8");
      this.stdErr = IOUtils.toString(es, "UTF-8");
      int retVal = p.waitFor();
      if (retVal == 0) {
        return SUCCEEDED;
      }
    } catch (InterruptedException e) {
      return INTERRUPTED;
    } catch (Exception e) {
      return FAILED;
    }
    tools.lg.info("{} - Execution of cineast finalized", name);
    return SUCCEEDED;
  }
}
