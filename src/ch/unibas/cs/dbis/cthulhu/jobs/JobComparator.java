package ch.unibas.cs.dbis.cthulhu.jobs;

import ch.unibas.cs.dbis.cthulhu.jobs.Job;
import java.util.Comparator;

/**
 * JobComparator is a simple static class that compares jobs according to their priorities.
 * It can be reimplemented to account for other factors (specially creation date)
 */
public class JobComparator implements Comparator<Job> {
    @Override
        public int compare(Job x, Job y) {
        if(x.jobPriority() < y.jobPriority()) return -1;
        if(x.jobPriority() > y.jobPriority()) return 1;
        return 0;
    }
}
