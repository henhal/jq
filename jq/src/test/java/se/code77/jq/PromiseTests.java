package se.code77.jq;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static se.code77.jq.util.Assert.*;

import se.code77.jq.JQ.Deferred;
import se.code77.jq.Promise.OnFulfilledCallback;
import se.code77.jq.Promise.OnRejectedCallback;
import se.code77.jq.util.AsyncTests;

public class PromiseTests extends AsyncTests {
    private static final String TEST_VALUE1 = "Hello";
    private static final String TEST_VALUE2 = "World";
    private static final Exception TEST_REASON1 = new IllegalArgumentException("foo");
    private static final Exception TEST_REASON2 = new IllegalArgumentException("bar");

    private static class SlowTask<T> implements Callable<T> {
        public final T value;

        public SlowTask(T value) {
            this.value = value;
        }

        @Override
        public T call() throws Exception {
            Thread.sleep(1000);
            return value;
        }
    }

    @Test
    public void pending_isPending() throws Exception {
        Deferred<String> deferred = JQ.defer();
        Promise<String> p = deferred.promise;

        assertPending(p);
        final Semaphore sem = new Semaphore(0);

        p.then(new OnFulfilledCallback<String, Void>() {
            @Override
            public Future<Void> onFulfilled(String value) throws Exception {
                sem.release();
                return null;
            }
        });

        assertFalse(sem.tryAcquire(2, TimeUnit.SECONDS));
        deferred.resolve(TEST_VALUE1);
    }

    @Test
    public void resolved_isResolved() throws Exception {
        Deferred<String> deferred = JQ.defer();
        Promise<String> p = deferred.promise;

        final Semaphore sem = new Semaphore(0);

        p.then(new OnFulfilledCallback<String, Void>() {
            @Override
            public Future<Void> onFulfilled(String value) throws Exception {
                sem.release();
                return null;
            }
        });

        deferred.resolve(TEST_VALUE1);

        assertTrue(sem.tryAcquire(2, TimeUnit.SECONDS));
        assertResolved(p, TEST_VALUE1);
    }


    @Test
    public void rejected_isRejected() throws Exception {
        Deferred<String> deferred = JQ.defer();
        Promise<String> p = deferred.promise;

        final Semaphore sem = new Semaphore(0);

        p.fail(new OnRejectedCallback<Void>() {
            @Override
            public Future<Void> onRejected(Exception reason) throws Exception {
                sem.release();
                return null;
            }
        });

        IllegalArgumentException reason = new IllegalArgumentException("Foobar");
        deferred.reject(reason);

        assertTrue(sem.tryAcquire(2, TimeUnit.SECONDS));
        assertRejected(p, reason);
    }

    @Test
    public void preResolved_isResolved() throws Exception {
        Promise<String> p = JQ.resolve(TEST_VALUE1);

        final Semaphore sem = new Semaphore(0);

        p.then(new OnFulfilledCallback<String, Void>() {
            @Override
            public Future<Void> onFulfilled(String value) throws Exception {
                sem.release();
                return null;
            }
        });

        assertTrue(sem.tryAcquire(2, TimeUnit.SECONDS));
        assertResolved(p, TEST_VALUE1);
    }

    @Test
    public void preRejected_isRejected() throws Exception {
        IllegalArgumentException reason = new IllegalArgumentException("Foobar");
        Promise<Void> p = JQ.reject(reason);

        final Semaphore sem = new Semaphore(0);

        p.fail(new OnRejectedCallback<Void>() {
            @Override
            public Future<Void> onRejected(Exception reason) throws Exception {
                sem.release();
                return null;
            }
        });

        assertTrue(sem.tryAcquire(2, TimeUnit.SECONDS));
        assertRejected(p, reason);
    }

    @Test
    public void resolved_isNotRejected() {

    }

    @Test
    public void rejected_isNotResolved() {

    }

