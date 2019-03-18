package org.vitrivr.cthulhu.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonParseException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JobFactoryTest {

  private static JobFactory jf;
  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @BeforeClass
  public static void setUpBeforeClass() {
    jf = new JobFactory();
  }

  @Test
  public void buildBasic() {
    String json = "{\"type\":\"BashJob\"}";
    Job job = jf.buildJob(json);
    assertEquals(job.getClass(), BashJob.class);
  }

  @Test
  public void defaultPriority() {
    String json = "{\"type\":\"BashJob\"}";
    Job job = jf.buildJob(json);
    assertEquals(2, job.getPriority());
  }

  @Test
  public void customPriority() {
    String json = "{\"type\":\"BashJob\",\"priority\":3}";
    Job job = jf.buildJob(json);
    assertEquals(3, job.getPriority());
  }

  @Test
  public void bashType() {
    String json = "{\"type\":\"BashJob\",\"priority\":3}";
    Job job = jf.buildJob(json);
    assertTrue(job instanceof BashJob);
  }

  @Test
  public void typelessJob() {
    String json = "{\"priority\":3}";
    exception.expect(JsonParseException.class);
    jf.buildJob(json);
  }

  @Test
  public void malformedJob() {
    String json = "Paoufbivsett}";
    exception.expect(JsonParseException.class);
    jf.buildJob(json);
  }
}
