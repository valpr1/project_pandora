# 🛩️ Pandora v2.0.0

**Pandora** is a Java CLI tool for analyzing military flight data records (`.frd` files). It extracts metadata, computes statistical and derived features, detects flight phases, and supports multi-file cross-flight analysis — all with metric or imperial unit output.

Built following strict **Test-Driven Development (TDD)** methodology with **142 unit tests** across 10 test suites.

---

## 📋 Table of Contents

- [Quick Start](#-quick-start)
- [Build & Test](#-build--test)
- [Usage](#-usage)
- [CLI Options](#-cli-options)
- [Features](#-features)
- [Flight Phase Detection](#-flight-phase-detection)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Unit Tests](#-unit-tests)
- [Changelog](#-changelog)

---

## 🚀 Quick Start

```bash
# Build the project
mvn package

# Run Pandora
java -jar target/pandora.jar [options] <file.frd ...>

# Examples
java -jar target/pandora.jar --version
java -jar target/pandora.jar -o avgAlt test/resources/0_201_MiG-23MLD.frd
java -jar target/pandora.jar test/resources/0_201_MiG-23MLD.frd          # Full report
```

---

## 🔨 Build & Test

### Prerequisites

| Tool   | Version      |
|--------|--------------|
| Java   | OpenJDK 17+  |
| Maven  | 3.9+         |

### Commands

```bash
# Compile and run all tests
mvn package

# Run tests only
mvn test

# Clean build artifacts
mvn clean
```

The build produces an executable JAR at `target/pandora.jar`.

---

## 💻 Usage

### Basic usage

```bash
java -jar target/pandora.jar [options] <source_file.frd ...>
```

When no `-o` option is specified, Pandora outputs a **full report** listing all features alphabetically with their values and units.

### Examples

```bash
# Display version
java -jar target/pandora.jar --version
# Output: Pandora v2.0.0

# Display help
java -jar target/pandora.jar --help

# Get a specific metadata field
java -jar target/pandora.jar -m flight\ code test/resources/0_201_MiG-23MLD.frd
# Output: MiG-23MLD

# Compute a single feature
java -jar target/pandora.jar -o avgAlt test/resources/0_201_MiG-23MLD.frd
# Output: 4182.45

# Same feature in imperial units
java -jar target/pandora.jar -I -o avgAlt test/resources/0_201_MiG-23MLD.frd
# Output: 13722.58

# Compute a feature on a specific flight phase
java -jar target/pandora.jar -P cruise -o avgAlt test/resources/phase_test.frd
# Output: 698.27

# Cross-flight analysis (multiple files)
java -jar target/pandora.jar -o highestAltitude file1.frd file2.frd
# Output: MiG-23MLD:8560.84

# Full report (default mode)
java -jar target/pandora.jar test/resources/simple_test.frd
# Output:
# === simple_test.frd ===
# avgAcceleration : 50.00 m/s²
# avgAirSpeed     : 300.00 m/s
# avgAlt          : 300.00 m
# ...

# Batch mode — writes one .txt file per .frd file
java -jar target/pandora.jar -b test/resources/*.frd
```

---

## ⚙️ CLI Options

| Option | Long Form | Argument | Description |
|--------|-----------|----------|-------------|
| `-v` | `--version` | — | Display application version |
| `-h` | `--help` | — | Display help message |
| `-m` | `--metadata` | `<name>` | Print a metadata field (e.g. `flight code`, `origin`) |
| `-p` | `--parameters` | — | List all CSV column names (alphabetical) |
| `-n` | `--number` | — | Print the number of data records |
| `-o` | `--output` | `<feature>` | Compute and print a specific feature |
| `-P` | `--phase` | `<phase>` | Restrict computation to a flight phase (`takeOff`, `cruise`, `landing`) |
| `-M` | `--metric` | — | Use metric units (default) |
| `-I` | `--imperial` | — | Use imperial units |
| `-u` | `--unit` | `metric\|imperial` | Choose unit system |
| `-b` | `--batch` | — | Batch mode: write one `.txt` report per file |
| `-d` | `--debug` | — | Enable debug output |

---

## 📊 Features

### Single-Flight Statistical Features (21)

Computed on individual CSV columns using `avg`, `max`, or `min` aggregation.

| Feature | Column | Stat | Unit (metric) | Unit (imperial) |
|---------|--------|------|---------------|-----------------|
| `avgAlt` | altitude | avg | m | ft |
| `maxAlt` | altitude | max | m | ft |
| `avgAirSpeed` | air_speed | avg | m/s | ft/s |
| `maxAirSpeed` | air_speed | max | m/s | ft/s |
| `avgEnginePower` | engine_* (sum) | avg | W | hp |
| `maxEnginePower` | engine_* (sum) | max | W | hp |
| `avgTemp` | temperature_in | avg | ℃ | K |
| `minTemp` | temperature_in | min | ℃ | K |
| `maxTemp` | temperature_in | max | ℃ | K |
| `avgPressure` | pressure_in | avg | Pa | psi |
| `minPressure` | pressure_in | min | Pa | psi |
| `maxPressure` | pressure_in | max | Pa | psi |
| `avgHumidity` | humidity_in | avg | % | % |
| `minHumidity` | humidity_in | min | % | % |
| `maxHumidity` | humidity_in | max | % | % |
| `avgHeartRate` | heart_rate | avg | bpm | bpm |
| `minHeartRate` | heart_rate | min | bpm | bpm |
| `maxHeartRate` | heart_rate | max | bpm | bpm |
| `avgOxygen` | oxygen_mask | avg | % | % |
| `minOxygen` | oxygen_mask | min | % | % |
| `maxOxygen` | oxygen_mask | max | % | % |

### Computed Features (7)

Derived from multi-row or multi-column calculations.

| Feature | Description | Output |
|---------|-------------|--------|
| `flightDuration` | Total flight time (last - first timestamp) | `HH:MM:SS` |
| `flightDistance` | Total distance flown (Haversine formula on GPS) | meters |
| `avgAcceleration` | Average horizontal acceleration | m/s² |
| `maxAcceleration` | Maximum horizontal acceleration | m/s² |
| `maxAccelG` | Maximum acceleration in G-forces (÷ 9.80665) | G |
| `avgMachSpeed` | Average speed as Mach number (÷ 1225 km/h) | Mach |
| `maxMachSpeed` | Maximum speed as Mach number | Mach |

### Flight Phase Features (6)

| Feature | Description | Output |
|---------|-------------|--------|
| `takeOff` | Take-off phase time range | `start - end` (timestamps) |
| `cruise` | Cruise phase time range | `start - end` (timestamps) |
| `landing` | Landing phase time range | `start - end` (timestamps) |
| `mostPowerPhase` | Phase with highest avg engine power | `takeOff\|cruise\|landing` |
| `mostStressPhase` | Phase with highest avg heart rate | `takeOff\|cruise\|landing` |
| `mostAccelPhase` | Phase with highest avg acceleration | `takeOff\|cruise\|landing` |

### Phase-Specific Features (suffix pattern)

Any base feature can be computed on a specific phase by appending `TakeOff`, `Cruise`, or `Landing`:

```
avgAltTakeOff       →  average altitude during take-off
maxAirSpeedCruise   →  maximum air speed during cruise
minTempLanding      →  minimum temperature during landing
```

Alternatively, use `--phase`:
```bash
java -jar target/pandora.jar -P cruise -o avgAlt file.frd
```

### Cross-Flight Features (17)

These operate on **multiple files** at once and compare or aggregate values.

| Feature | Description | Output Format |
|---------|-------------|---------------|
| `cumulDuration` | Total flight time across all files | `HH:MM:SS` |
| `cumulDistance` | Total distance across all files | km |
| `airportTakeOff` | Most frequently used take-off airport | airport name |
| `airportLanding` | Most frequently used landing airport | airport name |
| `highestAltitude` | Jet that flew the highest | `jet_id:max_altitude` |
| `longestDuration` | Jet with the longest flight | `jet_id:HH:MM:SS` |
| `highestSpeed` | Jet with the fastest avg speed | `jet_id:speed` |
| `slowestSpeed` | Jet with the slowest avg speed | `jet_id:speed` |
| `highestPower` | Jet with highest avg engine power | `jet_id:power` |
| `highestOxygen` | Jet using most oxygen | `jet_id:oxygen` |
| `highestHeartBeat` | Jet with highest avg heart rate | `jet_id:bpm` |
| `highestDrag` | Jet with highest drag coefficient | `jet_id:coef` |
| `smallestDrag` | Jet with smallest drag coefficient | `jet_id:coef` |
| `highestLift` | Jet with highest lift coefficient | `jet_id:coef` |
| `smallestLift` | Jet with smallest lift coefficient | `jet_id:coef` |
| `firstLanding` | Jet that landed first | `jet_id:airport:HH:MM:SS` |
| `lastLanding` | Jet that landed last | `jet_id:airport:HH:MM:SS` |

---

## ✈️ Flight Phase Detection

Pandora detects three flight phases using a **yaw-based plateau algorithm**:

```
┌──────────┬────────────────────────────────────┬──────────┐
│ Take-Off │              Cruise                │ Landing  │
│ (turbulent yaw)   (stable yaw plateau)   (turbulent yaw)│
└──────────┴────────────────────────────────────┴──────────┘
```

### Algorithm

1. **Compute yaw deltas**: `|yaw[i] - yaw[i-1]|` for each consecutive pair
2. **Identify valid plateau indexes**: `delta < 1`, `yaw ≠ -1`, preceded by ≥ 10 turbulent points (`delta > 1`)
3. **Group consecutive valid indexes** into raw plateaus
4. **Filter**: keep only plateaus lasting ≥ 60 seconds
5. **Assign phases**:
   - **Take-off** = before the first valid plateau
   - **Cruise** = first valid plateau start → last valid plateau end
   - **Landing** = after the last valid plateau

---

## 🏗️ Architecture

```
Main
 └─ CLIParser              ← parses command-line arguments
     └─ Pandora             ← orchestrator (dispatch logic)
         ├─ FlightRecordParser  ← reads .frd files
         │   └─ FlightRecord    ← data model (metadata + CSV rows)
         ├─ FeatureComputer     ← dispatches to stat/computed/phase features
         │   ├─ Statistics       ← avg(), max(), min(), format()
         │   └─ ComputedFeatures ← duration, distance, acceleration, Mach
         ├─ FlightPhaseDetector  ← yaw plateau algorithm
         ├─ CrossFlightComputer  ← multi-file comparison features
         ├─ UnitConverter        ← metric ↔ imperial conversions
         └─ ReportGenerator      ← full report formatting with alignment
```

### Design Principles

- **Single Responsibility**: Each class has one clear purpose
- **Strategy Pattern**: `FeatureComputer` dispatches to appropriate computation method
- **Suffix Pattern Matching**: Phase-specific features decompose automatically (e.g., `avgAltTakeOff` → `avgAlt` on take-off sub-record)
- **Sub-record Extraction**: `FlightRecord.subRecord(start, end)` creates filtered views for phase-based computation

---

## 📁 Project Structure

```
TDD_Project/
├── pom.xml                           # Maven build configuration
├── manifest.json                     # Feature manifest (52 features)
├── CHANGELOG.md                      # Keep a Changelog format
├── README.md                         # This file
├── .gitignore
│
├── src/
│   ├── main/java/com/music/pandora/
│   │   ├── Main.java                 # Entry point
│   │   ├── CLIParser.java            # CLI argument parser
│   │   ├── Pandora.java              # Main orchestrator
│   │   ├── FlightRecord.java         # Data model
│   │   ├── FlightRecordParser.java   # .frd file parser
│   │   ├── FeatureComputer.java      # Feature dispatch (stat + computed + phase)
│   │   ├── Statistics.java           # avg, max, min, format utilities
│   │   ├── ComputedFeatures.java     # Duration, distance, acceleration, Mach
│   │   ├── FlightPhaseDetector.java  # Yaw-based phase detection
│   │   ├── CrossFlightComputer.java  # Multi-file comparison features
│   │   ├── UnitConverter.java        # Metric ↔ Imperial conversion
│   │   └── ReportGenerator.java      # Full report formatting
│   │
│   └── test/java/com/music/pandora/
│       ├── CLIParserTest.java         # 15 tests
│       ├── PandoraTest.java           # 14 tests
│       ├── FlightRecordParserTest.java# 23 tests
│       ├── StatisticsTest.java        # 11 tests
│       ├── FeatureComputerTest.java   # 20 tests
│       ├── ComputedFeaturesTest.java  # 10 tests
│       ├── FlightPhaseDetectorTest.java # 10 tests
│       ├── UnitConverterTest.java     # 17 tests
│       ├── ReportGeneratorTest.java   # 7 tests
│       └── CrossFlightComputerTest.java # 15 tests
│
├── test/
│   ├── resources/
│   │   ├── 0_201_MiG-23MLD.frd       # Reference flight record (684 data points)
│   │   ├── 0_101_F-14A.frd           # Additional reference file
│   │   ├── simple_test.frd           # Hand-crafted test data (5 rows)
│   │   └── phase_test.frd            # Phase detection test data (84 rows)
│   └── testSuite.json                # Integration test suite
│
└── TDD_Sujet/                        # Project specification documents
```

---

## 🧪 Unit Tests

| Test Suite | Tests | Coverage |
|------------|-------|----------|
| `CLIParserTest` | 15 | All CLI options, short/long forms, edge cases |
| `PandoraTest` | 14 | Version, help, metadata, parameters, filenames, stat features |
| `FlightRecordParserTest` | 23 | Metadata, CSV, multi-file, directories, errors |
| `StatisticsTest` | 11 | avg, max, min, sum, format, edge cases |
| `FeatureComputerTest` | 20 | All 21 statistical features with pre-computed values |
| `ComputedFeaturesTest` | 10 | Duration, Haversine, acceleration, Mach |
| `FlightPhaseDetectorTest` | 10 | Phase detection, ordering, contiguity, edge cases |
| `UnitConverterTest` | 17 | All metric→imperial conversions, unit labels |
| `ReportGeneratorTest` | 7 | Report format, alphabetical order, units |
| `CrossFlightComputerTest` | 15 | All cross-flight features, static detection |
| **Total** | **142** | |

Run all tests:
```bash
mvn test
```

---

## 📝 Unit Conversion Reference

| Quantity | Metric | Imperial | Factor |
|----------|--------|----------|--------|
| Altitude / Distance | m | ft | × 3.281 |
| Speed | m/s | ft/s | × 3.281 |
| Power | W | hp | ÷ 754.7 |
| Temperature | ℃ | K | + 273.15 |
| Pressure | Pa | psi | ÷ 6894.76 |
| Acceleration | m/s² | ft/s² | × 3.281 |

Units that don't change: `%` (humidity, oxygen), `bpm` (heart rate), `Mach`, `G`.

---

## 📄 Changelog

See [CHANGELOG.md](CHANGELOG.md) for the full list of changes.

---

## 📜 License

This project is developed as part of a TDD academic exercise.

---

*Built with ☕ Java 17 and Maven • 142 tests • 52 features*
