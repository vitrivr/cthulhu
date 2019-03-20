package org.vitrivr.cthulhu.scheduler;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.vitrivr.cthulhu.jobs.Job;

class WorkerSchedulerTest {

  private static CthulhuScheduler ms;

  @BeforeAll
  static void setupBeforeClass() {
    ms = new WorkerScheduler(null, true); // Do not register
  }

  @Test
  void registerDeleteJob() {
    String jobDef = "{\"type\":\"BashJob\",\"action\":\"sleep 20\",\"name\":\"wahJob\"}";
    ms.registerJob(jobDef);
    Job jb;
    //Job jb = ms.getJobs("wahJob");
    try {
      Thread.sleep(5000);
    } catch (Exception e) { /* Ignoring */}
    try {
      ms.deleteJob("wahJob", true);
    } catch (Exception e) {
      System.out.println("Deleted.");
    }
    jb = ms.getJobs("wahJob");
    assertNull(jb);
  }
}
