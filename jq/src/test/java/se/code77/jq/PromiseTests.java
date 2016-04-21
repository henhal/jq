package se.code77.jq;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static se.code77.jq.util.Assert.*;

import se.code77.jq.JQ.Deferred;
import se.code77.jq.Promise.OnFulfilledCallback;
import se.code77.jq.Promise.UnhandledRejectionException;
import se.code77.jq.util.AsyncTests;
import se.code77.jq.util.BlockingDataHolder;
import se.code77.jq.util.DataFulfilledCallback;
import se.code77.jq.util.DataRejectedCallback;
import se.code77.jq.util.SlowTask;
import se.code77.jq.util.TestConfig;

public class PromiseTests extends AsyncTests {

    @Test
    public void pending_isPending() throws Exception {
        Deferred<String> deferred = JQ.defer();
        Promise<String> p = deferred.promise;

        assertPending(p);

        BlockingDataHolder<String> then1 = new BlockingDataHolder<>();
        p.then(new DataFulfilledCallback<String, Void>(then1));

        assertNoData(then1, 2000);
        deferred.resolve(TEST_VALUE1);
    }

    @Test
    public void resolved_isResolved() throws Exception {
        Deferred<String> deferred = JQ.defer();
        Promise<String> p = deferred.promise;

        // Test one-arg then
        BlockingDataHolder<String> then1 = new BlockingDataHolder<>();
        p.then(new DataFulfilledCallback<String, Void>(then1));

        // Test two-arg then
        BlockingDataHolder<String> then2 = new BlockingDataHolder<>();
        p.then(new DataFulfilledCallback<String, Void>(then2), null);

        deferred.resolve(TEST_VALUE1);

        assertData(then1, 2000, TEST_VALUE1);
        assertData(then2, 2000, TEST_VALUE1);
        assertResolved(p, TEST_VALUE1);
    }


    @Test
    public void rejected_isRejected() throws Exception {
        Deferred<String> deferred = JQ.defer();
        Promise<String> p = deferred.promise;

        // Test one-arg fail
        BlockingDataHolder<Exception> fail1 = new BlockingDataHolder<>();
        p.fail(new DataRejectedCallback<>(fail1));

        // Test two-arg then
        BlockingDataHolder<Exception> fail2 = new BlockingDataHolder<>();
        p.then(null, new DataRejectedCallback<>(fail2));

        deferred.reject(TEST_REASON1);

        assertData(fail1, 2000, TEST_REASON1);
        assertData(fail2, 2000, TEST_REASON1);
        assertRejected(p, TEST_REASON1);
    }

    @Test
    public void preResolved_isResolved() throws Exception {
        Promise<String> p = JQ.resolve(TEST_VALUE1);

        BlockingDataHolder<String> then1 = new BlockingDataHolder<>();
        p.then(new DataFulfilledCallback<String, Void>(then1));

        assertData(then1, 2000, TEST_VALUE1);
        assertResolved(p, TEST_VALUE1);
    }

    @Test
    public void preRejected_isRejected() throws Exception {
        Promise<Void> p = JQ.reject(TEST_REASON1);

        BlockingDataHolder<Exception> fail1 = new BlockingDataHolder<>();
        p.fail(new DataRejectedCallback<>(fail1));

        assertData(fail1, 2000, TEST_REASON1);
        assertRejected(p, TEST_REASON1);
    }

    @Test
    public void resolved_isNotRejected() {
        Promise<String> p = JQ.resolve(TEST_VALUE1);

        BlockingDataHolder<Exception> fail1 = new BlockingDataHolder<>();
        p.fail(new DataRejectedCallback<>(fail1));

        assertNoData(fail1, 2000);
    }

