import { useEffect, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { masterDataApi, type Location } from '@/api/services/masterDataService';
import { Roles, hasAnyRole } from '@/lib/roles';
import { useMe } from '@/hooks/useMe';

export function LocationsPage() {
  const qc = useQueryClient();
  const { data: me } = useMe();
  const canWrite = hasAnyRole(me?.roles ?? [], [Roles.SUPER_ADMIN, Roles.HR_ADMIN]);
  const [page, setPage] = useState(0);
  const [q, setQ] = useState('');
  const [search, setSearch] = useState('');
  const [selected, setSelected] = useState<Location | null>(null);
  const [form, setForm] = useState({ code: '', name: '', city: '', country: '' });
  useEffect(() => { const t = setTimeout(() => { setPage(0); setQ(search); }, 300); return () => clearTimeout(t); }, [search]);
  const list = useQuery({ queryKey: ['locations', page, q], queryFn: () => masterDataApi.listLocations(page, 10, q) });
  const create = useMutation({ mutationFn: () => masterDataApi.createLocation(form), onSuccess: () => { setForm({ code: '', name: '', city: '', country: '' }); qc.invalidateQueries({ queryKey: ['locations'] }); } });
  const update = useMutation({ mutationFn: () => selected ? masterDataApi.updateLocation(selected.id, { name: form.name, city: form.city, country: form.country, version: selected.version }) : Promise.reject(), onSuccess: () => { qc.invalidateQueries({ queryKey: ['locations'] }); setSelected(null); } });
  const remove = useMutation({ mutationFn: (id: string) => masterDataApi.deleteLocation(id), onSuccess: () => { qc.invalidateQueries({ queryKey: ['locations'] }); setSelected(null); } });
  const pick = (l: Location) => { setSelected(l); setForm({ code: l.code, name: l.name, city: l.city ?? '', country: l.country ?? '' }); };

  return <div className="space-y-4">
    <h1 className="text-2xl font-semibold">Locations</h1>
    <Card><CardContent className="pt-6"><input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search locations..." className="h-9 w-full rounded-md border border-border px-3" /></CardContent></Card>
    <div className="grid gap-4 xl:grid-cols-[2fr_1fr]">
      <Card><CardContent className="pt-6 overflow-x-auto"><table className="w-full text-sm"><thead><tr><th className="text-left py-2">Code</th><th className="text-left py-2">Name</th><th className="text-left py-2">City</th><th className="text-left py-2">Country</th></tr></thead><tbody>{(list.data?.content ?? []).map((l) => <tr key={l.id} onClick={() => pick(l)} className="cursor-pointer border-t hover:bg-muted/30"><td className="py-2">{l.code}</td><td>{l.name}</td><td>{l.city ?? '-'}</td><td>{l.country ?? '-'}</td></tr>)}</tbody></table><div className="mt-3 flex gap-2"><Button size="sm" variant="outline" onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={list.data?.first ?? true}>Prev</Button><Button size="sm" variant="outline" onClick={() => setPage((p) => p + 1)} disabled={list.data?.last ?? true}>Next</Button></div></CardContent></Card>
      <Card><CardHeader><CardTitle>{selected ? 'Edit Location' : 'Create Location'}</CardTitle></CardHeader><CardContent className="space-y-2">
        <input value={form.code} onChange={(e) => setForm((f) => ({ ...f, code: e.target.value }))} placeholder="Code" disabled={!!selected} className="h-9 w-full rounded-md border border-border px-3 disabled:bg-muted/40" />
        <input value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} placeholder="Name" className="h-9 w-full rounded-md border border-border px-3" />
        <input value={form.city} onChange={(e) => setForm((f) => ({ ...f, city: e.target.value }))} placeholder="City" className="h-9 w-full rounded-md border border-border px-3" />
        <input value={form.country} onChange={(e) => setForm((f) => ({ ...f, country: e.target.value }))} placeholder="Country" className="h-9 w-full rounded-md border border-border px-3" />
        {canWrite && !selected && <Button onClick={() => create.mutate()} disabled={create.isPending || !form.code || !form.name}>Create</Button>}
        {canWrite && selected && <div className="flex gap-2"><Button onClick={() => update.mutate()} disabled={update.isPending || !form.name}>Save</Button><Button variant="outline" onClick={() => setSelected(null)}>Cancel</Button><Button variant="outline" onClick={() => remove.mutate(selected.id)}>Delete</Button></div>}
      </CardContent></Card>
    </div>
  </div>;
}
