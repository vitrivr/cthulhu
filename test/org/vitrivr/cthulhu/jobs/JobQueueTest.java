package org.vitrivr.cthulhu.jobs;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import static org.junit.Assert.*;

public class JobQueueTest {
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Test
    public void BasicTest() {
        JobQueue jq = new JobQueue();
        BashJob lowPri = new BashJob("",2); // Low priority job
        BashJob hiestPri = new BashJob("",0); // Second high priority job        
        BashJob hiPri = new BashJob("",1); // High priority job

        jq.push(lowPri);
        jq.push(hiestPri);
        jq.push(hiPri);
        assertEquals(hiestPri, jq.pop());
        assertEquals(hiPri,jq.pop());
        assertEquals(lowPri,jq.pop());
    }

    @Test
    public void CreatedTimeTest() {
        JobQueue jq = new JobQueue();
        BashJob lowPri = new BashJob("",2); // Low priority job
        try {
            Thread.sleep(100);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        BashJob laterLowPri = new BashJob("",2); // Second high priority job
        try {
            Thread.sleep(100);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        BashJob latestLowPri = new BashJob("",2);

        jq.push(latestLowPri);
        jq.push(lowPri);
        jq.push(laterLowPri);
        assertEquals(lowPri,jq.pop());
        assertEquals(laterLowPri, jq.pop());
        assertEquals(latestLowPri, jq.pop());
    }
}
