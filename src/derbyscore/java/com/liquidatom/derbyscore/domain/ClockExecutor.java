package com.liquidatom.derbyscore.domain;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Russell Francis (russell.francis@gmail.com)
 * @version $Id: ClockExecutor.java 4 2010-03-12 11:40:07Z russ $
 */
@ThreadSafe
public class ClockExecutor implements Runnable {

    static private final Logger log = LoggerFactory.getLogger(ClockExecutor.class);
    static private final long FREQUENCY = 500;  // delay in milliseconds between updates of the clock.

    @GuardedBy("itself")
    private final List<Clock> clocks = new ArrayList<Clock>();

    public void run() {
        try {
            final List<Clock> removeClockList = new ArrayList<Clock>();
            while (true) {
                synchronized (clocks) {
                    try {
                        long currentTimeMillis = System.currentTimeMillis();
                        for (Clock clock : clocks) {
                            clock.tick(currentTimeMillis);

                            if (clock.isTerminated()) {
                                removeClockList.add(clock);
                            }
                        }
                    }
                    finally {
                        clocks.removeAll(removeClockList);
                        removeClockList.clear();
                    }
                }

                // yield to other thread allow them to do some work.
                try {
                    Thread.sleep(FREQUENCY);
                }
                catch (InterruptedException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Clock interupted exiting clock thread: " + e.getMessage());
                    }
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        finally {
            fireOnTerminate();
        }
    }

    protected void fireOnTerminate() {
        synchronized (clocks) {
            for (Clock clock : clocks) {
                clock.fireOnTerminate();
            }
        }
    }

    public void addClock(final Clock clock) {
        if (clock != null) {
            synchronized (clocks) {
                clocks.add(clock);
            }
        }
    }

    public void removeClock(final Clock clock) {
        if (clock != null) {
            synchronized (clocks) {
                clocks.remove(clock);
            }
        }
    }
}
