import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Loader2 } from 'lucide-react';
import { flightApi } from '../api/flightApi';
import type { Flight } from '../types/api';
import SynchronizedTimeSeries from '../components/charts/SynchronizedTimeSeries';

export default function FlightDetail() {
    const { filename } = useParams<{ filename: string }>();
    const navigate = useNavigate();
    const [flight, setFlight] = useState<Flight | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        if (!filename) return;

        flightApi.getFlight(filename)
            .then(setFlight)
            .catch(console.error)
            .finally(() => setIsLoading(false));

    }, [filename]);

    if (isLoading) {
        return (
            <div className="flex h-[50vh] items-center justify-center">
                <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
            </div>
        );
    }

    if (!flight) {
        return (
            <div className="max-w-7xl mx-auto px-4 text-center mt-12">
                <h2 className="text-2xl font-bold text-slate-900">Vol introuvable</h2>
                <button onClick={() => navigate('/')} className="mt-4 text-blue-600 hover:underline">
                    Retour au tableau de bord
                </button>
            </div>
        );
    }

    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="mb-8 flex items-center gap-4">
                <button
                    onClick={() => navigate('/')}
                    className="p-2 -ml-2 rounded-full hover:bg-slate-200 transition-colors text-slate-600"
                >
                    <ArrowLeft className="w-6 h-6" />
                </button>
                <div>
                    <h1 className="text-3xl font-bold text-slate-900 tracking-tight">{flight.filename}</h1>
                    <p className="text-sm text-slate-500 mt-1">
                        {flight.metadata['origin']} • {flight.metadata['date']}
                    </p>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
                {/* Sidebar */}
                <div className="lg:col-span-1 space-y-6">
                    <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
                        <h3 className="text-sm font-bold uppercase tracking-wider text-slate-500 mb-4 border-b pb-2">Métadonnées</h3>
                        <dl className="space-y-3">
                            {Object.entries(flight.metadata).map(([key, val]) => (
                                <div key={key}>
                                    <dt className="text-xs text-slate-500 capitalize">{key}</dt>
                                    <dd className="text-sm font-medium text-slate-900">{val || '-'}</dd>
                                </div>
                            ))}
                        </dl>
                    </div>
                </div>

                {/* Main Content (Charts Placeholder for now) */}
                <div className="lg:col-span-3 space-y-6">

                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-5">
                            <span className="text-xs font-medium text-slate-500">Durée</span>
                            <p className="mt-2 text-2xl font-bold text-slate-900">{flight.kpis.flightDuration}</p>
                        </div>
                        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-5">
                            <span className="text-xs font-medium text-slate-500">Mach Max (avg)</span>
                            <p className="mt-2 text-2xl font-bold text-slate-900">{flight.kpis.avgMachSpeed}</p>
                        </div>
                        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-5">
                            <span className="text-xs font-medium text-slate-500">Alt Moyenne</span>
                            <p className="mt-2 text-2xl font-bold text-slate-900">{flight.kpis.avgAlt}</p>
                        </div>
                        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-5">
                            <span className="text-xs font-medium text-slate-500">Accél. G Max</span>
                            <p className="mt-2 text-2xl font-bold text-slate-900">{flight.kpis.maxAccelG}</p>
                        </div>
                    </div>

                    <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 min-h-[400px]">
                        <SynchronizedTimeSeries filename={flight.filename} />
                    </div>
                </div>
            </div>
        </div>
    );
}
