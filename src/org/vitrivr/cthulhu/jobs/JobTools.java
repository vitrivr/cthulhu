package org.vitrivr.cthulhu.jobs;

import java.util.Properties;

import org.vitrivr.cthulhu.rest.CthulhuRESTConnector;

import org.vitrivr.cthulhu.worker.Worker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.URL;
import java.lang.ClassLoader;

public class JobTools {
    CthulhuRESTConnector conn;
    Properties props;
    Worker coord;
    Logger lg = LogManager.getLogger("r.job.tools");

    public JobTools(Properties props, CthulhuRESTConnector conn) {
        this.props = props;
        this.conn = conn;
    }
    public JobTools(Properties props, CthulhuRESTConnector conn, Worker coord) {
        this(props,conn);
        this.coord = coord;
    }
    public String getCineastLocation() {
        return props.getProperty("cineast_dir");
    }
    public String getJavaFlags() {
        return props.getProperty("cineast_java_flags","");
    }
    public boolean delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
        return true;
    }
    public void sendZipDirectory(String directory, String file_name) throws Exception {
        File fsDir = new File(directory);
        lg.info("Sending directory {} to host {} as file {}",
                directory, coord.getId(), file_name);
        conn.sendStream(fsDir, Zipper::zip, coord, file_name);
    }
    public boolean getFile(String filename, String workingDir) {
        File wdFl = new File(workingDir);
        File prseFname = new File(filename);
        File localFile = new File(wdFl, prseFname.getName());
        if(localFile.exists()) {
            // We have a problem: The file exists. What do we do?
            return true;
        }
        try {
            conn.getFile(coord, "/data/"+filename, localFile);
        } catch (Exception e) {
            lg.error("Trouble getting file {}. Exception: {}",
                     filename, e.toString());
            return false; // Error
        }
        return true;
    }
    public String setWorkingDirectory(Job j) {
        String workspaceDir = props.getProperty("workspace");
        File wsd = new File(workspaceDir);

        File dir = null;
        int tryCount = 0;
        String suffix = "";
        String fName = null;
        boolean created = false;
        while (dir == null || tryCount < 10) {
            fName = workspaceDir + "/" + j.getName() + suffix;
            dir = new File(fName);
            try {
                created = dir.mkdir();
            } catch (Exception e) {
                lg.error("Error when trying to create a working directory for {}. Exception: {}",
                         j.getName(), e.toString());
            }
            if(created == true) break;
            tryCount += 1;
            suffix = Integer.toString(tryCount);
        }
        if(dir == null || created == false) {
            lg.error("Error when trying to create working directory {}.", fName);
            return null; // ERROR
        }
        return fName;
    }
}
