package org.vitrivr.cthulhu.jobs;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import java.io.File;
import java.io.Writer;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.GsonBuilder;
import com.google.gson.Gson;

import java.util.stream.*;

public class FeatureExtractionJob extends Job {
    CineastConfig config;
    boolean immediate_cleanup = false;
    /**
     * The working directory of the job. If it's set, then it won't be recreated.
     * Files that are already in the working directory will override remote server files.
     */
    String workDir = null;
    public int execute() {
        if(tools == null) {
            this.status = Status.UNEXPECTED_ERROR;
            return 1;
        }
        if(workDir == null || workDir.isEmpty()) {
            workDir = tools.setWorkingDirectory(this);
        }
        obtainInputFiles(workDir);
        String cf = generateConfigFile(workDir);
        executeCineast(cf);
        if(immediate_cleanup) deleteWorkingDirectory();
        return 0;
    }
    protected void deleteWorkingDirectory() {
        File dir = new File(workDir);
        try {
            tools.delete(dir);
        } catch (Exception e) {
            // Now what?
        }
    }
    private void obtainInputFiles(String workingDir) {
        if(config == null || config.input == null) return;

        File wdf = new File(workingDir);
        File inpf = new File(config.input.file);
        String inpStr = inpf.getName();
        Set<String> dirFiles = new HashSet<String>(Arrays.asList(wdf.list()));
        if(!dirFiles.contains(inpStr)) tools.getFile(config.input.file, workingDir);
        config.input.subtitles.stream().forEach(s-> {
                File subf = new File(s);
                String fnme = subf.getName();
                if(!dirFiles.contains(fnme)) tools.getFile(s, workingDir);
            });
    }
    private String generateConfigFile(String workingDir) {
        String confileName = workingDir+"/"+name+"_config.json";
        try {
            Writer writer = new FileWriter(confileName);
            Gson gson = new GsonBuilder().create();
            gson.toJson(config, writer);
            writer.close();
        } catch (IOException io) {
        }
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
