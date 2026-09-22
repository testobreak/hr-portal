import { api } from '@/lib/api/client';

export type HrOverviewResponse = {
  headcountActive: number;
  joinersLast30Days: number;
  leaversLast30Days: number;
  headcount?: number;
};

export type BenchMetricsResponse = {
  activeRosterSize: number;
  fullyUtilizedCount: number;
  underutilizedOrBenchedCount: number;
  benchPercentage: number;
  benchPct?: number;
  utilizationPct?: number;
};

export function fetchHrOverview(): Promise<HrOverviewResponse> {
  return api.get<HrOverviewResponse>('/api/dashboard/hr-overview');
}

export function fetchBenchMetrics(): Promise<BenchMetricsResponse> {
  return api.get<BenchMetricsResponse>('/api/dashboard/bench');
}
