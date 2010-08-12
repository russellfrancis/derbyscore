package com.liquidatom.derbyscore.ui;

import com.liquidatom.derbyscore.domain.Clock;
import com.liquidatom.derbyscore.domain.Duration;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Russell Francis (russell.francis@gmail.com)
 * @version $Id: TimeTextFieldKeyListener.java 11 2010-04-03 03:43:27Z russ $
 */
@Immutable
@ThreadSafe
public class TimeTextFieldKeyListener implements KeyListener {
    
    static private final Logger log = LoggerFactory.getLogger(TimeTextFieldKeyListener.class);
    static private final Pattern colonPattern = Pattern.compile(":");

    final private JTextField timeTextField;
    final private Clock clock;

    public TimeTextFieldKeyListener(final JTextField timeTextField, final Clock clock) {
        super();
        if (timeTextField == null) {
            throw new NullPointerException("The parameter timeTextField must be non-null.");
        }
        if (clock == null) {
            throw new NullPointerException("The parameter clock must be non-null.");
        }
        this.timeTextField = timeTextField;
        this.clock = clock;
    }

    public void keyReleased(final KeyEvent e) {
    }

    public void keyTyped(final KeyEvent e) {
    }

    public void keyPressed(final KeyEvent e) {
        assertDispatchThread();
        if (KeyEvent.VK_ENTER == e.getKeyCode()) {
            final String text = timeTextField.getText();
            final String[] parts = colonPattern.split(text);
            if (parts != null && parts.length == 2) {
                try {
                    int minutes = Integer.parseInt(parts[0]);
                    int seconds = Integer.parseInt(parts[1]);
//                    clock.setTimeRemaining((((minutes * 60) + seconds) * 1000) + 999L);
                    clock.reset(new Duration(((minutes * 60) + seconds) * 1000, TimeUnit.MILLISECONDS));
                }
                catch (NumberFormatException ex) {
                    if (log.isWarnEnabled()) {
                        log.warn("Unable to convert '" + parts[0] + "' or '" + parts[1] + "' into integers.");
                    }
                }
            }

            timeTextField.setText(clock.getDisplayTime());
        }
    }

    protected void assertDispatchThread() {
        if (!EventQueue.isDispatchThread()) {
            throw new IllegalStateException("This method must be invoked from within the AWT Event Dispatch Thread!");
        }
    }
}
