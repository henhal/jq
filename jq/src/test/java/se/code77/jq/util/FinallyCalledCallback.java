package se.code77.jq.util;

import se.code77.jq.Promise;

public class FinallyCalledCallback extends DataCallback<Boolean, Void> implements Promise.OnFinallyCallback {
    public FinallyCalledCallback(BlockingDataHolder<Boolean> holder) {
        super(holder);
    }

    @Override
    public void onFinally() throws Exception {
        mHolder.set(true);
    }
}
