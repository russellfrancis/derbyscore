package com.liquidatom.derbyscore.domain;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A basic implementation of a clock which keeps track of time as it elapses and provides hooks for listeners to be
 * notified when certain events occur on the clock.
 *
 * @author Russell Francis (russ@metro-six.com)
 */
@ThreadSafe
public class Clock implements ReadWriteLock {

    static private final Logger log = LoggerFactory.getLogger(Clock.class);

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @GuardedBy("lock")
    private long totalTime;

    @GuardedBy("lock")
    private long elapsedTime;

    @GuardedBy("lock")
    private long intervalBegin = Long.MIN_VALUE;

    @GuardedBy("lock")
    private boolean paused = false;

    @GuardedBy("lock")
    private boolean ended = false;

    @GuardedBy("lock")
    private boolean terminated = false;

    final private boolean padMinutes;

    @GuardedBy("itself")
    private final Set<ClockListener> clockListeners = new LinkedHashSet<ClockListener>();

    public Clock(final Duration duration, boolean padMinutes) {
        super();
        this.padMinutes = padMinutes;
        reset(duration);
    }

    public boolean addListener(final ClockListener listener) {
        synchronized (clockListeners) {
            return clockListeners.add(listener);
        }
    }

    public boolean removeListener(final ClockListener listener) {
        synchronized (clockListeners) {
            return clockListeners.remove(listener);
        }
    }

    protected void fireOnChanged() {
        synchronized (clockListeners) {
            for (ClockListener listener : clockListeners) {
                listener.onChanged(this);
            }
        }
    }

    protected void fireOnBegin() {
        synchronized (clockListeners) {
            for (ClockListener listener : clockListeners) {
                listener.onBegin(this);
            }
        }
    }

    protected void fireOnEnd() {
        synchronized (clockListeners) {
            for (ClockListener listener : clockListeners) {
                listener.onEnd(this);
            }
        }
    }

    protected void fireOnTerminate() {
        synchronized (clockListeners) {
            for (ClockListener listener : clockListeners) {
                listener.onTerminate(this);
            }
        }
    }

    protected void fireOnPause() {
        synchronized (clockListeners) {
            for (ClockListener listener : clockListeners) {
                listener.onPause(this);
            }
        }
    }

    protected void fireOnResume() {
        synchronized (clockListeners) {
            for (ClockListener listener : clockListeners) {
                listener.onResume(this);
            }
        }
    }

    /**
     * Get the total time which must elapse before this clock should be considered finished.
     *
     * @return The total time which must elapse for this clock to be finished.
     */
    private long getTotalTime() {
        readLock().lock();
        try {
            return totalTime;
        }
        finally {
            readLock().unlock();
        }
    }
    
    private void setTotalTime(final long totalTime) {
        writeLock().lock();
        try {
            this.totalTime = totalTime;
        }
        finally {
            writeLock().unlock();
        }

        fireOnChanged();
    }

    private long getElapsedTime() {
        readLock().lock();
        try {
            return elapsedTime;
        }
        finally {
            readLock().unlock();
        }
    }

    private void setElapsedTime(final long elapsedTime) {
        writeLock().lock();
        try {
            this.elapsedTime = elapsedTime;
        }
        finally {
            writeLock().unlock();
        }
        
        fireOnChanged();
    }
    
    private void addElapsedTime(final long elapsedTime) {
        writeLock().lock();
        try {
            this.elapsedTime += elapsedTime;
        }
        finally {
            writeLock().unlock();
        }
        
        fireOnChanged();
    }

    private long getIntervalBegin() {
        readLock().lock();
        try {
            return intervalBegin;
        }
        finally {
            readLock().unlock();
        }
    }

    private void setIntervalBegin(final long intervalBegin) {
        writeLock().lock();
        try {
            this.intervalBegin = intervalBegin;
        }
        finally {
            writeLock().unlock();
        }

        fireOnChanged();
    }

    public boolean isEnded() {
        readLock().lock();
        try {
            return ended;
        }
        finally {
            readLock().unlock();
        }
    }

    private void setEnded(boolean ended) {
        writeLock().lock();
        try {
            this.ended = ended;
        }
        finally {
            writeLock().unlock();
        }
    }

    public void terminate() {
        writeLock().lock();
        try {
            terminated = true;
        }
        finally {
            writeLock().unlock();
        }
    }

    public boolean isTerminated() {
        readLock().lock();
        try {
            return terminated;
        }
        finally {
            readLock().unlock();
        }
    }

    public boolean isPaused() {
        readLock().lock();
        try {
            return paused;
        }
        finally {
            readLock().unlock();
        }
    }

    private void setPaused(boolean paused) {
        writeLock().lock();
        try {
            this.paused = paused;
        }
        finally {
            writeLock().unlock();
        }
    }

