package se.code77.jq;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import se.code77.jq.JQ.Deferred;
import se.code77.jq.Promise.OnFulfilledCallback;
import se.code77.jq.Promise.UnhandledRejectionException;
import se.code77.jq.util.AsyncTests;
import se.code77.jq.util.BlockingDataHolder;
import se.code77.jq.util.DataFulfilledCallback;
import se.code77.jq.util.DataRejectedCallback;
import se.code77.jq.util.SlowTask;
import se.code77.jq.util.TestConfig;

import static se.code77.jq.util.Assert.assertData;
import static se.code77.jq.util.Assert.assertEquals;
import static se.code77.jq.util.Assert.assertFalse;
import static se.code77.jq.util.Assert.assertNoData;
import static se.code77.jq.util.Assert.assertNotNull;
import static se.code77.jq.util.Assert.assertPending;
import static se.code77.jq.util.Assert.assertRejected;
import static se.code77.jq.util.Assert.assertResolved;
import static se.code77.jq.util.Assert.assertSame;
import static se.code77.jq.util.Assert.assertThrows;
import static se.code77.jq.util.Assert.assertTrue;

public class ValueTests extends AsyncTests {

    @Test
    public void value_get() {
        Value<String> value = Value.wrap(TEST_VALUE1);

        assertEquals(TEST_VALUE1, value.get());
    }

    @Test
    public void value_getWithTimeout() {
        Value<String> value = Value.wrap(TEST_VALUE1);

        assertEquals(TEST_VALUE1, value.get(1, TimeUnit.NANOSECONDS));
    }

    @Test
    public void value_cancel() {
        Value<String> value = Value.wrap(TEST_VALUE1);

        assertEquals(false, value.cancel(true));
        assertEquals(false, value.cancel(false));
    }

    @Test
    public void value_isCancelled() {
        Value<String> value = Value.wrap(TEST_VALUE1);

        assertEquals(false, value.isCancelled());
        assertEquals(false, value.cancel(true));
        assertEquals(false, value.isCancelled());
    }

    @Test
    public void value_isDone() {
        Value<String> value = Value.wrap(TEST_VALUE1);

        assertEquals(true, value.isDone());
        assertEquals(false, value.cancel(true));
        assertEquals(true, value.isDone());
    }
}
