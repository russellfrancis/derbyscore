package com.liquidatom.derbyscore.theme;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * The global reference to the {@code Theme} currently being used when rendering the scoreboard 
 * can be retrieved from this factory.
 *
 * @author Russell Francis (russ@metro-six.com)
 */
@ThreadSafe
public class ThemeFactory {

    final static private ThemeFactory instance = new ThemeFactory();

    /**
     * Grab a reference to the ThemeFactory singleton.
     *
     * @return The global ThemeFactory instance.
     */
    static public ThemeFactory getInstance() {
        return instance;
    }

    @GuardedBy("this")
    private Theme current;

    /**
     * A private constructor to prevent instantiation.
     */
    private ThemeFactory() {
    }

    /**
     * Get the current theme used to render the scoreboard.
     *
S     * @return The current theme used to render the scoreboard.
     */
    synchronized public Theme getCurrent() {
        return current;
    }

    /**
     * Set the current theme used to render the scoreboard.
     *
     * @param current The current theme to use to render the scoreboard.
     */
    synchronized public void setCurrent(final Theme current) {
        this.current = current;
    }
}
