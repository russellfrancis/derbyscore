package com.liquidatom.derbyscore.domain;

/**
 * Receive notification when events occur on a particular AbstractClock.
 *
 * @author Russell Francis (russell.francis@gmail.com)
 * @version $Id: ClockListener.java 4 2010-03-12 11:40:07Z russ $
 */
public interface ClockListener {
    public void onChanged(Clock clock);
    public void onBegin(Clock clock);
    public void onEnd(Clock clock);
    public void onPause(Clock clock);
    public void onResume(Clock clock);
    public void onTerminate(Clock clock);
}
