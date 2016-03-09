package se.code77.jq.util;

import org.junit.After;
import org.junit.Before;

public class AsyncTests {
    @Before
    public void setup() {
        TestConfig.start();
    }

    @After
    public void tearDown() {
        TestConfig.stop();
    }
}
