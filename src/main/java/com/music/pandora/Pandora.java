package com.music.pandora;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Main orchestrator for the Pandora application.
 * Dispatches work based on the parsed CLI options.
 */
public class Pandora {

    /** Current version of the application, following Semantic Versioning. */
    public static final String VERSION = "2.0.0";

    private final CLIParser cli;
    private final FlightRecordParser parser;

    /**
     * Constructs a Pandora instance with the given CLI configuration.
     *
     * @param cli parsed command-line arguments
     */
    public Pandora(CLIParser cli) {
        this.cli = cli;
        this.parser = new FlightRecordParser();
    }

    /**
     * Executes the main logic based on CLI options.
     * Priority order: version, help, then feature processing.
     */
    public void run() {
        if (cli.isVersionRequested()) {
            printVersion();
            return;
        }

        if (cli.isHelpRequested()) {
            printHelp();
            return;
        }

        // If no source files and no special option, print help
        if (cli.getSourceFiles().isEmpty()
                && cli.getMetadataName() == null
                && cli.getOutputFeature() == null
                && !cli.isParametersRequested()) {
            printHelp();
            return;
        }

        // Parse source files
        List<FlightRecord> records = parser.parseAll(cli.getSourceFiles());

        // Check for incomplete headers
        String headerError = parser.checkIncompleteHeaders(records);
        if (headerError != null) {
            System.out.println(headerError);
        }

        if (records.isEmpty()) {
            return;
        }

        // Handle metadata request
        if (cli.getMetadataName() != null) {
            handleMetadata(records);
            return;
        }

        // Handle parameters request
        if (cli.isParametersRequested()) {
            handleParameters(records);
            return;
        }

        // Handle number of records request
        if (cli.isNumberRequested()) {
            for (FlightRecord record : records) {
                System.out.println(record.getRecordCount());
            }
            return;
        }

        // Handle output feature request
        if (cli.getOutputFeature() != null) {
            handleOutputFeature(records);
            return;
        }

        // Default: Full Report
        handleFullReport(records);
    }

    /**
     * Returns the current unit system based on CLI options.
     */
    private UnitConverter.UnitSystem getUnitSystem() {
        return cli.isImperialUnit()
            ? UnitConverter.UnitSystem.IMPERIAL
            : UnitConverter.UnitSystem.METRIC;
    }

    /**
     * Handles the -m / --metadata option.
     * Prints the metadata value for each record.
     */
    private void handleMetadata(List<FlightRecord> records) {
        String metaName = cli.getMetadataName();
        for (FlightRecord record : records) {
            String value = record.getMetadataValue(metaName);
            if (value != null) {
                System.out.println(value);
            }
        }
    }

    /**
     * Handles the -p / --parameters option.
     * Lists all unique parameters across all records in alphabetical order.
     */
    private void handleParameters(List<FlightRecord> records) {
        Set<String> allParams = new TreeSet<>();
        for (FlightRecord record : records) {
            allParams.addAll(record.getColumnNames());
        }
        for (String param : allParams) {
            System.out.println(param);
        }
    }

    /**
     * Handles the -o / --output option.
     * Dispatches to the appropriate feature computation.
     */
    private void handleOutputFeature(List<FlightRecord> records) {
        String feature = cli.getOutputFeature();

        // Special case: filenames
        if ("filenames".equals(feature)) {
            handleFilenames(records);
            return;
        }

        // Cross-flight features (operate on all records at once)
        if (CrossFlightComputer.isCrossFlightFeature(feature)) {
            CrossFlightComputer crossComputer = new CrossFlightComputer(records);
            String result = crossComputer.compute(feature);
            if (result != null) {
                System.out.println(result);
            }
            return;
        }

        // Per-record features
        for (FlightRecord record : records) {
            // Apply phase filtering if -P is specified
            FlightRecord computeRecord = applyPhaseFilter(record);
            if (computeRecord == null) {
                System.out.println("not detected");
                continue;
            }

            FeatureComputer computer = new FeatureComputer(computeRecord);
            String result = computer.compute(feature);
            if (result != null) {
                // Apply unit conversion
                result = UnitConverter.convert(result, feature, getUnitSystem());
                System.out.println(result);
            } else if (cli.isDebugMode()) {
                System.err.println("DEBUG: Unknown feature: " + feature);
            }
        }
    }

