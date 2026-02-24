package com.music.pandora;

import java.util.Locale;

/**
 * Provides computed (derived) features that require multi-row or multi-column calculations.
 * These features go beyond simple column statistics.
 */
public class ComputedFeatures {

    /** Earth radius in meters for haversine formula. */
    private static final double EARTH_RADIUS_M = 6_371_000.0;

    /** Standard gravity in m/s². */
    private static final double G = 9.80665;

    /** 1 Mach = 1225 km/h. Air speed is in km/h. */
    private static final double MACH_KMH = 1225.0;

    private final FlightRecord record;

    public ComputedFeatures(FlightRecord record) {
        this.record = record;
    }

    // ===================== Flight Duration =====================

    /**
     * Computes flight duration as last_timestamp - first_timestamp.
     *
     * @return duration formatted as HH:MM:SS
     */
    public String flightDuration() {
        double[] timestamps = record.getColumnValues("timestamp");
        if (timestamps.length < 2) return "00:00:00";

        long durationSec = Math.round(timestamps[timestamps.length - 1] - timestamps[0]);
        long hours = durationSec / 3600;
        long minutes = (durationSec % 3600) / 60;
        long seconds = durationSec % 60;

        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    // ===================== Flight Distance (Haversine) =====================

    /**
     * Computes the total flight distance by summing haversine distances
     * between consecutive GPS points (longitude, latitude).
     *
     * @return distance in meters, formatted to 2 decimal places
     */
    public String flightDistance() {
        double[] lons = record.getColumnValues("longitude");
        double[] lats = record.getColumnValues("latitude");
        if (lons.length < 2) return Statistics.format(0.0);

        double totalDistance = 0.0;
        for (int i = 1; i < lons.length; i++) {
            totalDistance += haversine(lats[i - 1], lons[i - 1], lats[i], lons[i]);
        }

        return Statistics.format(totalDistance);
    }

    /**
     * Haversine formula: computes the great-circle distance between two points.
     *
     * @param lat1 latitude of point 1 (degrees)
     * @param lon1 longitude of point 1 (degrees)
     * @param lat2 latitude of point 2 (degrees)
     * @param lon2 longitude of point 2 (degrees)
     * @return distance in meters
     */
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                   * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_M * c;
    }

    // ===================== Acceleration =====================

    /**
     * Computes acceleration between consecutive data points.
     * Acceleration = delta_speed / delta_time.
     * The first point's acceleration is a repeat of the second point's value.
     *
     * @return array of acceleration values (m/s²)
     */
    public double[] computeAcceleration() {
        double[] speeds = record.getColumnValues("air_speed");
        double[] timestamps = record.getColumnValues("timestamp");
        if (speeds.length < 2) return new double[]{0.0};

        double[] accel = new double[speeds.length];
        for (int i = 1; i < speeds.length; i++) {
            double dt = timestamps[i] - timestamps[i - 1];
            if (dt != 0) {
                accel[i] = (speeds[i] - speeds[i - 1]) / dt;
            }
        }
        // First point approximation: repeat first computed value
        accel[0] = accel[1];

        return accel;
    }

    /**
     * @return average acceleration formatted to 2 decimal places
     */
    public String avgAcceleration() {
        return Statistics.format(Statistics.avg(computeAcceleration()));
    }

    /**
     * @return maximum acceleration formatted to 2 decimal places
     */
    public String maxAcceleration() {
        return Statistics.format(Statistics.max(computeAcceleration()));
    }

    /**
     * @return maximum acceleration in G units (maxAccel / 9.80665)
     */
    public String maxAccelG() {
        double maxAccel = Statistics.max(computeAcceleration());
        return Statistics.format(maxAccel / G);
    }

    // ===================== Mach Speed =====================

    /**
     * Computes Mach number at each data point.
     * Mach = air_speed_kmh / 1225.
     * Air speed in the data is already in km/h.
     *
     * @return array of Mach values
     */
    public double[] computeMachSpeeds() {
        double[] speeds = record.getColumnValues("air_speed");
        double[] mach = new double[speeds.length];
        for (int i = 0; i < speeds.length; i++) {
            mach[i] = speeds[i] / MACH_KMH;
        }
        return mach;
    }

    /**
     * @return average Mach number formatted to 2 decimal places
     */
    public String avgMachSpeed() {
        return Statistics.format(Statistics.avg(computeMachSpeeds()));
    }

    /**
     * @return maximum Mach number formatted to 2 decimal places
     */
    public String maxMachSpeed() {
        return Statistics.format(Statistics.max(computeMachSpeeds()));
    }
}
