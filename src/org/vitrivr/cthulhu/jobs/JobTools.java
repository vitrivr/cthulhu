package org.vitrivr.cthulhu.jobs;

import java.util.Properties;

import org.vitrivr.cthulhu.rest.CthulhuRESTConnector;

import org.vitrivr.cthulhu.worker.Worker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class JobTools {
    CthulhuRESTConnector conn;
    Properties props;
    Worker coord;
    private static Logger lg = LogManager.getLogger("r.job.tools");

    public JobTools(Properties props, CthulhuRESTConnector conn) {
        this.props = props;
        this.conn = conn;
    }
    public JobTools(Properties props, CthulhuRESTConnector conn, Worker coord) {
        this(props,conn);
        this.coord = coord;
    }
    
    public String setWorkingDirectory(Job j) {
        String workspaceDir = props.getProperty("workspace");
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
            lg.error("Error when trying to create working directory {}. Directory exists.", fName);
            return null; // ERROR
        }
        return fName;
    }
}
