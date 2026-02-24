package com.music.pandora;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for CLIParser.
 */
public class CLIParserTest {

    @Test
    public void testVersionShortFlag() {
        CLIParser cli = new CLIParser(new String[]{"-v"});
        assertTrue("Short -v flag should set versionRequested", cli.isVersionRequested());
        assertFalse(cli.isHelpRequested());
    }

    @Test
    public void testVersionLongFlag() {
        CLIParser cli = new CLIParser(new String[]{"--version"});
        assertTrue("Long --version flag should set versionRequested", cli.isVersionRequested());
    }

    @Test
    public void testHelpShortFlag() {
        CLIParser cli = new CLIParser(new String[]{"-h"});
        assertTrue("Short -h flag should set helpRequested", cli.isHelpRequested());
        assertFalse(cli.isVersionRequested());
    }

    @Test
    public void testHelpLongFlag() {
        CLIParser cli = new CLIParser(new String[]{"--help"});
        assertTrue("Long --help flag should set helpRequested", cli.isHelpRequested());
    }

    @Test
    public void testMetadataOption() {
        CLIParser cli = new CLIParser(new String[]{"-m", "flight_id", "file.frd"});
        assertEquals("flight_id", cli.getMetadataName());
        assertEquals(1, cli.getSourceFiles().size());
        assertEquals("file.frd", cli.getSourceFiles().get(0));
    }

    @Test
    public void testOutputOption() {
        CLIParser cli = new CLIParser(new String[]{"-o", "maxAlt", "file.frd"});
        assertEquals("maxAlt", cli.getOutputFeature());
    }

    @Test
    public void testBatchMode() {
        CLIParser cli = new CLIParser(new String[]{"-b", "folder/"});
        assertTrue(cli.isBatchMode());
    }

    @Test
    public void testDebugMode() {
        CLIParser cli = new CLIParser(new String[]{"-d", "file.frd"});
        assertTrue(cli.isDebugMode());
    }

    @Test
    public void testParametersOption() {
        CLIParser cli = new CLIParser(new String[]{"-p", "file.frd"});
        assertTrue(cli.isParametersRequested());
    }

    @Test
    public void testImperialShortFlag() {
        CLIParser cli = new CLIParser(new String[]{"-I", "file.frd"});
        assertTrue(cli.isImperialUnit());
    }

    @Test
    public void testMetricShortFlag() {
        CLIParser cli = new CLIParser(new String[]{"-M", "file.frd"});
        assertFalse(cli.isImperialUnit());
    }

    @Test
    public void testUnitOption() {
        CLIParser cli = new CLIParser(new String[]{"-u", "imperial", "file.frd"});
        assertTrue(cli.isImperialUnit());

        CLIParser cli2 = new CLIParser(new String[]{"--unit", "metric", "file.frd"});
        assertFalse(cli2.isImperialUnit());
    }

    @Test
    public void testPhaseOption() {
        CLIParser cli = new CLIParser(new String[]{"-P", "takeOff", "-o", "avgAirSpeed", "file.frd"});
        assertEquals("takeOff", cli.getPhaseName());
        assertEquals("avgAirSpeed", cli.getOutputFeature());
    }

    @Test
    public void testMultipleSourceFiles() {
        CLIParser cli = new CLIParser(new String[]{"-o", "maxAlt", "a.frd", "b.frd", "c.frd"});
        assertEquals(3, cli.getSourceFiles().size());
    }

    @Test
    public void testNoArguments() {
        CLIParser cli = new CLIParser(new String[]{});
        assertFalse(cli.isVersionRequested());
        assertFalse(cli.isHelpRequested());
        assertTrue(cli.getSourceFiles().isEmpty());
    }
}
