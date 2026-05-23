import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { operationsApi } from '@/api/services/operationsService';

export function AuditPage() {
  const [page, setPage] = useState(0);
  const logs = useQuery({ queryKey: ['audit-logs', page], queryFn: () => operationsApi.auditLogs(page, 20) });
  return <div className="space-y-4"><h1 className="text-2xl font-semibold">Audit log</h1><Card><CardContent className="pt-6 overflow-x-auto"><table className="w-full text-sm"><thead><tr><th className="text-left py-2">At</th><th className="text-left py-2">Actor</th><th className="text-left py-2">Action</th><th className="text-left py-2">Entity</th><th className="text-left py-2">Detail</th><th className="text-left py-2">Request</th></tr></thead><tbody>{(logs.data?.content ?? []).map((l) => <tr key={l.id} className="border-t"><td className="py-2">{new Date(l.at).toLocaleString()}</td><td>{l.actorLabel ?? l.actorId ?? '-'}</td><td>{l.action}</td><td>{l.entity}</td><td>{l.detail ?? '-'}</td><td>{l.requestId ?? '-'}</td></tr>)}</tbody></table><div className="mt-3 flex gap-2"><Button size="sm" variant="outline" onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={logs.data?.first ?? true}>Prev</Button><Button size="sm" variant="outline" onClick={() => setPage((p) => p + 1)} disabled={logs.data?.last ?? true}>Next</Button></div></CardContent></Card></div>;
}
