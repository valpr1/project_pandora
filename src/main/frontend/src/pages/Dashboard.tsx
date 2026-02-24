import { useState, useCallback, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Upload, FolderUp, Activity, AlertCircle, RefreshCw } from 'lucide-react';
import { flightApi } from '../api/flightApi';
import type { Flight } from '../types/api';

export default function Dashboard() {
    const [flights, setFlights] = useState<Flight[]>([]);
    const [isDragging, setIsDragging] = useState(false);
    const [isUploading, setIsUploading] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const navigate = useNavigate();

    const fetchFlights = async () => {
        try {
            setIsLoading(true);
            const data = await flightApi.getAllFlights();
            setFlights(data);
            setError(null);
        } catch (err) {
            setError('Impossible de se connecter au serveur local Pandora.');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchFlights();
    }, []);

    const handleDrag = useCallback((e: React.DragEvent) => {
        e.preventDefault();
        e.stopPropagation();
        if (e.type === 'dragenter' || e.type === 'dragover') {
            setIsDragging(true);
        } else if (e.type === 'dragleave') {
            setIsDragging(false);
        }
    }, []);

    const handleDrop = useCallback(async (e: React.DragEvent) => {
        e.preventDefault();
        e.stopPropagation();
        setIsDragging(false);

        const files = Array.from(e.dataTransfer.files).filter(f => f.name.endsWith('.frd'));
        if (files.length === 0) return;

        await uploadFiles(files);
    }, []);

    const handleFileInput = async (e: React.ChangeEvent<HTMLInputElement>) => {
        if (!e.target.files?.length) return;
        const files = Array.from(e.target.files).filter(f => f.name.endsWith('.frd'));
        if (files.length > 0) {
            await uploadFiles(files);
        }
    };

    const uploadFiles = async (files: File[]) => {
        try {
            setIsUploading(true);
            setError(null);
            await flightApi.uploadFiles(files);
            await fetchFlights();
        } catch (err) {
            setError('Erreur lors du traitement des fichiers .frd');
        } finally {
            setIsUploading(false);
        }
    };

    const clearSession = async () => {
        try {
            await flightApi.clearFlights();
            setFlights([]);
        } catch (err) {
            console.error(err);
        }
    };

    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">

            <div className="mb-8 flex justify-between items-end">
                <div>
                    <h1 className="text-3xl font-bold text-slate-900 tracking-tight">Vols analysés</h1>
                    <p className="text-sm text-slate-500 mt-1">Session In-Memory Temporaire</p>
                </div>

                {flights.length > 0 && (
                    <button
                        onClick={clearSession}
                        className="text-sm text-red-600 hover:text-red-700 font-medium px-4 py-2 rounded-lg hover:bg-red-50 transition-colors"
                    >
                        Purger la session
                    </button>
                )}
            </div>

            {error && (
                <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl flex items-center shadow-sm">
                    <AlertCircle className="w-5 h-5 mr-3 flex-shrink-0" />
                    <p className="text-sm font-medium">{error}</p>
                </div>
            )}

            {/* Zone de Drag & Drop */}
            <div
                onDragEnter={handleDrag}
                onDragLeave={handleDrag}
                onDragOver={handleDrag}
                onDrop={handleDrop}
                className={`relative mb-12 rounded-2xl border-2 border-dashed transition-all duration-200 ease-in-out
          ${isDragging
                        ? 'border-blue-500 bg-blue-50/50 shadow-inner'
                        : 'border-slate-300 hover:border-slate-400 bg-white hover:bg-slate-50/50'
                    }
          ${isUploading ? 'opacity-50 pointer-events-none' : ''}
        `}
            >
                <div className="absolute inset-x-0 -top-px h-px bg-gradient-to-r from-transparent via-blue-500/10 to-transparent"></div>
                <div className="absolute inset-x-0 -bottom-px h-px bg-gradient-to-r from-transparent via-blue-500/10 to-transparent"></div>

                <div className="px-6 py-16 text-center">
                    <div className="mx-auto w-16 h-16 mb-4 rounded-full bg-blue-50 flex items-center justify-center">
                        {isUploading ? (
                            <RefreshCw className="w-8 h-8 text-blue-600 animate-spin" />
                        ) : (
                            <Upload className="w-8 h-8 text-blue-600" />
                        )}
                    </div>

                    <h3 className="mt-2 text-lg font-semibold text-slate-900">
                        {isUploading ? 'Analyse en cours...' : 'Import de métrologie'}
                    </h3>
                    <p className="mt-2 text-sm text-slate-500 mb-6 max-w-sm mx-auto">
                        Glissez vos fichiers <code className="bg-slate-100 px-2 py-0.5 rounded text-slate-700">.frd</code> ici, ou cliquez pour parcourir.
                    </p>

                    <div className="flex justify-center gap-4">
                        <label className="cursor-pointer inline-flex items-center px-6 py-3 border border-transparent text-sm font-medium rounded-xl shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all">
                            <FolderUp className="w-4 h-4 mr-2" />
                            Parcourir les fichiers
                            <input type="file" multiple accept=".frd" className="hidden" onChange={handleFileInput} />
                        </label>
                    </div>
                </div>
            </div>

            {/* Liste des vols */}
            {!isLoading && flights.length > 0 && (
                <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
                    {flights.map((flight) => (
                        <div
                            key={flight.filename}
                            onClick={() => navigate(`/flight/${encodeURIComponent(flight.filename)}`)}
                            className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden hover:shadow-md hover:border-blue-300 transition-all cursor-pointer group"
                        >
                            <div className="p-6">
                                <div className="flex items-center justify-between mb-4">
                                    <div className="bg-blue-50 p-2 rounded-lg group-hover:bg-blue-100 transition-colors">
                                        <Activity className="w-5 h-5 text-blue-600" />
                                    </div>
                                    <span className="inline-flex items-center rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-medium text-slate-800">
                                        {flight.metadata?.['flight code'] || 'Unknown'}
                                    </span>
                                </div>

                                <h3 className="text-lg font-semibold text-slate-900 truncate mb-1">
                                    {flight.filename}
                                </h3>
                                <p className="text-sm text-slate-500 mb-4 truncate">
                                    {flight.metadata?.['origin'] || ''} • {flight.metadata?.['pilot'] || ''}
                                </p>

                                <dl className="mt-4 grid grid-cols-2 gap-4 border-t border-slate-100 pt-4">
                                    <div>
                                        <dt className="text-xs font-medium text-slate-500">Duration</dt>
                                        <dd className="mt-1 text-sm font-semibold text-slate-900">{flight.kpis.flightDuration || '-'}</dd>
                                    </div>
                                    <div>
                                        <dt className="text-xs font-medium text-slate-500">Max G</dt>
                                        <dd className="mt-1 text-sm font-semibold text-slate-900">{flight.kpis.maxAccelG || '-'}</dd>
                                    </div>
                                </dl>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {!isLoading && flights.length === 0 && (
                <div className="text-center py-12 bg-white rounded-2xl border border-dashed border-slate-200">
                    <Activity className="mx-auto h-12 w-12 text-slate-300" />
                    <h3 className="mt-2 text-sm font-medium text-slate-900">Aucun vol en session</h3>
                    <p className="mt-1 text-sm text-slate-500">Commencez par importer un fichier .frd pour l'analyser.</p>
                </div>
            )}

        </div>
    );
}
