package com.music.pandora;

import java.util.Locale;

/**
 * Converts values between metric and imperial unit systems.
 * Constants from the project specification (Constants page).
 */
public class UnitConverter {

    /** Unit system mode. */
    public enum UnitSystem {
        METRIC, IMPERIAL
    }

    // ===== Conversion factors =====
    private static final double M_TO_FT = 3.281;
    private static final double KG_TO_LBS = 2.205;
    private static final double W_TO_HP = 1.0 / 754.7;      // 1 hp = 754.7 W
    private static final double PA_TO_PSI = 1.0 / 6894.76;   // 1 psi = 6894.76 Pa
    private static final double CELSIUS_TO_KELVIN = 273.15;
    private static final double KMH_TO_MPH = 1.0 / 1.609;    // 1 mph = 1.609 km/h
    private static final double MS_TO_FTS = 3.281;            // m/s to ft/s

    /**
     * Converts a metric value to imperial based on the unit type.
     *
     * @param value    the metric value
     * @param unitType the type of unit (e.g., "altitude", "speed")
     * @return the converted value
     */
    public static double toImperial(double value, String unitType) {
        switch (unitType) {
            case "altitude":
            case "distance":
                return value * M_TO_FT;          // m → ft
            case "speed":
                return value * MS_TO_FTS;         // m/s → ft/s
            case "power":
                return value * W_TO_HP;           // W → hp
            case "temperature":
                return value + CELSIUS_TO_KELVIN;  // ℃ → K
            case "pressure":
                return value * PA_TO_PSI;          // Pa → psi
            case "acceleration":
                return value * M_TO_FT;           // m/s² → ft/s²
            default:
                return value; // No conversion (%, bpm, Mach, G, ratios, time)
        }
    }

    /**
     * Returns the unit label for a feature in the given system.
     */
    public static String getUnitLabel(String featureName, UnitSystem system) {
        String unitType = getUnitType(featureName);
        if (unitType == null) return "";

        if (system == UnitSystem.IMPERIAL) {
            switch (unitType) {
                case "altitude": return "ft";
                case "distance": return "ft";
                case "speed": return "ft/s";
                case "power": return "hp";
                case "temperature": return "K";
                case "pressure": return "psi";
                case "acceleration": return "ft/s²";
                case "humidity": return "%";
                case "heartRate": return "bpm";
                case "concentration": return "%";
                case "mach": return "Mach";
                case "g": return "G";
                case "time": return "";
                default: return "";
            }
        } else {
            // Metric
            switch (unitType) {
                case "altitude": return "m";
                case "distance": return "m";
                case "speed": return "m/s";
                case "power": return "W";
                case "temperature": return "℃";
                case "pressure": return "Pa";
                case "acceleration": return "m/s²";
                case "humidity": return "%";
                case "heartRate": return "bpm";
                case "concentration": return "%";
                case "mach": return "Mach";
                case "g": return "G";
                case "time": return "";
                default: return "";
            }
        }
    }

    /**
     * Determines the unit type for a given feature name.
     */
    public static String getUnitType(String featureName) {
        // Strip phase suffixes for lookup
        String base = stripPhaseSuffix(featureName);

        switch (base) {
            case "avgAlt": case "maxAlt":
                return "altitude";
            case "avgAirSpeed": case "maxAirSpeed":
                return "speed";
            case "avgEnginePower": case "maxEnginePower":
                return "power";
            case "avgTemp": case "minTemp": case "maxTemp":
                return "temperature";
            case "avgPressure": case "minPressure": case "maxPressure":
                return "pressure";
            case "avgHumidity": case "minHumidity": case "maxHumidity":
                return "humidity";
            case "avgHeartRate": case "minHeartRate": case "maxHeartRate":
                return "heartRate";
            case "avgOxygen": case "minOxygen": case "maxOxygen":
                return "concentration";
            case "flightDistance":
                return "distance";
            case "avgAcceleration": case "maxAcceleration":
                return "acceleration";
            case "avgMachSpeed": case "maxMachSpeed":
                return "mach";
            case "maxAccelG":
                return "g";
            case "flightDuration":
                return "time";
            default:
                return null;
        }
    }

    /**
     * Strips phase suffixes (TakeOff, Cruise, Landing) from a feature name.
     */
    private static String stripPhaseSuffix(String featureName) {
        String[] suffixes = {"TakeOff", "Cruise", "Landing"};
        for (String suffix : suffixes) {
            if (featureName.endsWith(suffix)) {
                return featureName.substring(0, featureName.length() - suffix.length());
            }
        }
        return featureName;
    }

    /**
     * Converts and formats a numeric feature value.
     *
     * @param rawValue    the formatted metric value string
     * @param featureName the feature name
     * @param system      the target unit system
     * @return the converted and formatted value
     */
    public static String convert(String rawValue, String featureName, UnitSystem system) {
        if (system == UnitSystem.METRIC || rawValue == null) {
            return rawValue;
        }

        String unitType = getUnitType(featureName);
        if (unitType == null || "time".equals(unitType)) {
            return rawValue; // No conversion for time, phases, etc.
        }

        try {
            double value = Double.parseDouble(rawValue);
            double converted = toImperial(value, unitType);
            return Statistics.format(converted);
        } catch (NumberFormatException e) {
            return rawValue; // Non-numeric values pass through
        }
    }
}
