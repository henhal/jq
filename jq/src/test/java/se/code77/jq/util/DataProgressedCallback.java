package se.code77.jq.util;

import se.code77.jq.Promise.OnProgressedCallback;

public class DataProgressedCallback implements OnProgressedCallback {

    private final BlockingDataHolder<Float> mHolder;

    public DataProgressedCallback(BlockingDataHolder<Float> holder) {
        mHolder = holder;
    }

    @Override
    public void onProgressed(float progress) {
        mHolder.set(progress);
    }
}
