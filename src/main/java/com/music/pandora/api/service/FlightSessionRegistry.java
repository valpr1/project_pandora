package com.music.pandora.api.service;

import com.music.pandora.FlightRecord;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registre en mémoire (In-Memory) conservant les sessions de vols pendant
 * l'exécution locale de l'application Desktop.
 */
@Component
public class FlightSessionRegistry {

    private final Map<String, FlightRecord> activeSessions = new ConcurrentHashMap<>();

    public void addSession(FlightRecord record) {
        if (record != null && record.getFilename() != null) {
            activeSessions.put(record.getFilename(), record);
        }
    }

    public FlightRecord getRecord(String filename) {
        return activeSessions.get(filename);
    }

    public Collection<FlightRecord> getAllRecords() {
        return activeSessions.values();
    }

    public void clearSessions() {
        activeSessions.clear();
    }

    public void removeSession(String filename) {
        activeSessions.remove(filename);
    }
}
