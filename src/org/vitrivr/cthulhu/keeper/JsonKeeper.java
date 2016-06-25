package org.vitrivr.cthulhu.keeper;

import org.vitrivr.cthulhu.scheduler.CthulhuScheduler;

import com.google.gson.GsonBuilder;
import com.google.gson.Gson;

import java.util.Properties;

import java.io.FileWriter;
import java.io.IOException;

public class JsonKeeper implements StatusKeeper {
    private Gson gson;
    private Properties prop;
    private String outFile;
    public JsonKeeper(Properties prop) {
        this.prop = prop;
        outFile = prop.getProperty("statusFile",".cthulhuStatus.json");
        gson = new Gson();
    }
    public void saveStatus(CthulhuScheduler cs) {
        try (FileWriter writer = new FileWriter(fileName)) {
                gson.toJson(cs, writer);
            } catch (IOException e) {
            /* Ignoring */
        } catch (Exception e) {
        }
    }
    public String getOutFile() { return outFile; }
}
