package org.vitrivr.cthulhu.runners;

import org.vitrivr.cthulhu.rest.CthulhuREST;
import org.vitrivr.cthulhu.scheduler.MasterScheduler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

abstract class CthulhuRunner {
    private CthulhuREST api;
    private MasterScheduler ms;
    private Logger LOGGER = LogManager.getLogger("r.m");
    public void start(String[] args) {
        LOGGER.info("Loading properties");
        Properties prop = new Properties();
        try {
            InputStream input = CthulhuRunner.class.getClassLoader().getResourceAsStream("cthulhu.properties");
            prop.load(input);
        } catch (IOException io) {
            LOGGER.warn("Failed to load properties file. Using default settings.");
        }
        LOGGER.info("Starting up");
        APICLIThread cli = new APICLIThread();
        cli.start();

        ms = new MasterScheduler();
        api = new CthulhuREST();
        api.init(ms,prop);

        String nodisp = prop.getProperty("nodispatch");
        if(nodisp == null) { // We 'activate' the dispatching logic
            int dispatchDelay = 10;
            if(prop.getProperty("dispatchDelay") != null) {
                dispatchDelay = Integer.parseInt(prop.getProperty("dispatchDelay"));
            }
            setupCoordinator(dispatchDelay);
        }
        setupWorker();
    }

    abstract public void setupCoordinator(int delay);
    abstract public void setupWorker();

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
