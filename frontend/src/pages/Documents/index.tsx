import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { operationsApi } from '@/api/services/operationsService';
import { projectApi } from '@/api/services/projectManagementService';
import { Roles, hasAnyRole } from '@/lib/roles';
import { useMe } from '@/hooks/useMe';

const types = ['PROFILE_PHOTO', 'OFFER_LETTER', 'ID_PROOF', 'OTHER'];

export function DocumentsPage() {
  const qc = useQueryClient();
  const { data: me } = useMe();
  const canDelete = hasAnyRole(me?.roles ?? [], [Roles.SUPER_ADMIN, Roles.HR_ADMIN]);
  const [employeeId, setEmployeeId] = useState('');
  const [form, setForm] = useState({ documentType: 'OTHER', contentType: 'application/pdf', originalFilename: '', sharable: false, sizeBytes: '1' });
  const employees = useQuery({ queryKey: ['employees', 'lookup'], queryFn: projectApi.employees });
  const docs = useQuery({ queryKey: ['documents', employeeId], queryFn: () => operationsApi.documentsForEmployee(employeeId), enabled: !!employeeId });
  const create = useMutation({ mutationFn: async () => { const p = await operationsApi.presignUpload({ employeeId, documentType: form.documentType, contentType: form.contentType, originalFilename: form.originalFilename, sharable: form.sharable }); return operationsApi.completeUpload(p.documentId, Number(form.sizeBytes)); }, onSuccess: () => { setForm({ documentType: 'OTHER', contentType: 'application/pdf', originalFilename: '', sharable: false, sizeBytes: '1' }); qc.invalidateQueries({ queryKey: ['documents', employeeId] }); } });
  const download = useMutation({ mutationFn: (id: string) => operationsApi.presignDownload(id), onSuccess: (r) => window.open(r.downloadUrl, '_blank', 'noopener,noreferrer') });
  const remove = useMutation({ mutationFn: (id: string) => operationsApi.deleteDocument(id), onSuccess: () => qc.invalidateQueries({ queryKey: ['documents', employeeId] }) });
  return <div className="space-y-4"><h1 className="text-2xl font-semibold">Documents</h1><Card><CardContent className="pt-6"><select value={employeeId} onChange={(e) => setEmployeeId(e.target.value)} className="h-9 w-full rounded-md border border-border px-3"><option value="">Select employee</option>{(employees.data?.content ?? []).map((e) => <option key={e.id} value={e.id}>{e.firstName} {e.lastName} ({e.employeeCode})</option>)}</select></CardContent></Card>{employeeId && <Card><CardHeader><CardTitle>Register Document Upload</CardTitle></CardHeader><CardContent className="grid gap-2 sm:grid-cols-3"><select value={form.documentType} onChange={(e) => setForm((f) => ({ ...f, documentType: e.target.value }))} className="h-9 rounded-md border border-border px-3">{types.map((t) => <option key={t}>{t}</option>)}</select><input value={form.originalFilename} onChange={(e) => setForm((f) => ({ ...f, originalFilename: e.target.value }))} placeholder="Filename" className="h-9 rounded-md border border-border px-3" /><input value={form.contentType} onChange={(e) => setForm((f) => ({ ...f, contentType: e.target.value }))} placeholder="Content type" className="h-9 rounded-md border border-border px-3" /><input type="number" value={form.sizeBytes} onChange={(e) => setForm((f) => ({ ...f, sizeBytes: e.target.value }))} placeholder="Size bytes" className="h-9 rounded-md border border-border px-3" /><label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={form.sharable} onChange={(e) => setForm((f) => ({ ...f, sharable: e.target.checked }))} />Sharable</label><Button onClick={() => create.mutate()} disabled={!form.originalFilename || create.isPending}>Create record</Button></CardContent></Card>}<Card><CardContent className="pt-6 overflow-x-auto"><table className="w-full text-sm"><thead><tr><th className="text-left py-2">File</th><th className="text-left py-2">Type</th><th className="text-left py-2">Status</th><th className="text-left py-2">Actions</th></tr></thead><tbody>{(docs.data ?? []).map((d) => <tr key={d.id} className="border-t"><td className="py-2">{d.originalFilename}</td><td>{d.documentType}</td><td>{d.uploadStatus}</td><td className="space-x-2"><Button size="sm" variant="outline" onClick={() => download.mutate(d.id)}>Download</Button>{canDelete && <Button size="sm" variant="outline" onClick={() => remove.mutate(d.id)}>Delete</Button>}</td></tr>)}</tbody></table></CardContent></Card></div>;
}
