package com.liquidatom.derbyscore.ui;

import com.liquidatom.derbyscore.domain.Team;
import java.awt.EventQueue;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Russell Francis (russ@metro-six.com)
 */
@Immutable
@ThreadSafe
public class TimeoutChangeListener implements ChangeListener {

    final private Team team;

    public TimeoutChangeListener(final Team team) {
        super();
        if (team == null) {
            throw new IllegalArgumentException("The parameter team must be non-null.");
        }
        this.team = team;
    }

    public void stateChanged(final ChangeEvent e) {
        assertDispatchThread();
        final Object eventSource = e.getSource();
        if (eventSource != null) {
            if (eventSource instanceof JSpinner) {
                final JSpinner spinner = (JSpinner)eventSource;
                team.setTimeouts(((Number)spinner.getValue()).intValue());
            }
            else {
                throw new IllegalArgumentException("Unable to update the timeouts based on an event generated from an " +
                        "object of type '" + eventSource.getClass().getName() + "'.");
            }
        }
    }

    protected void assertDispatchThread() {
        if (!EventQueue.isDispatchThread()) {
            throw new IllegalStateException("This method must be invoked from within the AWT Event Dispatch Thread!");
        }
    }
}
