package org.vitrivr.cthulhu.runners;

import org.vitrivr.cthulhu.rest.CthulhuREST;
import org.vitrivr.cthulhu.scheduler.CthulhuScheduler;
import org.vitrivr.cthulhu.scheduler.CoordinatorScheduler;
import org.vitrivr.cthulhu.scheduler.SchedulerFactory;
import org.vitrivr.cthulhu.jobs.JobAdapter;
import org.vitrivr.cthulhu.jobs.Job;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

import java.util.Enumeration;
import java.net.NetworkInterface;
import java.net.InetAddress;

public class CthulhuRunner {
    private static CthulhuREST api;
    private static CthulhuScheduler ms;
    private static Logger LOGGER = LogManager.getLogger("r.m");
    public enum RunnerType {
        COORDINATOR,
        WORKER
    }
    static RunnerType type = RunnerType.COORDINATOR;

    // Returns first available non-loopback, active IP address
    static String getIPAddress() {
        String ip = null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if(addr.isLinkLocalAddress()) continue;
                    ip = addr.getHostAddress();
                    return ip;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "127.0.0.1"; // If no IP was found earlier, just return loopback interface
    }

    static CommandLine populateProperties(String[] args, Properties prop) throws Exception {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("h","help",false,"Display help menu");
        options.addOption("C","coordinator",false,"Run as coordinator");
        options.addOption("W","worker",false,"Run as worker");
        options.addOption("ha","hostAddress",true,"Address of coordinator host [for workers] - overrides properties file");
        options.addOption("hp","hostPort",true,"Port of coordinator host [for workers] - overrides properties file");
        options.addOption("c","capacity",true,"Capacity, or number of jobs that can run simultaneously [for workers]");
        options.addOption("p","port",true,"Port in which to listen to - overrides properties file");
        options.addOption("a","address",true,"Address of the local host (DNS? IP?) - overrides properties file");
        options.addOption("sf","staticFiles",true,"Directory where the static files will be read from (worker)");
        options.addOption("r","restore",true,"Restore instance from status file");
        CommandLine line;
        try {
            line = parser.parse(options,args);
        } catch (ParseException exp) {
            line = null;
            LOGGER.error("ERROR Parsing command line.");
        }

        if(line != null && line.hasOption("W")) {
            LOGGER.info("Starting up as worker");
            type = RunnerType.WORKER; // Changing the runner type

            String hostAddress = null;
            if(prop.getProperty("hostAddress") != null) hostAddress = prop.getProperty("hostAddress");
            if(line.hasOption("ha")) hostAddress = line.getOptionValue("ha");
            prop.setProperty("hostAddress",hostAddress);

            String hostPort = "8082"; // Default port
            if(prop.getProperty("hostPort") != null) hostPort = prop.getProperty("hostPort");
            if(line.hasOption("hp")) hostPort = line.getOptionValue("hp");
            prop.setProperty("hostPort",hostPort);

            if(hostAddress == null) line = null; // Host address is unknown. Can't continue.
        }
        if(line.hasOption("r")) {
            prop.setProperty("restoreFile",line.getOptionValue("r"));
        }
        String staticFiles = type == RunnerType.WORKER ? "/workspace" : "/ui"; // Default values for worker:coordinator
        if(prop.getProperty("staticfiles") != null) staticFiles = prop.getProperty("staticfiles");
        if(line.hasOption("sf")) staticFiles = line.getOptionValue("sf");
        prop.setProperty("staticfiles",staticFiles);
        if((line != null && line.hasOption("p")) || prop.getProperty("port") == null) {
            LOGGER.info("Setting up port to listen on");
            String defaultPort = prop.getProperty("port") != null ? prop.getProperty("port") : "8082";
            prop.setProperty("port", (line.hasOption("p") ? line.getOptionValue("p") : defaultPort));
        }
        if((line != null && line.hasOption("a")) || prop.getProperty("address") == null) {
            LOGGER.info("Setting host address");
            String defaultIp = prop.getProperty("address") != null ? prop.getProperty("address") : getIPAddress();
            prop.setProperty("address", (line.hasOption("a") ? line.getOptionValue("a") : defaultIp));
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

    public static void restoreRun(Properties prop) throws Exception {
        Gson restoreGson = new GsonBuilder()
            .registerTypeAdapter(Job.class, new JobAdapter())
            .create();
        String jsonFile = prop.getProperty("restoreFile");
        String jsonContents;
        try {
            InputStream is = new FileInputStream(jsonFile);
            jsonContents = IOUtils.toString(is,"UTF-8");
        } catch (Exception e) {
            LOGGER.error("Unable to restore from file {}. Exception: {}",jsonFile,e.toString());
            throw e;
        }
        CthulhuScheduler rs = restoreGson.fromJson(jsonContents, CoordinatorScheduler.class);
        System.out.println(restoreGson.toJson(rs));
        rs.restoreStatus();
        ms = rs;
    }

    public static void main(String[] args) throws Exception {
        LOGGER.info("Loading properties");
        Properties prop = new Properties();
        try {
            InputStream input = CthulhuRunner.class.getClassLoader().getResourceAsStream("cthulhu.properties");
            prop.load(input);
        } catch (IOException io) {
            LOGGER.warn("Failed to load properties file. Using default settings.");
        }
        try {
            LOGGER.info("Reading command line arguments");
            CommandLine line = populateProperties(args,prop);
            if(line == null) return;
            
            LOGGER.info("Starting up");
            APICLIThread cli = new APICLIThread();
            cli.start();
            if(prop.getProperty("restoreFile") != null) {
                restoreRun(prop);
                prop = ms.getProperties();
            } else {
                SchedulerFactory sf = new SchedulerFactory();
                ms = sf.createScheduler(type, prop); // Update later
            }
            ms.init();
            api = new CthulhuREST();
            api.init(ms,prop);
        } catch (Exception e) {
            LOGGER.info("Exception has been thrown during startup.");
            if(ms != null) ms.stop();
            if(api != null) api.stopServer();
            System.exit(1);
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
