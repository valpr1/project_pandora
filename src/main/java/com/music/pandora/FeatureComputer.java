package com.music.pandora;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes flight analysis features from a FlightRecord.
 * Maps feature names (used in -o option) to computations on flight data columns.
 */
public class FeatureComputer {

    private final FlightRecord record;

    public FeatureComputer(FlightRecord record) {
        this.record = record;
    }

    /**
     * Computes a feature by name and returns its formatted value.
     *
     * @param featureName the feature name (e.g., "avgAlt", "maxAirSpeed")
     * @return the formatted result string, or null if the feature is unknown
     */
    public String compute(String featureName) {
        switch (featureName) {
            // ===== Start Time =====
            case "start_time":
                double[] ts = record.getColumnValues("timestamp");
                return ts.length > 0 ? Statistics.format(ts[0]) : null;

            // ===== Altitude =====
            case "avgAlt":
                return formatColumn("altitude", StatType.AVG);
            case "maxAlt":
                return formatColumn("altitude", StatType.MAX);

            // ===== Air Speed =====
            case "avgAirSpeed":
                return formatColumn("air_speed", StatType.AVG);
            case "maxAirSpeed":
                return formatColumn("air_speed", StatType.MAX);

            // ===== Engine Power =====
            case "avgEnginePower":
                return formatEngines(StatType.AVG);
            case "maxEnginePower":
                return formatEngines(StatType.MAX);

            // ===== Temperature =====
            case "avgTemp":
                return formatColumn("temperature_in", StatType.AVG);
            case "minTemp":
                return formatColumn("temperature_in", StatType.MIN);
            case "maxTemp":
                return formatColumn("temperature_in", StatType.MAX);

            // ===== Pressure =====
            case "avgPressure":
                return formatColumn("pressure_in", StatType.AVG);
            case "minPressure":
                return formatColumn("pressure_in", StatType.MIN);
            case "maxPressure":
                return formatColumn("pressure_in", StatType.MAX);

            // ===== Humidity =====
            case "avgHumidity":
                return formatColumn("humidity_in", StatType.AVG);
            case "minHumidity":
                return formatColumn("humidity_in", StatType.MIN);
            case "maxHumidity":
                return formatColumn("humidity_in", StatType.MAX);

            // ===== Heart Rate =====
            case "avgHeartRate":
                return formatColumn("heart_rate", StatType.AVG);
            case "minHeartRate":
                return formatColumn("heart_rate", StatType.MIN);
            case "maxHeartRate":
                return formatColumn("heart_rate", StatType.MAX);

            // ===== Oxygen =====
            case "avgOxygen":
                return formatColumn("oxygen_mask", StatType.AVG);
            case "minOxygen":
                return formatColumn("oxygen_mask", StatType.MIN);
            case "maxOxygen":
                return formatColumn("oxygen_mask", StatType.MAX);

            // ===== Computed Features (Tranche 4) =====
            case "flightDuration":
                return new ComputedFeatures(record).flightDuration();
            case "flightDistance":
                return new ComputedFeatures(record).flightDistance();
            case "avgAcceleration":
                return new ComputedFeatures(record).avgAcceleration();
            case "maxAcceleration":
                return new ComputedFeatures(record).maxAcceleration();
            case "maxAccelG":
                return new ComputedFeatures(record).maxAccelG();
            case "avgMachSpeed":
                return new ComputedFeatures(record).avgMachSpeed();
            case "maxMachSpeed":
                return new ComputedFeatures(record).maxMachSpeed();

            // ===== Phase Detection (Tranche 5) =====
            case "takeOff":
                return formatPhase(record, "takeOff");
            case "cruise":
                return formatPhase(record, "cruise");
            case "landing":
                return formatPhase(record, "landing");

            // ===== "most" Phase Features (Tranche 6) =====
            case "mostPowerPhase":
                return mostPhase(record, "avgEnginePower");
            case "mostStressPhase":
                return mostPhase(record, "avgHeartRate");
            case "mostAccelPhase":
                return mostPhase(record, "avgAcceleration");

            default:
                // Try phase-suffix pattern: e.g. "avgAltTakeOff" → base="avgAlt", phase="takeOff"
                return tryPhaseSuffixFeature(featureName);
        }
    }

