package com.liquidatom.derbyscore;

import org.picocontainer.MutablePicoContainer;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Russell Francis (russell.francis@gmail.com)
 * @version $Id: PicoContainerFactoryTest.java 5 2010-03-15 03:10:51Z russ $
 */
public class PicoContainerFactoryTest {

    @Test
    public void testAccessor() {
        MutablePicoContainer pico = PicoContainerFactory.getInstance().get();
        assertNotNull(pico);
    }
}
