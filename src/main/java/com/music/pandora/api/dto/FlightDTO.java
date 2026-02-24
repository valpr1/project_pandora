package com.music.pandora.api.dto;

import java.util.Map;

public class FlightDTO {

    private String filename;
    private Map<String, String> metadata;
    private FlightKpis kpis;

    public FlightDTO() {
    }

    public FlightDTO(String filename, Map<String, String> metadata, FlightKpis kpis) {
        this.filename = filename;
        this.metadata = metadata;
        this.kpis = kpis;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public FlightKpis getKpis() {
        return kpis;
    }

    public void setKpis(FlightKpis kpis) {
        this.kpis = kpis;
    }
}
