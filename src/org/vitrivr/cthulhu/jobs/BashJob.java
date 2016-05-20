package org.vitrivr.cthulhu.jobs;

import java.lang.ProcessBuilder;
import java.lang.Process;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;

import java.lang.InterruptedException;
import java.io.IOException;


public class BashJob extends Job {
    String command;
    String stdOut;
    String stdErr;

    public BashJob(){
    }

    public BashJob(String command) {
        this.command = command;
        this.type = "BashJob";
    }
    public BashJob(String command, int priority) {
        this.command = command;
        this.priority = priority;
        this.type = "BashJob";
    }

    public int execute() {
        ProcessBuilder pb = new ProcessBuilder("sh");
        Process p;
        try {
            p = pb.start();
            OutputStream os = p.getOutputStream();
            os.write(this.command.getBytes(Charset.forName("UTF-8")));
            os.flush();
            os.close();

            InputStream is = p.getInputStream();
            InputStream es = p.getErrorStream();
            
            this.stdOut = IOUtils.toString(is,"UTF-8");
            this.stdErr = IOUtils.toString(es,"UTF-8");
        } catch (IOException e) {
            this.res = Job.PROGRAM_FAILURE;
            return this.res;
        }
        try {
            this.res = p.waitFor();
        } catch (InterruptedException e) {
            this.res = Job.JOB_INTERRUPTION;
        } finally {
            return this.res;
        }
    }

    public String getStdOut() { return this.stdOut; }
    public String getStdErr() { return this.stdErr; }
}
