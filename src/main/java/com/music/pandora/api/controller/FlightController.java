package com.music.pandora.api.controller;

import com.music.pandora.FlightRecord;
import com.music.pandora.api.dto.FlightDTO;
import com.music.pandora.api.dto.TimeseriesDTO;
import com.music.pandora.api.service.FeatureService;
import com.music.pandora.api.service.FlightProcessingService;
import com.music.pandora.api.service.FlightSessionRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/flights")
@CrossOrigin(origins = "*") // Autorise le developpement frontend local
public class FlightController {

    private final FlightProcessingService processingService;
    private final FlightSessionRegistry registry;
    private final FeatureService featureService;

    public FlightController(FlightProcessingService processingService,
            FlightSessionRegistry registry,
            FeatureService featureService) {
        this.processingService = processingService;
        this.registry = registry;
        this.featureService = featureService;
    }

    @PostMapping("/upload")
    public ResponseEntity<List<FlightDTO>> uploadFiles(@RequestParam("file") MultipartFile[] files) {
        try {
            List<FlightRecord> records = processingService.processUpload(files);
            List<FlightDTO> dtos = records.stream()
                    .map(rec -> new FlightDTO(rec.getFilename(), rec.getMetadata(), featureService.extractKpis(rec)))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<FlightDTO>> getAllFlights() {
        List<FlightDTO> dtos = registry.getAllRecords().stream()
                .map(rec -> new FlightDTO(rec.getFilename(), rec.getMetadata(), featureService.extractKpis(rec)))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{filename}")
    public ResponseEntity<FlightDTO> getFlight(@PathVariable String filename) {
        FlightRecord record = registry.getRecord(filename);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity
                .ok(new FlightDTO(record.getFilename(), record.getMetadata(), featureService.extractKpis(record)));
    }

    @GetMapping("/{filename}/timeseries")
    public ResponseEntity<TimeseriesDTO> getTimeseries(
            @PathVariable String filename,
            @RequestParam(required = false, defaultValue = "1") int downsampleFactor,
            @RequestParam List<String> parameters) {

        FlightRecord record = registry.getRecord(filename);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, double[]> resultData = new HashMap<>();

        for (String param : parameters) {
            if ("acceleration".equals(param)) {
                // Feature specifique qui demande un calcul multi-lignes
                continue;
            } else if (!record.hasColumn(param)) {
                continue;
            }

            double[] rawValues = record.getColumnValues(param);

            if (downsampleFactor <= 1) {
                resultData.put(param, rawValues);
            } else {
                // Basic Downsampling par saut de valeur : très efficace In-Memory
                int newSize = (int) Math.ceil((double) rawValues.length / downsampleFactor);
                double[] downsampled = new double[newSize];
                for (int i = 0, j = 0; i < rawValues.length && j < newSize; i += downsampleFactor, j++) {
                    downsampled[j] = rawValues[i];
                }
                resultData.put(param, downsampled);
            }
        }

        return ResponseEntity.ok(new TimeseriesDTO(resultData));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearFlights() {
        registry.clearSessions();
        return ResponseEntity.ok().build();
    }
}
