import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { operationsApi } from '@/api/services/operationsService';
import { projectApi } from '@/api/services/projectManagementService';
import { Roles, hasAnyRole } from '@/lib/roles';
import { useMe } from '@/hooks/useMe';

export function SalaryPage() {
  const qc = useQueryClient();
  const { data: me } = useMe();
  const canWrite = hasAnyRole(me?.roles ?? [], [Roles.SUPER_ADMIN, Roles.HR_ADMIN, Roles.FINANCE_ADMIN]);
  const [employeeId, setEmployeeId] = useState('');
  const [form, setForm] = useState({ amount: '', currencyCode: 'INR', effectiveFrom: '', effectiveTo: '', reason: '' });
  const employees = useQuery({ queryKey: ['employees', 'lookup'], queryFn: projectApi.employees });
  const salary = useQuery({ queryKey: ['salary', employeeId || 'me'], queryFn: () => employeeId ? operationsApi.salaryForEmployee(employeeId) : operationsApi.salaryMe() });
  const create = useMutation({ mutationFn: () => operationsApi.createSalary({ employeeId, amount: Number(form.amount), currencyCode: form.currencyCode, effectiveFrom: form.effectiveFrom, effectiveTo: form.effectiveTo || undefined, reason: form.reason || undefined }), onSuccess: () => { setForm({ amount: '', currencyCode: 'INR', effectiveFrom: '', effectiveTo: '', reason: '' }); qc.invalidateQueries({ queryKey: ['salary'] }); } });
  return <div className="space-y-4"><h1 className="text-2xl font-semibold">Salary</h1><Card><CardContent className="pt-6"><select value={employeeId} onChange={(e) => setEmployeeId(e.target.value)} className="h-9 w-full rounded-md border border-border px-3"><option value="">My salary history</option>{(employees.data?.content ?? []).map((e) => <option key={e.id} value={e.id}>{e.firstName} {e.lastName} ({e.employeeCode})</option>)}</select></CardContent></Card>{canWrite && <Card><CardHeader><CardTitle>Add Salary Record</CardTitle></CardHeader><CardContent className="grid gap-2 sm:grid-cols-3"><input type="number" value={form.amount} onChange={(e) => setForm((f) => ({ ...f, amount: e.target.value }))} placeholder="Amount" className="h-9 rounded-md border border-border px-3" /><input value={form.currencyCode} onChange={(e) => setForm((f) => ({ ...f, currencyCode: e.target.value.toUpperCase() }))} className="h-9 rounded-md border border-border px-3" /><input type="date" value={form.effectiveFrom} onChange={(e) => setForm((f) => ({ ...f, effectiveFrom: e.target.value }))} className="h-9 rounded-md border border-border px-3" /><input type="date" value={form.effectiveTo} onChange={(e) => setForm((f) => ({ ...f, effectiveTo: e.target.value }))} className="h-9 rounded-md border border-border px-3" /><input value={form.reason} onChange={(e) => setForm((f) => ({ ...f, reason: e.target.value }))} placeholder="Reason" className="h-9 rounded-md border border-border px-3" /><Button onClick={() => create.mutate()} disabled={!employeeId || !form.amount || !form.effectiveFrom}>Create</Button></CardContent></Card>}<Card><CardContent className="pt-6 overflow-x-auto"><table className="w-full text-sm"><thead><tr><th className="text-left py-2">Amount</th><th className="text-left py-2">Currency</th><th className="text-left py-2">From</th><th className="text-left py-2">To</th><th className="text-left py-2">Reason</th></tr></thead><tbody>{(salary.data ?? []).map((s) => <tr key={s.id} className="border-t"><td className="py-2">{s.amount}</td><td>{s.currencyCode}</td><td>{s.effectiveFrom}</td><td>{s.effectiveTo ?? '-'}</td><td>{s.reason ?? '-'}</td></tr>)}</tbody></table></CardContent></Card></div>;
}
