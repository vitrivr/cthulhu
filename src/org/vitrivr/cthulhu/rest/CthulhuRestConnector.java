package org.vitrivr.cthulhu.rest;

import com.google.gson.Gson;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.BiPredicate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.worker.Worker;

/**
 * Class for sending requests between scheduler and workers.
 * @author <a href="mailto:mail@iampablo.me">Pablo E.</a>
 */
public class CthulhuRestConnector {

  /**
   * Protocol to be used when contacting to another host.
   */
  private static final String PROTOCOL = "http";
  /**
   * A gson instance to convert to JSON and to Job objects (or others) when exchanging data with
   * another host.
   */
  private Gson gson;

  public CthulhuRestConnector() {
    gson = new Gson();
  }

  /**
   * Private method that realizes a request to a different host. The method takes in the host it
   * shall communicate with, the method to use ("POST","GET",..), the path ("/jobs",..) and the body
   * contents.
   * <p>
   * If the host returns any text in the body of the response, this methor will return that text. If
   * an error occurs when communicating with the host, an exception will be thrown.
   * </p>
   *
   * @param method The method to use when realizing an http/https connection
   *     (POST,GET,PUT,DELETE)
   * @param path The path to access (/jobs, /jobs/jobName, /workers, etc.)
   * @param body The data to add in the body of the request (a Job's JSON definition, force
   *     parameter, etc)
   * @return The text returned by the remote host
   */
  private String makeRequest(Worker w, String method, String path, String body) throws IOException {
    URL workerUrl = new URL(PROTOCOL, w.getAddress(), w.getPort(), path);
    HttpURLConnection con = (HttpURLConnection) workerUrl.openConnection();
    con.setDoOutput(true);
    con.setChunkedStreamingMode(0);
    con.setRequestProperty("charset", "utf-8");
    con.setRequestMethod(method);
    if (body != null && !body.isEmpty()) {
      byte[] reqData = body.getBytes(StandardCharsets.UTF_8);
      int contLength = reqData.length;
      con.setRequestProperty("Content-Length", Integer.toString(contLength));
      OutputStream os = con.getOutputStream();
      os.write(reqData);
      os.flush();
    }
    InputStream in = new BufferedInputStream(con.getInputStream());
    String resBody = IOUtils.toString(in, "UTF-8");

    if (con.getResponseCode() != HttpURLConnection.HTTP_CREATED
        && con.getResponseCode() != HttpURLConnection.HTTP_OK) {
      throw new RuntimeException("Failed : HTTP error code : "
                                     + con.getResponseCode());
    }
    con.disconnect();
    return resBody;
  }

  /**
   * Realizes a 'GET' request for all jobs in the remote host.
   * <p>
   * Returns the json definition of a list of jobs. If an error occurs, an exception will be thrown.
   * Internally it uses the {@link #makeRequest(Worker, String, String, String) makeRequest}
   * method.
   * </p>
   *
   * @param w The object describing the remote host to communicate with.
   * @return The JSON definition of a list of jobs. An empty list if there are no jobs.
   */
  public String getJobs(Worker w) throws Exception {
    return makeRequest(w, "GET", "/jobs", "");
  }

  /**
   * Realizes a 'POST' request to send a job to a remote host.
   * <p>
   * If an error occurs, an exception will be thrown. Internally it uses the {@link
   * #makeRequest(Worker, String, String, String) makeRequest} method.
   * </p>
   *
   * @param w The object describing the remote host to communicate with.
   * @param j The job to send to the remote host
   */
  public void postJob(Job j, Worker w) throws Exception {
    makeRequest(w, "POST", "/jobs", gson.toJson(j));
  }

