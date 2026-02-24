package com.music.pandora.api.dto;

import java.util.Map;

public class TimeseriesDTO {

    private Map<String, double[]> data;

    public TimeseriesDTO() {
    }

    public TimeseriesDTO(Map<String, double[]> data) {
        this.data = data;
    }

    public Map<String, double[]> getData() {
        return data;
    }

    public void setData(Map<String, double[]> data) {
        this.data = data;
    }
}
