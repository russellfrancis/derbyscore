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
 * @author Russell Francis (russell.francis@gmail.com)
 * @version $Id: ScoreChangeListener.java 11 2010-04-03 03:43:27Z russ $
 */
@Immutable
@ThreadSafe
public class ScoreChangeListener implements ChangeListener {

    final private Team team;

    public ScoreChangeListener(final Team team) {
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
                team.setScore(((Number)spinner.getValue()).intValue());
            }
            else {
                throw new IllegalArgumentException("Unable to update the score based on an event generated from an " +
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
