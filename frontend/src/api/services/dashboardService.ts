import { api } from '@/lib/api/client';

export type HrOverviewResponse = Record<string, unknown>;
export type BenchMetricsResponse = Record<string, unknown>;

export function fetchHrOverview(): Promise<HrOverviewResponse> {
  return api.get('/api/dashboard/hr-overview');
}

export function fetchBenchMetrics(): Promise<BenchMetricsResponse> {
  return api.get('/api/dashboard/bench');
}
