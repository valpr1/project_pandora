package com.music.pandora.api.service;

import com.music.pandora.FlightRecord;
import com.music.pandora.FlightRecordParser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FlightProcessingService {

    private final FlightSessionRegistry registry;
    private final FlightRecordParser parser;

    public FlightProcessingService(FlightSessionRegistry registry) {
        this.registry = registry;
        this.parser = new FlightRecordParser();
    }

    public List<FlightRecord> processUpload(MultipartFile[] files) throws IOException {
        List<FlightRecord> parsedRecords = new ArrayList<>();

        for (MultipartFile file : files) {
            // Création d'un fichier temporaire
            File tempFile = File.createTempFile("pandora_", "_" + file.getOriginalFilename());
            try {
                file.transferTo(tempFile);
                FlightRecord record = parser.parse(tempFile.getAbsolutePath());
                registry.addSession(record);
                parsedRecords.add(record);
            } finally {
                // Destruction immédiate pour éviter les fuites de données (Zero Persistance)
                tempFile.delete();
            }
        }

        return parsedRecords;
    }
}
