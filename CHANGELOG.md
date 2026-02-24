# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.0] - 2024-01-15

### Added
- `--version` / `-v` — display application version
- `--help` / `-h` — display help message
- `--parameters` / `-p` — list all CSV column names alphabetically
- `--number` / `-n` — print the number of data records per file
- `--batch` / `-b` — process files in batch mode (write .txt output per file)
- `--debug` / `-d` — enable debug output
- `--metric` / `-M` — use metric units (default)
- `--imperial` / `-I` — use imperial units
- `--unit` / `-u` — choose unit system (metric|imperial)
- `--phase` / `-P` — restrict feature computation to a flight phase
- `-m <metadata>` — display a metadata field value
- `-o filenames` — list all source filenames alphabetically
- `-o start_time` — starting timestamp of the flight
- Single-flight statistical features: avgAlt, maxAlt, avgAirSpeed, maxAirSpeed, avgEnginePower, maxEnginePower, avgTemp, minTemp, maxTemp, avgPressure, minPressure, maxPressure, avgHumidity, minHumidity, maxHumidity, avgHeartRate, minHeartRate, maxHeartRate, avgOxygen, minOxygen, maxOxygen
- Computed features: flightDuration, flightDistance (haversine), avgAcceleration, maxAcceleration, maxAccelG, avgMachSpeed, maxMachSpeed
- Flight phase detection (yaw-based plateau algorithm): takeOff, cruise, landing
- Phase-specific features via suffix (e.g., avgAltTakeOff, maxAirSpeedCruise)
- Phase comparison features: mostPowerPhase, mostStressPhase, mostAccelPhase
- Cross-flight features: cumulDuration, cumulDistance, airportTakeOff, airportLanding, highestAltitude, longestDuration, highestSpeed, slowestSpeed, highestPower, highestOxygen, highestHeartBeat, highestDrag, smallestDrag, highestLift, smallestLift, firstLanding, lastLanding
- Full Report mode (default, all features alphabetically with unit labels)
- Unit conversion system (metric ↔ imperial)
- INCOMPLETE_HEADER error detection
- FlightRecord model, FlightRecordParser, FeatureComputer, ComputedFeatures, FlightPhaseDetector, CrossFlightComputer, Statistics, UnitConverter, ReportGenerator
- 142 unit tests across 10 test suites