    @Test
    public void rejected_isNotResolved() {
        Promise<Void> p = JQ.reject(TEST_REASON1);

        BlockingDataHolder<Void> then1 = new BlockingDataHolder<>();
        p.then(new DataFulfilledCallback<Void, Void>(then1));

        assertNoData(then1, 2000);
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
        final BlockingDataHolder<String> then1 = new BlockingDataHolder<>();
        final BlockingDataHolder<T> then2 = new BlockingDataHolder<>();
        final BlockingDataHolder<Exception> fail1 = new BlockingDataHolder<>();

        JQ.resolve(TEST_VALUE1).then(
                new DataFulfilledCallback<>(then1, future)).then(
                new DataFulfilledCallback<>(then2)).fail(
                new DataRejectedCallback<>(fail1));

        assertData(then1, 2000, TEST_VALUE1);
        assertData(then2, 2000, expected);
        assertNoData(fail1, 2000);
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
    public void chained_isResolvedWithFutureTask() throws InterruptedException {
        // OnFulfilled returns java.util.concurrent.Future, chained fulfillment handlers are invoked
        FutureTask<Integer> future = new FutureTask<>(new SlowTask<>(42));
        Executors.newSingleThreadExecutor().execute(future);

        resolveChain(future, 42);
    }

    @Test
    public void chained_isRejectedWithException() {
        // OnFulfilled throws exception, chained fulfillment handlers are not invoked, trailing rejection handler is

        final BlockingDataHolder<String> then1 = new BlockingDataHolder<>();
        final BlockingDataHolder<Integer> then2 = new BlockingDataHolder<>();
        final BlockingDataHolder<Exception> fail1 = new BlockingDataHolder<>();

        JQ.resolve(TEST_VALUE1).then(
                new DataFulfilledCallback<String, Integer>(then1) {
                    @Override
                    public Future<Integer> onFulfilled(String value) throws Exception {
                        super.onFulfilled(value);
                        throw TEST_REASON1;
                    }
                }).then(
                new DataFulfilledCallback<>(then2), new DataRejectedCallback<>(fail1));

        assertData(then1, 2000, TEST_VALUE1);
        assertNoData(then2, 2000);
        assertData(fail1, 2000, TEST_REASON1);
    }

    @Test
    public void chained_isResolvedWithoutHandler() {
        // resolved promise without fulfillment handler -> next promise is resolved with value

        final BlockingDataHolder<String> then1 = new BlockingDataHolder<>();
        final BlockingDataHolder<Exception> fail1 = new BlockingDataHolder<>();

        JQ.resolve(TEST_VALUE1).fail(
                new DataRejectedCallback<>(fail1)).then(
                new DataFulfilledCallback<>(then1));

        assertData(then1, 2000, TEST_VALUE1);
        assertNoData(fail1, 2000);
    }

    @Test
    public void chained_isRejectedWithoutHandler() {
        // rejected promise without rejection handler  -> next promise is rejected with reason
        // (this is standard case and implicitly tested elsewhere, but for clarity has its own test)

        final BlockingDataHolder<String> then1 = new BlockingDataHolder<>();
        final BlockingDataHolder<Integer> then2 = new BlockingDataHolder<>();
        final BlockingDataHolder<Exception> fail1 = new BlockingDataHolder<>();

        JQ.resolve(TEST_VALUE1).then(
                new DataFulfilledCallback<String, Integer>(then1) {
                    @Override
                    public Future<Integer> onFulfilled(String value) throws Exception {
                        super.onFulfilled(value);
                        throw TEST_REASON1;
                    }
                }).then(
                new DataFulfilledCallback<>(then2)).fail(
                new DataRejectedCallback<>(fail1));

        assertData(then1, 2000, TEST_VALUE1);
        assertNoData(then2, 2000);
        assertData(fail1, 2000, TEST_REASON1);
    }

    @Test
    public void terminated_isRejectedWithoutHandler() throws InterruptedException {
        // This should throw UnhandledRejectionException

        JQ.resolve(TEST_VALUE1).then(
                new OnFulfilledCallback<String, Void>() {
                    @Override
                    public Future<Void> onFulfilled(String value) throws Exception {
                        throw TEST_REASON1;
                    }
                }).done();

        Thread.sleep(1000);
        UnhandledRejectionException unhandledException = TestConfig.getTestThread().getUnhandledException();
        assertNotNull(unhandledException);
        assertTrue(UnhandledRejectionException.class.isAssignableFrom(unhandledException.getClass()));
    }

    @Test
    public void timeout_isResolved() {
        // new promise is resolved
        BlockingDataHolder<String> then1 = new BlockingDataHolder<>();

        JQ.resolve(TEST_VALUE1).timeout(1000).then(new DataFulfilledCallback<String, Void>(then1));

        assertData(then1, 500, TEST_VALUE1);
    }

    @Test
    public void timeout_isRejected() {
        // new promise is rejected
        BlockingDataHolder<Exception> fail1 = new BlockingDataHolder<>();

        JQ.reject(TEST_REASON1).timeout(1000).fail(new DataRejectedCallback<>(fail1));

        assertData(fail1, 500, TEST_REASON1);

    }

    @Test
    public void timeout_isResolvedAfterTimeout() throws InterruptedException {
        // Promise is resolved but too late, TimeoutException is thrown
        Promise<String> p = JQ.defer(new SlowTask<>(TEST_VALUE1, 2000)).timeout(1000);

        BlockingDataHolder<String> then1 = new BlockingDataHolder<>();
        p.then(new DataFulfilledCallback<>(then1));

        Thread.sleep(500);
        assertPending(p);

        Thread.sleep(1000);
        assertRejected(p, TimeoutException.class);

        assertNoData(then1, 1000);
    }

    @Test
    public void timeout_isRejectedAfterTimeout() throws InterruptedException {
        // Promise is rejected but too late, TimeoutException is thrown
        Promise<String> p = JQ.defer(new SlowTask<String>(TEST_REASON1, 2000)).timeout(1000);

        Thread.sleep(500);
        assertPending(p);

        Thread.sleep(1000);
        assertRejected(p, TimeoutException.class);
    }

    @Test
    public void delay_isResolved() throws InterruptedException {
        // new promise is resolved after delay
        Promise<String> p = JQ.resolve(TEST_VALUE1).delay(1000);

        Thread.sleep(500);
        assertPending(p);

        Thread.sleep(1000);
        assertResolved(p, TEST_VALUE1);
    }

    @Test
    public void delay_isRejected() throws InterruptedException {
        // new promise is rejected immediately
        Promise<Void> p = JQ.<Void>reject(TEST_REASON1).delay(1000);

        Thread.sleep(500);
        assertRejected(p, TEST_REASON1);
    }

    @Test
    public void delay_isPending() throws InterruptedException {
        // New promise is also pending after delay has passed if original promise is not resolved yet
        Promise<String> p = JQ.defer(new SlowTask<>(TEST_VALUE1, 2000)).delay(1000);

        Thread.sleep(1500);
        assertPending(p);

        Thread.sleep(1000);
        assertPending(p);

        Thread.sleep(1000);
        assertResolved(p, TEST_VALUE1);
    }

    @Test
    public void join_isResolvedThisThatEqual() throws InterruptedException {
        // v1 equals v2 -> new promise should be resolved with v1
        Promise<String> p1 = JQ.resolve(TEST_VALUE1);
        Promise<String> p2 = JQ.resolve(TEST_VALUE1);
        Promise<String> p3 = p1.join(p2);

        Thread.sleep(500);
        assertResolved(p3, TEST_VALUE1);
    }

    @Test
    public void join_isResolvedThisThatNotEqual() throws InterruptedException {
        // v1 not equals v2 -> new promise should be rejected
        Promise<String> p1 = JQ.resolve(TEST_VALUE1);
        Promise<String> p2 = JQ.resolve(TEST_VALUE2);
        Promise<String> p3 = p1.join(p2);

        Thread.sleep(500);
        assertRejected(p3);
    }

    @Test
    public void join_isResolvedThisNull() throws InterruptedException {
        // v1 == null, v2 != null -> new promise should be rejected
        Promise<String> p1 = JQ.resolve(null);
        Promise<String> p2 = JQ.resolve(TEST_VALUE1);
        Promise<String> p3 = p1.join(p2);

        Thread.sleep(500);
        assertRejected(p3);
    }

    @Test
    public void join_isResolvedThatNull() throws InterruptedException {
        // v1 != null, v2 == null -> new promise should be rejected
        Promise<String> p1 = JQ.resolve(TEST_VALUE1);
        Promise<String> p2 = JQ.resolve(null);
        Promise<String> p3 = p1.join(p2);

        Thread.sleep(500);
        assertRejected(p3);
    }

    @Test
    public void join_isResolvedThisThatNull() throws InterruptedException {
        // v1 == null, v2 == null -> new promise should be resolved with null
        Promise<String> p1 = JQ.resolve(null);
        Promise<String> p2 = JQ.resolve(null);
        Promise<String> p3 = p1.join(p2);

        Thread.sleep(500);
        assertResolved(p3, null);
    }

    @Test
    public void join_isThisRejected() throws InterruptedException {
        // this rejected, that resolved -> new promise should be rejected
        Promise<String> p1 = JQ.defer(new SlowTask<String>(TEST_REASON1, 0));
        Promise<String> p2 = JQ.resolve(TEST_VALUE1);
        Promise<String> p3 = p1.join(p2);

        Thread.sleep(500);
        assertRejected(p3);
    }

    @Test
    public void join_isThatRejected() throws InterruptedException {
        // this resolved, that rejected -> new promise should be rejected
        Promise<String> p1 = JQ.resolve(TEST_VALUE1);
        Promise<String> p2 = JQ.defer(new SlowTask<String>(TEST_REASON1, 0));
        Promise<String> p3 = p1.join(p2);

        Thread.sleep(500);
        assertRejected(p3);
    }

    @Test
    public void join_isPending() throws InterruptedException {
        // this or that is pending -> new promise should be pending
        Promise<String> p1 = JQ.defer(new SlowTask<>(TEST_VALUE1, 1000));
        Promise<String> p2 = JQ.resolve(TEST_VALUE1);
        Promise<String> p3 = p1.join(p2);

        Thread.sleep(500);
        assertPending(p3);
        Thread.sleep(1000);
        assertResolved(p3, TEST_VALUE1);

        p1 = JQ.resolve(TEST_VALUE1);
        p2 = JQ.defer(new SlowTask<>(TEST_VALUE1, 1000));
        p3 = p1.join(p2);

        Thread.sleep(500);
        assertPending(p3);
        Thread.sleep(1000);
        assertResolved(p3, TEST_VALUE1);
    }

}