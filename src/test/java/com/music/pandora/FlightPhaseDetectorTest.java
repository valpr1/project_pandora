package com.music.pandora;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Unit tests for FlightPhaseDetector.
 *
 * Test data from phase_test.frd:
 *   Rows 0-1:   yaw = -1 (sensors off)
 *   Rows 2-12:  yaw goes 10→90 (turbulence, deltas > 1 = 11 turbulences)
 *   Rows 13-75: yaw ~91-93.6 (plateau, deltas < 1, spans 62 seconds ≥ 60s threshold)
 *   Rows 76-83: yaw goes 95→160 (turbulence = landing)
 */
public class FlightPhaseDetectorTest {

    private FlightPhaseDetector detector;
    private FlightRecord phaseRecord;

    @Before
    public void setUp() throws IOException {
        detector = new FlightPhaseDetector();
        FlightRecordParser parser = new FlightRecordParser();
        phaseRecord = parser.parse("test/resources/phase_test.frd");
    }

    @Test
    public void testPhaseRecordHasYaw() {
        assertTrue("phase_test.frd should have yaw column", phaseRecord.hasColumn("yaw"));
    }

    @Test
    public void testDetectReturnsNonNull() {
        FlightPhaseDetector.PhaseResult result = detector.detect(phaseRecord);
        assertNotNull(result);
    }

    @Test
    public void testCruiseDetected() {
        FlightPhaseDetector.PhaseResult result = detector.detect(phaseRecord);
        assertNotNull("Cruise phase should be detected", result.cruise);
    }

    @Test
    public void testTakeOffDetected() {
        FlightPhaseDetector.PhaseResult result = detector.detect(phaseRecord);
        assertNotNull("TakeOff phase should be detected", result.takeOff);
    }

    @Test
    public void testLandingDetected() {
        FlightPhaseDetector.PhaseResult result = detector.detect(phaseRecord);
        assertNotNull("Landing phase should be detected", result.landing);
    }

    @Test
    public void testTakeOffBeforeCruise() {
        FlightPhaseDetector.PhaseResult result = detector.detect(phaseRecord);
        if (result.takeOff != null && result.cruise != null) {
            assertTrue("TakeOff should end before cruise starts",
                result.takeOff.endIndex < result.cruise.startIndex);
        }
    }

    @Test
    public void testCruiseBeforeLanding() {
        FlightPhaseDetector.PhaseResult result = detector.detect(phaseRecord);
        if (result.cruise != null && result.landing != null) {
            assertTrue("Cruise should end before landing starts",
                result.cruise.endIndex < result.landing.startIndex);
        }
    }

    @Test
    public void testPhasesAreContiguous() {
        FlightPhaseDetector.PhaseResult result = detector.detect(phaseRecord);
        if (result.takeOff != null && result.cruise != null) {
            assertEquals("TakeOff end + 1 should be cruise start",
                result.takeOff.endIndex + 1, result.cruise.startIndex);
        }
        if (result.cruise != null && result.landing != null) {
            assertEquals("Cruise end + 1 should be landing start",
                result.cruise.endIndex + 1, result.landing.startIndex);
        }
    }

    // ===== Reference file test =====

    @Test
    public void testReferenceFilePhaseDetection() throws IOException {
        FlightRecordParser parser = new FlightRecordParser();
        FlightRecord refRecord = parser.parse("test/resources/0_201_MiG-23MLD.frd");
        FlightPhaseDetector.PhaseResult result = detector.detect(refRecord);
        assertNotNull("Should detect some phases in reference file", result);
        // The MiG-23 reference file should have detectable cruise phase
        assertNotNull("Cruise should be detected in reference file", result.cruise);
    }

    // ===== Edge case: no yaw column =====

    @Test
    public void testNoYawColumn() throws IOException {
        FlightRecordParser parser = new FlightRecordParser();
        FlightRecord record = parser.parse("test/resources/simple_test.frd");
        FlightPhaseDetector.PhaseResult result = detector.detect(record);
        // No yaw column → all phases null
        assertNull(result.cruise);
    }
}
