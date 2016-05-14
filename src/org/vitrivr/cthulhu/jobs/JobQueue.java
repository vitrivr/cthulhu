package org.vitrivr.cthulhu.jobs;

import java.util.PriorityQueue;

public class JobQueue {
    private PriorityQueue<Job> pq;
    public JobQueue() {
        pq = new PriorityQueue<Job>();
    }
    public void push(Job j) { pq.add(j); }
    public Job pop() { return pq.poll(); }
    public Job peek() { return pq.peek(); }
    public int size() { return pq.size(); }
}
