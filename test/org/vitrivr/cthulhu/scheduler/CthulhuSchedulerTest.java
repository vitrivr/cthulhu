package org.vitrivr.cthulhu.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vitrivr.cthulhu.jobs.BashJob;
import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.jobs.JobFactory;

public class CthulhuSchedulerTest {

  private static CthulhuScheduler ms;

  @BeforeClass
  public static void setupBeforeClass() {
    ms = new CoordinatorScheduler(null);
  }

  @Test
  public void registerDeleteJob() {
    String jobDef = "{\"type\":\"BashJob\",\"action\":\"echo wah\",\"name\":\"wahJob\"}";
    ms.registerJob(jobDef);
    Job jb = ms.getJobs("wahJob");
    assertTrue(jb instanceof BashJob);
    assertEquals("echo wah", jb.getAction());
    assertEquals(2, jb.getPriority()); // Default priority

    try {
      ms.deleteJob("wahJob");
    } catch (Exception e) {/*Ignoring...*/}
    jb = ms.getJobs("wahJob");
    assertNull(jb);
  }

  @Test
  public void getJobList() {
    String jobDefSt = "{\"type\":\"BashJob\",\"action\":\"echo wah\",\"name\":\"";
    String jobDefEnd = "\"}";
    ArrayList<String> jobNames = new ArrayList<>();
    // Register 10 jobs. Store their names in jobNames.
    for (int i = 0; i < 10; i++) {
      String jobName = "wahJob" + i;
      jobNames.add(jobName);
      ms.registerJob(jobDefSt + jobName + jobDefEnd);
    }
    Set<String> nameSet = ms.getJobs().stream().map(Job::getName).collect(Collectors.toSet());
    assertEquals(jobNames.containsAll(nameSet), nameSet.containsAll(jobNames));
    for (int i = 10; i < 20; i++) {
      String jobName = "wahJob" + i;
      jobNames.add(jobName);
      ms.registerJob(jobDefSt + jobName + jobDefEnd);
    }
    nameSet = ms.getJobs().stream().map(Job::getName).collect(Collectors.toSet());
    assertTrue(nameSet.containsAll(jobNames));
    assertTrue(jobNames.containsAll(nameSet));
  }
}
