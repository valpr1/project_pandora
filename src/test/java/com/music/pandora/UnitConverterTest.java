package com.music.pandora;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for UnitConverter.
 */
public class UnitConverterTest {

    // ===== Metric → Imperial conversions =====

    @Test
    public void testAltitudeConversion() {
        // 100 m → 328.1 ft (100 * 3.281)
        assertEquals(328.10, UnitConverter.toImperial(100.0, "altitude"), 0.01);
    }

    @Test
    public void testSpeedConversion() {
        // 100 m/s → 328.1 ft/s
        assertEquals(328.10, UnitConverter.toImperial(100.0, "speed"), 0.01);
    }

    @Test
    public void testPowerConversion() {
        // 754.7 W → 1 hp
        assertEquals(1.0, UnitConverter.toImperial(754.7, "power"), 0.01);
    }

    @Test
    public void testTemperatureConversion() {
        // 20 ℃ → 293.15 K
        assertEquals(293.15, UnitConverter.toImperial(20.0, "temperature"), 0.01);
    }

    @Test
    public void testPressureConversion() {
        // 6894.76 Pa → 1 psi
        assertEquals(1.0, UnitConverter.toImperial(6894.76, "pressure"), 0.01);
    }

    @Test
    public void testNoConversionForHumidity() {
        assertEquals(50.0, UnitConverter.toImperial(50.0, "humidity"), 0.01);
    }

    @Test
    public void testNoConversionForMach() {
        assertEquals(0.85, UnitConverter.toImperial(0.85, "mach"), 0.01);
    }

    // ===== Unit labels =====

    @Test
    public void testMetricAltLabel() {
        assertEquals("m", UnitConverter.getUnitLabel("avgAlt", UnitConverter.UnitSystem.METRIC));
    }

    @Test
    public void testImperialAltLabel() {
        assertEquals("ft", UnitConverter.getUnitLabel("avgAlt", UnitConverter.UnitSystem.IMPERIAL));
    }

    @Test
    public void testMetricSpeedLabel() {
        assertEquals("m/s", UnitConverter.getUnitLabel("avgAirSpeed", UnitConverter.UnitSystem.METRIC));
    }

    @Test
    public void testImperialTempLabel() {
        assertEquals("K", UnitConverter.getUnitLabel("avgTemp", UnitConverter.UnitSystem.IMPERIAL));
    }

    // ===== Unit type identification =====

    @Test
    public void testUnitTypeAlt() {
        assertEquals("altitude", UnitConverter.getUnitType("avgAlt"));
    }

    @Test
    public void testUnitTypePhaseSuffix() {
        // avgAltTakeOff → strip TakeOff → avgAlt → altitude
        assertEquals("altitude", UnitConverter.getUnitType("avgAltTakeOff"));
    }

    @Test
    public void testUnitTypeMach() {
        assertEquals("mach", UnitConverter.getUnitType("avgMachSpeed"));
    }

    // ===== Convert method =====

    @Test
    public void testConvertMetricNoChange() {
        assertEquals("300.00", UnitConverter.convert("300.00", "avgAlt", UnitConverter.UnitSystem.METRIC));
    }

    @Test
    public void testConvertImperialAlt() {
        // 300 m → 984.30 ft
        assertEquals("984.30", UnitConverter.convert("300.00", "avgAlt", UnitConverter.UnitSystem.IMPERIAL));
    }

    @Test
    public void testConvertNonNumeric() {
        // Non-numeric strings pass through
        assertEquals("not detected", UnitConverter.convert("not detected", "takeOff", UnitConverter.UnitSystem.IMPERIAL));
    }
}
