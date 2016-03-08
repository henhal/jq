package se.code77.jq;

import org.junit.Test;

import static se.code77.jq.util.Assert.*;

import se.code77.jq.JQ.Deferred;
import se.code77.jq.Promise.OnFulfilledCallback;
import se.code77.jq.Promise.OnRejectedCallback;
import se.code77.jq.util.AsyncTests;

public class JQTests extends AsyncTests {
    @Test
    public void resolve_isResolved() {
        String value = "Hello";
        Promise<String> p = JQ.resolve(value);
        assertResolved(p, value);
    }

    @Test
    public void resolveVoid_isResolved() {
        Promise<Void> p = JQ.resolve();
        assertResolved(p, null);
    }

    @Test
    public void reject_isRejected() {
        IllegalArgumentException reason = new IllegalArgumentException("foobar");
        Promise<Void> p = JQ.reject(reason);
        assertRejected(p, reason);
    }

    @Test
    public void all_isResolvedList() {

    }

    @Test
    public void all_isResolvedVarArg() {

    }

    @Test
    public void all_isRejected() {

    }

    @Test
    public void all_isPending() {
        // at least one promise is not done -> resulting is pending forever
    }

    @Test
    public void any_isResolvedByFirst() {

    }

    @Test
    public void any_isResolvedByLast() {

    }

    @Test
    public void any_isRejected() {

    }

    @Test
    public void any_isPending() {

    }

    @Test
    public void race_isResolvedByFirst() {

    }

    @Test
    public void race_isResolvedByLast() {

    }

    @Test
    public void race_isRejectedByFirst() {

    }

    @Test
    public void race_isRejectedByLast() {

    }

    @Test
    public void race_isPending() {

    }

    @Test
    public void allSettled_isResolvedAllResolved() {

    }

    @Test
    public void allSettled_isResolvedAllResolvedOrRejected() {

    }

    @Test
    public void allSettled_isResolvedAllRejected() {

    }

    @Test
    public void allSettled_isPending() {

    }

    @Test
    public void wrap_isReturnedForPromise() {

    }

    @Test
    public void wrap_isResolvedForValue() {

    }

    @Test
    public void wrap_isResolvedForFutureTaskCompleted() {

    }

    @Test
    public void wrap_isRejectedForFutureTaskFailedWithException() {

    }

    @Test
    public void wrap_isRejectedForFutureTaskFailedWithError() {
        // Hmm consider if this should be rejected with dummy Exception, use ExecutionException(Throwable) or actually throw the Error
    }

    @Test
    public void wrap_isRejectedForFutureTaskInterrupted() {

    }

    @Test
    public void wrap_isRejectedForFutureTaskCancelled() {

    }

    // when, fail, done, timeout, delay are just convenience wrappers. That's not evident from a black box pov, but are they worth writing tests for?
}
