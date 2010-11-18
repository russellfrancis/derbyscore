package com.liquidatom.derbyscore.theme;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.easymock.classextension.EasyMock.*;

/**
 *
 * @author Russell Francis (russ@metro-six.com)
 */
public class ImageElementTest {

    @Test
    public void testConstructor_NullTheme_ThrowsNPE() {
        try {
            ImageElement ie = new ImageElement(null, "return true;", new Rectangle(5, 5), "team-a-logo");
            fail("Expected to throw a NPE.");
        }
        catch (NullPointerException e) {
            // success
        }
    }

    @Test
    public void testConstructor_NullPosition_DefaultsToImageSize() {
        final String IMAGE_REFERENCE = "team-a-logo";
        final Integer WIDTH = new Integer(10);
        final Integer HEIGHT = new Integer(15);

        Theme theme = createStrictMock(Theme.class);
        BufferedImage img = createStrictMock(BufferedImage.class);

        expect(theme.getImage(IMAGE_REFERENCE)).andReturn(img);
        expect(img.getWidth()).andReturn(WIDTH);
        expect(theme.getImage(IMAGE_REFERENCE)).andReturn(img);
        expect(img.getHeight()).andReturn(HEIGHT);

        replay(theme, img);

        ImageElement element = new ImageElement(theme, null, null, IMAGE_REFERENCE);
        assertEquals(WIDTH.intValue(), (int)element.getPosition().getWidth());
        assertEquals(HEIGHT.intValue(), (int)element.getPosition().getHeight());
        assertEquals(IMAGE_REFERENCE, element.getImgRef());

        verify(theme, img);
    }

    @Test
    public void testConstructor_PositionProvidedIsUsedButCloned() {
        final String IMAGE_REFERENCE = "team-a-logo";
        final int X = 10;
        final int Y = 20;
        final int WIDTH = 30;
        final int HEIGHT = 40;
        final Rectangle position = new Rectangle(X, Y, WIDTH, HEIGHT);

        Theme theme = createStrictMock(Theme.class);

        replay(theme);

        ImageElement element = new ImageElement(theme, null, position, IMAGE_REFERENCE);
        assertEquals(X, (int)element.getPosition().getX());
        assertEquals(Y, (int)element.getPosition().getY());
        assertEquals(WIDTH, (int)element.getPosition().getWidth());
        assertEquals(HEIGHT, (int)element.getPosition().getHeight());
        assertFalse(position == element.getPosition());

        verify(theme);
    }
}
