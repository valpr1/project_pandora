package com.music.pandora;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses .frd (Flight Record Data) files into FlightRecord objects.
 *
 * File format:
 * - Metadata section: key:value pairs, one per line
 * - Empty line separator
 * - Data section: CSV with header row followed by data rows
 */
public class FlightRecordParser {

    /** Required metadata fields for header completeness check. */
    private static final List<String> REQUIRED_METADATA = Arrays.asList(
        "flight id", "flight code", "origin", "date", "from", "to"
    );

    /**
     * Parses a single .frd file.
     *
     * @param filePath path to the .frd file
     * @return parsed FlightRecord
     * @throws IOException if the file cannot be read
     */
    public FlightRecord parse(String filePath) throws IOException {
        File file = new File(filePath);
        String filename = file.getName();

        Map<String, String> metadata = new LinkedHashMap<>();
        List<String> columnNames = new ArrayList<>();
        List<double[]> dataRows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Phase 1: Read metadata section
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    break; // Empty line = end of metadata section
                }
                int colonIdx = line.indexOf(':');
                if (colonIdx >= 0) {
                    String key = line.substring(0, colonIdx).trim();
                    String value = line.substring(colonIdx + 1).trim();
                    metadata.put(key, value);
                }
            }

            // Phase 2: Read data section header (column names)
            line = reader.readLine();
            if (line != null && !line.trim().isEmpty()) {
                String[] columns = line.trim().split(",");
                for (String col : columns) {
                    columnNames.add(col.trim());
                }
            }

            // Phase 3: Read data rows
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",");
                double[] row = new double[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    try {
                        row[i] = Double.parseDouble(parts[i].trim());
                    } catch (NumberFormatException e) {
                        row[i] = Double.NaN;
                    }
                }
                dataRows.add(row);
            }
        }

        return new FlightRecord(filename, metadata, columnNames, dataRows);
    }

    /**
     * Parses multiple files and/or directories.
     * If a path is a directory, all .frd/.csv files in it are parsed.
     *
     * @param paths list of file/directory paths
     * @return list of parsed FlightRecords
     */
    public List<FlightRecord> parseAll(List<String> paths) {
        List<FlightRecord> records = new ArrayList<>();
        for (String path : paths) {
            File file = new File(path);
            if (file.isDirectory()) {
                File[] files = file.listFiles((dir, name) ->
                    name.endsWith(".frd") || name.endsWith(".csv"));
                if (files != null) {
                    Arrays.sort(files); // Alphabetical order
                    for (File f : files) {
                        try {
                            records.add(parse(f.getAbsolutePath()));
                        } catch (IOException e) {
                            // Silently skip (unless debug mode)
                        }
                    }
                }
            } else {
                try {
                    records.add(parse(file.getAbsolutePath()));
                } catch (IOException e) {
                    // Silently skip (unless debug mode)
                }
            }
        }
        return records;
    }

    /**
     * Checks for missing required metadata fields across multiple flight records.
     *
     * @param records list of flight records
     * @return error message if any records have incomplete headers, or null if OK
     */
    public String checkIncompleteHeaders(List<FlightRecord> records) {
        Map<String, List<String>> missingByFile = new LinkedHashMap<>();

        for (FlightRecord record : records) {
            List<String> missing = new ArrayList<>();
            for (String field : REQUIRED_METADATA) {
                if (record.getMetadataValue(field) == null) {
                    missing.add(field);
                }
            }
            if (!missing.isEmpty()) {
                missingByFile.put(record.getFilename(), missing);
            }
        }

        if (missingByFile.isEmpty()) {
            return null;
        }

        // Build error message: alphabetical order
        List<String> filenames = new ArrayList<>(missingByFile.keySet());
        filenames.sort(String::compareTo);

        StringBuilder sb = new StringBuilder("ERROR: INCOMPLETE_HEADER - ");
        for (int i = 0; i < filenames.size(); i++) {
            String fname = filenames.get(i);
            List<String> fields = missingByFile.get(fname);
            fields.sort(String::compareTo);
            sb.append(fname).append("=[");
            for (int j = 0; j < fields.size(); j++) {
                if (j > 0) sb.append(",");
                sb.append(fields.get(j));
            }
            sb.append("]");
            if (i < filenames.size() - 1) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }
}
