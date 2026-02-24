package com.music.pandora;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Unit tests for FeatureComputer.
 * Uses the hand-crafted simple_test.frd with known values:
 *   altitude:    100, 200, 300, 400, 500  → avg=300, max=500, min=100
 *   air_speed:   200, 250, 300, 350, 400  → avg=300, max=400
 *   temperature: 20, 18, 15, 12, 10       → avg=15, min=10, max=20
 *   pressure:    101325, 95000, 90000, 85000, 80000 → avg=90265, min=80000, max=101325
 *   humidity:    50, 55, 60, 65, 70        → avg=60, min=50, max=70
 *   heart_rate:  70, 75, 80, 85, 90        → avg=80, min=70, max=90
 *   oxygen_mask: 98, 97, 95, 93, 90        → avg=94.60, min=90, max=98
 */
public class FeatureComputerTest {

    private FeatureComputer computer;

    @Before
    public void setUp() throws IOException {
        FlightRecordParser parser = new FlightRecordParser();
        FlightRecord record = parser.parse("test/resources/simple_test.frd");
        computer = new FeatureComputer(record);
    }

    // ===== Altitude =====

    @Test
    public void testAvgAlt() {
        assertEquals("300.00", computer.compute("avgAlt"));
    }

    @Test
    public void testMaxAlt() {
        assertEquals("500.00", computer.compute("maxAlt"));
    }

    // ===== Air Speed =====

    @Test
    public void testAvgAirSpeed() {
        assertEquals("300.00", computer.compute("avgAirSpeed"));
    }

    @Test
    public void testMaxAirSpeed() {
        assertEquals("400.00", computer.compute("maxAirSpeed"));
    }

    // ===== Temperature =====

    @Test
    public void testAvgTemp() {
        assertEquals("15.00", computer.compute("avgTemp"));
    }

    @Test
    public void testMinTemp() {
        assertEquals("10.00", computer.compute("minTemp"));
    }

    @Test
    public void testMaxTemp() {
        assertEquals("20.00", computer.compute("maxTemp"));
    }

    // ===== Pressure =====

    @Test
    public void testAvgPressure() {
        assertEquals("90265.00", computer.compute("avgPressure"));
    }

    @Test
    public void testMinPressure() {
        assertEquals("80000.00", computer.compute("minPressure"));
    }

    @Test
    public void testMaxPressure() {
        assertEquals("101325.00", computer.compute("maxPressure"));
    }

    // ===== Humidity =====

    @Test
    public void testAvgHumidity() {
        assertEquals("60.00", computer.compute("avgHumidity"));
    }

    @Test
    public void testMinHumidity() {
        assertEquals("50.00", computer.compute("minHumidity"));
    }

    @Test
    public void testMaxHumidity() {
        assertEquals("70.00", computer.compute("maxHumidity"));
    }

    // ===== Heart Rate =====

    @Test
    public void testAvgHeartRate() {
        assertEquals("80.00", computer.compute("avgHeartRate"));
    }

    @Test
    public void testMinHeartRate() {
        assertEquals("70.00", computer.compute("minHeartRate"));
    }

    @Test
    public void testMaxHeartRate() {
        assertEquals("90.00", computer.compute("maxHeartRate"));
    }

    // ===== Oxygen =====

    @Test
    public void testAvgOxygen() {
        assertEquals("94.60", computer.compute("avgOxygen"));
    }

    @Test
    public void testMinOxygen() {
        assertEquals("90.00", computer.compute("minOxygen"));
    }

    @Test
    public void testMaxOxygen() {
        assertEquals("98.00", computer.compute("maxOxygen"));
    }

    // ===== Unknown Feature =====

    @Test
    public void testUnknownFeature() {
        assertNull(computer.compute("nonExistentFeature"));
    }
}
