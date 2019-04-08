package org.vitrivr.cthulhu.jobs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.vitrivr.cthulhu.cineast.config.CineastConfig;
import org.vitrivr.cthulhu.cineast.config.CineastExtractorConfig;

/**
 * Job for integration with Cineast deprecated as outdated but kept for use with old versions of
 * Cineast.
 *
 * @see CineastJob
 */
@Deprecated
public class FeatureExtractionJob extends Job {

  /**
   * The working directory of the job. If it's set, then it won't be recreated. Files that are
   * already in the working directory will override remote server files.
   */
  String workDir = null;
  private CineastConfig config;
  private String note;
  private boolean immediateCleanup = true;
  private boolean returnZipfile = false;

  /**
   * Triggers the execution of the job, running a Cineast jar.
   *
   * @return status of the job
   */
  public int execute() {
    if (tools == null) {
      this.status = Status.UNEXPECTED_ERROR;
      return 1;
    }
    if (workDir == null || workDir.isEmpty()) {
      tools.lg.info("{} - Setting up the working directory.", name);
      workDir = tools.setWorkingDirectory(this);
    }
    tools.lg.info("{} - Working directory is {}.", name, workDir);
    obtainInputFiles(workDir);
    String cf = generateConfigFile(workDir);
    executeCineast(cf);
    if (returnZipfile) {
      returnResult();
    } else {
      tools.lg.info("{} - Results will not be returned to coordinator");
    }
    if (immediateCleanup) {
      deleteWorkingDirectory();
    } else {
      tools.lg.info("{} - No cleanup to be done", name);
    }
    return status.getValue();
  }

  protected void returnResult() {
    String newfileName =
        config.input.file.substring(0, config.input.file.indexOf(".")) + "_result.zip";
    try {
      tools.sendZipDirectory(workDir, newfileName);
    } catch (Exception e) {
      tools.lg.error("{} - Failed to send zipped results: {}", name, e.toString());
      note = (note == null ? "" : note + " ; ") + "Unable to send zipped results to coordinator";
    }
  }

  protected void deleteWorkingDirectory() {
    tools.lg.info("{} - Deleting the working directory", name);
    File dir = new File(workDir);
    try {
      tools.delete(dir);
    } catch (Exception e) {
      note =
          (note == null ? "" : note + " ; ") + "Unable to delete the working directory " + workDir;
      tools.lg.warn("{} - Unable to delete the working directory: {}", name, e.toString());
    }
  }

  private void obtainInputFiles(String workingDir) {
    if (config == null || config.input == null) {
      return;
    }
    File workingDirectory = new File(workingDir);
    File inputFile = new File(workingDirectory, config.input.file);
    String inputStream = inputFile.getName();
    Set<String> dirFiles = new HashSet<>(Arrays.asList(workingDirectory.list()));
    if (!dirFiles.contains(inputStream)) {
      tools.lg.debug("{} - Obtaining file {}", name, config.input.file);
      if (!tools.getFile(config.input.file, workingDir)) {
        tools.lg.error("{} - Trouble getting file {}", name, config.input.file);
        status = Status.UNEXPECTED_ERROR;
        note = (note == null ? "" : note + " ; ") + "Unable to get remote files";
      }
    }
    config.input.folder = workingDirectory.getAbsolutePath();
    if (config.input.subtitles != null) {
      config.input.subtitles.forEach(s -> {
        File subf = new File(workingDirectory, s);
        String fnme = subf.getName();
        tools.lg.debug("{} - Obtaining file {}", name, fnme);
        if (!dirFiles.contains(fnme)) {
          if (!tools.getFile(s, workingDir) && status != Status.UNEXPECTED_ERROR) {
            tools.lg.error("{} - Unable to get remote file {}", name, fnme);
            status = Status.UNEXPECTED_ERROR;
            note = (note == null ? "" : note + " ; ") + "Unable to get remote files";
          }
        }
      });
    }
    tools.lg.info("{} - Obtained allremote files.", name);
  }

  private String generateConfigFile(String workingDir) {
    if (config == null) {
      return null;
    }
    if (config.extractor == null) {
      config.extractor = new CineastExtractorConfig();
    }
    if (config.extractor.outputLocation == null) {
      config.extractor.outputLocation = new File(workingDir).getAbsolutePath();
    }
    String configFileName = workingDir + "/" + name + "_config.json";
    tools.lg.info("{} - Generating config file in {}", name, configFileName);
    try {
      Writer writer = new FileWriter(configFileName);
      Gson gson = new GsonBuilder().create();
      gson.toJson(config, writer);
      writer.close();
    } catch (IOException io) {
      tools.lg.error("{} - Unable to generate a config file.", name);
    }
    return configFileName;
  }

  private void executeCineast(String cfile) {
    String cineastDir = tools.getCineastLocation();
    tools.lg.info("{} - Preparing to execute cineast", name);
    if (cineastDir == null || cineastDir.isEmpty()) {
      return;
    }
    String javaFlags = tools.getJavaFlags();
    String command = "java " + javaFlags + " -jar " + cineastDir + " --job " + cfile;
    //System.out.println("Command: "+command);
    try {
      tools.lg.debug("{} - Preparing to create process", name);
      Process p = Runtime.getRuntime().exec(command);
      int retVal = waitForProcess(p);
      if (retVal == 0) {
        status = Job.Status.SUCCEEDED;
      }
    } catch (InterruptedException e) {
      status = Job.Status.INTERRUPTED;
      tools.lg.warn("{} - Job has been interrupted", name);
    } catch (Exception e) {
      status = Job.Status.FAILED;
      tools.lg.error("{} - Exception occurred during execution: {}", name, e, toString());
    }
    tools.lg.info("{} - Execution of cineast finalized", name);
  }
}
