package org.vitrivr.cthulhu.scheduler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class MainCoordinator {
    private static CoordinatorAPI api;
    private static MasterScheduler ms;
    private static Logger LOGGER = LogManager.getLogger("r.m");
    public static void main(String[] args) {
        LOGGER.info("Loading properties");
        Properties prop = new Properties();
        try {
            InputStream input = MainCoordinator.class.getClassLoader().getResourceAsStream("cthulhu.properties");
            prop.load(input);
        } catch (IOException io) {
            LOGGER.warn("Failed to load properties file. Using default settings.");
        }
        LOGGER.info("Starting up");
        APICLIThread cli = new APICLIThread();
        cli.start();

        ms = new MasterScheduler();
        api = new CoordinatorAPI();
        api.init(ms,prop);

        String nodisp = prop.getProperty("nodispatch");
        if(nodisp == null) { // We 'activate' the dispatching logic
            int dispatchDelay = 10;
            if(prop.getProperty("dispatchDelay") != null) {
                dispatchDelay = Integer.parseInt(prop.getProperty("dispatchDelay"));
            }
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(new Runnable() {
                    public void run() {
                        ms.runDispatch();
                    }
                }, dispatchDelay, dispatchDelay, TimeUnit.SECONDS);
        }
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
