package se.code77.jq.util;

import java.util.concurrent.Callable;

import se.code77.jq.Promise;

public class Assert extends org.junit.Assert {
    public static <T> void assertPending(Promise<T> p) {
        assertTrue(p.isPending());
        assertFalse(p.isFulfilled());
        assertFalse(p.isRejected());
        assertFalse(p.isCancelled());
        assertFalse(p.isDone());
        Promise.StateSnapshot<T> snapshot = p.inspect();
        assertSame(snapshot.state, Promise.State.PENDING);
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
        assertSame(snapshot.state, Promise.State.FULFILLED);
        assertEquals(snapshot.value, expected);
        assertNull(snapshot.reason);
    }

    public static void assertRejected(Promise<?> p, Exception reason) {
        assertFalse(p.isPending());
        assertFalse(p.isFulfilled());
        assertTrue(p.isRejected());
        assertFalse(p.isCancelled());
        assertTrue(p.isDone());
        Promise.StateSnapshot<?> snapshot = p.inspect();
        assertSame(snapshot.state, Promise.State.REJECTED);
        assertNull(snapshot.value);
        assertSame(snapshot.reason, reason);
    }

    public static Exception assertThrows(Callable<?> task, Class<? extends Exception> expectedExceptionClass) {
        try {
            task.call();
            assertTrue(false);
            return null;
        } catch (Exception e) {
            assertSame(expectedExceptionClass, e.getClass());
            return e;
        }
    }
}
