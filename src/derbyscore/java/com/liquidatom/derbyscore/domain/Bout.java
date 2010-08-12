package com.liquidatom.derbyscore.domain;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A domain object which represents a bout between two teams.
 *
 * @author Russell Francis (russell.francis@gmail.com)
 * @version $Id: Bout.java 11 2010-04-03 03:43:27Z russ $
 */
@ThreadSafe
public class Bout implements ClockListener, TeamListener, ReadWriteLock {

    static public final Duration PERIOD_DURATION = new Duration(30, TimeUnit.MINUTES);
    static public final Duration JAM_DURATION = new Duration(2, TimeUnit.MINUTES);
    static public final Duration TEAM_TIMEOUT_DURATION = new Duration(1, TimeUnit.MINUTES);
    static public final Duration LINEUP_DURATION = new Duration(30, TimeUnit.SECONDS);
    static public final Duration INTERMISSION_DURATION = new Duration(10, TimeUnit.MINUTES);
    static public final Duration OVERTIME_LINEUP_DURATION = new Duration(1, TimeUnit.MINUTES);

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final ClockExecutor clockExecutor = new ClockExecutor();
    private final Clock periodClock = new Clock(PERIOD_DURATION, true);
    private final Clock jamClock = new Clock(JAM_DURATION, false);
    private final Team teamA;
    private final Team teamB;

    @GuardedBy("itself")
    private final Set<BoutListener> boutListeners = new LinkedHashSet<BoutListener>();

    @GuardedBy("lock")
    private int period = 1;
    @GuardedBy("lock")
    private boolean overtime = false;
    
    // The team which currently has lead jammer.
    @GuardedBy("lock")
    private Team lead;

    @GuardedBy("lock")
    private BoutState boutState = BoutState.UNCONFIGURED;

    public Bout(final Team teamA, final Team teamB) {
        super();

        this.teamA = teamA;
        this.teamA.addListener(this);

        this.teamB = teamB;
        this.teamB.addListener(this);

        clockExecutor.addClock(periodClock);
        periodClock.addListener(this);

        clockExecutor.addClock(jamClock);
        jamClock.addListener(this);

        executorService.submit(clockExecutor);
    }

    public boolean addListener(BoutListener listener) {
        synchronized (boutListeners) {
            return boutListeners.add(listener);
        }
    }

    public boolean removeListener(BoutListener listener) {
        synchronized (boutListeners) {
            return boutListeners.remove(listener);
        }
    }

    private void fireOnChanged() {
        synchronized (boutListeners) {
            for (BoutListener listener : boutListeners) {
                listener.onChanged();
            }
        }
    }

    /**
     * Returns the team which currently has lead or null if the lead jammer status has not been assigned to any
     * particular team for this jam.
     *
     * @return The team which has the lead jammer status or null if it has not been assigned to any team.
     */
    public Team getLead() {
        readLock().lock();
        try {
            return lead;
        }
        finally {
            readLock().unlock();
        }
    }

    public boolean isTeamALead() {
        readLock().lock();
        try {
            return lead != null && lead.equals(teamA);
        }
        finally {
            readLock().unlock();
        }
    }

    public boolean isTeamBLead() {
        readLock().lock();
        try {
            return lead != null && lead.equals(teamB);
        }
        finally {
            readLock().unlock();
        }
    }

    /**
     * Set the team which has lead jammer status.
     *
     * @param lead The team which has lead jammer status or null if the status should be cleared from both teams.
     */
    public void setLead(final Team lead) {
        boolean fireOnChanged = false;
        
        writeLock().lock();
        try {
            if (lead != null && !lead.equals(teamA) && !lead.equals(teamB)) {
                throw new IllegalStateException("Unable to assign lead status to a team which isn't in this bout.");
            }
            
            if (this.lead != lead) {
                fireOnChanged = true;
                this.lead = lead;
            }
        }
        finally {
            writeLock().unlock();
        }
        
        if (fireOnChanged) {
            fireOnChanged();
        }
    }

    public int getPeriod() {
        readLock().lock();
        try {
            return period;
        }
        finally {
            readLock().unlock();
        }
    }

    public void setPeriod(final int period) {
        if (period != 1 && period != 2) {
            throw new IllegalArgumentException("The period must be 1 or 2.");
        }

        writeLock().lock();
        try {
            this.period = period;
        }
        finally {
            writeLock().unlock();
        }
    }

    public boolean isOvertime() {
        readLock().lock();
        try {
            return overtime;
        }
        finally {
            readLock().unlock();
        }
    }

    public void setOvertime(final boolean overtime) {
        writeLock().lock();
        try {
            this.overtime = overtime;
        }
        finally {
            writeLock().unlock();
        }
    }

    public BoutState getBoutState() {
        readLock().lock();
        try {
            return boutState;
        }
        finally {
            readLock().unlock();
        }
    }

