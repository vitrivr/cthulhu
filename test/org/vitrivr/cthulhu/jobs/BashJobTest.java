package org.vitrivr.cthulhu.jobs;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BashJobTest {

  private void runCheck(String command, String stdOut) {
    runCheck(command, stdOut, null);
  }

  private void runCheck(String command, String stdOut, String stdErr) {
    BashJob bj = new BashJob(command);
    bj.execute();
    if (stdOut != null) {
      assertEquals(stdOut, bj.getStdOut());
    }
    if (stdErr != null) {
      assertEquals(stdErr, bj.getStdErr());
    }
  }

  @Test
  public void RunEcho() {
    runCheck("echo pablo", "pablo\n");
  }

  @Test
  public void RunLarge() {
    String twoEcho =
        "echo pablo\n" +
            "echo was\n" +
            "echo here";
    BashJob bj = new BashJob(twoEcho);
    bj.execute();
    assertEquals("pablo\nwas\nhere\n", bj.getStdOut());
  }
}
