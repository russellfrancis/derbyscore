package com.liquidatom.derbyscore.domain;

import java.awt.image.BufferedImage;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Domain object which represents a team.
 *
 * @author Russell Francis (russ@metro-six.com)
 */
@ThreadSafe
public class Team implements ReadWriteLock {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @GuardedBy("lock")
    private String name;

    @GuardedBy("lock")
    private BufferedImage image;

    @GuardedBy("lock")
    private int score = 0;

    @GuardedBy("lock")
    private int jamPoints = 0;

    @GuardedBy("lock")
    private int timeouts = 3;

    @GuardedBy("lock")
    private Bout bout;

    @GuardedBy("itself")
    private final Set<TeamListener> teamListeners = new LinkedHashSet<TeamListener>();

    public boolean addListener(TeamListener listener) {
        synchronized (teamListeners) {
            return teamListeners.add(listener);
        }
    }

    public boolean removeListener(TeamListener listener) {
        synchronized (teamListeners) {
            return teamListeners.remove(listener);
        }
    }

    protected void fireOnChanged() {
        synchronized (teamListeners) {
            for (TeamListener listener : teamListeners) {
                listener.onChanged(this);
            }
        }
    }

    public Bout getBout() {
        readLock().lock();
        try {
            return bout;
        }
        finally {
            readLock().unlock();
        }
    }

    public void setBout(final Bout bout) {
        writeLock().lock();
        try {
            this.bout = bout;
        }
        finally {
            writeLock().unlock();
        }
    }
    
    public String getName() {
        readLock().lock();
        try {
            return name;
        }
        finally {
            readLock().unlock();
        }
    }

    public void setName(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("The parameter name must be non-null.");
        }

        boolean fireOnChange = false;
        writeLock().lock();
        try {
            if (!name.equals(this.name)) {
                fireOnChange = true;
                this.name = name;
            }
        }
        finally {
            writeLock().unlock();
        }

        if (fireOnChange) {
            fireOnChanged();
        }
    }

    public BufferedImage getImage() {
        readLock().lock();
        try {
            return image;
        }
        finally {
            readLock().unlock();
        }
    }

    public void setImage(final BufferedImage image) {
        writeLock().lock();
        try {
            this.image = image;
        }
        finally {
            writeLock().unlock();
        }
        fireOnChanged();
    }

    public int getTimeouts() {
        readLock().lock();
        try {
            return timeouts;
        }
        finally {
            readLock().unlock();
        }
    }

    public void setTimeouts(final int timeouts) {
        if (timeouts < 0) {
            throw new IllegalArgumentException("The parameter timeouts must be >= 0.");
        }
        if (timeouts > 3) {
            throw new IllegalArgumentException("The parameter timeouts must be <= 3.");
        }

        boolean fireOnChange = false;
        writeLock().lock();
        try {
            if (timeouts != this.timeouts) {
                fireOnChange = true;
                this.timeouts = timeouts;
            }
        }
        finally {
            writeLock().unlock();
        }

        if (fireOnChange) {
            fireOnChanged();
        }
    }

    public int getJamPoints() {
        readLock().lock();
        try {
            return jamPoints;
        }
        finally {
            readLock().unlock();
        }
    }

    public void setJamPoints(final int jamPoints) {
        if (jamPoints < 0) {
            throw new IllegalArgumentException("The parameter jamPoints must be greater than or equal to zero.");
        }

        boolean fireOnChange = false;
        writeLock().lock();
        try {
            if (jamPoints != this.jamPoints) {
                fireOnChange = true;
                this.jamPoints = jamPoints;
            }
        }
        finally {
            writeLock().unlock();
        }

        if (fireOnChange) {
            fireOnChanged();
        }
    }

    public String getJamPointsString() {
        String value = Integer.valueOf(getJamPoints()).toString();
        while (value.length() < 2) {
            value = "0" + value;
        }
        return value;
    }

    public void applyJamPoints() {
        boolean fireOnChanged = false;
        writeLock().lock();
        try {
            if (jamPoints != 0) {
                fireOnChanged = true;
                score += jamPoints;
                jamPoints = 0;
            }
        }
        finally {
            writeLock().unlock();
        }

        if (fireOnChanged) {
            fireOnChanged();
        }
    }

    public int getScore() {
        readLock().lock();
        try {
            return score;
        }
        finally {
            readLock().unlock();
        }
    }

    public void setScore(final int score) {
        if (score < 0) {
            throw new IllegalArgumentException("The parameter score must be >= 0");
        }

        boolean fireOnChanged = false;

        writeLock().lock();
        try {
            if (score != this.score) {
                fireOnChanged = true;
                this.score = score;
            }
        }
        finally {
            writeLock().unlock();
        }

        if (fireOnChanged) {
            fireOnChanged();
        }
    }

    public String getScoreString() {
        String value = Integer.valueOf(getScore()).toString();
        while (value.length() < 3) {
            value = "0" + value;
        }
        return value;
    }

    public Lock readLock() {
        return lock.readLock();
    }

    public Lock writeLock() {
        return lock.writeLock();
    }
}
