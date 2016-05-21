package org.vitrivr.cthulhu.scheduler;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import static org.junit.Assert.*;

import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.jobs.JobFactory;

public class MasterSchedulerTest {
    static MasterScheduler ms;
    static JobFactory jf;
    @BeforeClass
    public static void setupBeforeClass() {
        ms = new MasterScheduler();
        jf = new JobFactory();
    }

    @Test
    public void registerDeleteJob() {
        String jobDef = "{\"type\":\"BashJob\",\"action\":\"echo wah\",\"name\":\"wahJob\"}";
        ms.registerJob(jobDef);
        Job jb = ms.getJobs("wahJob");
        assertEquals(jb.getType(),"BashJob");
        assertEquals(jb.getAction(),"echo wah");
        assertEquals(jb.getPriority(),2); // Default priority
        
        ms.deleteJob("wahJob");
        jb = ms.getJobs("wahJob");
        assertEquals(jb,null);
    }
    
    @Test
    public void getJobList() {
        String jobDef = "{\"type\":\"BashJob\",\"action\":\"echo wah\",\"name\":\"wahJob\"}";
        Job job = jf.buildJob(jobDef);
        
    }
}
