package org.vitrivr.cthulhu.scheduler;

import static spark.Spark.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MasterAPI {
    private static MasterScheduler ms;
    public static void main(String[] args) {
        ms = new MasterScheduler();
        /* Registering all the REST calls*/
        get("/jobs", (req, res) -> "Job 1\nJob2");
        get("/job/:id", (req, res) -> "Job: "+req.params(":id"));
        get("/workers", (req, res) -> "Worker 1\nWorker 2\nWorker 3");
        get("/worker/:id", (req, res) -> "Worker: "+req.params(":id"));

        post("/job", (req, res) -> "Created job 3");
        post("/worker", (req, res) -> {
                int st = ms.RegisterWorker((String) req.attribute("address"),req.ip(), 12345);
                if(st != 0) res.status(400);
                return "";
            });

        delete("/job/:id", (req, res) -> "Created job 3");
        delete("/worker/:id", (req, res) -> "Deleted worker 4");
    }
}
