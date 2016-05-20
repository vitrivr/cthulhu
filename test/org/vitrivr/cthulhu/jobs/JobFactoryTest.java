package org.vitrivr.cthulhu.jobs;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import static org.junit.Assert.*;

public class JobFactoryTest {
    static JobFactory jf;
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        jf = new JobFactory();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void buildBasic() {
        String json = "{\"type\":\"BashJob\"}";
        Job job = jf.buildJob(json);
        assertEquals(job.getClass(), BashJob.class);
    }
    @Test
    public void defaultPriority() {
        String json = "{\"type\":\"BashJob\"}";
        Job job = jf.buildJob(json);
        assertEquals(job.getPriority(), 2);
    }
    @Test
    public void customPriority() {
        String json = "{\"type\":\"BashJob\",\"priority\":3}";
        Job job = jf.buildJob(json);
        assertEquals(job.getPriority(), 3);
    }
    @Test
    public void bashType() {
        String json = "{\"type\":\"BashJob\",\"priority\":3}";
        Job job = jf.buildJob(json);
        assertEquals(job.getType(), "BashJob");
    }
}
