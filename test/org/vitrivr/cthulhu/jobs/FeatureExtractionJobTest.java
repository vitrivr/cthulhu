package org.vitrivr.cthulhu.jobs;

import org.junit.Ignore;
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

import org.vitrivr.cthulhu.cineast.config.CineastConfig;
import org.vitrivr.cthulhu.runners.CthulhuRunner;

import com.google.gson.*;
import static org.mockito.Mockito.*;

public class FeatureExtractionJobTest {
    static JobFactory jf;
    static JobTools jt;
    static JobTools mockTools;
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
        mockTools = mock(JobTools.class);
        when(mockTools.getFile(any(), any())).thenReturn(true);
        when(mockTools.delete(any())).thenCallRealMethod();
        when(mockTools.setWorkingDirectory(any())).thenCallRealMethod();
        mockTools.lg = jt.lg;
        mockTools.props = jt.props;
    }

    @AfterClass
    public static void tearDownAfterClass() {
    }

    private String readWholeFile(String jsonFile) throws IOException {
        String jsonRestore;
        try {
            InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(jsonFile);
            jsonRestore = IOUtils.toString(is,"UTF-8");
        } catch (IOException e) {
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
        String json = "{\"type\":\"FeatureExtractionJob\",\"priority\":3, \"name\":\"configjob\"," +
                           "\"immediate_cleanup\":\"false\", \"config\":{\"retriever\":\"aretriever\", " +
                           "\"input\":{ \"id\":\"vidioid\", \"file\":\"file.avi\", \"name\":\"crazy vid\", " +
                           " \"subtitles\": [\"subtitle1\", \"subtitle4\"]}}}";
        FeatureExtractionJob jb = (FeatureExtractionJob) jf.buildJob(json);
        jb.setTools(mockTools);
        jb.execute();
        Gson gson = new Gson();
        String conFile = jb.workDir+"/"+jb.getName()+"_config.json";
        Reader fr = new FileReader(conFile);
        CineastConfig cc = gson.fromJson(fr, CineastConfig.class);
        assertEquals(cc.retriever, "aretriever");
        assertEquals(cc.input.id, "vidioid");
        assertEquals(cc.input.file, "file.avi");
        assertEquals(cc.input.subtitles.get(0), "subtitle1");
        assertEquals(cc.input.subtitles.get(1), "subtitle4");
        jb.deleteWorkingDirectory();
    }

    /**
     * This is the test that will be fixed once the connection to Cineast is up to date.
     * @throws Exception when the json file specified doesn't exist
     */
    @Test
    @Ignore
    public void withValidConfFile() throws IOException {
        String json = readWholeFile("full_fe_job.json");
        FeatureExtractionJob jb = (FeatureExtractionJob) jf.buildJob(json);
        jb.execute();
        //Gson gson = new Gson();
        //System.out.println(gson.toJson(jb));
        assertEquals(jb.getStatus(), 0); // Job succeeded
    }
}
