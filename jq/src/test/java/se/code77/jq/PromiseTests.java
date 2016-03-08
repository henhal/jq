package se.code77.jq;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static se.code77.jq.util.Assert.*;

import se.code77.jq.JQ.Deferred;
import se.code77.jq.Promise.OnFulfilledCallback;
import se.code77.jq.Promise.OnRejectedCallback;
import se.code77.jq.util.AsyncTests;

public class PromiseTests extends AsyncTests {

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
        deferred.resolve("Hello");
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

        String value = "Hello";
        deferred.resolve(value);

        assertTrue(sem.tryAcquire(2, TimeUnit.SECONDS));
        assertResolved(p, value);
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
        String value = "Hello";
        Promise<String> p = JQ.resolve(value);

        final Semaphore sem = new Semaphore(0);

        p.then(new OnFulfilledCallback<String, Void>() {
            @Override
            public Future<Void> onFulfilled(String value) throws Exception {
                sem.release();
                return null;
            }
        });

        assertTrue(sem.tryAcquire(2, TimeUnit.SECONDS));
        assertResolved(p, value);
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

        deferred.resolve("resolve1");

        assertThrows(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                deferred.resolve("resolve2");
                return null;
            }
        }, IllegalStateException.class);

        assertThrows(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                deferred.reject(new Exception("reject1"));
                return null;
            }
        }, IllegalStateException.class);
    }

    @Test
    public void rejected_isImmutable() {
        final Deferred<String> deferred = JQ.defer();
        Promise<String> p = deferred.promise;

        deferred.reject(new Exception("reject1"));

        assertThrows(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                deferred.reject(new Exception("reject2"));
                return null;
            }
        }, IllegalStateException.class);

        assertThrows(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                deferred.resolve("resolve1");
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
    public void resolved_isResolvedSync() {
        // Promise.get
    }

    @Test
    public void rejected_isRejectedSync() {
        // Promise.get
    }

    @Test
    public void pending_hasTimeoutSync() {
        // Promise.get(timeout, TimeUnit)
    }

    @Test
    public void chained_isResolvedWithValue() {
        // OnFulfilled returns Value, chained fulfillment handlers are invoked
    }

    @Test
    public void chained_isResolvedWithPromise() {
        // OnFulfilled returns new Promise, chained fulfillment handlers are invoked
    }

    @Test
    public void chained_isResolvedWithFutureTask() {
        // OnFulfilled returns java.util.concurrent.FutureTask, chained fulfillment handlers are invoked
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
