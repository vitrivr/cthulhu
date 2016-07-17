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
            note = (note == null ? "" : note + " ; ") + "Unable to delete the working directory "+workDir;
        }
    }
    private void obtainInputFiles(String workingDir) {
        if(config == null || config.input == null) return;
        boolean missing_files = false;
        File wdf = new File(workingDir);
        //System.out.println(wdf.getPath());
        File inpf = new File(wdf, config.input.file);
        String inpStr = inpf.getName();
        Set<String> dirFiles = new HashSet<String>(Arrays.asList(wdf.list()));
        if(!dirFiles.contains(inpStr)) {
            tools.getFile(config.input.file, workingDir);
        }
        config.input.folder = wdf.getAbsolutePath();
        if(config.input.subtitles != null) {
            config.input.subtitles.stream().forEach(s-> {
                    File subf = new File(wdf,s);
                    String fnme = subf.getName();
                    if(!dirFiles.contains(fnme)) {
                        tools.getFile(s, workingDir);
                    }
                });
        }
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
        return confileName;
    }
    private void executeCineast(String cfile) {
        String cineastDir = tools.getCineastLocation();
        if(cineastDir == null || cineastDir.isEmpty()) return ;
        try {
            Process p = Runtime.getRuntime().exec("java -Xmx10G -Xms10G -jar "+cineastDir + " --job " + cfile);
            InputStream is = p.getInputStream();
            InputStream es = p.getErrorStream();
            this.stdOut = IOUtils.toString(is,"UTF-8");
            this.stdErr = IOUtils.toString(es,"UTF-8");
            //System.out.println("SDOUT: "+stdOut);
            //System.out.println("SDERR: "+stdErr);
        } catch (Exception e) {
            status = Job.Status.FAILED;
            // Now what?
        }
    }
}

class CineastConfig {
    CineastInput input;
    List<CineastFeature> features;
    List<CineastExporter> exporters;
    CineastDBConfig database;
    String retriever;
    String decoder;
    String extractor;
    String imagecache;
}

class CineastInput {
    String id;
    protected String folder;
    protected String file;
    String name;
    protected List<String> subtitles;
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
}
class CineastExporter {
    String name;
    @SerializedName("class")
    String _class;
}
