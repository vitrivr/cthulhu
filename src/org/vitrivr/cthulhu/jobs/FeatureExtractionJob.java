package org.vitrivr.cthulhu.jobs;

import java.util.List;
import java.io.File;

public class FeatureExtractionJob extends Job {
    CineastConfig config;
    boolean immediate_cleanup = false;
    public int execute() {
        if(tools == null) {
            this.status = Status.UNEXPECTED_ERROR;
            return 1;
        }
        String workDir = tools.setWorkingDirectory(this);
        obtainInputFiles(workDir);
        String cf = generateConfigFile();
        executeCineast(cf);
        if(immediate_cleanup) deleteWorkingDirectory(workDir);
        return 0;
    }
    private void deleteWorkingDirectory(String workingDir) {
        File dir = new File(workingDir);
        try {
            tools.delete(dir);
        } catch (Exception e) {
            // Now what?
        }
    }
    private void obtainInputFiles(String workingDir) {
    }
    private String generateConfigFile() {
        return "";
    }
    private void executeCineast(String cfile) {
    }
}

class CineastConfig {
    CineastInput input;
    List<String> features;
    List<String> exporters;
    String database;
    String retriever;
    String decoder;
    String extractor;
    String imagecache;
}

class CineastInput {
    String id;
    String folder;
    String file;
    String name;
    List<String> subtitles;
}
