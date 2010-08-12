package com.liquidatom.derbyscore.theme;

import java.awt.Rectangle;
import java.util.Map;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * An abstract base class for theme elements such as text or images that can conditionally be rendered on the 
 * scoreboard.
 *
 * @author Russell Francis (russell.francis@gmail.com)
 * @version $Id: ThemeElement.java 9 2010-03-17 02:13:12Z russ $
 */
@ThreadSafe
@Immutable
abstract public class ThemeElement {

    final private Theme theme;
    final private String visibleConditionScript;

    /**
     * Construct a new ThemeElement.
     *
     * @param theme The theme which this element belongs to.
     * @param visibleConditionScript An optional snippet of javascript which will be evaluated to determine whether we
     *  should render this element.
     */
    protected ThemeElement(final Theme theme, final String visibleConditionScript) {
        super();
        if (theme == null) {
            throw new NullPointerException("The parameter theme must be non-null.");
        }
        this.theme = theme;
        this.visibleConditionScript = visibleConditionScript;
    }

    /**
     * Determine whether this element is visible or not.
     *
     * @param scope The variables which should be exposed to the javascript engine.
     * @return true if this element is visible, false otherwise.
     */
    public boolean isVisible(final Map<String, Object> scope) {
        if (getVisibleConditionScript() != null) {
            ScriptEngine engine = getTheme().acquireJavascriptEngine(scope);
            try {
                try {
                    Object result = engine.eval(getVisibleConditionScript());
                    return Boolean.parseBoolean(result.toString());
                }
                catch (ScriptException e) {
                    throw new RuntimeException(e);
                }
            }
            finally {
                getTheme().releaseJavascriptEngine();
            }
        }
        return true;
    }

    /**
     * Get the {@link Theme} which this element belongs too.
     *
     * @return The Theme which this element belongs too.
     */
    protected Theme getTheme() {
        return theme;
    }

    /**
     * Get the javascript which should be evaluated to determine whether this element is visible or not.
     *
     * @return The javascript script which will be evaluated to determine whether this element is visible or not.
     */
    public String getVisibleConditionScript() {
        return visibleConditionScript;
    }

    /**
     * Get the bounding rectangle into which this element should be rendered.
     *
     * @return The bounding rectangle into which this element should be rendered.
     */
    abstract public Rectangle getPosition();
}
