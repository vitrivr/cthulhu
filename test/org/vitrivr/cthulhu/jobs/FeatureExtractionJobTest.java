package org.vitrivr.cthulhu.jobs;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import static org.junit.Assert.*;

import java.util.Properties;

import java.io.PrintWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.Reader;

import org.apache.commons.io.IOUtils;

import org.vitrivr.cthulhu.runners.CthulhuRunner;

import com.google.gson.*;
import static org.mockito.Mockito.*;

public class FeatureExtractionJobTest {
    static JobFactory jf;
    static JobTools jt;
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Properties props = new Properties();
        try {
            InputStream input = CthulhuRunner.class.getClassLoader().getResourceAsStream("cthulhu.properties");
            props.load(input);
        } catch (IOException io) {
            throw new Exception("Could not read props");
        }
        CthulhuRunner.populateProperties(new String[0], props);
        //PrintWriter writr = new PrintWriter(System.out);
        //props.list(writr);
        //writr.flush();
        jt = new JobTools(props, null);
        jf = new JobFactory();
        jf.setTools(jt);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    private String readWholeFile(String jsonFile) throws Exception {
        String jsonRestore;
        try {
            InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(jsonFile);
            jsonRestore = IOUtils.toString(is,"UTF-8");
        } catch (Exception e) {
            System.out.println("Unable to restore from file "+jsonFile+". Exception: "+e.toString());
            throw e;
        }
        return jsonRestore;
    }

    @Test
    public void makeDirectory() {
        // Create job, with working directory, and then delete them
        String json = "{\"type\":\"FeatureExtractionJob\",\"priority\":3, \"name\":\"fetujob\", \"immediate_cleanup\":true}";
        Job jb = jf.buildJob(json);
        jb.execute();
    }
    
    @Test
    public void makeConfigFile() throws Exception {
        JobTools mockTools = mock(JobTools.class);
        when(mockTools.getFile(any(), any())).thenReturn(true);
        when(mockTools.delete(any())).thenCallRealMethod();
        when(mockTools.setWorkingDirectory(any())).thenCallRealMethod();
        String json = "{\"type\":\"FeatureExtractionJob\",\"priority\":3, \"name\":\"configjob\"," +
                           "\"config\":{\"database\":\"somedbase\", \"retriever\":\"aretriever\", " +
                           "\"features\":[\"feat1\", \"feat3\"], " +
                           "\"input\":{ \"id\":\"vidioid\", \"file\":\"file.avi\", \"name\":\"crazy vid\", " +
                           " \"subtitles\": [\"subtitle1\", \"subtitle4\"]}}}";
        FeatureExtractionJob jb = (FeatureExtractionJob) jf.buildJob(json);
        jb.setTools(jt);
        jb.execute();
        Gson gson = new Gson();
        String conFile = jb.workDir+"/"+jb.getName()+"_config.json";
        Reader fr = new FileReader(conFile);
        CineastConfig cc = gson.fromJson(fr, CineastConfig.class);
        assertEquals(cc.database, "somedbase");
        assertEquals(cc.retriever, "aretriever");
        assertEquals(cc.features.get(0), "feat1");
        assertEquals(cc.features.get(1), "feat3");
        assertEquals(cc.input.id, "vidioid");
        assertEquals(cc.input.file, "file.avi");
        assertEquals(cc.input.subtitles.get(0), "subtitle1");
        assertEquals(cc.input.subtitles.get(1), "subtitle4");
        jb.deleteWorkingDirectory();
    }

    @Test
    public void withValidConfFile() throws Exception {
        String json = readWholeFile("full_fe_job.json");
        FeatureExtractionJob jb = (FeatureExtractionJob) jf.buildJob(json);
        jb.execute();
    }
}
