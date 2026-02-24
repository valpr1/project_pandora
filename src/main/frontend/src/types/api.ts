export interface FlightMetadata {
    [key: string]: string;
}

export interface FlightKpis {
    avgMachSpeed?: string;
    avgAlt?: string;
    flightDuration?: string;
    maxAccelG?: string;
}

export interface Flight {
    filename: string;
    metadata: FlightMetadata;
    kpis: FlightKpis;
}

export interface TimeseriesData {
    data: Record<string, number[]>;
}
