package org.vitrivr.cthulhu.scheduler;

import org.apache.commons.io.IOUtils;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import static org.junit.Assert.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.vitrivr.cthulhu.jobs.JobAdapter;
import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.worker.Worker;

import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.jobs.JobFactory;
import org.vitrivr.cthulhu.rest.CthulhuRESTConnector;

import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import static org.mockito.Mockito.*;

public class CoordinatorSchedulerTest {
    static JobFactory jf;
    static Gson gson;
    @BeforeClass
    public static void setupBeforeClass() {
        jf = new JobFactory();
        gson = new Gson();
    }

    private void addMockCall(CthulhuRESTConnector mockonnector, Worker inWorker, String returnList) {
        try {
            when(mockonnector.getJobs(inWorker)).thenReturn(returnList);
        } catch (Exception e) {
            System.out.println("problem mocking the CthulhuRESTConnector");
        }
    }
    
    /** This takes in the two arguments for the mock connector's getJobs method,
        and the expected return value */
    private CthulhuRESTConnector mockConnectorGetJobs(Worker inWorker, String returnList) {
        CthulhuRESTConnector mockonnector = mock(CthulhuRESTConnector.class);
        addMockCall(mockonnector, inWorker, returnList);
        return mockonnector;
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

    private List<Job> joblistFromFile(String jsonFile) throws Exception {
        String jsonRestore = readWholeFile(jsonFile);
        return jf.buildJobs(jsonRestore);
    }
    private CoordinatorScheduler restoreFromFile(String jsonFile) throws Exception {
        String jsonRestore = readWholeFile(jsonFile);
        Gson restoreGson = new GsonBuilder()
            .registerTypeAdapter(Job.class, new JobAdapter())
            .create();
        CoordinatorScheduler rs = restoreGson.fromJson(jsonRestore, CoordinatorScheduler.class);
        return rs;
    }

    /** Helper function for restoreTestJobDone */
    private CoordinatorScheduler runJobDoneTest(int jobStatus, String jobListFile, int finalJQSize, String mainFile) 
        throws Exception {
        CoordinatorScheduler rs = restoreFromFile(mainFile);
        String resultJobs = readWholeFile(jobListFile);
        //System.out.println(gson.toJson(rs));
        CthulhuRESTConnector nwConn = mockConnectorGetJobs(rs.getWorkers("147.46.117.72:8081"),resultJobs);
        rs.setConn(nwConn);
        assertEquals(rs.getJobs().size(),1); // Restoring a single job
        assertEquals(rs.jq.size(),0); // No jobs on the queue
        assertEquals(rs.getJobs().get(0).getStatus(), Job.Status.RUNNING.getValue()); // Before checking job is running

        rs.restoreStatus();
        //System.out.println(gson.toJson(rs));
        assertEquals(rs.getJobs().size(),1); // Restoring a single job
        assertEquals(rs.jq.size(),finalJQSize); // No jobs added to the queue
        assertEquals(rs.getJobs().get(0).getStatus(), jobStatus); // Before checking job is DONE
        return rs;
    }

    @Test
    public void restoreTestSimple() throws Exception{
        /* RESTORING Two waiting jobs, and no workers at all
         * RESULT: Both are added to the job queue and that's it
         *
         * Im short, this is the case where no workers exist, thus jobs are simply recovered and enqueued
         */
        CoordinatorScheduler rs = restoreFromFile("testRestore1.json");
        // At this point the job queue should be empty, and all jobs must be waitin
        assertEquals(rs.jq.size(),0);
        rs.getJobs().stream().forEach(j->{ assertTrue(j.isWaiting()); });
        rs.restoreStatus();
        assertEquals(rs.jq.size(),2);
    }

    @Test
    public void restoreTestJobDone() throws Exception{
        /* RESTORING: Coord restore: One running job in one worker
         * Worker responds: Running job has SUCCEEDED, then INTERRUPTED, then FAILED, then ERROR
         * RESULT: Job is saved as having succeeded and so on.
         *
         * In short, this is the case where a job finished running while we recovered
         */
        runJobDoneTest(Job.Status.SUCCEEDED.getValue(), "testJobListA.json",0,"testRestore2.json");
        runJobDoneTest(Job.Status.INTERRUPTED.getValue(), "testJobListAInterrupted.json",0,"testRestore2.json");
        runJobDoneTest(Job.Status.FAILED.getValue(), "testJobListAFailed.json",0,"testRestore2.json");
        //runJobDoneTest(Job.Status.UNEXPECTED_ERROR.getValue(), "testJobListA.json");
    }

    @Test
    public void restoreTestJobRunDone() throws Exception {
        /* RESTORING: Coord restore: Two running jobs in same worker
         * Worker responds: One running job has succeeded, the other has failed
         * RESULT: Both are saved as failed and succeeded, and not put in the job queue
         */
        CoordinatorScheduler rs = restoreFromFile("testRestore3.json");
        String resultJobs = readWholeFile("testJobList3-1.json");
        CthulhuRESTConnector nwConn = mockConnectorGetJobs(rs.getWorkers("147.46.117.72:8081"),resultJobs);
        rs.setConn(nwConn);
        assertEquals(rs.getJobs().size(),3); // Restoring three jobs
        assertEquals(rs.jq.size(),0); // No jobs on the queue
        // Two running jobs, one waiting job
        Set<Integer> st = new HashSet<Integer>(Arrays.asList(Job.Status.RUNNING.getValue(), 
                                                             Job.Status.RUNNING.getValue(),
                                                             Job.Status.WAITING.getValue()));
        Set<Integer> rsSt = rs.getJobs().stream().map(j->j.getStatus()).collect(Collectors.toSet());
        assertTrue(rsSt.containsAll(st));
        assertEquals(rsSt.size(),st.size());
        rs.restoreStatus();
        assertEquals(rs.jq.size(),1); // No jobs on the queue        
        rsSt = rs.getJobs().stream().map(j->j.getStatus()).collect(Collectors.toSet());
        st = new HashSet<Integer>(Arrays.asList(Job.Status.RUNNING.getValue(), 
                                                Job.Status.SUCCEEDED.getValue(),
                                                Job.Status.WAITING.getValue()));
        assertTrue(rsSt.containsAll(st));
        assertEquals(rsSt.size(),st.size());
        assertEquals(rs.getJobs("pabs2").getStatus(), Job.Status.SUCCEEDED.getValue());
        assertEquals(rs.getJobs("pabs").getStatus(), Job.Status.RUNNING.getValue());
    }

    @Test
    public void restoreTestWorkerLost() throws Exception {
        /* RESTORING: Coord restore: Two running jobs in two different workers
         * Worker 1 responds: One running job is still running
         * Worker 2 does not respond: We assume that its job has been lost
         * RESULT: Job in worker 1 is saved as running. Job in worker 2 is readded to the job queue, worker 2 is removed.
         */
        CoordinatorScheduler rs = restoreFromFile("testRestore4.json");
        String resultJobs = readWholeFile("testJobList4-1.json");
        CthulhuRESTConnector nwConn = mockConnectorGetJobs(rs.getWorkers("147.46.117.72:8081"),resultJobs);
        when(nwConn.getJobs(rs.getWorkers("147.46.117.72:8083"))).thenThrow(new Exception("Lost the workah"));
        rs.setConn(nwConn);
        assertEquals(rs.getJobs().size(),3); // Restoring three jobs
        assertEquals(rs.jq.size(),0); // No jobs on the queue
        assertEquals(rs.getWorkers().size(),2);
        // Two running jobs, one waiting job
        Set<Integer> st = new HashSet<Integer>(Arrays.asList(Job.Status.RUNNING.getValue(), 
                                                             Job.Status.RUNNING.getValue(),
                                                             Job.Status.WAITING.getValue()));
        Set<Integer> rsSt = rs.getJobs().stream().map(j->j.getStatus()).collect(Collectors.toSet());
        assertTrue(rsSt.containsAll(st));
        assertEquals(rsSt.size(),st.size());
        //System.out.println(gson.toJson(rs));
        rs.restoreStatus();
        //System.out.println(gson.toJson(rs));
        assertEquals(rs.getWorkers().size(),1);
        assertEquals(rs.getWorkers("147.46.117.72:8083"),null);
        assertEquals(rs.getWorkers("147.46.117.72:8081").getId(),"147.46.117.72:8081");
        assertEquals(rs.jq.size(),2); // Two jobs on the queue
        rsSt = rs.getJobs().stream().map(j->j.getStatus()).collect(Collectors.toSet());
        st = new HashSet<Integer>(Arrays.asList(Job.Status.RUNNING.getValue(), 
                                                Job.Status.WAITING.getValue(),
                                                Job.Status.WAITING.getValue()));
        assertTrue(rsSt.containsAll(st));
        assertEquals(rsSt.size(),st.size());
        assertEquals(rs.getJobs("pabs2").getStatus(), Job.Status.WAITING.getValue());
        assertEquals(rs.getJobs("pabs").getStatus(), Job.Status.RUNNING.getValue());
    }
}
