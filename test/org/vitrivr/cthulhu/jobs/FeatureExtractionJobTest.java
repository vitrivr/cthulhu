package org.vitrivr.cthulhu.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.vitrivr.cthulhu.cineast.config.CineastConfig;
import org.vitrivr.cthulhu.runners.CthulhuRunner;

public class FeatureExtractionJobTest {

  private static JobFactory jf;
  private static JobTools mockTools;

  @BeforeAll
  public static void setUpBeforeClass() throws Exception {
    Properties props = new Properties();
    try {
      InputStream input = CthulhuRunner.class.getClassLoader()
          .getResourceAsStream("cthulhu.properties");
      props.load(input);
    } catch (IOException io) {
      throw new Exception("Could not read props");
    }
    CthulhuRunner.populateProperties(new String[0], props);
    //PrintWriter writr = new PrintWriter(System.out);
    //props.list(writr);
    //writr.flush();
    JobTools jt = new JobTools(props, null);
    jf = new JobFactory();
    jf.setTools(jt);
    mockTools = mock(JobTools.class);
    when(mockTools.getFile(any(), any())).thenReturn(true);
    when(mockTools.delete(any())).thenCallRealMethod();
    when(mockTools.setWorkingDirectory(any())).thenCallRealMethod();
    mockTools.lg = jt.lg;
    mockTools.props = jt.props;
  }

  private String readWholeFile(String jsonFile) throws IOException {
    String jsonRestore;
    try {
      InputStream is = Thread.currentThread().getContextClassLoader()
          .getResourceAsStream(jsonFile);
      jsonRestore = IOUtils.toString(is, "UTF-8");
    } catch (IOException e) {
      System.out
          .println("Unable to restore from file " + jsonFile + ". Exception: " + e.toString());
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
    String conFile = jb.workDir + "/" + jb.getName() + "_config.json";
    Reader fr = new FileReader(conFile);
    CineastConfig cc = gson.fromJson(fr, CineastConfig.class);
    assertEquals("aretriever", cc.retriever);
    assertEquals("vidioid", cc.input.id);
    assertEquals("file.avi", cc.input.file);
    assertEquals("subtitle1", cc.input.subtitles.get(0));
    assertEquals("subtitle4", cc.input.subtitles.get(1));
    jb.deleteWorkingDirectory();
  }

  /**
   * This is the test that will be fixed once the connection to Cineast is up to date.
   *
   * @throws IOException when the json file specified doesn't exist
   */
  @Test
  @Disabled
  public void withValidConfFile() throws IOException {
    String json = readWholeFile("full_fe_job.json");
    FeatureExtractionJob jb = (FeatureExtractionJob) jf.buildJob(json);
    jb.execute();
    //Gson gson = new Gson();
    //System.out.println(gson.toJson(jb));
    assertEquals(0, jb.getStatus()); // Job succeeded
  }
}
