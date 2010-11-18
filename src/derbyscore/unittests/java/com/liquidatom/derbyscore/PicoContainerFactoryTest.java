package com.liquidatom.derbyscore;

import org.picocontainer.MutablePicoContainer;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Russell Francis (russ@metro-six.com)
 */
public class PicoContainerFactoryTest {

    @Test
    public void testAccessor() {
        MutablePicoContainer pico = PicoContainerFactory.getInstance().get();
        assertNotNull(pico);
    }
}
