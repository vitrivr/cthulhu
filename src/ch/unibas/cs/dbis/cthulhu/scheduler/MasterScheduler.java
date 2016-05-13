package ch.unibas.cs.dbis.cthulhu.scheduler;

import ch.unibas.cs.dbis.cthulhu.jobs.Job;
import ch.unibas.cs.dbis.cthulhu.jobs.JobFactory;
import ch.unibas.cs.dbis.cthulhu.jobs.JobQueue;

public class MasterScheduler {
    private JobQueue jq;
    public MasterScheduler() {
        jq = new JobQueue();
    }
}
