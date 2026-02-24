package com.music.pandora;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses command-line arguments for the Pandora application.
 * Supports short and long option forms.
 */
public class CLIParser {

    private boolean versionRequested = false;
    private boolean helpRequested = false;
    private boolean batchMode = false;
    private boolean debugMode = false;
    private boolean imperialUnit = false;
    private String metadataName = null;
    private String outputFeature = null;
    private String phaseName = null;
    private boolean parametersRequested = false;
    private boolean numberRequested = false;
    private final List<String> sourceFiles = new ArrayList<>();

    /**
     * Constructs a CLIParser and parses the given arguments.
     *
     * @param args command-line arguments
     */
    public CLIParser(String[] args) {
        parse(args);
    }

    private void parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-v":
                case "--version":
                    versionRequested = true;
                    break;
                case "-h":
                case "--help":
                    helpRequested = true;
                    break;
                case "-b":
                case "--batch":
                    batchMode = true;
                    break;
                case "-d":
                case "--debug":
                    debugMode = true;
                    break;
                case "-p":
                case "--parameters":
                    parametersRequested = true;
                    break;
                case "-n":
                case "--number":
                    numberRequested = true;
                    break;
                case "-M":
                case "--metric":
                    imperialUnit = false;
                    break;
                case "-I":
                case "--imperial":
                    imperialUnit = true;
                    break;
                case "-m":
                case "--metadata":
                    if (i + 1 < args.length) {
                        metadataName = args[++i];
                    }
                    break;
                case "-o":
                case "--output":
                    if (i + 1 < args.length) {
                        outputFeature = args[++i];
                    }
                    break;
                case "-u":
                case "--unit":
                    if (i + 1 < args.length) {
                        String unit = args[++i];
                        imperialUnit = "imperial".equalsIgnoreCase(unit);
                    }
                    break;
                case "-P":
                case "--phase":
                    if (i + 1 < args.length) {
                        phaseName = args[++i];
                    }
                    break;
                default:
                    // Anything else is treated as a source file/directory
                    sourceFiles.add(arg);
                    break;
            }
        }
    }

    public boolean isVersionRequested() {
        return versionRequested;
    }

    public boolean isHelpRequested() {
        return helpRequested;
    }

    public boolean isBatchMode() {
        return batchMode;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean isImperialUnit() {
        return imperialUnit;
    }

    public String getMetadataName() {
        return metadataName;
    }

    public String getOutputFeature() {
        return outputFeature;
    }

    public String getPhaseName() {
        return phaseName;
    }

    public boolean isParametersRequested() {
        return parametersRequested;
    }

    public List<String> getSourceFiles() {
        return sourceFiles;
    }

    public boolean isNumberRequested() {
        return numberRequested;
    }
}
