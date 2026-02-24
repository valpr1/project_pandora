package com.music.pandora;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a parsed flight record file (.frd).
 * Contains metadata (key-value pairs) and flight data (CSV rows).
 */
public class FlightRecord {

    private final String filename;
    private final Map<String, String> metadata;
    private final List<String> columnNames;
    private final List<double[]> dataRows;

    /**
     * Constructs a FlightRecord.
     *
     * @param filename    the source filename
     * @param metadata    ordered map of metadata key-value pairs
     * @param columnNames list of CSV column names
     * @param dataRows    list of data rows (each row is an array of doubles)
     */
    public FlightRecord(String filename, Map<String, String> metadata,
                        List<String> columnNames, List<double[]> dataRows) {
        this.filename = filename;
        this.metadata = metadata;
        this.columnNames = columnNames;
        this.dataRows = dataRows;
    }

    /**
     * Returns the source filename.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Returns the metadata map (insertion-ordered).
     */
    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Returns the value of a specific metadata field, or null if not found.
     */
    public String getMetadataValue(String key) {
        return metadata.get(key);
    }

    /**
     * Returns the list of column names from the data section header.
     */
    public List<String> getColumnNames() {
        return Collections.unmodifiableList(columnNames);
    }

    /**
     * Returns all parameter names (columns) sorted alphabetically.
     */
    public List<String> getParametersSorted() {
        List<String> sorted = new ArrayList<>(columnNames);
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * Returns the data rows.
     */
    public List<double[]> getDataRows() {
        return Collections.unmodifiableList(dataRows);
    }

    /**
     * Returns the number of data records.
     */
    public int getRecordCount() {
        return dataRows.size();
    }

    /**
     * Returns the index of a column by name, or -1 if not found.
     */
    public int getColumnIndex(String columnName) {
        return columnNames.indexOf(columnName);
    }

    /**
     * Extracts all values of a given column as a double array.
     *
     * @param columnName the column name
     * @return array of values, or empty array if column not found
     */
    public double[] getColumnValues(String columnName) {
        int index = getColumnIndex(columnName);
        if (index < 0) {
            return new double[0];
        }
        double[] values = new double[dataRows.size()];
        for (int i = 0; i < dataRows.size(); i++) {
            values[i] = dataRows.get(i)[index];
        }
        return values;
    }

    /**
     * Checks if a column exists in this flight record.
     */
    public boolean hasColumn(String columnName) {
        return columnNames.contains(columnName);
    }

    /**
     * Creates a sub-record containing only rows from startIndex to endIndex (inclusive).
     * Useful for extracting data for a specific flight phase.
     *
     * @param startIndex first row index (inclusive)
     * @param endIndex   last row index (inclusive)
     * @return a new FlightRecord with the same metadata and columns but only the specified rows
     */
    public FlightRecord subRecord(int startIndex, int endIndex) {
        List<double[]> subRows = new ArrayList<>();
        for (int i = startIndex; i <= endIndex && i < dataRows.size(); i++) {
            subRows.add(dataRows.get(i));
        }
        return new FlightRecord(filename, metadata, columnNames, subRows);
    }
}
