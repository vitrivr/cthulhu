package org.vitrivr.cthulhu.scheduler;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import static org.junit.Assert.*;

import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.jobs.JobFactory;

import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Set;

public class WorkerSchedulerTest {
    static CthulhuScheduler ms;
    static JobFactory jf;
    @BeforeClass
    public static void setupBeforeClass() {
        ms = new WorkerScheduler(null,true); // Do not register
        jf = new JobFactory();
    }
    @Test
    public void registerDeleteJob() {
        String jobDef = "{\"type\":\"BashJob\",\"action\":\"sleep 20\",\"name\":\"wahJob\"}";
        ms.registerJob(jobDef);
        Job jb;
        //Job jb = ms.getJobs("wahJob");
        try { Thread.sleep(5000); } catch (Exception e) { /* Ignoring */}
        try {
            Job rm = ms.deleteJob("wahJob", true);
        } catch (Exception e) {
            System.out.println("Deleted.");
        }
        jb = ms.getJobs("wahJob");
        assertEquals(jb,null);
    }
}
