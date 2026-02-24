package com.music.pandora;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for CrossFlightComputer.
 * Uses both test files to validate multi-file features.
 */
public class CrossFlightComputerTest {

    private List<FlightRecord> records;
    private CrossFlightComputer computer;

    @Before
    public void setUp() throws IOException {
        FlightRecordParser parser = new FlightRecordParser();
        records = parser.parseAll(Arrays.asList(
            "test/resources/simple_test.frd",
            "test/resources/0_201_MiG-23MLD.frd"
        ));
        computer = new CrossFlightComputer(records);
    }

    // ===== Cumulative features =====

    @Test
    public void testCumulDuration() {
        String result = computer.compute("cumulDuration");
        assertNotNull(result);
        // simple_test = 4s, MiG-23 = 684s → total ≈ 00:11:28
        assertTrue("Should have non-zero duration", !"00:00:00".equals(result));
    }

    @Test
    public void testCumulDistance() {
        String result = computer.compute("cumulDistance");
        assertNotNull(result);
        double dist = Double.parseDouble(result);
        assertTrue("Cumulative distance should be positive", dist > 0);
    }

    // ===== Airports =====

    @Test
    public void testAirportTakeOff() {
        String result = computer.compute("airportTakeOff");
        assertNotNull(result);
        assertNotEquals("not detected", result);
    }

    @Test
    public void testAirportLanding() {
        String result = computer.compute("airportLanding");
        assertNotNull(result);
        assertNotEquals("not detected", result);
    }

    // ===== Best features =====

    @Test
    public void testHighestAltitude() {
        String result = computer.compute("highestAltitude");
        assertNotNull(result);
        assertTrue("Should contain jet ID", result.contains(":"));
    }

    @Test
    public void testLongestDuration() {
        String result = computer.compute("longestDuration");
        assertNotNull(result);
        // MiG-23 has longer duration than simple_test
        assertTrue("Should contain MiG-23MLD", result.contains("MiG-23MLD"));
    }

    @Test
    public void testHighestSpeed() {
        String result = computer.compute("highestSpeed");
        assertNotNull(result);
        assertTrue("Should contain jet ID and speed", result.contains(":"));
    }

    @Test
    public void testSlowestSpeed() {
        String result = computer.compute("slowestSpeed");
        assertNotNull(result);
        assertTrue("Should contain jet ID and speed", result.contains(":"));
    }

    @Test
    public void testHighestPower() {
        String result = computer.compute("highestPower");
        assertNotNull(result);
        assertTrue("Should contain colon separator", result.contains(":"));
    }

    // ===== Coefficients =====

    @Test
    public void testHighestDrag() {
        String result = computer.compute("highestDrag");
        assertNotNull(result);
    }

    @Test
    public void testHighestLift() {
        String result = computer.compute("highestLift");
        assertNotNull(result);
    }

    // ===== Landing features =====

    @Test
    public void testFirstLanding() {
        String result = computer.compute("firstLanding");
        assertNotNull(result);
        assertTrue("Should have format id:airport:time", result.contains(":"));
    }

    @Test
    public void testLastLanding() {
        String result = computer.compute("lastLanding");
        assertNotNull(result);
    }

    // ===== Static check =====

    @Test
    public void testIsCrossFlightFeature() {
        assertTrue(CrossFlightComputer.isCrossFlightFeature("cumulDuration"));
        assertTrue(CrossFlightComputer.isCrossFlightFeature("highestAltitude"));
        assertFalse(CrossFlightComputer.isCrossFlightFeature("avgAlt"));
        assertFalse(CrossFlightComputer.isCrossFlightFeature("flightDuration"));
    }

    // ===== Unknown feature =====

    @Test
    public void testUnknownFeature() {
        assertNull(computer.compute("nonExistent"));
    }
}
