package org.vitrivr.cthulhu.runners;

import org.vitrivr.cthulhu.rest.CthulhuREST;
import org.vitrivr.cthulhu.scheduler.CthulhuScheduler;
import org.vitrivr.cthulhu.scheduler.SchedulerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CthulhuRunner {
    private static CthulhuREST api;
    private static CthulhuScheduler ms;
    private static Logger LOGGER = LogManager.getLogger("r.m");
    public static void main(String[] args) {
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

        SchedulerFactory sf = new SchedulerFactory();
        ms = sf.createScheduler("coordinator", prop); // Update later
        api = new CthulhuREST();
        api.init(ms,prop);
        
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
                        LOGGER.info("Exiting...");
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
