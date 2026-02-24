package com.music.pandora;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Unit tests for ReportGenerator.
 */
public class ReportGeneratorTest {

    private static final String SIMPLE_FILE = "test/resources/simple_test.frd";

    @Test
    public void testReportContainsFilename() throws IOException {
        FlightRecordParser parser = new FlightRecordParser();
        FlightRecord record = parser.parse(SIMPLE_FILE);
        ReportGenerator gen = new ReportGenerator(UnitConverter.UnitSystem.METRIC);
        String report = gen.generate(record);
        assertTrue("Report should contain filename header",
            report.contains("=== simple_test.frd ==="));
    }

    @Test
    public void testReportContainsAvgAlt() throws IOException {
        FlightRecordParser parser = new FlightRecordParser();
        FlightRecord record = parser.parse(SIMPLE_FILE);
        ReportGenerator gen = new ReportGenerator(UnitConverter.UnitSystem.METRIC);
        String report = gen.generate(record);
        assertTrue("Report should contain avgAlt", report.contains("avgAlt"));
        assertTrue("Report should contain 300.00", report.contains("300.00"));
    }

    @Test
    public void testReportContainsFlightDuration() throws IOException {
        FlightRecordParser parser = new FlightRecordParser();
        FlightRecord record = parser.parse(SIMPLE_FILE);
        ReportGenerator gen = new ReportGenerator(UnitConverter.UnitSystem.METRIC);
        String report = gen.generate(record);
        assertTrue("Report should contain flightDuration", report.contains("flightDuration"));
        assertTrue("Report should contain 00:00:04", report.contains("00:00:04"));
    }

    @Test
    public void testReportFeaturesAlphabetical() throws IOException {
        FlightRecordParser parser = new FlightRecordParser();
        FlightRecord record = parser.parse(SIMPLE_FILE);
        ReportGenerator gen = new ReportGenerator(UnitConverter.UnitSystem.METRIC);
        String report = gen.generate(record);
        String[] lines = report.split("\n");
        // Skip header line, verify features are alphabetical
        String prevFeature = "";
        for (int i = 1; i < lines.length; i++) {
            String feature = lines[i].split(":")[0].trim();
            assertTrue("Features should be alphabetical: " + prevFeature + " <= " + feature,
                prevFeature.compareTo(feature) <= 0);
            prevFeature = feature;
        }
    }

    @Test
    public void testReportMetricUnits() throws IOException {
        FlightRecordParser parser = new FlightRecordParser();
        FlightRecord record = parser.parse(SIMPLE_FILE);
        ReportGenerator gen = new ReportGenerator(UnitConverter.UnitSystem.METRIC);
        String report = gen.generate(record);
        assertTrue("Metric report should contain 'm'", report.contains(" m\n") || report.contains(" m/s"));
    }

    @Test
    public void testReportImperialUnits() throws IOException {
        FlightRecordParser parser = new FlightRecordParser();
        FlightRecord record = parser.parse(SIMPLE_FILE);
        ReportGenerator gen = new ReportGenerator(UnitConverter.UnitSystem.IMPERIAL);
        String report = gen.generate(record);
        assertTrue("Imperial report should contain 'ft'", report.contains("ft"));
    }

    @Test
    public void testReportImperialConversion() throws IOException {
        FlightRecordParser parser = new FlightRecordParser();
        FlightRecord record = parser.parse(SIMPLE_FILE);
        ReportGenerator gen = new ReportGenerator(UnitConverter.UnitSystem.IMPERIAL);
        String report = gen.generate(record);
        // avgAlt in metric = 300.00 m → imperial = 984.30 ft
        assertTrue("Imperial report should contain 984.30", report.contains("984.30"));
    }
}