    @Test
    public void resolved_isImmutable() {
        final Deferred<String> deferred = JQ.defer();
        Promise<String> p = deferred.promise;

        deferred.resolve(TEST_VALUE1);

        assertThrows(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                deferred.resolve(TEST_VALUE2);
                return null;
            }
        }, IllegalStateException.class);

        assertThrows(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                deferred.reject(TEST_REASON1);
                return null;
            }
        }, IllegalStateException.class);
    }

    @Test
    public void rejected_isImmutable() {
        final Deferred<String> deferred = JQ.defer();
        Promise<String> p = deferred.promise;

        deferred.reject(TEST_REASON1);

        assertThrows(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                deferred.reject(TEST_REASON2);
                return null;
            }
        }, IllegalStateException.class);

        assertThrows(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                deferred.resolve(TEST_VALUE1);
                return null;
            }
        }, IllegalStateException.class);
    }

    @Test
    public void cancel_isNotSupported() {
        final Deferred<String> deferred = JQ.defer();
        Promise<String> p = deferred.promise;

        assertFalse(p.cancel(true));
        assertFalse(p.isCancelled());
    }

    @Test
    public void resolved_isResolvedSync() throws Exception {
        Promise<String> p = JQ.resolve(TEST_VALUE1);

        assertEquals(TEST_VALUE1, p.get());
    }

    @Test
    public void rejected_isRejectedSync() throws Exception {
        final Promise<Void> p = JQ.reject(TEST_REASON1);

        Exception e = assertThrows(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                return p.get();
            }
        }, ExecutionException.class);

        assertSame(TEST_REASON1, e.getCause());
    }

    @Test
    public void pending_hasTimeoutSync() {
        Deferred<String> deferred = JQ.defer();
        final Promise<String> p = deferred.promise;

        assertThrows(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return p.get(100, TimeUnit.MILLISECONDS);
            }
        }, TimeoutException.class);
    }

    private <T> void resolveChain(final Future<T> future, final T expected) throws InterruptedException {
        final Semaphore resolved = new Semaphore(0);
        final Semaphore rejected = new Semaphore(0);

        JQ.resolve(TEST_VALUE1).then(new OnFulfilledCallback<String, T>() {
            @Override
            public Future<T> onFulfilled(String value) throws Exception {
                return future;
            }
        }).then(new OnFulfilledCallback<T, Void>() {
            @Override
            public Future<Void> onFulfilled(T value) throws Exception {
                assertEquals(expected, value);
                resolved.release();
                return null;
            }
        }).fail(new OnRejectedCallback<Void>() {
            @Override
            public Future<Void> onRejected(Exception reason) throws Exception {
                assertTrue(false);
                rejected.release();
                return null;
            }
        });

        assertTrue(resolved.tryAcquire(2, TimeUnit.SECONDS));
        assertFalse(rejected.tryAcquire(2, TimeUnit.SECONDS));
    }

    @Test
    public void chained_isResolvedWithValue() throws InterruptedException {
        // OnFulfilled returns Value, chained fulfillment handlers are invoked
        resolveChain(Value.wrap(42), 42);
    }

    @Test
    public void chained_isResolvedWithPromise() throws InterruptedException {
        // OnFulfilled returns new Promise, chained fulfillment handlers are invoked

        resolveChain(JQ.defer(new SlowTask<>(42)), 42);
    }

    @Test
    public void chained_isResolvedWithFuture() throws InterruptedException {
        // OnFulfilled returns java.util.concurrent.Future, chained fulfillment handlers are invoked
        resolveChain(new FutureTask<>(new SlowTask<>(42)), 42);
    }

    @Test
    public void chained_isRejectedWithException() {
        // OnFulfilled throws exception, chained fulfillment handlers are not invoked, trailing rejection handler is

    }

    @Test
    public void chained_isResolvedWithoutHandler() {
        // resolved promise without fulfillmenthandler -> next promise is resolved with value
    }

    @Test
    public void chained_isRejectedWithoutHandler() {
        // rejected promise without rejection handler  -> next promise is rejected with reason
        // (this is standard case and implicitly tested elsewhere, but for clarity has its own test)
    }

    @Test
    public void terminated_isRejectedWithoutHandler() {
        // This should throw UnhandledRejectionException
    }

    @Test
    public void timeout_isResolved() {
        // new promise is resolved
    }

    @Test
    public void timeout_isRejected() {
        // new promise is rejected
    }

    @Test
    public void timeout_isResolvedAfterTimeout() {
        // Promise is resolved but too late, TimeoutException is thrown
    }

    @Test
    public void timeout_isRejectedAfterTimeout() {
        // Promise is rejected but too late, TimeoutException is thrown
    }

    @Test
    public void delay_isResolved() {
        // new promise is resolved after delay
    }

    @Test
    public void delay_isRejected() {
        // new promise is rejected immediately
    }

    @Test
    public void delay_isPending() {
        // New promise is also pending after delay has passed
    }

    @Test
    public void join_isResolvedThisThatEqual() {
        // v1 equals v2 -> new promise should be resolved with v1
    }

    @Test
    public void join_isResolvedThisThatNotEqual() {
        // v1 not equals v2 -> new promise should be rejected
    }

    @Test
    public void join_isResolvedThisNull() {
        // v1 == null, v2 != null -> new promise should be rejected
    }

    @Test
    public void join_isResolvedThatNull() {
        // v1 != null, v2 == null -> new promise should be rejected
    }

    @Test
    public void join_isResolvedThisThatNull() {
        // v1 == null, v2 == null -> new promise should be resolved with null
    }

    @Test
    public void join_isThisRejected() {
        // this rejected, that resolved -> new promise should be rejected
    }

    @Test
    public void join_isThatRejected() {
        // this resolved, that rejected -> new promise should be rejected
    }

    @Test
    public void join_isPending() {
        // this or that is pending -> new promise should be pending
    }

}