    /**
     * Handles the full report (default mode, no -o specified).
     * In batch mode, writes one report per file. Otherwise, prints all.
     */
    private void handleFullReport(List<FlightRecord> records) {
        ReportGenerator reportGen = new ReportGenerator(getUnitSystem());

        if (cli.isBatchMode()) {
            for (FlightRecord record : records) {
                String report = reportGen.generate(record);
                String outputFilename = record.getFilename().replace(".frd", ".txt");
                try (PrintWriter writer = new PrintWriter(new FileWriter(outputFilename))) {
                    writer.println(report);
                } catch (IOException e) {
                    System.err.println("ERROR: Could not write to " + outputFilename);
                }
            }
        } else {
            System.out.println(reportGen.generateAll(records));
        }
    }

    /**
     * Applies phase filtering if -P option is set.
     * Returns a sub-record containing only the rows for the specified phase,
     * or the original record if no phase is specified.
     *
     * @return filtered record, or null if the phase is not detected
     */
    private FlightRecord applyPhaseFilter(FlightRecord record) {
        String phaseName = cli.getPhaseName();
        if (phaseName == null) {
            return record; // No phase filtering
        }

        FlightPhaseDetector detector = new FlightPhaseDetector();
        FlightPhaseDetector.PhaseResult result = detector.detect(record);

        FlightPhaseDetector.Phase phase;
        switch (phaseName) {
            case "takeOff": phase = result.takeOff; break;
            case "cruise":  phase = result.cruise;  break;
            case "landing": phase = result.landing; break;
            default: return null;
        }

        if (phase == null) {
            return null; // Phase not detected
        }

        return record.subRecord(phase.startIndex, phase.endIndex);
    }

    /**
     * Handles -o filenames: prints all filenames sorted alphabetically.
     */
    private void handleFilenames(List<FlightRecord> records) {
        List<String> filenames = new ArrayList<>();
        for (FlightRecord record : records) {
            filenames.add(record.getFilename());
        }
        Collections.sort(filenames);
        for (String name : filenames) {
            System.out.println(name);
        }
    }

    /**
     * Prints the version string in the format: pandora@X.Y.Z
     */
    private void printVersion() {
        System.out.println("pandora@" + VERSION);
    }

    /**
     * Prints the help/usage message.
     */
    private void printHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("pandora - a CLI tool to analyze Flight-records data\n");
        sb.append("\n");
        sb.append("Usage:\n");
        sb.append("  java -jar pandora.jar [-v|-h]\n");
        sb.append("  java -jar pandora.jar [OPTIONS] ...sources\n");
        sb.append("\n");
        sb.append("  ...source - path to flightRecord files or folder containing flightRecord files\n");
        sb.append("\n");
        sb.append("OPTIONS\n");
        sb.append("  -b, --batch              Batch Mode - process all files in the source folder one by one\n");
        sb.append("  -d, --debug              Debug - print additional debug information on Unhandled\n");
        sb.append("  -h, --help               Help - print this help message\n");
        sb.append("  -I, --imperial           Imperial - set the unit system for output values to imperial\n");
        sb.append("  -m <metadata>, --metadata <metadata>\n");
        sb.append("                           Metadata - Print the value of the specified metadata\n");
        sb.append("  -M, --metric             Metric - set the unit system for output values to SI (default)\n");
        sb.append("  -o <feature>, --output <feature>\n");
        sb.append("                           Output - Print only the specified feature on the command line\n");
        sb.append("  -p, --parameters         Parameters - List in alphabetical order the parameters presents in the source\n");
        sb.append("  -P, --phase <phase_name> Phase - restrict computations to the specified flight phase\n");
        sb.append("  -u <metric|imperial>, --unit <metric|imperial>\n");
        sb.append("                           Unit - set the unit system for output values (default: metric)\n");
        sb.append("  -v, --version            Version - print the version of the application\n");
        System.out.print(sb.toString());
    }
}
