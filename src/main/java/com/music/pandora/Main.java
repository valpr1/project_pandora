package com.music.pandora;

/**
 * Entry point for the Pandora Flight Data Recorder Analyzer.
 * Parses command-line arguments and dispatches to the appropriate handler.
 */
public class Main {

    public static void main(String[] args) {
        CLIParser cli = new CLIParser(args);
        Pandora pandora = new Pandora(cli);
        pandora.run();
    }
}
