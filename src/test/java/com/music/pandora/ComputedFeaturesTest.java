package com.music.pandora;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Unit tests for ComputedFeatures.
 *
 * Test data from simple_test.frd:
 *   timestamps: 1000000000, 1000000001, 1000000002, 1000000003, 1000000004 → duration = 4s
 *   air_speed:  200, 250, 300, 350, 400 → acceleration = 50 m/s² per interval
 *   GPS:        (lon,lat) = (2.3522,48.8566), (2.3530,48.8570), (2.3540,48.8575), (2.3550,48.8580), (2.3560,48.8585)
 */
public class ComputedFeaturesTest {

    private ComputedFeatures computed;

    @Before
    public void setUp() throws IOException {
        FlightRecordParser parser = new FlightRecordParser();
        FlightRecord record = parser.parse("test/resources/simple_test.frd");
        computed = new ComputedFeatures(record);
    }

    // ===== Flight Duration =====

    @Test
    public void testFlightDuration() {
        // 1000000004 - 1000000000 = 4 seconds
        assertEquals("00:00:04", computed.flightDuration());
    }

    // ===== Flight Distance (Haversine) =====

    @Test
    public void testFlightDistancePositive() {
        String result = computed.flightDistance();
        double distance = Double.parseDouble(result);
        // GPS points cover about 200m total near Paris
        assertTrue("Flight distance should be positive", distance > 0);
        assertTrue("Flight distance should be reasonable (< 10000m)", distance < 10000);
    }

    @Test
    public void testHaversineKnownDistance() {
        // Paris (48.8566, 2.3522) to London (51.5074, -0.1278) ≈ 344 km
        double distance = ComputedFeatures.haversine(48.8566, 2.3522, 51.5074, -0.1278);
        assertEquals("Paris to London distance", 344_000, distance, 5000); // ±5 km tolerance
    }

    @Test
    public void testHaversineSamePoint() {
        double distance = ComputedFeatures.haversine(48.8566, 2.3522, 48.8566, 2.3522);
        assertEquals("Same point distance should be 0", 0.0, distance, 0.001);
    }

    // ===== Acceleration =====

    @Test
    public void testAvgAcceleration() {
        // Speed increments: 50, 50, 50, 50 m/s per 1 second intervals
        // First point approximation repeats first value, so all values are 50
        assertEquals("50.00", computed.avgAcceleration());
    }

    @Test
    public void testMaxAcceleration() {
        assertEquals("50.00", computed.maxAcceleration());
    }

    @Test
    public void testMaxAccelG() {
        // 50 / 9.80665 ≈ 5.10
        String result = computed.maxAccelG();
        double gVal = Double.parseDouble(result);
        assertEquals(50.0 / 9.80665, gVal, 0.01);
    }

    @Test
    public void testComputeAccelerationLength() {
        double[] accel = computed.computeAcceleration();
        assertEquals(5, accel.length); // Same as number of data rows
    }

    // ===== Mach Speed =====

    @Test
    public void testAvgMachSpeed() {
        // Speeds: 200, 250, 300, 350, 400 → Mach: 200/1225, ..., 400/1225
        // avg = 300/1225 ≈ 0.24
        String result = computed.avgMachSpeed();
        double mach = Double.parseDouble(result);
        assertEquals(300.0 / 1225.0, mach, 0.01);
    }

    @Test
    public void testMaxMachSpeed() {
        // 400/1225 ≈ 0.33
        String result = computed.maxMachSpeed();
        double mach = Double.parseDouble(result);
        assertEquals(400.0 / 1225.0, mach, 0.01);
    }
}
