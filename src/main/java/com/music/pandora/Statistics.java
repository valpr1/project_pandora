package com.music.pandora;

import java.util.Locale;

/**
 * Utility class for basic statistical computations on double arrays.
 */
public final class Statistics {

    private Statistics() {
        // Utility class — no instantiation
    }

    /**
     * Computes the arithmetic mean of an array of values.
     *
     * @param values non-empty array of doubles
     * @return average value
     */
    public static double avg(double[] values) {
        if (values.length == 0) return 0.0;
        double sum = 0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.length;
    }

    /**
     * Finds the maximum value in an array.
     *
     * @param values non-empty array of doubles
     * @return maximum value
     */
    public static double max(double[] values) {
        if (values.length == 0) return 0.0;
        double max = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        return max;
    }

    /**
     * Finds the minimum value in an array.
     *
     * @param values non-empty array of doubles
     * @return minimum value
     */
    public static double min(double[] values) {
        if (values.length == 0) return 0.0;
        double min = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] < min) {
                min = values[i];
            }
        }
        return min;
    }

    /**
     * Computes the sum of an array of values.
     *
     * @param values array of doubles
     * @return sum
     */
    public static double sum(double[] values) {
        double sum = 0;
        for (double v : values) {
            sum += v;
        }
        return sum;
    }

    /**
     * Formats a double value with 2 decimal places.
     *
     * @param value the value to format
     * @return formatted string
     */
    public static String format(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
}
