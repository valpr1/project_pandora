package com.music.pandora.api.dto;

public class FlightKpis {
    private String avgMachSpeed;
    private String avgAlt;
    private String flightDuration;
    private String maxAccelG;

    public String getAvgMachSpeed() {
        return avgMachSpeed;
    }

    public void setAvgMachSpeed(String avgMachSpeed) {
        this.avgMachSpeed = avgMachSpeed;
    }

    public String getAvgAlt() {
        return avgAlt;
    }

    public void setAvgAlt(String avgAlt) {
        this.avgAlt = avgAlt;
    }

    public String getFlightDuration() {
        return flightDuration;
    }

    public void setFlightDuration(String flightDuration) {
        this.flightDuration = flightDuration;
    }

    public String getMaxAccelG() {
        return maxAccelG;
    }

    public void setMaxAccelG(String maxAccelG) {
        this.maxAccelG = maxAccelG;
    }
}
