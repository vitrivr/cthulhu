package org.vitrivr.cthulhu.jobs;

import java.util.function.Consumer;

public class JobExecutor implements Runnable {
    Consumer<Job> callback;
    Job job;
    public JobExecutor(Consumer<Job> done, Job j) {
        this.callback = done;
        job = j;
    }
    public void run() {
        int result = job.execute();
        this.callback.accept(job);
    }
}
