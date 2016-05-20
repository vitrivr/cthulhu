package org.vitrivr.cthulhu.scheduler;

import static spark.Spark.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class MasterAPI {
    private static MasterScheduler ms;
    private static Logger LOGGER = LogManager.getLogger("r.m");
    public static void main(String[] args) {
        LOGGER.info("Starting up");
        ms = new MasterScheduler();
        APICLIThread cli = new APICLIThread();
        cli.start();

        LOGGER.info("Creating REST paths");
        get("/jobs/:id", (req, res) -> {
                String result = req.params(":id");
                if(result.equals("")) res.status(400);
                return result;
            });
        get("/jobs", (req, res) -> {
                return "";
            });
        get("/workers/:id", (req, res) -> "Worker: "+req.params(":id"));
        get("/workers", (req, res) -> "Worker 1\nWorker 2\nWorker 3");

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

        delete("/jobs/:id", (req, res) -> "Created job 3");
        delete("/workers/:id", (req, res) -> "Deleted worker 4");
        LOGGER.info("Ready!");
    }

    private static final class APICLIThread extends Thread {
        @Override
        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) {
                        continue;
                    }
                    switch(line) {
                    case "exit":
                    case "quit":
                        LOGGER.info("Exiting");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Unrecognized command: "+line);
                    }
                }
            } catch (IOException e) {
                // Ignore the exception
            }
        }
    }
}
