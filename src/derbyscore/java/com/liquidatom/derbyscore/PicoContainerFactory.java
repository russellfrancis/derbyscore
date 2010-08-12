package com.liquidatom.derbyscore;

import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.OptInCaching;

/**
 * A utility for accessing the global pico-container for the application.
 *
 *
 * @author Russell Francis (russell.francis@gmail.com)
 * @version $Id: PicoContainerFactory.java 5 2010-03-15 03:10:51Z russ $
 */
public class PicoContainerFactory {

    static private final PicoContainerFactory instance = new PicoContainerFactory();

    /**
     * Get a reference to the current PicoContainerFactory instance.
     *
     * @return A non-null reference to the PicoContainerFactory instance.
     */
    public static PicoContainerFactory getInstance() {
        return instance;
    }

    private MutablePicoContainer pico = new DefaultPicoContainer(new OptInCaching());

    /**
     * Private constructor to prevent instantiation.
     */
    private PicoContainerFactory() {
    }

    /**
     * A utility for accessing the global pico-container for the application.
     *
     * @return A non-null reference which can be used to get instances of particular classes and interfaces in disparate
     * locations within the application.
     */
    public MutablePicoContainer get() {
        return pico;
    }
}
