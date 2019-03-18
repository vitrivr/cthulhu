package org.vitrivr.cthulhu.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.jobs.JobAdapter;
import org.vitrivr.cthulhu.jobs.JobFactory;
import org.vitrivr.cthulhu.rest.CthulhuRESTConnector;
import org.vitrivr.cthulhu.worker.Worker;

public class CoordinatorSchedulerTest {

  private static JobFactory jf;

  @BeforeClass
  public static void setupBeforeClass() {
    jf = new JobFactory();
  }

  private void addMockCall(CthulhuRESTConnector mockonnector, Worker inWorker, String returnList) {
    try {
      when(mockonnector.getJobs(inWorker)).thenReturn(returnList);
    } catch (Exception e) {
      System.out.println("problem mocking the CthulhuRESTConnector");
    }
  }

  /**
   * This takes in the two arguments for the mock connector's getJobs method, and the expected
   * return value
   */
  private CthulhuRESTConnector mockConnectorGetJobs(Worker inWorker, String returnList) {
    CthulhuRESTConnector mockConnector = mock(CthulhuRESTConnector.class);
    addMockCall(mockConnector, inWorker, returnList);
    return mockConnector;
  }

  private String readWholeFile(String jsonFile) throws Exception {
    String jsonRestore;
    try {
      InputStream is = Thread.currentThread().getContextClassLoader()
          .getResourceAsStream(jsonFile);
      jsonRestore = IOUtils.toString(is, "UTF-8");
    } catch (Exception e) {
      System.out
          .println("Unable to restore from file " + jsonFile + ". Exception: " + e.toString());
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

  /**
   * Helper function for restoreTestJobDone
   */
  private CoordinatorScheduler runJobDoneTest(
      int jobStatus,
      String jobListFile,
      int finalJQSize,
      String mainFile)
      throws Exception {
    CoordinatorScheduler rs = restoreFromFile(mainFile);
    String resultJobs = readWholeFile(jobListFile);
    //System.out.println(gson.toJson(rs));
    CthulhuRESTConnector nwConn = mockConnectorGetJobs(
        rs.getWorkers("147.46.117.72:8081"),
        resultJobs);
    rs.setConn(nwConn);
    assertEquals(1, rs.getJobs().size()); // Restoring a single job
    assertEquals(0, rs.jq.size()); // No jobs on the queue
    assertEquals(
        Job.Status.RUNNING.getValue(),
        rs.getJobs().get(0).getStatus()); // Before checking job is running

    rs.restoreStatus();
    //System.out.println(gson.toJson(rs));
    assertEquals(1, rs.getJobs().size()); // Restoring a single job
    assertEquals(finalJQSize, rs.jq.size()); // No jobs added to the queue
    assertEquals(jobStatus, rs.getJobs().get(0).getStatus()); // Before checking job is DONE
    return rs;
  }

  @Test
  public void restoreTestSimple() throws Exception {
    /* RESTORING Two waiting jobs, and no workers at all
     * RESULT: Both are added to the job queue and that's it
     *
     * Im short, this is the case where no workers exist, thus jobs are simply recovered and enqueued
     */
    CoordinatorScheduler rs = restoreFromFile("testRestore1.json");
    // At this point the job queue should be empty, and all jobs must be waitin
    assertEquals(0, rs.jq.size());
    rs.getJobs().forEach(j -> assertTrue(j.isWaiting()));
    rs.restoreStatus();
    assertEquals(2, rs.jq.size());
  }

  @Test
  public void restoreTestJobDone() throws Exception {
    /* RESTORING: Coord restore: One running job in one worker
     * Worker responds: Running job has SUCCEEDED, then INTERRUPTED, then FAILED, then ERROR
     * RESULT: Job is saved as having succeeded and so on.
     *
     * In short, this is the case where a job finished running while we recovered
     */
    runJobDoneTest(Job.Status.SUCCEEDED.getValue(), "testJobListA.json", 0, "testRestore2.json");
    runJobDoneTest(Job.Status.INTERRUPTED.getValue(), "testJobListAInterrupted.json", 0,
                   "testRestore2.json");
    runJobDoneTest(Job.Status.FAILED.getValue(), "testJobListAFailed.json", 0, "testRestore2.json");
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
    CthulhuRESTConnector nwConn = mockConnectorGetJobs(
        rs.getWorkers("147.46.117.72:8081"),
        resultJobs);
    rs.setConn(nwConn);
    assertEquals(3, rs.getJobs().size()); // Restoring three jobs
    assertEquals(0, rs.jq.size()); // No jobs on the queue
    // Two running jobs, one waiting job
    Set<Integer> st = new HashSet<>(Arrays.asList(
        Job.Status.RUNNING.getValue(),
        Job.Status.RUNNING.getValue(),
        Job.Status.WAITING.getValue()));
    Set<Integer> rsSt = rs.getJobs().stream().map(Job::getStatus).collect(Collectors.toSet());
    assertTrue(rsSt.containsAll(st));
    assertEquals(st.size(), rsSt.size());
    rs.restoreStatus();
    assertEquals(1, rs.jq.size()); // No jobs on the queue
    rsSt = rs.getJobs().stream().map(Job::getStatus).collect(Collectors.toSet());
    st = new HashSet<>(Arrays.asList(
        Job.Status.RUNNING.getValue(),
        Job.Status.SUCCEEDED.getValue(),
        Job.Status.WAITING.getValue()));
    assertTrue(rsSt.containsAll(st));
    assertEquals(st.size(), rsSt.size());
    assertEquals(Job.Status.SUCCEEDED.getValue(), rs.getJobs("pabs2").getStatus());
    assertEquals(Job.Status.RUNNING.getValue(), rs.getJobs("pabs").getStatus());
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
    CthulhuRESTConnector nwConn = mockConnectorGetJobs(
        rs.getWorkers("147.46.117.72:8081"),
        resultJobs);
    when(nwConn.getJobs(rs.getWorkers("147.46.117.72:8083")))
        .thenThrow(new Exception("Lost the workah"));
    rs.setConn(nwConn);
    assertEquals(3, rs.getJobs().size()); // Restoring three jobs
    assertEquals(0, rs.jq.size()); // No jobs on the queue
    assertEquals(2, rs.getWorkers().size());
    // Two running jobs, one waiting job
    Set<Integer> st = new HashSet<>(Arrays.asList(
        Job.Status.RUNNING.getValue(),
        Job.Status.RUNNING.getValue(),
        Job.Status.WAITING.getValue()));
    Set<Integer> rsSt = rs.getJobs().stream().map(Job::getStatus).collect(Collectors.toSet());
    assertTrue(rsSt.containsAll(st));
    assertEquals(st.size(), rsSt.size());
    //System.out.println(gson.toJson(rs));
    rs.restoreStatus();
    //System.out.println(gson.toJson(rs));
    assertEquals(1, rs.getWorkers().size());
    assertNull(rs.getWorkers("147.46.117.72:8083"));
    assertEquals("147.46.117.72:8081", rs.getWorkers("147.46.117.72:8081").getId());
    assertEquals(2, rs.jq.size()); // Two jobs on the queue
    rsSt = rs.getJobs().stream().map(Job::getStatus).collect(Collectors.toSet());
    st = new HashSet<>(Arrays.asList(
        Job.Status.RUNNING.getValue(),
        Job.Status.WAITING.getValue(),
        Job.Status.WAITING.getValue()));
    assertTrue(rsSt.containsAll(st));
    assertEquals(rsSt.size(), st.size());
    assertEquals(Job.Status.WAITING.getValue(), rs.getJobs("pabs2").getStatus());
    assertEquals(Job.Status.RUNNING.getValue(), rs.getJobs("pabs").getStatus());
  }
}