    /**
     * Pause the execution of this clock.
     */
    public void pause() {
        boolean fireOnPause = false;
        writeLock().lock();
        try {
            if (isStarted() && !isPaused()) {
                fireOnPause = true;
                setPaused(true);
                addElapsedTime(System.currentTimeMillis() - getIntervalBegin());
            }
        }
        finally {
            writeLock().unlock();
        }

        if (fireOnPause) {
            fireOnPause();
        }
    }

    /**
     * Resume execution of the clock.
     */
    public void resume() {
        boolean fireOnResume = false;
        writeLock().lock();
        try {
            if (isPaused()) {
                fireOnResume = true;
                setPaused(false);
                setIntervalBegin(System.currentTimeMillis());
            }
        }
        finally {
            writeLock().unlock();
        }

        if (fireOnResume) {
            fireOnResume();
        }
    }

    public void reset(final Duration duration) {
        writeLock().lock();
        try {
            setElapsedTime(0L);
            setIntervalBegin(Long.MIN_VALUE);
            setTotalTime(duration.getTimeInMilliseconds() + 999L);
            setEnded(false);
            setPaused(false);
        }
        finally {
            writeLock().unlock();
        }
    }

    private boolean isStarted() {
        return getIntervalBegin() != Long.MIN_VALUE;
    }

    public String getDisplayTime() {
        long elapsedMilliseconds;

        readLock().lock();
        try {
            elapsedMilliseconds = getTotalTime() - Math.max(0, getElapsedTime());
        }
        finally {
            readLock().unlock();
        }

        elapsedMilliseconds = Math.max(0L, elapsedMilliseconds);
        long minutesRemaining = elapsedMilliseconds / 60000;
        long secondsRemaining = (elapsedMilliseconds % 60000) / 1000;

        StringBuilder result = new StringBuilder();
        result.append((padMinutes && minutesRemaining < 10) ? "0" : "").append(minutesRemaining);
        result.append(":");
        result.append((secondsRemaining < 10 ? "0" : "")).append(secondsRemaining);
        return result.toString();
    }

    /**
     * Start this clock running.
     */
    public void begin() {
        boolean fireOnBegin = false;

        writeLock().lock();
        try {
            if (!isStarted()) {
                fireOnBegin = true;
                setIntervalBegin(System.currentTimeMillis());
            }
        }
        finally {
            writeLock().unlock();
        }

        if (fireOnBegin) {
            fireOnBegin();
        }
    }

    public void beginAndSynchronizeTo(Clock clock) {
        writeLock().lock();
        try {
            if (!isStarted()) {
                clock.writeLock().lock();
                try {
                    begin();

                    long syncBegin = clock.getIntervalBegin() % 1000;
                    long currentBegin = getIntervalBegin() % 1000;
                    if (currentBegin != syncBegin) {
                        long adjustment = syncBegin - currentBegin;
                        if (Math.abs(adjustment) > 500) {
                            adjustment -= 1000;
                        }
                        setIntervalBegin(getIntervalBegin() + adjustment);
                    }

                    syncBegin = clock.getElapsedTime() % 1000;
                    currentBegin = getElapsedTime() % 1000;
                    if (currentBegin != syncBegin) {
                        long adjustment = syncBegin - currentBegin;
                        if (Math.abs(adjustment) > 500) {
                            adjustment -= 1000;
                        }
                        addElapsedTime(adjustment);
                    }

                    if (log.isTraceEnabled()) {
                        log.trace("jam elapsed time = '" + getElapsedTime() % 1000 + "' period begin = '" + clock.getElapsedTime() % 1000 + "'.");
                        log.trace("jam interval begin = '" + getIntervalBegin() % 1000 + "' period begin = '" + clock.getIntervalBegin() % 1000 + "'.");
                    }
                }
                finally {
                    clock.writeLock().unlock();
                }
            }
        }
        finally {
            writeLock().unlock();
        }
    }


    /**
     * End the execution of this clock.
     */
    public void end() {
        boolean fireOnEnd = false;
        writeLock().lock();
        try {
            if (isStarted() && !isEnded()) {
                fireOnEnd = true;
                setEnded(true);
                if (!isPaused()) {
                    final long intervalEnd = System.currentTimeMillis();
                    addElapsedTime(intervalEnd - getIntervalBegin());
                    setIntervalBegin(intervalEnd);
                }
            }
        }
        finally {
            writeLock().unlock();
        }

        if (fireOnEnd) {
            fireOnEnd();
        }
    }

    public void tick(long currentTimeMillis) {
        writeLock().lock();
        try {
            if (!isTerminated() && !isPaused() && !isEnded()) {
                if (getElapsedTime() >= (getTotalTime()-999L)) {
                    // this timer has expired.
                    setElapsedTime(getTotalTime());
                    end();
                }
                else if (isStarted()) {
                    long intervalEnd = currentTimeMillis;
                    addElapsedTime(intervalEnd - getIntervalBegin());
                    setIntervalBegin(intervalEnd);
                }
            }
        }
        finally {
            writeLock().unlock();
        }
    }

    public Lock readLock() {
        return lock.readLock();
    }

    public Lock writeLock() {
        return lock.writeLock();
    }
}
