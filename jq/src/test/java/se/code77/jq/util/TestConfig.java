package se.code77.jq.util;

import java.util.PriorityQueue;

import se.code77.jq.config.Config;

public class TestConfig {
    public static void init() {
        final TestThread tt = new TestThread();
        tt.start();

        Config.setConfig(new Config(true) {
            @Override
            public Dispatcher createDispatcher() {
                return new TestDispatcher(tt);
            }

            @Override
            public Logger getLogger() {
                return new Logger() {
                    private void log(String level, String s) {
                        System.out.println(System.currentTimeMillis() + " [PROMISE." + level + "] "
                                + s);
                    }

                    @Override
                    public void debug(String s) {
                        log("debug", s);
                    }

                    @Override
                    public void info(String s) {
                        log("info", s);
                    }

                    @Override
                    public void warn(String s) {
                        log("warn", s);
                    }

                    @Override
                    public void error(String s) {
                        log("error", s);
                    }

                };
            }
        });

    }

    private static class TestDispatcher implements Config.Dispatcher {

        private TestThread mThread;

        public TestDispatcher(TestThread t) {
            mThread = t;
        }

        public TestDispatcher() {
            mThread = (TestThread) Thread.currentThread();
        }

        @Override
        public void dispatch(Runnable r) {
            dispatch(r, 0);
        }

        @Override
        public void dispatch(Runnable r, long ms) {
            mThread.addEvent(r, ms);
        }

    }

    private static class TestThread extends Thread {
        private static class Event implements Comparable<Event> {
            public final Runnable r;
            public final long due;

            public Event(Runnable r, long due) {
                this.r = r;
                this.due = due;
            }

            @Override
            public int compareTo(Event o) {
                return (int) (due - o.due);
            }
        }

        private final PriorityQueue<Event> mEvents = new PriorityQueue<Event>();
        private boolean mStopped;

        private synchronized Event getEvent() throws InterruptedException {
            while (!Thread.interrupted()) {
                Event e = mEvents.peek();

                if (e == null) {
                    wait();
                } else {
                    long timeLeft = e.due - System.currentTimeMillis();

                    if (timeLeft > 0) {
                        wait(timeLeft);
                    } else {
                        return mEvents.poll();
                    }
                }
            }

            throw new InterruptedException();
        }

        @Override
        public void run() {
            System.out.println("Starting event thread");
            while (!mStopped) {
                try {
                    Event e = getEvent();

                    e.r.run();
                } catch (InterruptedException ex) {
                }
            }
        }

        public void exit() {
            System.out.println("exit");
            mStopped = true;
            interrupt();
        }

        public synchronized void addEvent(Runnable r, long ms) {
            mEvents.add(new Event(r, System.currentTimeMillis() + ms));
            notify();
        }
    }
}
