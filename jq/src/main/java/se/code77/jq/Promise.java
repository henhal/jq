
package se.code77.jq;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A Promises/A+ style promise offering simple, chainable asynchronous invocations.
 * 
 * @param <V> Type of the value carried by the promise
 */
public interface Promise<V> extends Future<V> {
    /**
     * Callback interface for resolved/fulfilled promises.
     * 
     * @param <V> Type of the value resolved by the promise on which this
     *            callback is registered by calling
     *            {@link Promise#then(OnFulfilledCallback)} or any of its
     *            overloaded variants.
     * @param <NV> Type of the value returned by the callback, which will be
     *            used to resolve the next promise in the chain.
     */
    public interface OnFulfilledCallback<V, NV> {
        /**
         * The promise on which this callback was registered has been
         * resolved/fulfilled.
         * 
         * @param value The resolved value of the promise.
         * @return A direct value (wrapped in a {@link Value#wrap(Object)}, a
         *         new Promise or any other kind of Future.
         * @throws Exception Any exception thrown by this method will lead to
         *             the rejection of the next promise in the chain.
         */
        Future<NV> onFulfilled(V value) throws Exception;
    }

    /**
     * Callback interface for rejected promises.
     * 
     * @param <NV> Type of the value returned by the callback, which will be
     *            used to resolve the next promise in the chain.
     */
    public interface OnRejectedCallback<NV> {
        /**
         * The promise on which this callback was registered has been rejected.
         * 
         * @param reason The exception that caused the promise to be rejected.
         * @return A direct value (wrapped in a {@link Value#wrap(Object)}, a
         *         new Promise or any other kind of Future.
         * @throws Exception Any exception thrown by this method will lead to
         *             the rejection of the next promise in the chain.
         */
        Future<NV> onRejected(Exception reason) throws Exception;
    }

    /**
     * An exception thrown by a promise which is terminated without any
     * rejection callback. A promise is terminated by calling
     * {@link Promise#done()} or any of its overloaded variants. Note that if a
     * promise is NOT terminated, unhandled errors will pass unnoticed.
     */
    @SuppressWarnings("serial")
    public static final class UnhandledRejectionException extends RuntimeException {
        private final Exception mReason;

        UnhandledRejectionException(Exception reason) {
            mReason = reason;
        }

        public Exception getRejection() {
            return mReason;
        }

        @Override
        public String toString() {
            return getClass().getName() + ": " + mReason.toString();
        }
    }

    /**
     * Callback interface for promise progress
     */
    public interface OnProgressedCallback {
        /**
         * The promise on which this callback was registered has progressed
         *
         * @param progress Progress
         */
        void onProgressed(float progress);
    }

    /**
     * State of a promise. All promises will initially be {@link State#PENDING}
     * and may change into {@link State#FULFILLED} or {@link State#REJECTED}
     * only once.
     */
    public static enum State {
        /**
         * The promise is not completed yet, typically because the task promising the value is still ongoing.
         */
        PENDING,
        /**
         * The promise is fulfilled, typically because the task promising the value succeeded.
         */
        FULFILLED,
        /**
         * The promise is rejected, typically because the task promising the value failed. 
         */
        REJECTED
    }

    /**
     * The state snapshot of a promise as returned by {@link Promise#inspect()}.
     * 
     * @param <V> Type of the value carried by the promise.
     */
    public static final class StateSnapshot<V> {
        /**
         * The current state of the promise
         */
        public final State state;
        /**
         * The value (only applicable if state is {@link State#FULFILLED})
         */
        public final V value;
        /**
         * The rejection reason (only applicable if state is {@link State#REJECTED})
         */
        public final Exception reason;

        StateSnapshot(State state, V value, Exception reason) {
            this.state = state;
            this.value = value;
            this.reason = reason;
        }

        @Override
        public int hashCode() {
            return state.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof StateSnapshot) {
                StateSnapshot that = (StateSnapshot)o;

                return state.equals(that.state) &&
                        (value == null ? that.value == null : value.equals(that.value)) &&
                        (reason == null ? that.reason == null : reason.equals(that.reason));
            }

            return false;
        }

