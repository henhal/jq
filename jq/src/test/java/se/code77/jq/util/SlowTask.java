package se.code77.jq.util;

import java.util.concurrent.Callable;

public class SlowTask<T> implements Callable<T> {
    private T mValue;
    private Throwable mReason;
    private long mDelayMillis;

    public SlowTask(T value) {
        this(value, 1000);
    }

    public SlowTask(T value, long delayMillis) {
        mValue = value;
        mDelayMillis = delayMillis;
    }

    public SlowTask(Throwable reason) {
        this(reason, 1000);
    }

    public SlowTask(Throwable reason, long delayMillis) {
        mReason = reason;
        mDelayMillis = delayMillis;
    }

    @Override
    public T call() throws Exception {
        Thread.sleep(mDelayMillis);
        if (mReason instanceof Exception) {
            throw (Exception)mReason;
        } else if (mReason instanceof Error) {
            throw (Error)mReason;
        } else {
            return mValue;
        }
    }
}
