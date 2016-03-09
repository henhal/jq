package se.code77.jq.util;

import java.util.concurrent.Future;

import se.code77.jq.Promise.OnFulfilledCallback;

public class DataFulfilledCallback<V, NV> extends DataCallback<V, NV> implements OnFulfilledCallback<V, NV> {

    public DataFulfilledCallback(BlockingDataHolder<V> holder) {
        super(holder);
    }

    public DataFulfilledCallback(BlockingDataHolder<V> holder, Future<NV> nextValue) {
        super(holder, nextValue);
    }

    @Override
    public Future<NV> onFulfilled(V value) throws Exception {
        mHolder.set(value);
        return mNextValue;
    }
}
