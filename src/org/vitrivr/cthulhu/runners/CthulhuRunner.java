package org.vitrivr.cthulhu.runners;

import org.vitrivr.cthulhu.rest.CthulhuREST;
import org.vitrivr.cthulhu.scheduler.CthulhuScheduler;
import org.vitrivr.cthulhu.scheduler.SchedulerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

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
    public enum RunnerType {
        COORDINATOR,
        WORKER
    }
    static RunnerType type = RunnerType.COORDINATOR;

    static CommandLine parseCommandLine(String[] args) {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("h","help",false,"Display help menu");
        options.addOption("C","coordinator",false,"Run as coordinator");
        options.addOption("W","worker",false,"Run as worker");
        options.addOption("ha","host",true,"Address of coordinator host [for workers]");
        options.addOption("hp","hostPort",true,"Port of coordinator host [for workers]");
        CommandLine line;
        try {
            line = parser.parse(options,args);
        } catch (ParseException exp) {
            line = null;
            LOGGER.error("ERROR Parsing command line.");
        }
        if(line != null && line.hasOption("W")) {
            type = RunnerType.WORKER; // Changing the runner type
            // If it's a woker, but no COORDINATOR information is input
            if(!line.hasOption("ha") || !line.hasOption("hp")) line = null;
        }
        if(line == null || line.hasOption("h")) {
            String header = "Run the Cthulhu task scheduler\n\n";
            String footer = "\nPlease report issues at http://github.com/vitrivr/cthulhu/issues";
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Cthulhu", header, options, footer, true);
            line = null; // To instruct the main method to leave
        }
        return line;
    }

    public static void main(String[] args) {
        LOGGER.info("Reading command line arguments");
        CommandLine line = parseCommandLine(args);
        if(line == null) return;

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
        ms = sf.createScheduler(type, prop); // Update later
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
