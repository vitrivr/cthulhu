package org.vitrivr.cthulhu.worker;

import org.vitrivr.cthulhu.jobs.Job;
import java.util.HashSet;

public class Worker{ 
    String state;
    String address;
    HashSet<Job> jobs;
}
