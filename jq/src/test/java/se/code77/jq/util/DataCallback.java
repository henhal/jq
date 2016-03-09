package se.code77.jq.util;

import java.util.concurrent.Future;

public class DataCallback<V, NV> {
    protected final BlockingDataHolder<V> mHolder;
    protected Future<NV> mNextValue;

    public DataCallback(BlockingDataHolder<V> holder) {
        mHolder = holder;
    }

    public DataCallback(BlockingDataHolder<V> holder, Future<NV> nextValue) {
        mHolder = holder;
        mNextValue = nextValue;
    }
}
