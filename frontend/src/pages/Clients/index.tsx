import { useEffect, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { projectApi, type Client } from '@/api/services/projectManagementService';
import { Roles, hasAnyRole } from '@/lib/roles';
import { useMe } from '@/hooks/useMe';

export function ClientsPage() {
  const qc = useQueryClient();
  const { data: me } = useMe();
  const canWrite = hasAnyRole(me?.roles ?? [], [Roles.SUPER_ADMIN, Roles.FINANCE_ADMIN]);
  const [page, setPage] = useState(0);
  const [q, setQ] = useState('');
  const [search, setSearch] = useState('');
  const [selected, setSelected] = useState<Client | null>(null);
  const [form, setForm] = useState({ code: '', name: '', description: '' });
  useEffect(() => { const t = setTimeout(() => { setPage(0); setQ(search); }, 300); return () => clearTimeout(t); }, [search]);
  const list = useQuery({ queryKey: ['clients', page, q], queryFn: () => projectApi.clients(page, 10, q) });
  const create = useMutation({ mutationFn: () => projectApi.createClient(form), onSuccess: () => { setForm({ code: '', name: '', description: '' }); qc.invalidateQueries({ queryKey: ['clients'] }); } });
  const update = useMutation({ mutationFn: () => selected ? projectApi.updateClient(selected.id, { name: form.name, description: form.description, version: selected.version }) : Promise.reject(), onSuccess: () => { setSelected(null); qc.invalidateQueries({ queryKey: ['clients'] }); } });
  const remove = useMutation({ mutationFn: (id: string) => projectApi.deleteClient(id), onSuccess: () => { setSelected(null); qc.invalidateQueries({ queryKey: ['clients'] }); } });
  const pick = (c: Client) => { setSelected(c); setForm({ code: c.code, name: c.name, description: c.description ?? '' }); };
  return <div className="space-y-4"><h1 className="text-2xl font-semibold">Clients</h1><Card><CardContent className="pt-6"><input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search clients..." className="h-9 w-full rounded-md border border-border px-3" /></CardContent></Card><div className="grid gap-4 xl:grid-cols-[2fr_1fr]"><Card><CardContent className="pt-6 overflow-x-auto"><table className="w-full text-sm"><thead><tr><th className="text-left py-2">Code</th><th className="text-left py-2">Name</th><th className="text-left py-2">Description</th></tr></thead><tbody>{(list.data?.content ?? []).map((c) => <tr key={c.id} className="cursor-pointer border-t hover:bg-muted/30" onClick={() => pick(c)}><td className="py-2">{c.code}</td><td>{c.name}</td><td>{c.description ?? '-'}</td></tr>)}</tbody></table><div className="mt-3 flex gap-2"><Button size="sm" variant="outline" onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={list.data?.first ?? true}>Prev</Button><Button size="sm" variant="outline" onClick={() => setPage((p) => p + 1)} disabled={list.data?.last ?? true}>Next</Button></div></CardContent></Card><Card><CardHeader><CardTitle>{selected ? 'Edit Client' : 'Create Client'}</CardTitle></CardHeader><CardContent className="space-y-2"><input value={form.code} disabled={!!selected} onChange={(e) => setForm((f) => ({ ...f, code: e.target.value }))} placeholder="Code" className="h-9 w-full rounded-md border border-border px-3 disabled:bg-muted/40" /><input value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} placeholder="Name" className="h-9 w-full rounded-md border border-border px-3" /><input value={form.description} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} placeholder="Description" className="h-9 w-full rounded-md border border-border px-3" />{canWrite && !selected && <Button onClick={() => create.mutate()} disabled={!form.code || !form.name || create.isPending}>Create</Button>}{canWrite && selected && <div className="flex gap-2"><Button onClick={() => update.mutate()} disabled={!form.name || update.isPending}>Save</Button><Button variant="outline" onClick={() => setSelected(null)}>Cancel</Button><Button variant="outline" onClick={() => remove.mutate(selected.id)}>Delete</Button></div>}</CardContent></Card></div></div>;
}
