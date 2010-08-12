package com.liquidatom.derbyscore.domain;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A basic immutable data structure which holds a scalar and a unit for measuring the duration of time.
 *
 * @author Russell Francis (russell.francis@gmail.com)
 * @version $Id: Duration.java 10 2010-03-30 02:48:45Z russ $
 */
@Immutable
@ThreadSafe
public class Duration implements Serializable, Comparable<Duration> {

    final private long time;
    final private TimeUnit unit;

    /**
     * Construct a new Duration instance with the provided length and unit.
     *
     * @param time The number of units of duration this instance will represent.
     * @param unit The unit of time this duration will represent.
     */
    public Duration(final long time, final TimeUnit unit) {
        super();
        if (unit == null) {
            throw new IllegalArgumentException("The parameter unit must be non-null.");
        }
        if (unit.compareTo(TimeUnit.MILLISECONDS) < 0) {
            throw new IllegalArgumentException("The parameter unit must be milliseconds or larger.");
        }
        this.time = time;
        this.unit = unit;
    }

    /**
     * Get the length represented by this duration.  This number will be in the units
     * identified by {@link #getUnit()}.
     *
     * @return The length of this duration.
     */
    public long getTime() {
        return time;
    }

    /**
     * Get the unit that the length of this duration is represented in.
     *
     * @return The unit that the length of this duration is represented in.
     */
    public TimeUnit getUnit() {
        return unit;
    }

    /**
     * Get the length of time represented in milliseconds.
     *
     * @return The length of time represented in milliseconds.
     */
    public long getTimeInMilliseconds() {
        long result = time;

        if (TimeUnit.DAYS.equals(getUnit())) {
            result *= 24 * 60 * 60 * 1000;
        }
        else if (TimeUnit.HOURS.equals(getUnit())) {
            result *= 60 * 60 * 1000;
        }
        else if (TimeUnit.MINUTES.equals(getUnit())) {
            result *= 60 * 1000;
        }
        else if (TimeUnit.SECONDS.equals(getUnit())) {
            result *= 1000;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final Duration o) {
        long result = getTimeInMilliseconds() - o.getTimeInMilliseconds();
        return result > 0 ? 1 : result < 0 ? -1 : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Duration) {
            Duration that = (Duration)o;
            return getTimeInMilliseconds() == that.getTimeInMilliseconds();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        long value = getTimeInMilliseconds();
        return 413 * (int) (value ^ (value >>> 32));
    }
}
