package com.liquidatom.derbyscore.theme;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.easymock.classextension.EasyMock.*;

/**
 *
 * @author Russell Francis (russell.francis@gmail.com)
 * @version $Id: TextElementTest.java 9 2010-03-17 02:13:12Z russ $
 */
public class TextElementTest {

    @Test
    public void testConstructor_NullPosition_ThrowsIAE() {
        Theme theme = new Theme(null);

        try {
            new TextElement(theme, "return true;", null, "Hello", "font-ref");
            fail("Should throw an IAE.");
        }
        catch (IllegalArgumentException e) {
            // success.
        }
    }

    @Test
    public void  testConstructor_NullText_ThrowsIAE() {
        Theme theme = new Theme(null);

        try {
            new TextElement(theme, "return true;", new Rectangle(0, 0, 10, 10), null, "font-ref");
            fail("Should throw an IAE.");
        }
        catch (IllegalArgumentException e) {
            // success.
        }
    }

    @Test
    public void testConstructor_EmptyText_ThrowsIAE() {
        Theme theme = new Theme(null);

        try {
            new TextElement(theme, "return true;", new Rectangle(0, 0, 10, 10), "  ", "font-ref");
            fail("Should throw an IAE.");
        }
        catch (IllegalArgumentException e) {
            // success.
        }
    }

    @Test
    public void testConstructor_NullColor_ThrowsIAE() {
        Theme theme = new Theme(null);

        try {
            new TextElement(theme, "return true;", new Rectangle(0, 0, 10, 10), "Hello", "font-ref", null);
            fail("Should throw an IAE.");
        }
        catch (IllegalArgumentException e) {
            // success.
        }
    }
    
    @Test
    public void testConstructor_NullFontRef_ThrowsIAE() {
        Theme theme = new Theme(null);
        
        try {
            new TextElement(theme, "return true;", new Rectangle(0, 0, 10, 10), "Hello", "font-ref-not-available", Color.RED);
            fail("Should throw an IAE.");
        }
        catch (IllegalArgumentException e) {
            // success.
        }
    }

    @Test
    public void testConstructorSlim_Success() {
        final Font FONT = new Font("my", 1, 1);
        final String FONT_REF = "font";
        final Rectangle position = new Rectangle(0, 0, 10, 10);
        final String text = "Hello";

        Theme theme = createStrictMock(Theme.class);
        expect(theme.getFont(FONT_REF)).andReturn(FONT);
        expect(theme.getFont(FONT_REF)).andReturn(FONT);
        replay(theme);

        TextElement e = new TextElement(theme, "return true;", position, text, FONT_REF);
        assertEquals(Color.WHITE, e.getColor());
        assertEquals(position, e.getPosition());
        assertFalse(position == e.getPosition());
        assertEquals(FONT_REF, e.getFontRef());
        assertEquals(FONT, e.getFont());
        verify(theme);
    }

    @Test
    public void testConstructor_Success() {
        final Font FONT = new Font("my", 1, 1);
        final String FONT_REF = "font";
        final Rectangle position = new Rectangle(0, 0, 10, 10);
        final String text = "Hello";

        Theme theme = createStrictMock(Theme.class);
        expect(theme.getFont(FONT_REF)).andReturn(FONT);
        expect(theme.getFont(FONT_REF)).andReturn(FONT);
        replay(theme);

        TextElement e = new TextElement(theme, "return true;", position, text, FONT_REF, Color.RED);
        assertEquals(Color.RED, e.getColor());
        assertEquals(position, e.getPosition());
        assertFalse(position == e.getPosition());
        assertEquals(FONT_REF, e.getFontRef());
        assertEquals(FONT, e.getFont());
        verify(theme);
    }

    @Test
    public void testGetText() {
        final Theme theme = new Theme();
        final String FONT_REF = "myfont";
        final Font font = new Font("myfont", 1, 1);
        final Rectangle position = new Rectangle(0, 0, 10, 10);
        final String text = "'Hello, World'";
        theme.addResource(FONT_REF, font);

        TextElement e = new TextElement(theme, "return true;", position, text, FONT_REF);
        assertEquals("Hello, World", e.getText(null));
    }

    @Test
    public void testGetText_NullResult_ReturnsEmptyString() {
        final Theme theme = new Theme();
        final String FONT_REF = "myfont";
        final Font font = new Font("myfont", 1, 1);
        final Rectangle position = new Rectangle(0, 0, 10, 10);
        final String text = "null";
        theme.addResource(FONT_REF, font);

        TextElement e = new TextElement(theme, "return true;", position, text, FONT_REF);
        assertEquals("", e.getText(null));
    }

    @Test
    public void testGetText_InvalidScript_ThrowsRuntimeException() throws Exception {
        final Theme theme = createStrictMock(Theme.class);
        final ScriptEngine engine = createStrictMock(ScriptEngine.class);
        final String FONT_REF = "myfont";
        final Font FONT = new Font("myfont", 1, 1);
        final Rectangle position = new Rectangle(0, 0, 10, 10);
        final String text = "var x;";

        expect(theme.getFont(FONT_REF)).andReturn(FONT);
        expect(theme.getFont(FONT_REF)).andReturn(FONT);
        expect(theme.acquireJavascriptEngine(null)).andReturn(engine);
        expect(engine.eval(text)).andThrow(new ScriptException("Yikes!"));
        theme.releaseJavascriptEngine();

        replay(theme, engine);

        TextElement e = new TextElement(theme, "return true;", position, text, FONT_REF);
        try {
            e.getText(null);
            fail("Expected to throw a RuntimeException");
        }
        catch (RuntimeException ex) {
            // expected.
        }

        verify(theme, engine);
    }

    @Test
    public void testIsVisible() {
        final Theme theme = new Theme();
        final String FONT_REF = "myfont";
        final Font font = new Font("myfont", 1, 1);
        final Rectangle position = new Rectangle(0, 0, 10, 10);
        final String text = "null";
        theme.addResource(FONT_REF, font);

        TextElement e;

        // Script returns false.
        e = new TextElement(theme, "false;", position, text, FONT_REF);
        assertFalse(e.isVisible(null));

        // Script returns true
        e = new TextElement(theme, "true;", position, text, FONT_REF);
        assertTrue(e.isVisible(null));

        // Script is null
        e = new TextElement(theme, null, position, text, FONT_REF);
        assertTrue(e.isVisible(null));

        // Invalid script.
        e = new TextElement(theme, "var x-p;", position, text, FONT_REF);
        try {
            e.isVisible(null);
            fail("Expected to throw RuntimeException");
        }
        catch (RuntimeException ex) {
            // success.
        }

        // Script returns null
        e = new TextElement(theme, "null;", position, text, FONT_REF);
        try {
            e.isVisible(null);
            fail("Expected to throw RuntimeException");
        }
        catch (RuntimeException ex) {
            // success.
        }
    }
}
