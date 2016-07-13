package org.vitrivr.cthulhu.jobs;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import static org.junit.Assert.*;

import java.util.Properties;

import java.io.PrintWriter;
import java.io.InputStream;
import java.io.IOException;

import org.vitrivr.cthulhu.runners.CthulhuRunner;

import com.google.gson.*;

public class FeatureExtractionJobTest {
    static JobFactory jf;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Properties props = new Properties();
        try {
            InputStream input = CthulhuRunner.class.getClassLoader().getResourceAsStream("cthulhu.properties");
            props.load(input);
        } catch (IOException io) {
            throw new Exception("Could not read props");
        }
        CthulhuRunner.populateProperties(new String[0], props);
        //PrintWriter writr = new PrintWriter(System.out);
        //props.list(writr);
        //writr.flush();
        JobTools jbt = new JobTools(props, null);
        jf = new JobFactory();
        jf.setTools(jbt);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void make() {
    }
}
