package com.music.pandora;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

/**
 * Unit tests for the Pandora orchestrator.
 */
public class PandoraTest {

    /**
     * Helper: captures stdout output of a Pandora run.
     */
    private String captureOutput(String... args) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(baos));
        try {
            CLIParser cli = new CLIParser(args);
            Pandora pandora = new Pandora(cli);
            pandora.run();
        } finally {
            System.setOut(original);
        }
        return baos.toString().trim();
    }

    // ========== Tranche 1: Version & Help ==========

    @Test
    public void testVersionOutput() {
        String output = captureOutput("-v");
        assertEquals("pandora@2.0.0", output);
    }

    @Test
    public void testVersionLongOutput() {
        String output = captureOutput("--version");
        assertEquals("pandora@2.0.0", output);
    }

    @Test
    public void testHelpContainsUsage() {
        String output = captureOutput("-h");
        assertTrue("Help should contain 'Usage'", output.contains("Usage"));
        assertTrue("Help should mention --version", output.contains("--version"));
        assertTrue("Help should mention --help", output.contains("--help"));
    }

    @Test
    public void testNoArgsShowsHelp() {
        String output = captureOutput();
        assertTrue("No args should display help", output.contains("Usage"));
    }

    @Test
    public void testVersionConstant() {
        assertEquals("2.0.0", Pandora.VERSION);
    }

    // ========== Tranche 2: Metadata ==========

    @Test
    public void testMetadataFlightId() {
        String output = captureOutput("-m", "flight id", "test/resources/simple_test.frd");
        assertEquals("999", output);
    }

    @Test
    public void testMetadataFlightCode() {
        String output = captureOutput("-m", "flight code", "test/resources/simple_test.frd");
        assertEquals("TestJet", output);
    }

    @Test
    public void testMetadataOrigin() {
        String output = captureOutput("-m", "origin", "test/resources/simple_test.frd");
        assertEquals("TEST", output);
    }

    @Test
    public void testMetadataDate() {
        String output = captureOutput("--metadata", "date", "test/resources/simple_test.frd");
        assertEquals("2024-01-15", output);
    }

    @Test
    public void testMetadataReferenceFile() {
        String output = captureOutput("-m", "flight id", "test/resources/0_201_MiG-23MLD.frd");
        assertEquals("201", output);
    }

    // ========== Tranche 2: Parameters ==========

    @Test
    public void testParametersAreSorted() {
        String output = captureOutput("-p", "test/resources/simple_test.frd");
        String[] lines = output.split("\n");
        // Check alphabetical order
        for (int i = 0; i < lines.length - 1; i++) {
            assertTrue("Parameters should be alphabetical: " + lines[i] + " <= " + lines[i+1],
                lines[i].compareTo(lines[i+1]) <= 0);
        }
    }

    @Test
    public void testParametersContainExpected() {
        String output = captureOutput("-p", "test/resources/simple_test.frd");
        assertTrue(output.contains("altitude"));
        assertTrue(output.contains("air_speed"));
        assertTrue(output.contains("timestamp"));
    }

    // ========== Tranche 2: Filenames ==========

    @Test
    public void testFilenamesSingle() {
        String output = captureOutput("-o", "filenames", "test/resources/simple_test.frd");
        assertEquals("simple_test.frd", output);
    }

    @Test
    public void testFilenamesMultipleSorted() {
        String output = captureOutput("-o", "filenames",
            "test/resources/simple_test.frd",
            "test/resources/0_201_MiG-23MLD.frd");
        String[] lines = output.split("\n");
        assertEquals(2, lines.length);
        // Should be alphabetically sorted
        assertEquals("0_201_MiG-23MLD.frd", lines[0]);
        assertEquals("simple_test.frd", lines[1]);
    }
}
