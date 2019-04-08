package org.vitrivr.cthulhu.keeper;

import static org.apache.logging.log4j.LogManager.getLogger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cthulhu.scheduler.CthulhuScheduler;

public class JsonKeeper implements StatusKeeper {

  private static final Logger LOGGER = getLogger(JsonKeeper.class);
  private final ObjectMapper mapper;
  private final File outFile;

  /**
   * Constructs the class using the properties file to get the status file location.
   * @param prop properties of the system
   */
  public JsonKeeper(Properties prop) {
    String filePath = prop != null ? prop.getProperty("statusFile", ".cthulhuStatus.json")
        : ".cthulhuStatus.json";
    outFile = new File(filePath);
    mapper = new ObjectMapper();
  }

  /**
   * Saves the status of the scheduler to a file.
   * @param cs the scheduler to write out
   */
  public void saveStatus(CthulhuScheduler cs) {
    try {
      mapper.writeValue(outFile, cs);
    } catch (IOException e) {
      LOGGER.error("Failed to save status", e);
    }
  }

  public File getOutFile() {
    return outFile;
  }
}
