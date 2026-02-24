import axios from 'axios';
import type { Flight, TimeseriesData } from '../types/api';

const api = axios.create({
    baseURL: '/api',
});

export const flightApi = {
    uploadFiles: async (files: FileList | File[]): Promise<Flight[]> => {
        const formData = new FormData();
        Array.from(files).forEach((file) => {
            formData.append('file', file);
        });
        const response = await api.post<Flight[]>('/flights/upload', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        return response.data;
    },

    getAllFlights: async (): Promise<Flight[]> => {
        const response = await api.get<Flight[]>('/flights');
        return response.data;
    },

    getFlight: async (filename: string): Promise<Flight> => {
        const response = await api.get<Flight>(`/flights/${encodeURIComponent(filename)}`);
        return response.data;
    },

    getTimeseries: async (
        filename: string,
        parameters: string[],
        downsampleFactor: number = 1
    ): Promise<TimeseriesData> => {
        const params = new URLSearchParams();
        params.append('downsampleFactor', downsampleFactor.toString());
        parameters.forEach((p) => params.append('parameters', p));

        const response = await api.get<TimeseriesData>(
            `/flights/${encodeURIComponent(filename)}/timeseries`,
            { params }
        );
        return response.data;
    },

    clearFlights: async (): Promise<void> => {
        await api.delete('/flights');
    },
};
