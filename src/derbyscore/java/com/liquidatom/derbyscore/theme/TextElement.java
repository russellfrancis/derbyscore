package com.liquidatom.derbyscore.theme;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.Map;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Used to represent a snippet of text which will be conditionally rendered onto the scoreboard.
 *
 * @author Russell Francis (russ@metro-six.com)
 */
@ThreadSafe
@Immutable
public class TextElement extends ThemeElement {

    final private String text;
    final private String fontRef;
    final private Font font;
    final private Color color;
    final private Rectangle position;

    /**
     * Construct a new TextElement instance.
     * 
     * @param theme The theme that this element is contained within.
     * @param visibleConditionScript An optional snippet of javascript which will be evaluated to determine if this 
     *  text should be rendered.
     * @param position The bounding rectangle where this text should be rendered.
     * @param textScript A snippet of javascript text which will be evaluated, the result will be rendered as text.
     * @param fontRef A reference to the font used to render the text.
     */
    public TextElement(
            final Theme theme,
            final String visibleConditionScript,
            final Rectangle position,
            final String textScript,
            final String fontRef) {
        this(theme, visibleConditionScript, position, textScript, fontRef, Color.WHITE);
    }

    /**
     * Construct a new TextElement instance.
     *
     * @param theme The theme that this element is contained within.
     * @param visibleConditionScript An optional snippet of javascript which will be evaluated to determine if this
     *  text should be rendered.
     * @param position The bounding rectangle where this text should be rendered.
     * @param textScript A snippet of javascript text which will be evaluated, the result will be rendered as text.
     * @param fontRef A reference to the font used to render the text.
     * @param color The color used to render this text.
     */
    public TextElement(
            final Theme theme,
            final String visibleConditionScript,
            final Rectangle position,
            final String text,
            final String fontRef,
            final Color color)
    {
        super(theme, visibleConditionScript);
        
        if (position == null) {
            throw new IllegalArgumentException("The parameter text must be non-null.");
        }
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("The parameter text must be non-null and non-empty.");
        }
        if (color == null) {
            throw new IllegalArgumentException("The parameter color must be non-null.");
        }
        if (theme.getFont(fontRef) == null) {
            throw new IllegalArgumentException("The paramter fontRef must reference a font no font identified by '" +
                    fontRef + "' was found!");
        }

        this.position = (Rectangle) position.clone();
        this.text = text;
        this.fontRef = fontRef;
        this.font = theme.getFont(fontRef);
        this.color = color;
    }

    /**
     * Get the font which should be used to render this text.
     *
     * @return The font which should be used to render this text.
     */
    public Font getFont() {
        return font;
    }

    /**
     * Evaluate the text javascript and return the result.
     *
     * @param scope A map containing any variables which should be exposed to the evaluated script.
     * @return The result of the script as a string.
     */
    public String getText(final Map<String, Object> scope) {
        Object result = null;
        ScriptEngine engine = getTheme().acquireJavascriptEngine(scope);
        try {
            try {
                result = engine.eval(text);
            }
            catch (ScriptException e) {
                throw new RuntimeException(e);
            }
        }
        finally {
            getTheme().releaseJavascriptEngine();
        }

        return result == null ? "" : result.toString();
    }

    /**
     * The unique label used to identify the font used when rendering this text.
     *
     * @return The unique label used to identify the font used when rendering this text.
     */
    public String getFontRef() {
        return fontRef;
    }

    /**
     * The color to use when rendering this text.
     *
     * @return The color to use when rendering this text.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Get a {@link Rectangle} describing the bounds which this text will be rendered into.
     *
     * @return A Rectangle describing the bounds which this text will be rendered into.
     */
    @Override
    public Rectangle getPosition() {
        return (Rectangle) position.clone();
    }
}
