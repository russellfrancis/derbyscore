package com.liquidatom.derbyscore.domain;

import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Russell Francis (russ@metro-six.com)
 */
public class DurationTest {

    @Test
    public void testConstructor_NullTimeUnit() {
        try {
            new Duration(1, null);
            fail("Expected to throw IAE.");
        }
        catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testConstructor_TinyTimeUnit_ThrowsIAE() {
        try {
            new Duration(1, TimeUnit.NANOSECONDS);
            fail("Expected to throw IAE.");
        }
        catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testConstructor() {
        final long TIME = 10;
        final TimeUnit UNIT = TimeUnit.DAYS;
        Duration duration = new Duration(TIME, UNIT);
        assertEquals(UNIT, duration.getUnit());
        assertEquals(TIME, duration.getTime());
        assertEquals(TIME * 24 * 60 * 60 * 1000, duration.getTimeInMilliseconds());
    }

    @Test
    public void testCompareTo() {
        Duration a = new Duration(1000, TimeUnit.MILLISECONDS);
        Duration b = new Duration(2, TimeUnit.SECONDS);
        Duration c = new Duration(3, TimeUnit.SECONDS);

        assertTrue(a.compareTo(a) == 0);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(a.compareTo(c) < 0);

        assertTrue(b.compareTo(a) > 0);
        assertTrue(b.compareTo(b) == 0);
        assertTrue(b.compareTo(c) < 0);

        assertTrue(c.compareTo(a) > 0);
        assertTrue(c.compareTo(b) > 0);
        assertTrue(c.compareTo(c) == 0);
    }
}
