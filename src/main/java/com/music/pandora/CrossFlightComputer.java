package com.music.pandora;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Computes features that span multiple flight records (cross-flight analysis).
 * These features compare or aggregate values across all files.
 */
public class CrossFlightComputer {

    private final List<FlightRecord> records;

    public CrossFlightComputer(List<FlightRecord> records) {
        this.records = records;
    }

    /**
     * Computes a cross-flight feature by name.
     *
     * @param featureName the feature to compute
     * @return formatted result, or null if unknown
     */
    public String compute(String featureName) {
        switch (featureName) {
            case "cumulDuration":   return cumulDuration();
            case "cumulDistance":   return cumulDistance();
            case "airportTakeOff": return mostUsedAirport("from");
            case "airportLanding": return mostUsedAirport("to");
            case "highestDrag":    return bestCoef("drag coef", true);
            case "smallestDrag":   return bestCoef("drag coef", false);
            case "highestLift":    return bestCoef("lift coef", true);
            case "smallestLift":   return bestCoef("lift coef", false);
            case "highestSpeed":   return bestFeature("avgAirSpeed", true, "km/h");
            case "slowestSpeed":   return bestFeature("avgAirSpeed", false, "km/h");
            case "highestAltitude": return bestFeature("maxAlt", true, null);
            case "longestDuration": return longestDuration();
            case "firstLanding":   return firstOrLastLanding(true);
            case "lastLanding":    return firstOrLastLanding(false);
            case "highestPower":   return bestFeature("avgEnginePower", true, null);
            case "highestOxygen":  return bestFeature("avgOxygen", true, null);
            case "highestHeartBeat": return bestFeature("avgHeartRate", true, null);
            default: return null;
        }
    }

    /**
     * Checks if a feature is a cross-flight feature.
     */
    public static boolean isCrossFlightFeature(String featureName) {
        switch (featureName) {
            case "cumulDuration": case "cumulDistance":
            case "airportTakeOff": case "airportLanding":
            case "highestDrag": case "smallestDrag":
            case "highestLift": case "smallestLift":
            case "highestSpeed": case "slowestSpeed":
            case "highestAltitude": case "longestDuration":
            case "firstLanding": case "lastLanding":
            case "highestPower": case "highestOxygen": case "highestHeartBeat":
                return true;
            default:
                return false;
        }
    }

    // ===== Cumulative features =====

    private String cumulDuration() {
        long totalSeconds = 0;
        for (FlightRecord record : records) {
            double[] ts = record.getColumnValues("timestamp");
            if (ts.length >= 2) {
                totalSeconds += Math.round(ts[ts.length - 1] - ts[0]);
            }
        }
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d:%02d", h, m, s);
    }

    private String cumulDistance() {
        double totalKm = 0;
        for (FlightRecord record : records) {
            ComputedFeatures cf = new ComputedFeatures(record);
            String dist = cf.flightDistance();
            try {
                totalKm += Double.parseDouble(dist) / 1000.0; // m → km
            } catch (NumberFormatException e) {
                // skip
            }
        }
        return Statistics.format(totalKm);
    }

    // ===== Most used airport =====

    private String mostUsedAirport(String metaKey) {
        Map<String, Integer> counts = new HashMap<>();
        for (FlightRecord record : records) {
            String airport = record.getMetadataValue(metaKey);
            if (airport != null) {
                counts.put(airport, counts.getOrDefault(airport, 0) + 1);
            }
        }
        String best = null;
        int bestCount = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestCount = entry.getValue();
                best = entry.getKey();
            }
        }
        return best != null ? best : "not detected";
    }

    // ===== Coefficient comparison =====

    private String bestCoef(String metaKey, boolean highest) {
        String bestId = null;
        double bestVal = highest ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for (FlightRecord record : records) {
            String coefStr = record.getMetadataValue(metaKey);
            String flightCode = record.getMetadataValue("flight code");
            if (coefStr == null || flightCode == null) continue;
            try {
                double coef = Double.parseDouble(coefStr);
                if ((highest && coef > bestVal) || (!highest && coef < bestVal)) {
                    bestVal = coef;
                    bestId = flightCode;
                }
            } catch (NumberFormatException e) {
                // skip
            }
        }
        return bestId != null ? bestId + ":" + Statistics.format(bestVal) : "not detected";
    }

    // ===== Best feature comparison =====

    private String bestFeature(String baseFeature, boolean highest, String unitOverride) {
        String bestId = null;
        double bestVal = highest ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for (FlightRecord record : records) {
            FeatureComputer fc = new FeatureComputer(record);
            String result = fc.compute(baseFeature);
            String flightCode = record.getMetadataValue("flight code");
            if (result == null || flightCode == null) continue;
            try {
                double val = Double.parseDouble(result);
                if ((highest && val > bestVal) || (!highest && val < bestVal)) {
                    bestVal = val;
                    bestId = flightCode;
                }
            } catch (NumberFormatException e) {
                // skip
            }
        }
        return bestId != null ? bestId + ":" + Statistics.format(bestVal) : "not detected";
    }

    // ===== Longest Duration =====

    private String longestDuration() {
        String bestId = null;
        long bestSec = 0;

        for (FlightRecord record : records) {
            double[] ts = record.getColumnValues("timestamp");
            if (ts.length < 2) continue;
            long dur = Math.round(ts[ts.length - 1] - ts[0]);
            String flightCode = record.getMetadataValue("flight code");
            if (dur > bestSec && flightCode != null) {
                bestSec = dur;
                bestId = flightCode;
            }
        }
        if (bestId == null) return "not detected";
        long h = bestSec / 3600;
        long m = (bestSec % 3600) / 60;
        long s = bestSec % 60;
        return bestId + ":" + String.format(Locale.US, "%02d:%02d:%02d", h, m, s);
    }

    // ===== First/Last Landing =====

    private String firstOrLastLanding(boolean first) {
        String bestId = null;
        String bestAirport = null;
        double bestTime = first ? Double.MAX_VALUE : Double.MIN_VALUE;

        for (FlightRecord record : records) {
            double[] ts = record.getColumnValues("timestamp");
            if (ts.length < 2) continue;
            double landingTime = ts[ts.length - 1];
            String flightCode = record.getMetadataValue("flight code");
            String to = record.getMetadataValue("to");
            if (flightCode == null) continue;

            if ((first && landingTime < bestTime) || (!first && landingTime > bestTime)) {
                bestTime = landingTime;
                bestId = flightCode;
                bestAirport = to != null ? to : "unknown";
            }
        }
        if (bestId == null) return "not detected";

        // Format landing time as HH:MM:SS from start of day (approximate)
        long sec = Math.round(bestTime) % 86400;
        long h = sec / 3600;
        long m = (sec % 3600) / 60;
        long s = sec % 60;
        return bestId + ":" + bestAirport + ":" + String.format(Locale.US, "%02d:%02d:%02d", h, m, s);
    }
}
