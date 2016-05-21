
package se.code77.jq.config.android;

import se.code77.jq.config.Config;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class AndroidConfig extends Config {
    private final AndroidDispatcher mDefaultDispatcher;

    public AndroidConfig(boolean monitorUnterminated) {
        this(monitorUnterminated, LogLevel.WARN);
    }

    public AndroidConfig(boolean monitorUnterminated, LogLevel logLevel) {
        super(monitorUnterminated, logLevel);

        mDefaultDispatcher = new AndroidDispatcher(Looper.getMainLooper());
    }

    public static class AndroidDispatcher implements Dispatcher {
        protected final Handler mHandler;

        public AndroidDispatcher(Looper looper) {
            mHandler = new Handler(looper);
        }

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

        private final LogLevel mLogLevel;

        public AndroidLogger(LogLevel logLevel) {
            mLogLevel = logLevel;
        }

        private boolean hasLevel(LogLevel level) {
            return mLogLevel.ordinal() <= level.ordinal();
        }

        @Override
        public void verbose(String s) {
            if (hasLevel(LogLevel.VERBOSE)) {
                Log.v(LOG_TAG, s);
            }
        }

        @Override
        public void debug(String s) {
            if (hasLevel(LogLevel.DEBUG)) {
                Log.d(LOG_TAG, s);
            }
        }

        @Override
        public void info(String s) {
            if (hasLevel(LogLevel.INFO)) {
                Log.i(LOG_TAG, s);
            }
        }

        @Override
        public void warn(String s) {
            if (hasLevel(LogLevel.WARN)) {
                Log.w(LOG_TAG, s);
            }
        }

        @Override
        public void error(String s) {
            if (hasLevel(LogLevel.ERROR)) {
                Log.e(LOG_TAG, s);
            }
        }
    }

    @Override
    public Dispatcher createDispatcher() {
        Dispatcher d = mDispatchers.get(Thread.currentThread());

        return d != null ? d : mDefaultDispatcher;
    }

    @Override
    public Logger getLogger() {
        return new AndroidLogger(logLevel);
    }
}
