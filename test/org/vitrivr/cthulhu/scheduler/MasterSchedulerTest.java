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
        String jobDefSt = "{\"type\":\"BashJob\",\"action\":\"echo wah\",\"name\":\"";
        String jobDefEnd = "\"}";
        ArrayList<String> jobNames = new ArrayList<String>();
        // Register 10 jobs. Store their names in jobNames.
        for(int i = 0; i < 10; i++) {
            String jobName = "wahJob"+Integer.toString(i);
            jobNames.add(jobName);
            ms.registerJob(jobDefSt+jobName+jobDefEnd);
        }
        Set<String> nameSet = ms.getJobs().stream().map(j->j.getName()).collect(Collectors.toSet());
        assertEquals(nameSet.containsAll(jobNames),jobNames.containsAll(nameSet));
        for(int i = 10; i < 20; i++) {
            String jobName = "wahJob"+Integer.toString(i);
            jobNames.add(jobName);
            ms.registerJob(jobDefSt+jobName+jobDefEnd);
        }
        nameSet = ms.getJobs().stream().map(j->j.getName()).collect(Collectors.toSet());
        assertEquals(nameSet.containsAll(jobNames),jobNames.containsAll(nameSet));
    }
}
