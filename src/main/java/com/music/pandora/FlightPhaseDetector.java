package com.music.pandora;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects flight phases (takeOff, cruise, landing) using yaw-based plateau detection.
 *
 * Algorithm:
 * 1. Compute yaw deltas: |yaw[i] - yaw[i-1]|
 * 2. Find indexes where delta < 1, yaw != -1, and at least 10 turbulences occurred
 * 3. Group consecutive indexes into plateaus
 * 4. Keep only plateaus lasting >= 60 seconds
 * 5. takeOff = before first plateau, cruise = first to last plateau, landing = after last plateau
 */
public class FlightPhaseDetector {

    /** Minimum delta threshold to consider "turbulence". */
    private static final double DELTA_THRESHOLD = 1.0;

    /** Number of turbulences (delta > threshold) required before plateaus can start. */
    private static final int MIN_TURBULENCES = 10;

    /** Minimum duration (seconds) for a plateau to be valid. */
    private static final double MIN_PLATEAU_DURATION = 60.0;

    /** Represents a time range [startIndex, endIndex] within the data. */
    public static class Phase {
        public final int startIndex;
        public final int endIndex;

        public Phase(int startIndex, int endIndex) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
    }

    /** Result of flight phase detection. */
    public static class PhaseResult {
        public final Phase takeOff;  // may be null if not detected
        public final Phase cruise;   // may be null if not detected
        public final Phase landing;  // may be null if not detected

        public PhaseResult(Phase takeOff, Phase cruise, Phase landing) {
            this.takeOff = takeOff;
            this.cruise = cruise;
            this.landing = landing;
        }
    }

    /**
     * Detects flight phases from a FlightRecord.
     *
     * @param record the flight record containing yaw and timestamp columns
     * @return phase detection results (phases may be null if not detected)
     */
    public PhaseResult detect(FlightRecord record) {
        double[] yaw = record.getColumnValues("yaw");
        double[] timestamps = record.getColumnValues("timestamp");

        if (yaw.length < 2) {
            return new PhaseResult(null, null, null);
        }

        // Step 1: Compute deltas
        double[] deltas = new double[yaw.length];
        for (int i = 1; i < yaw.length; i++) {
            deltas[i] = Math.abs(yaw[i] - yaw[i - 1]);
        }
        deltas[0] = deltas[1]; // First point approximation

        // Step 2: Find valid plateau indexes
        int turbulenceCount = 0;
        List<Integer> validIndexes = new ArrayList<>();

        for (int i = 0; i < yaw.length; i++) {
            if (deltas[i] >= DELTA_THRESHOLD) {
                turbulenceCount++;
            } else if (yaw[i] != -1.0 && turbulenceCount >= MIN_TURBULENCES) {
                validIndexes.add(i);
            }
        }

        if (validIndexes.isEmpty()) {
            // No plateaus detected — everything is takeOff
            return new PhaseResult(
                new Phase(0, yaw.length - 1),
                null,
                null
            );
        }

        // Step 3: Group consecutive valid indexes into raw plateaus
        List<int[]> rawPlateaus = new ArrayList<>();
        int start = validIndexes.get(0);
        int prev = start;

        for (int i = 1; i < validIndexes.size(); i++) {
            int curr = validIndexes.get(i);
            if (curr != prev + 1) {
                // Gap detected, close previous plateau
                rawPlateaus.add(new int[]{start, prev});
                start = curr;
            }
            prev = curr;
        }
        rawPlateaus.add(new int[]{start, prev}); // Close last plateau

        // Step 4: Filter plateaus by minimum duration (>= 60 seconds)
        List<int[]> plateaus = new ArrayList<>();
        for (int[] plateau : rawPlateaus) {
            double duration = timestamps[plateau[1]] - timestamps[plateau[0]];
            if (duration >= MIN_PLATEAU_DURATION) {
                plateaus.add(plateau);
            }
        }

        if (plateaus.isEmpty()) {
            // No valid plateaus after filtering — everything is takeOff
            return new PhaseResult(
                new Phase(0, yaw.length - 1),
                null,
                null
            );
        }

        // Step 5: Determine phases
        int firstPlateauStart = plateaus.get(0)[0];
        int lastPlateauEnd = plateaus.get(plateaus.size() - 1)[1];

        Phase takeOff = null;
        Phase cruise = null;
        Phase landing = null;

        // takeOff = everything before first plateau
        if (firstPlateauStart > 0) {
            takeOff = new Phase(0, firstPlateauStart - 1);
        }

        // cruise = from first plateau start to last plateau end
        cruise = new Phase(firstPlateauStart, lastPlateauEnd);

        // landing = everything after last plateau
        if (lastPlateauEnd < yaw.length - 1) {
            landing = new Phase(lastPlateauEnd + 1, yaw.length - 1);
        }

        return new PhaseResult(takeOff, cruise, landing);
    }
}
