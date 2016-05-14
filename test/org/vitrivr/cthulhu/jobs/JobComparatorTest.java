package org.vitrivr.cthulhu.jobs;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import java.util.PriorityQueue;

public class JobComparatorTest {
    static JobComparator jc;
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        JobComparatorTest.jc = new JobComparator();
    }

    @Test
    public void BasicTest() {
        BashJob lowPri = new BashJob("",2); // Low priority job
        BashJob hiPri = new BashJob("",1); // High priority job
        BashJob hiPriToo = new BashJob("",1); // Second high priority job
        assertEquals(JobComparatorTest.jc.compare(lowPri,hiPri),1);
        assertEquals(JobComparatorTest.jc.compare(hiPri,lowPri),-1);
        assertEquals(JobComparatorTest.jc.compare(hiPri,hiPriToo),0);
    }

    @Test
    public void PQTest() {
        PriorityQueue<Job> pq = new PriorityQueue<Job>(10,JobComparatorTest.jc);
        BashJob lowPri = new BashJob("",2); // Low priority job
        BashJob hiestPri = new BashJob("",0); // Second high priority job        
        BashJob hiPri = new BashJob("",1); // High priority job
        pq.add(lowPri);
        pq.add(hiestPri);
        pq.add(hiPri);
        assertEquals(hiestPri, pq.poll());
        assertEquals(hiPri,pq.poll());
        assertEquals(lowPri,pq.poll());
    }
}

