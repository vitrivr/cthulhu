package org.vitrivr.cthulhu.keeper;

import static org.apache.logging.log4j.LogManager.getLogger;

import com.google.gson.Gson;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cthulhu.scheduler.CthulhuScheduler;

public class JsonKeeper implements StatusKeeper {

  Logger logger = getLogger(JsonKeeper.class);
  private Gson gson;
  private String outFile;

  public JsonKeeper(Properties prop) {
    outFile = prop != null ? prop.getProperty("statusFile", ".cthulhuStatus.json")
        : ".cthulhuStatus.json";
    gson = new Gson();
  }

  public void saveStatus(CthulhuScheduler cs) {
    try (FileWriter writer = new FileWriter(outFile)) {
      gson.toJson(cs, writer);
    } catch (IOException e) {
      logger.error("Failed to save status", e);
    }
  }

  public String getOutFile() {
    return outFile;
  }
}
