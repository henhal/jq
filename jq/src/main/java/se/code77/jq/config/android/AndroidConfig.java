
package se.code77.jq.config.android;

import se.code77.jq.config.Config;
import android.os.Handler;
import android.util.Log;

public class AndroidConfig extends Config {
    public AndroidConfig(boolean monitorUnterminated) {
        super(monitorUnterminated);
    }

    public static class AndroidDispatcher implements Dispatcher {
        protected final Handler mHandler = new Handler();

        @Override
        public void dispatch(Runnable r) {
            mHandler.post(r);
        }

        @Override
        public void dispatch(Runnable r, long ms) {
            mHandler.postDelayed(r, ms);
        }
    }

    public static class AndroidLogger implements Logger {
        private static final String LOG_TAG = "Promise";

        @Override
        public void debug(String s) {
            Log.d(LOG_TAG, s);
        }

        @Override
        public void info(String s) {
            Log.i(LOG_TAG, s);
        }

        @Override
        public void warn(String s) {
            Log.w(LOG_TAG, s);
        }

        @Override
        public void error(String s) {
            Log.e(LOG_TAG, s);
        }
    }

    @Override
    public Dispatcher createDispatcher() {
        return new AndroidDispatcher();
    }

    @Override
    public Logger getLogger() {
        return new AndroidLogger();
    }
}
