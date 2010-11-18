package com.liquidatom.derbyscore.domain;

/**
 * Receive notification when events occur on a particular AbstractClock.
 *
 * @author Russell Francis (russ@metro-six.com)
 */
public interface ClockListener {
    public void onChanged(Clock clock);
    public void onBegin(Clock clock);
    public void onEnd(Clock clock);
    public void onPause(Clock clock);
    public void onResume(Clock clock);
    public void onTerminate(Clock clock);
}