    /**
     * Tries to match features with a phase suffix (TakeOff, Cruise, Landing).
     * E.g., "avgAltTakeOff" → compute "avgAlt" on takeOff phase sub-record.
     */
    private String tryPhaseSuffixFeature(String featureName) {
        String[] phases = {"TakeOff", "Cruise", "Landing"};
        String[] phaseKeys = {"takeOff", "cruise", "landing"};

        for (int i = 0; i < phases.length; i++) {
            if (featureName.endsWith(phases[i])) {
                String baseFeature = featureName.substring(0, featureName.length() - phases[i].length());
                FlightRecord phaseRecord = getPhaseSubRecord(record, phaseKeys[i]);
                if (phaseRecord == null) return "not detected";
                return new FeatureComputer(phaseRecord).compute(baseFeature);
            }
        }
        return null; // Truly unknown feature
    }

    /**
     * Determines which phase has the highest value for a given feature.
     * Returns "takeOff", "cruise", or "landing".
     */
    private String mostPhase(FlightRecord rec, String baseFeature) {
        String[] phaseKeys = {"takeOff", "cruise", "landing"};
        String bestPhase = "not detected";
        double bestValue = Double.NEGATIVE_INFINITY;

        for (String phaseKey : phaseKeys) {
            FlightRecord phaseRecord = getPhaseSubRecord(rec, phaseKey);
            if (phaseRecord == null || phaseRecord.getRecordCount() == 0) continue;
            String result = new FeatureComputer(phaseRecord).compute(baseFeature);
            if (result != null) {
                try {
                    double val = Double.parseDouble(result);
                    if (val > bestValue) {
                        bestValue = val;
                        bestPhase = phaseKey;
                    }
                } catch (NumberFormatException e) {
                    // skip non-numeric results
                }
            }
        }
        return bestPhase;
    }

    /**
     * Extracts a sub-record for a specific flight phase.
     */
    private static FlightRecord getPhaseSubRecord(FlightRecord rec, String phaseKey) {
        FlightPhaseDetector detector = new FlightPhaseDetector();
        FlightPhaseDetector.PhaseResult result = detector.detect(rec);

        FlightPhaseDetector.Phase phase;
        switch (phaseKey) {
            case "takeOff": phase = result.takeOff; break;
            case "cruise":  phase = result.cruise;  break;
            case "landing": phase = result.landing; break;
            default: return null;
        }
        if (phase == null) return null;
        return rec.subRecord(phase.startIndex, phase.endIndex);
    }

    /**
     * Applies a statistical function to a single column and returns formatted result.
     */
    private String formatColumn(String columnName, StatType type) {
        double[] values = record.getColumnValues(columnName);
        if (values.length == 0) return null;
        return Statistics.format(applyStat(values, type));
    }

    /**
     * Computes total engine power (sum of all engine_N columns) at each timestamp,
     * then applies the requested statistical function.
     */
    private String formatEngines(StatType type) {
        // Find all engine columns (engine_0, engine_1, ...)
        List<String> engineCols = new ArrayList<>();
        for (String col : record.getColumnNames()) {
            if (col.startsWith("engine_")) {
                engineCols.add(col);
            }
        }
        if (engineCols.isEmpty()) return null;

        int rowCount = record.getRecordCount();
        double[] totalPower = new double[rowCount];

        for (String col : engineCols) {
            double[] engineValues = record.getColumnValues(col);
            for (int i = 0; i < rowCount; i++) {
                totalPower[i] += engineValues[i];
            }
        }

        return Statistics.format(applyStat(totalPower, type));
    }

    /**
     * Applies the given statistical operation.
     */
    private double applyStat(double[] values, StatType type) {
        switch (type) {
            case AVG: return Statistics.avg(values);
            case MAX: return Statistics.max(values);
            case MIN: return Statistics.min(values);
            default: return 0.0;
        }
    }

    /** Enumeration of supported statistical operations. */
    private enum StatType {
        AVG, MAX, MIN
    }

    /**
     * Formats a flight phase as a timestamp range, or "not detected".
     */
    private String formatPhase(FlightRecord rec, String phaseName) {
        FlightPhaseDetector detector = new FlightPhaseDetector();
        FlightPhaseDetector.PhaseResult result = detector.detect(rec);
        double[] timestamps = rec.getColumnValues("timestamp");

        FlightPhaseDetector.Phase phase;
        switch (phaseName) {
            case "takeOff": phase = result.takeOff; break;
            case "cruise":  phase = result.cruise;  break;
            case "landing": phase = result.landing; break;
            default: return "not detected";
        }

        if (phase == null) {
            return "not detected";
        }

        double startTs = timestamps[phase.startIndex];
        double endTs = timestamps[phase.endIndex];
        return Statistics.format(startTs) + " - " + Statistics.format(endTs);
    }
}
