import { useEffect, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { masterDataApi, type Designation } from '@/api/services/masterDataService';
import { Roles, hasAnyRole } from '@/lib/roles';
import { useMe } from '@/hooks/useMe';

export function DesignationsPage() {
  const qc = useQueryClient();
  const { data: me } = useMe();
  const canWrite = hasAnyRole(me?.roles ?? [], [Roles.SUPER_ADMIN, Roles.HR_ADMIN]);
  const [page, setPage] = useState(0);
  const [q, setQ] = useState('');
  const [search, setSearch] = useState('');
  const [selected, setSelected] = useState<Designation | null>(null);
  const [form, setForm] = useState({ title: '', level: '', description: '' });
  useEffect(() => { const t = setTimeout(() => { setPage(0); setQ(search); }, 300); return () => clearTimeout(t); }, [search]);
  const list = useQuery({ queryKey: ['designations', page, q], queryFn: () => masterDataApi.listDesignations(page, 10, q) });
  const create = useMutation({ mutationFn: () => masterDataApi.createDesignation(form), onSuccess: () => { setForm({ title: '', level: '', description: '' }); qc.invalidateQueries({ queryKey: ['designations'] }); } });
  const update = useMutation({ mutationFn: () => selected ? masterDataApi.updateDesignation(selected.id, { ...form, version: selected.version }) : Promise.reject(), onSuccess: () => { qc.invalidateQueries({ queryKey: ['designations'] }); setSelected(null); } });
  const remove = useMutation({ mutationFn: (id: string) => masterDataApi.deleteDesignation(id), onSuccess: () => { qc.invalidateQueries({ queryKey: ['designations'] }); setSelected(null); } });
  const pick = (d: Designation) => { setSelected(d); setForm({ title: d.title, level: d.level ?? '', description: d.description ?? '' }); };

  return <div className="space-y-4">
    <h1 className="text-2xl font-semibold">Designations</h1>
    <Card><CardContent className="pt-6"><input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search designations..." className="h-9 w-full rounded-md border border-border px-3" /></CardContent></Card>
    <div className="grid gap-4 xl:grid-cols-[2fr_1fr]">
      <Card><CardContent className="pt-6 overflow-x-auto"><table className="w-full text-sm"><thead><tr><th className="text-left py-2">Title</th><th className="text-left py-2">Level</th><th className="text-left py-2">Description</th></tr></thead><tbody>{(list.data?.content ?? []).map((d) => <tr key={d.id} onClick={() => pick(d)} className="cursor-pointer border-t hover:bg-muted/30"><td className="py-2">{d.title}</td><td>{d.level ?? '-'}</td><td>{d.description ?? '-'}</td></tr>)}</tbody></table><div className="mt-3 flex gap-2"><Button size="sm" variant="outline" onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={list.data?.first ?? true}>Prev</Button><Button size="sm" variant="outline" onClick={() => setPage((p) => p + 1)} disabled={list.data?.last ?? true}>Next</Button></div></CardContent></Card>
      <Card><CardHeader><CardTitle>{selected ? 'Edit Designation' : 'Create Designation'}</CardTitle></CardHeader><CardContent className="space-y-2">
        <input value={form.title} onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))} placeholder="Title" className="h-9 w-full rounded-md border border-border px-3" />
        <input value={form.level} onChange={(e) => setForm((f) => ({ ...f, level: e.target.value }))} placeholder="Level" className="h-9 w-full rounded-md border border-border px-3" />
        <input value={form.description} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} placeholder="Description" className="h-9 w-full rounded-md border border-border px-3" />
        {canWrite && !selected && <Button onClick={() => create.mutate()} disabled={create.isPending || !form.title}>Create</Button>}
        {canWrite && selected && <div className="flex gap-2"><Button onClick={() => update.mutate()} disabled={update.isPending || !form.title}>Save</Button><Button variant="outline" onClick={() => setSelected(null)}>Cancel</Button><Button variant="outline" onClick={() => remove.mutate(selected.id)}>Delete</Button></div>}
      </CardContent></Card>
    </div>
  </div>;
}
