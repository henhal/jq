
package se.code77.jq.config.android;

import se.code77.jq.config.Config;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class AndroidConfig extends Config {
    public AndroidConfig(boolean monitorUnterminated) {
        super(monitorUnterminated);
    }

    public AndroidConfig(boolean monitorUnterminated, LogLevel logLevel) {
        super(monitorUnterminated, logLevel);
    }

    public static class AndroidDispatcher implements Dispatcher {
        protected final Handler mHandler;

        public AndroidDispatcher() {
            Looper looper = Looper.myLooper();

            if (looper == null) {
                looper = Looper.getMainLooper();
            }

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
        return new AndroidDispatcher();
    }

    @Override
    public Logger getLogger() {
        return new AndroidLogger(logLevel);
    }
}