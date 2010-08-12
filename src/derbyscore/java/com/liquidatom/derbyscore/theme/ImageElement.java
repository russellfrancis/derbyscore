package com.liquidatom.derbyscore.theme;

import java.awt.Rectangle;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Represents an image element within the theme.
 *
 * @author Russell Francis (russell.francis@gmail.com)
 * @version $Id: ImageElement.java 4 2010-03-12 11:40:07Z russ $
 */
@ThreadSafe
@Immutable
public class ImageElement extends ThemeElement {

    final private String imgRef;
    final private Rectangle position;

    /**
     * Construct a new {@code ImageElement} used to conditionally render an image as part of the theme.
     *
     * @param theme The theme which this element belongs too.
     * @param conditionJs A snippet of javascript code which if non-null will be evaluated to determine if the image
     *      should be rendered.  The javascript has access to the current {@link Bout} instance.
     * @param position An optional rectangle which defines the confines into which the image should be scaled and
     *      rendered.  If this has 0 width the width of the image will be used.  If this has 0 height, the height of the
     *      image will be used.  If this is null the width and height will be taken from the referenced image.
     * @param imgRef The unique label for the image which we wish to render.
     */
    public ImageElement(final Theme theme, final String conditionJs, final Rectangle position, final String imgRef) {
        super(theme, conditionJs);
        this.imgRef = imgRef;

        int x = (int) (position == null ? 0 : position.getX());
        int y = (int) (position == null ? 0 : position.getY());
        int width = (int) (position == null ? 0 : position.getWidth());
        if (width == 0) {
            width = getTheme().getImage(imgRef).getWidth();
        }

        int height = (int) (position == null ? 0 : position.getHeight());
        if (height == 0) {
            height = getTheme().getImage(imgRef).getHeight();
        }

        this.position = new Rectangle(x, y, width, height);
    }

    /**
     * Get the unique label used to identify the image that this element will render.
     *
     * @return The unique label used to identify the image that this element will render.
     */
    public String getImgRef() {
        return imgRef;
    }

    /**
     * Get a {@link Rectangle} describing the bounds which this image will be rendered into.
     *
     * @return A Rectangle describing the bounds which this image will be rendered into.
     */
    @Override
    public Rectangle getPosition() {
        return (Rectangle) position.clone();
    }
}
