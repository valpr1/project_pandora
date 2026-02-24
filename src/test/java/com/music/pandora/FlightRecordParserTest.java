package com.music.pandora;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for FlightRecordParser and FlightRecord.
 */
public class FlightRecordParserTest {

    private FlightRecordParser parser;
    private static final String SIMPLE_FILE = "test/resources/simple_test.frd";
    private static final String REFERENCE_FILE = "test/resources/0_201_MiG-23MLD.frd";

    @Before
    public void setUp() {
        parser = new FlightRecordParser();
    }

    // ========== Metadata Parsing ==========

    @Test
    public void testParseMetadataFlightId() throws IOException {
        FlightRecord record = parser.parse(SIMPLE_FILE);
        assertEquals("999", record.getMetadataValue("flight id"));
    }

    @Test
    public void testParseMetadataFlightCode() throws IOException {
        FlightRecord record = parser.parse(SIMPLE_FILE);
        assertEquals("TestJet", record.getMetadataValue("flight code"));
    }

    @Test
    public void testParseMetadataOrigin() throws IOException {
        FlightRecord record = parser.parse(SIMPLE_FILE);
        assertEquals("TEST", record.getMetadataValue("origin"));
    }

    @Test
    public void testParseMetadataDate() throws IOException {
        FlightRecord record = parser.parse(SIMPLE_FILE);
        assertEquals("2024-01-15", record.getMetadataValue("date"));
    }

    @Test
    public void testParseMetadataFrom() throws IOException {
        FlightRecord record = parser.parse(SIMPLE_FILE);
        assertEquals("paris", record.getMetadataValue("from"));
    }

    @Test
    public void testParseMetadataTo() throws IOException {
        FlightRecord record = parser.parse(SIMPLE_FILE);
        assertEquals("london", record.getMetadataValue("to"));
    }

    @Test
    public void testParseMetadataMotors() throws IOException {
        FlightRecord record = parser.parse(SIMPLE_FILE);
        assertEquals("2", record.getMetadataValue("motor(s)"));
    }

    @Test
    public void testParseMetadataNonExistent() throws IOException {
        FlightRecord record = parser.parse(SIMPLE_FILE);
        assertNull(record.getMetadataValue("nonexistent_field"));
    }

    // ========== Column Names ==========

    @Test
    public void testParseColumnNames() throws IOException {
        FlightRecord record = parser.parse(SIMPLE_FILE);
        List<String> columns = record.getColumnNames();
        assertTrue(columns.contains("timestamp"));
        assertTrue(columns.contains("altitude"));
        assertTrue(columns.contains("air_speed"));
        assertTrue(columns.contains("temperature_in"));
    }

    @Test
    public void testParametersSorted() throws IOException {
        FlightRecord record = parser.parse(SIMPLE_FILE);
        List<String> sorted = record.getParametersSorted();
        // Verify alphabetical order
        for (int i = 0; i < sorted.size() - 1; i++) {
            assertTrue(sorted.get(i).compareTo(sorted.get(i + 1)) <= 0);
        }
    }

    @Test
    public void testColumnCount() throws IOException {
        FlightRecord record = parser.parse(SIMPLE_FILE);
        assertEquals(10, record.getColumnNames().size());
    }

    // ========== Data Rows ==========

    @Test
    public void testDataRowCount() throws IOException {
        FlightRecord record = parser.parse(SIMPLE_FILE);
        assertEquals(5, record.getRecordCount());
    }

    @Test
    public void testColumnValues() throws IOException {
        FlightRecord record = parser.parse(SIMPLE_FILE);
        double[] altitudes = record.getColumnValues("altitude");
        assertEquals(5, altitudes.length);
        assertEquals(100.00, altitudes[0], 0.01);
        assertEquals(200.00, altitudes[1], 0.01);
        assertEquals(300.00, altitudes[2], 0.01);
        assertEquals(400.00, altitudes[3], 0.01);
        assertEquals(500.00, altitudes[4], 0.01);
    }

    @Test
    public void testAirSpeedValues() throws IOException {
        FlightRecord record = parser.parse(SIMPLE_FILE);
        double[] speeds = record.getColumnValues("air_speed");
        assertEquals(200.00, speeds[0], 0.01);
        assertEquals(400.00, speeds[4], 0.01);
    }

    @Test
    public void testNonExistentColumn() throws IOException {
        FlightRecord record = parser.parse(SIMPLE_FILE);
        double[] values = record.getColumnValues("nonexistent");
        assertEquals(0, values.length);
    }

    @Test
    public void testHasColumn() throws IOException {
        FlightRecord record = parser.parse(SIMPLE_FILE);
        assertTrue(record.hasColumn("altitude"));
        assertFalse(record.hasColumn("nonexistent"));
    }

    // ========== Filename ==========

    @Test
    public void testFilename() throws IOException {
        FlightRecord record = parser.parse(SIMPLE_FILE);
        assertEquals("simple_test.frd", record.getFilename());
    }

    // ========== Reference File ==========

    @Test
    public void testReferenceFileMetadata() throws IOException {
        FlightRecord record = parser.parse(REFERENCE_FILE);
        assertEquals("201", record.getMetadataValue("flight id"));
        assertEquals("MiG-23MLD", record.getMetadataValue("flight code"));
        assertEquals("RU", record.getMetadataValue("origin"));
    }

    @Test
    public void testReferenceFileDataCount() throws IOException {
        FlightRecord record = parser.parse(REFERENCE_FILE);
        assertTrue("Should have many data rows", record.getRecordCount() > 100);
    }

    @Test
    public void testReferenceFileHasExpectedColumns() throws IOException {
        FlightRecord record = parser.parse(REFERENCE_FILE);
        assertTrue(record.hasColumn("timestamp"));
        assertTrue(record.hasColumn("longitude"));
        assertTrue(record.hasColumn("latitude"));
        assertTrue(record.hasColumn("altitude"));
        assertTrue(record.hasColumn("air_speed"));
    }

    // ========== Multi-file Parsing ==========

    @Test
    public void testParseMultipleFiles() {
        List<FlightRecord> records = parser.parseAll(
            Arrays.asList(SIMPLE_FILE, REFERENCE_FILE));
        assertEquals(2, records.size());
    }

    // ========== Incomplete Header ==========

    @Test
    public void testCompleteHeaderNoError() {
        FlightRecord record = new FlightRecord("test.frd",
            createMetadata("flight id", "1", "flight code", "X",
                          "origin", "US", "date", "2024", "from", "A", "to", "B"),
            Arrays.asList("timestamp"), new java.util.ArrayList<>());
        assertNull(parser.checkIncompleteHeaders(Arrays.asList(record)));
    }

    @Test
    public void testIncompleteHeaderError() {
        FlightRecord record = new FlightRecord("test.frd",
            createMetadata("flight id", "1"),
            Arrays.asList("timestamp"), new java.util.ArrayList<>());
        String error = parser.checkIncompleteHeaders(Arrays.asList(record));
        assertNotNull(error);
        assertTrue(error.startsWith("ERROR: INCOMPLETE_HEADER"));
        assertTrue(error.contains("test.frd"));
    }

    // Helper to create metadata maps
    private java.util.Map<String, String> createMetadata(String... kvPairs) {
        java.util.Map<String, String> map = new java.util.LinkedHashMap<>();
        for (int i = 0; i < kvPairs.length; i += 2) {
            map.put(kvPairs[i], kvPairs[i + 1]);
        }
        return map;
    }
}
