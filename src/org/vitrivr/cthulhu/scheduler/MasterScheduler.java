package org.vitrivr.cthulhu.scheduler;

import org.vitrivr.cthulhu.jobs.Job;
import org.vitrivr.cthulhu.jobs.JobFactory;
import org.vitrivr.cthulhu.jobs.JobQueue;

public class MasterScheduler {
    private JobQueue jq;
    public MasterScheduler() {
        jq = new JobQueue();
    }
}
