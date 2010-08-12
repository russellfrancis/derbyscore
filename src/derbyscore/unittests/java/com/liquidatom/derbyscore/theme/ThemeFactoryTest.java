package com.liquidatom.derbyscore.theme;

import java.net.URL;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Russell Francis (russell.francis@gmail.com)
 * @version $Id: ThemeFactoryTest.java 4 2010-03-12 11:40:07Z russ $
 */
public class ThemeFactoryTest {

    @Test
    public void testGetCurrent() {
        URL themeXml = getClass().getResource("/themes/default/theme.xml");
        Theme theme = new Theme(themeXml);

        assertNull(ThemeFactory.getInstance().getCurrent());
        ThemeFactory.getInstance().setCurrent(theme);
        assertEquals(theme, ThemeFactory.getInstance().getCurrent());
        ThemeFactory.getInstance().setCurrent(null);
        assertNull(ThemeFactory.getInstance().getCurrent());
    }
}
