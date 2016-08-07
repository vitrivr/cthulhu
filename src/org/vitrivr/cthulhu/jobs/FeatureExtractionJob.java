package org.vitrivr.cthulhu.jobs;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import java.io.InputStream;
import java.io.File;
import java.io.Writer;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import java.lang.Process;

import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.stream.*;

public class FeatureExtractionJob extends Job {
    CineastConfig config;
    String stdOut;
    String stdErr;
    String note;
    boolean immediate_cleanup = true;
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
            tools.lg.info("{} - Setting up the working directory.", name);
            workDir = tools.setWorkingDirectory(this);
        }
        tools.lg.info("{} - Working directory is {}.", name, workDir);
        obtainInputFiles(workDir);
        String cf = generateConfigFile(workDir);
        executeCineast(cf);
        if(immediate_cleanup) deleteWorkingDirectory();
        return status.getValue();
    }
    protected void deleteWorkingDirectory() {
        tools.lg.info("{} - Deleting the working directory", name);
        File dir = new File(workDir);
        try {
            tools.delete(dir);
        } catch (Exception e) {
            note = (note == null ? "" : note + " ; ") + "Unable to delete the working directory "+workDir;
            tools.lg.warn("{} - Unable to delete the working directory", name);
        }
    }
    private void obtainInputFiles(String workingDir) {
        if(config == null || config.input == null) return;
        File wdf = new File(workingDir);
        File inpf = new File(wdf, config.input.file);
        String inpStr = inpf.getName();
        Set<String> dirFiles = new HashSet<String>(Arrays.asList(wdf.list()));
        if(!dirFiles.contains(inpStr)) {
            tools.lg.debug("{} - Obtaining file {}", name, config.input.file);
            if(!tools.getFile(config.input.file, workingDir)) {
                tools.lg.error("{} - Trouble getting file {}",name,config.input.file);
                status = Status.UNEXPECTED_ERROR;
                note = (note == null ? "" : note + " ; ") + "Unable to get remote files";
            }
        }
        config.input.folder = wdf.getAbsolutePath();
        if(config.input.subtitles != null) {
            config.input.subtitles.stream().forEach(s-> {
                    File subf = new File(wdf,s);
                    String fnme = subf.getName();
                    tools.lg.debug("{} - Obtaining file {}", name, fnme);
                    if(!dirFiles.contains(fnme)) {
                        if(!tools.getFile(s, workingDir) && status != Status.UNEXPECTED_ERROR) {
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
        String confileName = workingDir+"/"+name+"_config.json";
        if(config == null) return null;
        if(config.extractor == null) config.extractor = new CineastExtractorConfig();
        if(config.extractor.outputLocation == null) config.extractor.outputLocation = new File(workingDir).getAbsolutePath();
        try {
            Writer writer = new FileWriter(confileName);
            Gson gson = new GsonBuilder().create();
            gson.toJson(config, writer);
            writer.close();
        } catch (IOException io) {
            tools.lg.error("{} - Unable to generate a config file.", name);
        }
        return confileName;
    }
    private void executeCineast(String cfile) {
        String cineastDir = tools.getCineastLocation();
        if(cineastDir == null || cineastDir.isEmpty()) return ;
        String javaFlags = tools.getJavaFlags();
        String command = "java " + javaFlags + " -jar "+ cineastDir + " --job " + cfile;
        //System.out.println("Command: "+command);
        try {
            Process p = Runtime.getRuntime().exec(command);
            InputStream is = p.getInputStream();
            InputStream es = p.getErrorStream();
            this.stdOut = IOUtils.toString(is,"UTF-8");
            this.stdErr = IOUtils.toString(es,"UTF-8");
            //System.out.println("SDOUT: "+stdOut);
            //System.out.println("SDERR: "+stdErr);
            int retVal = p.waitFor();
            if(retVal == 0) status = Job.Status.SUCCEEDED;
        } catch (InterruptedException e) {
            status = Job.Status.INTERRUPTED;
            tools.lg.warn("{} - Job has been interrupted",name);
        } catch (Exception e) {
            status = Job.Status.FAILED;
            tools.lg.error("{} - Exception occurred during execution: {}", name, e,toString());
        }
    }
}

class CineastConfig {
    CineastInput input;
    List<CineastFeature> features;
    List<CineastFeature> exporters;
    CineastDBConfig database;
    String retriever;
    String decoder;
    CineastExtractorConfig extractor;
    String imagecache;
}

class CineastInput {
    String id;
    protected String folder;
    protected String file;
    String name;
    protected List<String> subtitles;
}

class CineastExtractorConfig {
    Integer shotQueueSize;
    Integer threadPoolSize;
    Integer taskQueueSize;
    String outputLocation;
}

class CineastDBConfig {
    String host;
    int port;
    boolean plaintext;
    String writer;
}

class CineastFeature {
    String name;
    @SerializedName("class")
    String _class;
    String config;
}