  /**
   * Realizes a 'PUT' request to update a job in a remote host.
   * <p>
   * If an error occurs, an exception will be thrown. Internally it uses the {@link
   * #makeRequest(Worker, String, String, String) makeRequest} method.
   * </p>
   *
   * @param w The object describing the remote host to communicate with.
   * @param j The job to put to the remote host
   */
  public void putJob(Job j, Worker w) throws Exception {
    makeRequest(w, "PUT", "/jobs", gson.toJson(j));
  }

  /**
   * Realizes a 'DELETE' request to delete a job in a remote host.
   * <p>
   * If force is `false`, and the job is running, an exception will be thrown. If force is `true`
   * and the job is running, it will be stopped and removed completely. If any errors occur, a
   * exception will be thrown. Internally it uses the {@link #makeRequest(Worker, String, String,
   * String) makeRequest} method.
   * </p>
   *
   * @param w The object describing the remote host to communicate with.
   * @param j The job to delete in the remote host
   */
  public void deleteJob(Job j, Worker w, boolean force) throws Exception {
    String forceStr = force ? "TRUE" : "FALSE";
    makeRequest(w, "DELETE", "/jobs/" + j.getName(), "force=" + forceStr);
  }

  /**
   * Realizes a 'POST' request to register a worker to a remote host.
   * <p>
   * This is normally called by a WORKER to inform a COORDINATOR that it is online and ready to
   * work. If an error occurs, an exception will be thrown. Internally it uses the {@link
   * #makeRequest(Worker, String, String, String) makeRequest} method.workerAddress
   * </p>
   *
   * @param coordinator The object describing the remote host to communicate with
   * @param workerAddress the address of the worker
   * @param workerPort the port the worker is communicating on
   */
  public void postWorker(
      Worker coordinator,
      String workerAddress, int workerPort) throws Exception {
    String input = "address=" + workerAddress + "&port=" + workerPort;
    makeRequest(coordinator, "POST", "/workers", input);
  }

  public void getFile(Worker host, String remoteFile, File localFile) throws IOException {
    URL workerUrl = new URL(PROTOCOL, host.getAddress(), host.getPort(), remoteFile);
    FileUtils.copyURLToFile(workerUrl, localFile);
  }

  /**
   * POST's a file to a worker.
   * @param sendFile The file to send
   * @param callback callback function that writes the file to a given location
   * @param worker the worker to send the job to
   * @param remoteFile the name of the file for the request uri and Content-Disposition header
   * @throws Exception when zipped directory fails to send
   */
  public void sendStream(
      File sendFile, BiPredicate<File, OutputStream> callback,
      Worker worker, String remoteFile) throws Exception {
    String boundary = "cthulhuBoundary123456789";
    URL workerUrl = new URL(PROTOCOL, worker.getAddress(), worker.getPort(),
                            "/data/" + remoteFile);
    HttpURLConnection con = (HttpURLConnection) workerUrl.openConnection();
    con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
    con.setDoOutput(true);
    con.setDoInput(true);
    con.setUseCaches(false);
    con.setRequestMethod("POST");
    OutputStream out = con.getOutputStream();
    String newLineFlag = "\r\n";
    PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(out));
    printWriter.print("--" + boundary + newLineFlag);
    printWriter.print(
        "Content-Disposition: form-data; name=\"file\"; filename=\"" + remoteFile + "\""
            + newLineFlag);
    printWriter.print("Content-Type: application/octet-stream" + newLineFlag);
    printWriter.print(newLineFlag);
    printWriter.flush();
    boolean res = callback.test(sendFile, out);
    out.flush();
    if (!res) {
      throw new Exception("Unable to send zipped directory");
    }
    printWriter.print(newLineFlag);
    printWriter.print("--" + boundary + "--" + newLineFlag);
    printWriter.flush();
    if (con.getResponseCode() != HttpURLConnection.HTTP_CREATED
        && con.getResponseCode() != HttpURLConnection.HTTP_OK) {
      throw new RuntimeException("Failed : HTTP error code : "
                                     + con.getResponseCode());
    }
    con.disconnect();
  }
}
