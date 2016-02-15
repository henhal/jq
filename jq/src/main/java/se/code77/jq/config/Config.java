
package se.code77.jq.config;

import se.code77.jq.Promise;
import se.code77.jq.config.android.AndroidConfig;

/**
 * Configuration for JQ. The configuration offers integration points into the
 * host environment, as well as tweaking of behaviour.
 */
public abstract class Config {
    /**
     * A dispatcher is capable of asynchronously dispatching events containing
     * code to be executed in a thread implementing an event loop. Each
     * dispatcher is associated with exactly one Thread, determined upon
     * construction, and all events will be dispatched to this thread.
     */
    public interface Dispatcher {
        /**
         * Dispatch the given runnable for future execution on the Thread
         * associated with this dispatcher
         *
         * @param r Code to be executed
         */
        void dispatch(Runnable r);

        /**
         * Dispatch the given runnable for future execution on the Thread
         * associated with this dispatcher, after the specified time has passed.
         *
         * @param r Code to be executed
         * @param ms Time to be passed before code is executed
         */
        void dispatch(Runnable r, long ms);
    }

    /**
     * A logger creates log prints.
     */
    public interface Logger {
        /**
         * Print a debug log
         * @param s log text
         */
        void debug(String s);

        /**
         * Print a info log
         * @param s log text
         */
        void info(String s);

        /**
         * Print a warning log
         * @param s log text
         */
        void warn(String s);

        /**
         * Print a error log
         * @param s log text
         */
        void error(String s);
    }

    public enum LogLevel {
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    private static Config CONFIG;

    /**
     * Set the configuration to be used by JQ
     * @param config New configuration
     */
    public static synchronized void setConfig(Config config) {
        CONFIG = config;
    }

    /**
     * Get the configuration used by JQ. If no configuration is explicitly given
     * through {@link #setConfig(Config)}, JQ will attempt to create a default
     * configuration if the host environment can be detected. Otherwise an exception will
     * be thrown.
     *
     * @return Current configuration
     */
    public static synchronized Config getConfig() {
        if (CONFIG == null) {
            CONFIG = getDefaultConfig();

            if (CONFIG == null) {
                throw new IllegalStateException("No config supplied and no default config available");
            }
        }
        return CONFIG;
    }

    private static Config getDefaultConfig() {
        try {
            Class.forName("android.os.Handler");
            return new AndroidConfig(false);
        } catch (ClassNotFoundException e) {
        }

        return null;
    }

    protected Config(boolean monitorUnterminated) {
        this(monitorUnterminated, LogLevel.WARN);
    }

    protected Config(boolean monitorUnterminated, LogLevel logLevel) {
        this.monitorUnterminated = monitorUnterminated;
        this.logLevel = logLevel;
    }

    /**
     * Log level
     */
    public final LogLevel logLevel;

    /**
     * Set to true if unterminated promise chains should cause warnings to be
     * logged. An unterminated promise chain is a chain of promises ending with
     * a promise that has produced a result (fulfilled or rejected) without
     * anyone observing it and without explicitly terminating the chain with {@link Promise#done()}.
     */
    public final boolean monitorUnterminated;

    /**
     * Create a dispatcher capable of dispatching events to the current thread;
     * or if the current thread does not implement an event loop, to another thread that does.
     * @return Dispatcher
     */
    public abstract Dispatcher createDispatcher();

    /**
     * Get a logger
     * @return Logger
     */
    public abstract Logger getLogger();

}
