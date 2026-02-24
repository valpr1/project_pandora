package com.music.pandora.api.service;

import com.music.pandora.FeatureComputer;
import com.music.pandora.FlightRecord;
import com.music.pandora.api.dto.FlightKpis;
import org.springframework.stereotype.Service;

@Service
public class FeatureService {

    public FlightKpis extractKpis(FlightRecord record) {
        FeatureComputer computer = new FeatureComputer(record);
        FlightKpis kpis = new FlightKpis();
        kpis.setAvgAlt(computer.compute("avgAlt"));
        kpis.setAvgMachSpeed(computer.compute("avgMachSpeed"));
        kpis.setFlightDuration(computer.compute("flightDuration"));
        kpis.setMaxAccelG(computer.compute("maxAccelG"));
        return kpis;
    }
}
