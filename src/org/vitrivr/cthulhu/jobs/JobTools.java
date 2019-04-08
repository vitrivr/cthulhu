package org.vitrivr.cthulhu.jobs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cthulhu.jobs.util.Zipper;
import org.vitrivr.cthulhu.rest.CthulhuRestConnector;
import org.vitrivr.cthulhu.worker.Worker;

public class JobTools {

  private CthulhuRestConnector conn;
  Properties props;
  private Worker coord;
  Logger lg = LogManager.getLogger("r.job.tools");

  public JobTools(Properties props, CthulhuRestConnector conn) {
    this.props = props;
    this.conn = conn;
  }

  public JobTools(Properties props, CthulhuRestConnector conn, Worker coord) {
    this(props, conn);
    this.coord = coord;
  }

  public String getCineastLocation() {
    return props.getProperty("cineast_dir");
  }

  public String getCineastConfigLocation() {
    return props.getProperty("cineast_config_dir");
  }

  public String getJavaFlags() {
    return props.getProperty("cineast_java_flags", "");
  }

  /**
   * Deletes a file or directory.
   * @param file the file or directory to delete
   * @return true on completion
   */
  public boolean delete(File file) throws IOException {
    if (file.isDirectory()) {
      for (File c : file.listFiles()) {
        delete(c);
      }
    }
    if (!file.delete()) {
      throw new FileNotFoundException("Failed to delete file: " + file);
    }
    return true;
  }

  /**
   * Zips a directory and sends it to a worker/scheduler.
   * @param directory Where the directory is located
   * @param fileName what to call the zipped file
   */
  public void sendZipDirectory(String directory, String fileName) throws Exception {
    File fsDir = new File(directory);
    lg.info("Sending directory {} to host {} as file {}",
            directory, coord.getId(), fileName);
    conn.sendStream(fsDir, Zipper::zip, coord, fileName);
  }

  /**
   * Retrieves a file from a worker/scheduler and saves it to the given filename.
   * @param filename the name of the file
   * @param workingDir where to save the file to
   * @return If file exists or file has been gathered
   */
  public boolean getFile(String filename, String workingDir) {
    File directory = new File(workingDir);
    File fileName = new File(filename);
    File localFile = new File(directory, fileName.getName());
    if (localFile.exists()) {
      // We have a problem: The file exists. What do we do?
      return true;
    }
    try {
      conn.getFile(coord, "/data/" + filename, localFile);
    } catch (Exception e) {
      lg.error("Trouble getting file {}. Exception: {}",
               filename, e.toString());
      return false; // Error
    }
    return true;
  }

  /**
   * Creates a working directory without overwriting any directories of jobs with the same name.
   * @param job the job to create a directory for
   * @return the name of the directory
   */
  public String setWorkingDirectory(Job job) {
    String workspaceDir = props.getProperty("workspace");

    File dir = null;
    int tryCount = 0;
    String suffix = "";
    String fileName = null;
    boolean created = false;
    while (dir == null || tryCount < 10) {
      fileName = workspaceDir + "/" + job.getName() + suffix;
      dir = new File(fileName);
      try {
        created = dir.mkdir();
      } catch (Exception e) {
        lg.error("Error when trying to create a working directory for {}. Exception: {}",
                 job.getName(), e.toString());
      }
      if (created) {
        break;
      }
      tryCount += 1;
      suffix = Integer.toString(tryCount);
    }
    if (!created) {
      lg.error("Error when trying to create working directory {}.", fileName);
      return null; // ERROR
    }
    return fileName;
  }
}
