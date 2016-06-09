package org.vitrivr.cthulhu.rest;

import org.vitrivr.cthulhu.scheduler.CthulhuScheduler;

import static spark.Spark.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Properties;

import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.worker.Worker;

public class CthulhuREST {
    private CthulhuScheduler ms;
    private Logger LOGGER = LogManager.getLogger("r.m.api");
    private Gson gson;
    public void init(CthulhuScheduler ms, Properties prop) {
        //ms = new MasterScheduler();
        this.ms = ms;
        gson = new Gson();

        LOGGER.info("Creating REST paths");
        String sf = prop.getProperty("staticfiles");
        int port = Integer.parseInt(prop.getProperty("port") != null ? prop.getProperty("port") : "8082");
        setupRESTCalls(sf,port);
        LOGGER.info("Ready!");
    }

    public void setupRESTCalls(String staticFilesDir, int listenPort) {
        port(listenPort);
        LOGGER.info("Static files are served from: "+staticFilesDir);
        staticFileLocation(staticFilesDir);
        get("/jobs/:id", (req, res) -> {
                String id = req.params(":id");
                if(id.equals("")) res.status(400);
                return gson.toJson(ms.getJobs(id));
            });
        get("/jobs", (req, res) -> gson.toJson(ms.getJobs()) );
        get("/workers/:id", (req, res) -> {
                String id = req.params(":id");
                if(id.equals("")) res.status(400);
                return gson.toJson(ms.getWorkers(id));
            });
        get("/workers", (req, res) -> gson.toJson(ms.getWorkers()) );

        post("/jobs", (req, res) -> {
                int st = ms.registerJob(req.body());
                if(st != 0) res.status(400);
                return "";
            });
        post("/workers", (req, res) -> {
                String address = req.attribute("address");
                int port = Integer.parseInt(req.attribute("port"));
                int st = ms.registerWorker(address,req.ip(), port);
                if(st != 0) res.status(400);
                return "";
            });

        delete("/jobs/:id", (req, res) -> {
                String id = req.params(":id");
                boolean force = false;
                if(req.attribute("force") != null) force = Boolean.parseBoolean(req.attribute("force"));
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
        put("/jobs/:id",(req,res) -> {
                return "";
            });
    }
}
