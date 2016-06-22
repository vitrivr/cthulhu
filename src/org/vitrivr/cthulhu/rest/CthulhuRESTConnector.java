package org.vitrivr.cthulhu.rest;

import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.worker.Worker;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import com.google.gson.Gson;

public class CthulhuRESTConnector {
    final String PROTOCOL = "http";
    Gson gson;
    public CthulhuRESTConnector() {
        gson = new Gson();
    }
    String makeRequest(Worker w, String method, String path, String body) throws Exception {
        String resBody;
        try {
            URL workerUrl = new URL(PROTOCOL, w.getAddress(), w.getPort(), path);
            HttpURLConnection con = (HttpURLConnection) workerUrl.openConnection();
            con.setDoOutput(true);
            con.setChunkedStreamingMode(0);
            con.setRequestProperty("charset", "utf-8");
            con.setRequestMethod(method);
            if(body != null && !body.equals("")) {
                byte[] reqData = body.getBytes(StandardCharsets.UTF_8);
                int contLength = reqData.length;
                con.setRequestProperty("Content-Length", Integer.toString(contLength));
                OutputStream os = con.getOutputStream();
                os.write(reqData);
                os.flush();
            }
            InputStream in = new BufferedInputStream(con.getInputStream());
            resBody = IOUtils.toString(in, "UTF-8"); 

            if (con.getResponseCode() != HttpURLConnection.HTTP_CREATED &&
                con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : "
                                           + con.getResponseCode());
            }
            con.disconnect();
        } catch (Exception e) {
            throw e;
        }
        return resBody;
    }
    public String getJobs(Worker w) throws Exception {
        String res = makeRequest(w, "GET","/jobs","");
        return res;
    }
    public void postJob(Job j, Worker w) throws Exception {
        makeRequest(w,"POST", "/jobs",gson.toJson(j));
    }
    public void putJob(Job j, Worker w) throws Exception {
        makeRequest(w,"PUT", "/jobs",gson.toJson(j));
    }
    public void deleteJob(Job j, Worker w,String forceStr) throws Exception {
        makeRequest(w,"DELETE", "/jobs/"+j.getName(),"force="+forceStr);
    }
    public void postWorker(Worker coordinator,
                           String workerAddress, int workerPort)  throws Exception {
            String input = "address="+workerAddress+"&port="+workerPort;
            makeRequest(coordinator, "POST", "/workers", input);
    }
}
