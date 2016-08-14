package org.vitrivr.cthulhu.rest;

import org.vitrivr.cthulhu.scheduler.CthulhuScheduler;

import org.vitrivr.cthulhu.jobs.StreamUtils;

import static spark.Spark.*;

import javax.servlet.MultipartConfigElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileNotFoundException;

import java.util.Properties;

import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.worker.Worker;

public class CthulhuREST {
    private String workspace;
    private CthulhuScheduler ms;
    private Logger LOGGER = LogManager.getLogger("r.m.api");
    /**
     * A gson instance to convert to JSON and to Job objects (or others) when exchanging data with another host.
     */
    private Gson gson;
    private int port;
    /**
     * Initializes all the REST services for this agent (worker/coordinator)
     * <p>
     * If no port can be determined to listen on, an exception will be thrown
     *
     * @param ms This is the scheduler that is in charge of managing microservice REST calls
     * @param prop This is a properties object with all runtime properties of the agent (worker/coord)
     */
    public void init(CthulhuScheduler ms, Properties prop) throws Exception {
        this.ms = ms;
        gson = new Gson();
        LOGGER.info("Creating REST paths");
        String sf = prop.getProperty("staticfiles");
        workspace = prop.getProperty("workspace");
        try {
            port = Integer.parseInt(prop.getProperty("port"));
        } catch (Exception e) { throw e; }
        setupRESTCalls(sf,port);
        LOGGER.info("Ready!");
    }

    /**
     * Stops the REST service.
     * <p>
     * Stops the Jetty server that is in charge of running the REST service.
     */
    public void stopServer() {
        stop();
    }

    public void setupRESTCalls(String staticFilesDir, int listenPort) {
        port(listenPort);
        LOGGER.info("Static files are served from: "+staticFilesDir);
        staticFileLocation(staticFilesDir);
        get("/data/*", (req, res) -> {
                String fname = req.pathInfo().replaceFirst("/data/", "");
                fname = fname.replace("../", "/");
                InputStream is;
                try {
                    File infile = new File(workspace, fname);
                    is = new FileInputStream(infile);
                    if (is != null) {
                        //res.type(getContentType(fname));
                        res.status(200);

                        byte[] buf = new byte[1024];
                        OutputStream os = res.raw().getOutputStream();
                        int count = 0;
                        while ((count = is.read(buf)) >= 0) {
                            os.write(buf, 0, count);
                        }
                        is.close();
                        os.close();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                return "";
            });
        get("/jobs/:id", (req, res) -> {
                String id = req.params(":id");
                if(id.isEmpty()) res.status(400);
                return gson.toJson(ms.getJobs(id));
            });
        get("/jobs", (req, res) -> gson.toJson(ms.getJobs()) );
        get("/workers/:id", (req, res) -> {
                String id = req.params(":id");
                if(id.isEmpty()) res.status(400);
                return gson.toJson(ms.getWorkers(id));
            });
        get("/workers", (req, res) -> gson.toJson(ms.getWorkers()) );
        post("/data/*", (req, res) -> {
                String fname = req.pathInfo().replaceFirst("/data/", "");
                fname = fname.replace("../", "/");
                System.out.println("Receiving a file back: "+fname);
                File fout = new File(fname);
                if(fout.exists()) {
                    System.out.println("File exists. Not saving");
                    res.status(400);
                    //return "";
                }
                req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
                try (InputStream is = req.raw().getPart("file").getInputStream()) {
                        // Use the input stream to create a file
                        FileOutputStream fos = new FileOutputStream(fname);
                        StreamUtils.copy(is, fos);
                    }
                return "";
            });
        post("/jobs", (req, res) -> {
                Job j = ms.registerJob(req.body());
                if(j == null) res.status(400);
                return "";
            });
        post("/workers", (req, res) -> {
                String address = req.queryParams("address");
                int port = Integer.parseInt(req.queryParams("port"));
                String ip = req.ip();
                if(address == null || address.isEmpty()) address = ip;
                int st = ms.registerWorker(address, port);
                if(st != 0) res.status(400);
                return "";
            });

        delete("/jobs/:id", (req, res) -> {
                String id = req.params(":id");
                boolean force = false;
                if(req.queryParams("force") != null) force = Boolean.parseBoolean(req.queryParams("force"));
                else if(req.params("force") != null) force = Boolean.parseBoolean(req.params("force"));
                Job job;
                try {
                    job = ms.deleteJob(id,force);
                } catch (Exception e) {
                    res.status(400);
                    return e.getMessage();
                }
                if(job == null) res.status(400);
                return "";
            });
        delete("/workers/:id", (req, res) -> {
                String id = req.params(":id");
                Worker worker = ms.deleteWorker(id);
                if(worker == null) res.status(400);
                return "";
            });
        put("/jobs",(req,res) -> {
                Job old = ms.updateJob(req.body());
                if(old == null) res.status(400);
                return "";
            });
    }
}
