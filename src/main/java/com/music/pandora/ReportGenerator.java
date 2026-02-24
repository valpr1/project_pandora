package com.music.pandora;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates the full flight analysis report.
 * Lists all features alphabetically with proper alignment.
 */
public class ReportGenerator {

    /** All supported feature names for the full report. */
    private static final String[] ALL_FEATURES = {
        "avgAcceleration",
        "avgAirSpeed",
        "avgAlt",
        "avgEnginePower",
        "avgHeartRate",
        "avgHumidity",
        "avgMachSpeed",
        "avgOxygen",
        "avgPressure",
        "avgTemp",
        "cruise",
        "flightDistance",
        "flightDuration",
        "landing",
        "maxAccelG",
        "maxAcceleration",
        "maxAirSpeed",
        "maxAlt",
        "maxEnginePower",
        "maxHeartRate",
        "maxHumidity",
        "maxMachSpeed",
        "maxOxygen",
        "maxPressure",
        "maxTemp",
        "minHeartRate",
        "minHumidity",
        "minOxygen",
        "minPressure",
        "minTemp",
        "takeOff"
    };

    private final UnitConverter.UnitSystem unitSystem;

    public ReportGenerator(UnitConverter.UnitSystem unitSystem) {
        this.unitSystem = unitSystem;
    }

    /**
     * Generates a full report for a single flight record.
     *
     * @param record the flight record
     * @return the formatted report string
     */
    public String generate(FlightRecord record) {
        StringBuilder sb = new StringBuilder();
        FeatureComputer computer = new FeatureComputer(record);

        // Find max feature name length for alignment
        int maxLen = 0;
        for (String feature : ALL_FEATURES) {
            if (feature.length() > maxLen) maxLen = feature.length();
        }

        // Header
        sb.append("=== ").append(record.getFilename()).append(" ===\n");

        // Features
        for (String feature : ALL_FEATURES) {
            String value = computer.compute(feature);
            if (value == null) continue;

            // Convert units if needed
            value = UnitConverter.convert(value, feature, unitSystem);

            // Get unit label
            String unit = UnitConverter.getUnitLabel(feature, unitSystem);

            // Format line with alignment
            sb.append(String.format("%-" + maxLen + "s : %s", feature, value));
            if (!unit.isEmpty()) {
                sb.append(" ").append(unit);
            }
            sb.append("\n");
        }

        return sb.toString().trim();
    }

    /**
     * Generates a report for multiple flight records.
     *
     * @param records the flight records
     * @return the formatted report string
     */
    public String generateAll(List<FlightRecord> records) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < records.size(); i++) {
            if (i > 0) sb.append("\n\n");
            sb.append(generate(records.get(i)));
        }
        return sb.toString();
    }
}
