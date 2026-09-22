import { useQuery } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { fetchBenchMetrics, fetchHrOverview } from '@/api/services/dashboardService';
import { useMe } from '@/hooks/useMe';
import { Roles, hasAnyRole } from '@/lib/roles';
import { Activity, BriefcaseBusiness, TrendingUp, Users } from 'lucide-react';

function MetricCard({
  label,
  value,
  hint,
  icon: Icon,
}: {
  label: string;
  value: string;
  hint: string;
  icon: typeof Users;
}) {
  return (
    <Card>
      <CardHeader className="pb-2">
        <CardDescription className="flex items-center gap-2">
          <Icon className="h-4 w-4" />
          {label}
        </CardDescription>
      </CardHeader>
      <CardContent>
        <p className="text-3xl font-semibold tracking-tight">{value}</p>
        <p className="mt-1 text-xs text-muted-foreground">{hint}</p>
      </CardContent>
    </Card>
  );
}

function toNumber(value: unknown): number {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value;
  }
  if (typeof value === 'string') {
    const n = Number(value);
    return Number.isFinite(n) ? n : 0;
  }
  return 0;
}

export function DashboardPage() {
  const { data: me, isLoading: meLoading } = useMe();
  const roles = me?.roles ?? [];

  const hrEnabled = hasAnyRole(roles, [
    Roles.SUPER_ADMIN,
    Roles.HR_ADMIN,
    Roles.LEADERSHIP,
  ]);
  const benchEnabled = hasAnyRole(roles, [
    Roles.SUPER_ADMIN,
    Roles.FINANCE_ADMIN,
    Roles.LEADERSHIP,
    Roles.PROJECT_MANAGER,
  ]);

  const hr = useQuery({
    queryKey: ['dashboard', 'hr-overview'],
    queryFn: fetchHrOverview,
    enabled: hrEnabled,
  });

  const bench = useQuery({
    queryKey: ['dashboard', 'bench'],
    queryFn: fetchBenchMetrics,
    enabled: benchEnabled,
  });

  if (meLoading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-8 w-64" />
        <Skeleton className="h-40 w-full" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-2xl font-semibold tracking-tight">Dashboard</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Signed in as {me?.username ?? me?.email ?? 'user'}.
        </p>
      </header>

      {hrEnabled && (
        <section className="space-y-3">
          <h2 className="text-sm font-medium uppercase tracking-wide text-muted-foreground">HR overview</h2>
          {hr.isLoading && <Skeleton className="h-28 w-full" />}
          {hr.isError && <p className="text-sm text-red-600">Unable to load HR overview.</p>}
          {hr.data != null && (
            <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
              <MetricCard
                label="Headcount"
                value={String(toNumber(hr.data.headcountActive ?? hr.data.headcount))}
                hint="Active employees"
                icon={Users}
              />
              <MetricCard
                label="Joiners (30d)"
                value={String(toNumber(hr.data.joinersLast30Days))}
                hint="Recent additions"
                icon={TrendingUp}
              />
              <MetricCard
                label="Leavers (30d)"
                value={String(toNumber(hr.data.leaversLast30Days))}
                hint="Recent exits"
                icon={BriefcaseBusiness}
              />
              <MetricCard
                label="Attrition (12m)"
                value={`${toNumber((hr.data as Record<string, unknown>).attrition12mPct ?? 0)}%`}
                hint="Rolling trend"
                icon={Activity}
              />
            </div>
          )}
        </section>
      )}

      {benchEnabled && (
        <section className="space-y-3">
          <h2 className="text-sm font-medium uppercase tracking-wide text-muted-foreground">Allocation health</h2>
          {bench.isLoading && <Skeleton className="h-28 w-full" />}
          {bench.isError && <p className="text-sm text-red-600">Unable to load bench metrics.</p>}
          {bench.data != null && (
            <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
              <MetricCard
                label="Utilization"
                value={`${bench.data.utilizationPct !== undefined ? toNumber(bench.data.utilizationPct) : Math.max(0, Math.round(100 - toNumber(bench.data.benchPercentage)))}%`}
                hint="Active project coverage"
                icon={TrendingUp}
              />
              <MetricCard
                label="Bench"
                value={`${toNumber(bench.data.benchPercentage ?? bench.data.benchPct)}%`}
                hint={`${toNumber(bench.data.underutilizedOrBenchedCount)} unallocated`}
                icon={Users}
              />
              <MetricCard
                label="Fully Allocated"
                value={String(toNumber(bench.data.fullyUtilizedCount))}
                hint="100% project staffed"
                icon={Activity}
              />
              <MetricCard
                label="Active Roster"
                value={String(toNumber(bench.data.activeRosterSize))}
                hint="Total staff pool"
                icon={Users}
              />
            </div>
          )}
        </section>
      )}

      {!hrEnabled && !benchEnabled && (
        <Card>
          <CardHeader>
            <CardTitle>Welcome</CardTitle>
            <CardDescription>
              Your role does not include dashboard metrics. Use the sidebar to open available
              modules.
            </CardDescription>
          </CardHeader>
        </Card>
      )}
    </div>
  );
}
