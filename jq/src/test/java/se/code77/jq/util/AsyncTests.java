package se.code77.jq.util;

import org.junit.After;
import org.junit.Before;

public class AsyncTests {
    protected static final String TEST_VALUE1 = "Hello";
    protected static final String TEST_VALUE2 = "World";
    protected static final String TEST_VALUE3 = "JQ";
    protected static final Integer TEST_INTEGER1 = 42;
    protected static final Double TEST_DOUBLE1 = 1.0;
    protected static final Exception TEST_REASON1 = new IllegalArgumentException("foo");
    protected static final Exception TEST_REASON2 = new IllegalArgumentException("bar");

    @Before
    public void setup() {
        TestConfig.start();
    }

    @After
    public void tearDown() {
        TestConfig.stop();
    }
}
