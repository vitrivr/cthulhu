package org.vitrivr.cthulhu.jobs;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.*;

import com.google.gson.*;

public class JobFactoryTest {
    static JobFactory jf;
    @Rule
    public final ExpectedException exception = ExpectedException.none();

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
    @Test
    public void typelessJob() {
        String json = "{\"priority\":3}";
        exception.expect(JsonParseException.class);
        Job job = jf.buildJob(json);
    }
    @Test
    public void malformedJob() {
        String json = "Paoufbivsett}";
        exception.expect(JsonParseException.class);
        Job job = jf.buildJob(json);
    }
}
