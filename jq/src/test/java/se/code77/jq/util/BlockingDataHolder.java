package se.code77.jq.util;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BlockingDataHolder<T> {
    private Semaphore mSem = new Semaphore(0);
    private T mData;

    public void set(T data) {
        mData = data;
        mSem.release();
    }

    public void set() {
        set(null);
    }

    public T get(long timeoutMillis) throws InterruptedException, TimeoutException {
        if (mSem.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS)) {
            return mData;
        } else {
            throw new TimeoutException("Data not set");
        }
    }

}
