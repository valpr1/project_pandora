package com.music.pandora;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Statistics utility class.
 */
public class StatisticsTest {

    @Test
    public void testAvg() {
        assertEquals(3.0, Statistics.avg(new double[]{1, 2, 3, 4, 5}), 0.001);
    }

    @Test
    public void testAvgSingle() {
        assertEquals(42.0, Statistics.avg(new double[]{42.0}), 0.001);
    }

    @Test
    public void testMax() {
        assertEquals(5.0, Statistics.max(new double[]{1, 5, 3, 2, 4}), 0.001);
    }

    @Test
    public void testMaxNegatives() {
        assertEquals(-1.0, Statistics.max(new double[]{-5, -3, -1, -4}), 0.001);
    }

    @Test
    public void testMin() {
        assertEquals(1.0, Statistics.min(new double[]{3, 1, 4, 1, 5}), 0.001);
    }

    @Test
    public void testMinNegatives() {
        assertEquals(-5.0, Statistics.min(new double[]{-5, -3, -1, -4}), 0.001);
    }

    @Test
    public void testSum() {
        assertEquals(15.0, Statistics.sum(new double[]{1, 2, 3, 4, 5}), 0.001);
    }

    @Test
    public void testFormat() {
        assertEquals("3.14", Statistics.format(3.14159));
        assertEquals("100.00", Statistics.format(100.0));
        assertEquals("-0.30", Statistics.format(-0.3));
        assertEquals("0.00", Statistics.format(0.0));
    }

    @Test
    public void testAvgEmpty() {
        assertEquals(0.0, Statistics.avg(new double[]{}), 0.001);
    }

    @Test
    public void testMaxEmpty() {
        assertEquals(0.0, Statistics.max(new double[]{}), 0.001);
    }

    @Test
    public void testMinEmpty() {
        assertEquals(0.0, Statistics.min(new double[]{}), 0.001);
    }
}
