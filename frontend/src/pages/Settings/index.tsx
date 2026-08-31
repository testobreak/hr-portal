import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { useMe } from '@/hooks/useMe';
import { apiBase } from '@/api/apiConfig';

export function SettingsPage() {
  const { data: me } = useMe();
  const rows = [
    ['API base', apiBase],
    ['Current user', me?.email ?? me?.username ?? '-'],
    ['Roles', (me?.roles ?? []).join(', ') || '-'],
    ['Auth mode', 'Custom Database JWT'],
    ['Storage', 'Presigned document workflow'],
    ['Default timezone', 'Asia/Kolkata'],
  ];
  return <div className="space-y-4"><h1 className="text-2xl font-semibold">Settings</h1><Card><CardHeader><CardTitle>Runtime configuration</CardTitle></CardHeader><CardContent><dl className="grid gap-3 text-sm sm:grid-cols-2">{rows.map(([k, v]) => <div key={k} className="rounded-md border border-border p-3"><dt className="text-muted-foreground">{k}</dt><dd className="mt-1 font-medium break-words">{v}</dd></div>)}</dl></CardContent></Card></div>;
}