    protected void setBoutState(final BoutState boutState) {
        if (boutState == null) {
            throw new IllegalArgumentException("The parameter boutState must be non-null.");
        }

        boolean fireOnChanged = false;

        writeLock().lock();
        try {
            if (!boutState.equals(this.boutState)) {
                fireOnChanged = true;
                this.boutState = boutState;
            }
        }
        finally {
            writeLock().unlock();
        }

        if (fireOnChanged) {
            fireOnChanged();
        }
    }

    public boolean isTimeout() {
        return BoutState.TEAM_TIMEOUT.equals(getBoutState());
    }

    public Clock getPeriodClock() {
        return periodClock;
    }

    public Clock getJamClock() {
        return jamClock;
    }

    public Team getTeamA() {
        return teamA;
    }

    public Team getTeamB() {
        return teamB;
    }

    public void markConfigured() {
        setBoutState(BoutState.CONFIGURED);
    }

    /**
     * Call a timeout for the given team, in order for a timeout to be successfully called, the bout must be in the
     * LINEUP state and the team must have a positive number of timeouts remaining.
     *
     * @return true if the timeout was called successfully, false otherwise.
     */
    public void timeout() {
        writeLock().lock();
        try {
            getPeriodClock().pause();
            getJamClock().reset(TEAM_TIMEOUT_DURATION);
            getJamClock().beginAndSynchronizeTo(periodClock);

            setBoutState(BoutState.TEAM_TIMEOUT);
        }
        finally {
            writeLock().unlock();
        }
    }

    public void officialTimeout() {
        writeLock().lock();
        try {
            getPeriodClock().pause();
            getJamClock().pause();
            setBoutState(BoutState.OFFICIAL_TIMEOUT);
        }
        finally {
            writeLock().unlock();
        }
    }

    public void beginJam() {
        writeLock().lock();
        try {
//            if (getBoutState().equals(BoutState.INTERMISSION)) {
//                getPeriodClock().end();
//                getPeriodClock().reset(PERIOD_DURATION);
//            }

            getJamClock().end();
            getJamClock().reset(JAM_DURATION);

            setLead(null);
            getTeamA().applyJamPoints();
            getTeamB().applyJamPoints();

            getPeriodClock().begin();
            getPeriodClock().resume();
            getJamClock().beginAndSynchronizeTo(periodClock);

            setBoutState(BoutState.JAMMING);
        }
        finally {
            writeLock().unlock();
        }
    }

    public void lineup() {
        writeLock().lock();
        try {
            getJamClock().end();
            getJamClock().reset(LINEUP_DURATION);
            getJamClock().beginAndSynchronizeTo(getPeriodClock());
            setBoutState(BoutState.LINEUP);
        }
        finally {
            writeLock().unlock();
        }
    }

    public void endJam() {
        lineup();
    }

    public void onPause(Clock clock) {
    }

    public void onResume(Clock clock) {
    }

    public void onChanged(Clock clock) {
        fireOnChanged();
    }

    public void onBegin(Clock clock) {
        fireOnChanged();
    }

    public void onEnd(Clock clock) {
        if (clock == getPeriodClock()) {
            onPeriodClockEnd();
        }
        else if (clock == getJamClock()) {
            onJamClockEnd();
        }

        fireOnChanged();
    }

    public void onTerminate(Clock clock) {
        fireOnChanged();
    }

    public void onChanged(Team team) {
        fireOnChanged();
    }

    private void onPeriodClockEnd() {
//        writeLock().lock();
//        try {
//            // period ended.
//            if (getBoutState().equals(BoutState.INTERMISSION)) {
//                getPeriodClock().reset(PERIOD_DURATION);
//                getJamClock().reset(JAM_DURATION);
//            }
//            // if state is jamming let it finish.
//            else if (!getBoutState().equals(BoutState.JAMMING)) {
//                getJamClock().reset(JAM_DURATION);
//                if (getPeriod() == 1) {
//                    setPeriod(getPeriod() + 1);
//                    getPeriodClock().reset(INTERMISSION_DURATION);
//                    getPeriodClock().begin();
//                    setBoutState(BoutState.INTERMISSION);
//                }
//                else {
//                    // overtime (?)
//                }
//            }
//        }
//        finally {
//            writeLock().unlock();
//        }
    }

    private void onJamClockEnd() {
        writeLock().lock();
        try {
            if (getBoutState().equals(BoutState.LINEUP)) {
                // Don't automatically start the next jam.  require a user click to start.
                // beginJam();
            }
            else if (getBoutState().equals(BoutState.JAMMING) || isTimeout()) {
                endJam();
            }
//
//            if (getPeriodClock().isEnded()) {
//                if (getPeriod() == 1) {
//                    setPeriod(getPeriod() + 1);
//                    getJamClock().reset(JAM_DURATION);
//                    getPeriodClock().reset(INTERMISSION_DURATION);
//                    getPeriodClock().begin();
//                    setBoutState(BoutState.INTERMISSION);
//                }
//                else if (getPeriod() == 2 && !isOvertime()) {
//                    setOvertime(true);
//                    getJamClock().reset(OVERTIME_LINEUP_DURATION);
//                    getJamClock().beginAndSynchronizeTo(getPeriodClock());
//                    setBoutState(BoutState.OVERTIME_LINEUP);
//                }
//            }
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
