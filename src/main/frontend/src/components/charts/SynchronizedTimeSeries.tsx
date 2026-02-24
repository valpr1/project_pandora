import { useEffect, useState } from 'react';
import ReactECharts from 'echarts-for-react';
import { flightApi } from '../../api/flightApi';
import { Loader2 } from 'lucide-react';

interface Props {
    filename: string;
}

export default function SynchronizedTimeSeries({ filename }: Props) {
    const [loading, setLoading] = useState(true);
    const [chartOptions, setChartOptions] = useState<any>(null);

    useEffect(() => {
        let active = true;

        async function fetchData() {
            try {
                setLoading(true);
                // On récupère uniquement les colonnes essentielles pour le rendu
                const tsReq = await flightApi.getTimeseries(
                    filename,
                    ['timestamp', 'altitude', 'air_speed'],
                    5 // Downsampling drastique pour garder < 10k points par série (évite DOM lag)
                );

                if (!active) return;

                const data = tsReq.data;
                if (!data.timestamp) {
                    setLoading(false);
                    return;
                }

                const options = {
                    tooltip: {
                        trigger: 'axis',
                        axisPointer: { type: 'cross' }
                    },
                    legend: {
                        data: ['Altitude', 'Air Speed'],
                        bottom: 0
                    },
                    grid: {
                        left: '3%',
                        right: '4%',
                        bottom: '10%',
                        containLabel: true
                    },
                    toolbox: {
                        feature: {
                            dataZoom: { yAxisIndex: 'none' },
                            restore: {},
                            saveAsImage: {}
                        }
                    },
                    dataZoom: [
                        { type: 'inside', start: 0, end: 100 },
                        { type: 'slider', start: 0, end: 100 }
                    ],
                    xAxis: {
                        type: 'category',
                        data: data.timestamp.map(Number),
                        boundaryGap: false,
                        axisLabel: {
                            formatter: (value: string) => {
                                const sec = parseInt(value, 10);
                                const h = Math.floor(sec / 3600);
                                const m = Math.floor((sec % 3600) / 60);
                                return `${h}h${m.toString().padStart(2, '0')}`;
                            }
                        }
                    },
                    yAxis: [
                        {
                            type: 'value',
                            name: 'Altitude',
                            position: 'left',
                            axisLine: { show: true, lineStyle: { color: '#3b82f6' } },
                            axisLabel: { formatter: '{value}' }
                        },
                        {
                            type: 'value',
                            name: 'Vitesse',
                            position: 'right',
                            axisLine: { show: true, lineStyle: { color: '#ef4444' } },
                            axisLabel: { formatter: '{value}' }
                        }
                    ],
                    series: [] as any[]
                };

                if (data.altitude) {
                    options.series.push({
                        name: 'Altitude',
                        type: 'line',
                        data: data.altitude,
                        yAxisIndex: 0,
                        showSymbol: false,
                        lineStyle: { width: 1.5, color: '#3b82f6' },
                        itemStyle: { color: '#3b82f6' }
                    });
                }

                if (data.air_speed) {
                    options.series.push({
                        name: 'Air Speed',
                        type: 'line',
                        data: data.air_speed,
                        yAxisIndex: 1,
                        showSymbol: false,
                        lineStyle: { width: 1.5, color: '#ef4444' },
                        itemStyle: { color: '#ef4444' }
                    });
                }

                setChartOptions(options);

            } catch (err) {
                console.error('Failed to load chart data', err);
            } finally {
                if (active) setLoading(false);
            }
        }

        fetchData();
        return () => { active = false; };
    }, [filename]);

    if (loading) {
        return (
            <div className="h-full w-full flex items-center justify-center min-h-[400px]">
                <Loader2 className="w-8 h-8 text-blue-500 animate-spin" />
            </div>
        );
    }

    if (!chartOptions) {
        return (
            <div className="h-full w-full flex items-center justify-center min-h-[400px] text-slate-500">
                Impossible de charger le graphique temporel.
            </div>
        )
    }

    return (
        <ReactECharts
            option={chartOptions}
            style={{ height: '500px', width: '100%' }}
            opts={{ renderer: 'canvas' }}
        />
    );
}