        @Override
        public String toString() {
            switch (state) {
                case FULFILLED:
                    return state.toString() + ": " + value;
                case REJECTED:
                    return state.toString() + ": " + reason.toString();
                default:
                    return state.toString();
            }
        }
    }

    /**
     * Get the state snapshot of this promise
     * 
     * @return State snapshot
     */
    public StateSnapshot<V> inspect();

    /**
     * Returns true if this promise is fulfilled (resolved)
     * 
     * @return true if fulfilled, false otherwise
     */
    public boolean isFulfilled();

    /**
     * Returns true if this promise is rejected
     * 
     * @return true if rejected, false otherwise
     */
    public boolean isRejected();

    /**
     * Returns true if this promise is pending
     * 
     * @return true if pending, false otherwise
     */
    public boolean isPending();

    /**
     * Observe the state of this promise by adding callbacks which will be
     * called when the promise is resolved, rejected or progresses. This method will return
     * a new promise which will be chained to this promise, so that any value
     * returned from or exception thrown by any of the invoked callbacks will be
     * carried over to the new promise. This means that callbacks may be chained
     * such as p.then(...).then(...).then(...).fail(...) etc. The supplied
     * callbacks will be invoked on the thread from which this method is called.
     * If the promise is already completed when this method is called, the
     * relevant callback (onFulfilled or onRejected) will be dispatched for
     * immediate execution in a future event loop turn, but will never be
     * inlined. Similarly, if progress is previously notified when this method is
     * called, the onProgressed callback will be dispatched for immediate execution.
     * Note that this method MUST be run on a thread implementing an event loop.
     *
     * @param <NV> Type of the value returned by the callback handlers
     * @param onFulfilled Fulfillment handler
     * @param onRejected Rejection handler
     * @param onProgressed Progress handler
     * @return A new promise that will be resolved with the value
     *         returned/rejected with the reason thrown from any of the callback
     *         handlers.
     */
    public <NV> Promise<NV> then(
            OnFulfilledCallback<V, NV> onFulfilled, OnRejectedCallback<NV> onRejected, OnProgressedCallback onProgressed);

    /**
     * Observe the state of this promise by adding callbacks which will be
     * called when the promise is resolved or rejected. This method will return
     * a new promise which will be chained to this promise, so that any value
     * returned from or exception thrown by any of the invoked callbacks will be
     * carried over to the new promise. This means that callbacks may be chained
     * such as p.then(...).then(...).then(...).fail(...) etc. The supplied
     * callbacks will be invoked on the thread from which this method is called.
     * If the promise is already completed when this method is called, the
     * relevant callback (onFulfilled or onRejected) will be dispatched for
     * immediate execution in a future event loop turn, but will never be
     * inlined.
     * Note that this method MUST be run on a thread implementing an event loop.
     * 
     * @param <NV> Type of the value returned by the callback handlers
     * @param onFulfilled Fulfillment handler
     * @param onRejected Rejection handler
     * @return A new promise that will be resolved with the value
     *         returned/rejected with the reason thrown from any of the callback
     *         handlers.
     */
    public <NV> Promise<NV> then(
            OnFulfilledCallback<V, NV> onFulfilled, OnRejectedCallback<NV> onRejected);

    /**
     * Observe the state of this promise by adding a fulfillment callback which
     * will be called when the promise is resolved. This is the same as calling
     * {@link #then(OnFulfilledCallback, OnRejectedCallback)} with null as the
     * onRejected argument.
     * Note that this method MUST be run on a thread implementing an event loop.
     * 
     * @see #then(OnFulfilledCallback, OnRejectedCallback)
     * @param <NV> Type of the value returned by the callback handlers
     * @param onFulfilled Fulfillment handler
     * @return A new promise that will be resolved with the value
     *         returned/rejected with the reason thrown from the supplied
     *         callback handler.
     */
    public <NV> Promise<NV> then(OnFulfilledCallback<V, NV> onFulfilled);

    /**
     * Observe the state of this promise by adding a rejection callback which
     * will be called when the promise is rejected. This is the same as calling
     * {@link #then(OnFulfilledCallback, OnRejectedCallback)} with null as the
     * onFulfilled argument.
     * Note that this method MUST be run on a thread implementing an event loop.
     * 
     * @see #then(OnFulfilledCallback, OnRejectedCallback)
     * @param onRejected Rejection handler
     * @param <NV> Type of the value returned by the callback handlers
     * @return A new promise that will be resolved with the value
     *         returned/rejected with the reason thrown from the supplied
     *         callback handler.
     */
    public <NV> Promise<NV> fail(OnRejectedCallback<NV> onRejected);

    /**
     * Observe the progress of this promise by adding a progress callback which
     * will be called whenever progress of the promise is notified. This is the same
     * as calling {@link #then(OnFulfilledCallback, OnRejectedCallback, OnProgressedCallback)}
     * with null as the onFulfilled and onRejected arguments.
     * Note that this method MUST be run on a thread implementing an event loop.
     *
     * @see #then(OnFulfilledCallback, OnRejectedCallback, OnProgressedCallback)
     *
     * @param onProgressed Progress handler
     * @return A new promise that will inherit the state of this promise.
     */
    public Promise<V> progress(OnProgressedCallback onProgressed);

    /**
     * Adds the supplied callback handlers, just like calling
     * {@link #then(OnFulfilledCallback, OnRejectedCallback)}, but also adds a
     * default rejection handler and then terminates the promise chain. It's
     * good practice to terminate a promise chain to avoid unhandled rejections
     * to silently pass unnoticed. If an unterminated chain of promises without
     * a rejection handler is rejected, nothing will happen. However, a
     * terminated chain will result in a UnhandledRejectionException carrying
     * the rejection reason to be thrown on the dispatcher thread. Since this
     * exception will typically not be possible to catch, this means the
     * application is likely to crash. Clients should take caution to always
     * include a rejection handler, and should consider termination of promise
     * chains as an aid during development.
     * Note that this method MUST be run on a thread implementing an event loop.
     * 
     * @param onFulfilled Fulfillment handler
     * @param onRejected Rejection handler
     */
    public void done(
            OnFulfilledCallback<V, Void> onFulfilled, OnRejectedCallback<Void> onRejected);

    /**
     * Adds the supplied fulfillment handler, just like calling
     * {@link #then(OnFulfilledCallback)}, but also adds a default rejection
     * handler and then terminates the promise chain. It's good practice to
     * terminate a promise chain to avoid unhandled rejections to silently pass
     * unnoticed. If an unterminated chain of promises without a rejection
     * handler is rejected, nothing will happen. However, a terminated chain
     * will result in a UnhandledRejectionException carrying the rejection
     * reason to be thrown on the dispatcher thread. Since this exception will
     * typically not be possible to catch, this means the application is likely
     * to crash. Clients should take caution to always include a rejection
     * handler, and should consider termination of promise chains as an aid
     * during development.
     * Note that this method MUST be run on a thread implementing an event loop.
     * 
     * @param onFulfilled Fulfillment handler
     */
    public void done(OnFulfilledCallback<V, Void> onFulfilled);

    /**
     * Adds a default rejection handler and then terminates the promise chain.
     * It's good practice to terminate a promise chain to avoid unhandled
     * rejections to silently pass unnoticed. If an unterminated chain of
     * promises without a rejection handler is rejected, nothing will happen.
     * However, a terminated chain will result in a UnhandledRejectionException
     * carrying the rejection reason to be thrown on the dispatcher thread.
     * Since this exception will typically not be possible to catch, this means
     * the application is likely to crash. Clients should take caution to always
     * include a rejection handler, and should consider termination of promise
     * chains as an aid during development.
     * Note that this method MUST be run on a thread implementing an event loop.
     */
    public void done();

    /**
     * Add a timeout to a promise by returning a new promise that will be
     * rejected with a TimeoutException when the specified time has passed if
     * this promise isn't completed by then.
     * Note that this method MUST be run on a thread implementing an event loop.
     * 
     * @param ms Timeout, in milliseconds
     * @return New promise
     */
    public Promise<V> timeout(final long ms);

    /**
     * Delays a resolved promise by the specified time, by creating a new
     * promise that will inherit the state of this promise; if this promise is
     * rejected, the new promise will also be immediately rejected but if this
     * promise is resolved, the new promise will be resolved when the specified
     * time has passed.
     * Note that this method MUST be run on a thread implementing an event loop.
     * 
     * @param ms Time to delay resolved promise, in milliseconds
     * @return New promise
     */
    public Promise<V> delay(final long ms);

    /**
     * Creates a new promise that will wait for the two promises to complete,
     * and which will be resolved if and only if the two promises are resolved
     * with the exact same value. If any of the promises are rejected, the new
     * promise will be immediately rejected as well. If both promises are
     * resolved but with different values, the new promise will be rejected.
     * Note that this method MUST be run on a thread implementing an event loop.
     * 
     * @param that A promise
     * @return A promise that will be resolved only if both promises are
     *         resolved with the same value; rejected otherwise
     */
    public Promise<V> join(Promise<V> that);

    /**
     * Returns the resolved value carried by this promise. If the value is not
     * readily available, this method will block while waiting for the promise
     * to be completed or an interrupt to occur. Calling this method on a
     * dispatcher thread might cause a deadlock. If the promise is rejected, an
     * ExecutionException will be thrown, wrapping the rejection reason.
     * 
     * @return The value carried by this promise
     * @throws InterruptedException If the blocked thread was interrupted
     * @throws ExecutionException If the promise was rejected, the reason will
     *             be wrapped in an ExecutionException.
     */
    @Override
    public V get() throws InterruptedException, ExecutionException;

    /**
     * Returns the resolved value carried by this promise. If the value is not
     * readily available, this method will block while waiting for the promise
     * to be completed or an interrupt to occur, however for no longer than the
     * supplied duration. Calling this method on a dispatcher thread might cause
     * a deadlock. If the promise is rejected, an ExecutionException will be
     * thrown, wrapping the rejection reason.
     * 
     * @return The value carried by this promise
     * @throws InterruptedException If the blocked thread was interrupted
     * @throws ExecutionException If the promise was rejected, the reason will
     *             be wrapped in an ExecutionException.
     * @throws TimeoutException If the promise was not completed before the
     *             specified duration passed.
     */
    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
            TimeoutException;

    /**
     * Canceling promises is not supported since a promise is completely
     * decoupled from the task and only observable (its state may be changed
     * only through the Deferred object). This method will always return false.
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning);

    /**
     * Since cancellation is not supported this method will always return false.
     */
    @Override
    public boolean isCancelled();

    /**
     * Returns true if this promise is completed, i.e., either resolved or
     * rejected.
     */
    @Override
    public boolean isDone();
}
