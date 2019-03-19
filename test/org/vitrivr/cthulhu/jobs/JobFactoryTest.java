package org.vitrivr.cthulhu.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JobFactoryTest {

  private static JobFactory jf;

  @BeforeAll
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
    assertThrows(JsonParseException.class, () -> jf.buildJob(json));
  }

  @Test
  public void malformedJob() {
    String json = "Paoufbivsett}";
    assertThrows(JsonParseException.class, () -> jf.buildJob(json));
  }
}
