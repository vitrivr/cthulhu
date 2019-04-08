package org.vitrivr.cthulhu.jobs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;


public class BashJob extends Job {

  private String action;

  protected BashJob() {
    super();
  }

  protected BashJob(String action) {
    this(action, 2);
  }

  protected BashJob(String action, int priority) {
    super();
    this.action = action;
    this.priority = priority;
  }

  /**
   * Executes the bash command given.
   * @return the status of the execution
   */
  public int execute() {
    ProcessBuilder pb = new ProcessBuilder("sh");
    Process p;
    try {
      p = pb.start();
      OutputStream os = p.getOutputStream();
      os.write(this.action.getBytes(Charset.forName("UTF-8")));
      os.flush();
      os.close();

      int retVal = waitForProcess(p);
      if (retVal == 0) {
        status = Job.Status.SUCCEEDED;
      }
    } catch (IOException e) {
      status = Job.Status.FAILED;
      return status.getValue();
    } catch (InterruptedException e) {
      status = Job.Status.INTERRUPTED;
    }
    return status.getValue();
  }

  /**
   * Returns the action property of the job. The main action of it.
   */
  public String getAction() {
    return action;
  }
}
