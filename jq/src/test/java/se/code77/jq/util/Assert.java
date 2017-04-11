package se.code77.jq.util;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import se.code77.jq.Promise;

public class Assert extends org.junit.Assert {
    public static <T> void assertPending(Promise<T> p) {
        assertTrue(p.isPending());
        assertFalse(p.isFulfilled());
        assertFalse(p.isRejected());
        assertFalse(p.isCancelled());
        assertFalse(p.isDone());
        Promise.StateSnapshot<T> snapshot = p.inspect();
        assertSame(Promise.State.PENDING, snapshot.state);
        assertNull(snapshot.value);
        assertNull(snapshot.reason);
    }

    public static <T> void assertResolved(Promise<T> p, T expected) {
        assertFalse(p.isPending());
        assertTrue(p.isFulfilled());
        assertFalse(p.isRejected());
        assertFalse(p.isCancelled());
        assertTrue(p.isDone());
        Promise.StateSnapshot<T> snapshot = p.inspect();
        assertSame(Promise.State.FULFILLED, snapshot.state);
        assertEquals(expected, snapshot.value);
        assertNull(snapshot.reason);
    }

    public static void assertRejected(Promise<?> p) {
        assertFalse(p.isPending());
        assertFalse(p.isFulfilled());
        assertTrue(p.isRejected());
        assertFalse(p.isCancelled());
        assertTrue(p.isDone());
        Promise.StateSnapshot<?> snapshot = p.inspect();
        assertSame(Promise.State.REJECTED, snapshot.state);
        assertNull(snapshot.value);
    }

    public static void assertRejected(Promise<?> p, Exception reason) {
        assertRejected(p);
        assertSame(reason, p.inspect().reason);
    }

    public static void assertRejected(Promise<?> p, String reason) {
        assertRejected(p);
        assertSame(reason, p.inspect().reason.getMessage());
    }

    public static void assertRejected(Promise<?> p, Class<? extends Exception> reasonClass) {
        assertRejected(p);
        assertTrue(reasonClass.isAssignableFrom(p.inspect().reason.getClass()));
    }

    public static Exception assertThrows(Callable<?> task, Class<? extends Exception> expectedExceptionClass) {
        try {
            task.call();
            assertTrue(false);
            return null;
        } catch (Exception e) {
            assertTrue(expectedExceptionClass.isAssignableFrom(e.getClass()));
            return e;
        }
    }

    public static <T> void assertData(BlockingDataHolder<T> holder, long timeoutMillis, T expected) {
        try {
            assertEquals(expected, holder.get(timeoutMillis));
        } catch (TimeoutException e) {
            fail("Data not set");
        } catch (InterruptedException e) {
            fail("Test interrupted");
        }

    }

    public static <T> void assertData(BlockingDataHolder<T> holder, long timeoutMillis) {
        try {
            holder.get(timeoutMillis);
        } catch (TimeoutException e) {
            fail("Data was not set");
        } catch (InterruptedException e) {
            fail("Test interrupted");
        }
    }

    public static <T> void assertNoData(BlockingDataHolder<T> holder, long timeoutMillis) {
        try {
            holder.get(timeoutMillis);
            fail("Data was set");
        } catch (TimeoutException e) {
        } catch (InterruptedException e) {
            fail("Test interrupted");
        }
    }

}
