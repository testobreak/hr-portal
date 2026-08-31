import { useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Search, Users } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { createEmployee, fetchEmployeeById, fetchEmployees, updateEmployee, type EmployeeCreatePayload } from '@/api/services/employeeService';
import { fetchDepartmentLookups, fetchDesignationLookups, fetchLocationLookups } from '@/api/services/lookupsService';
import { useMe } from '@/hooks/useMe';
import { Roles, hasAnyRole } from '@/lib/roles';
import { ApiError } from '@/lib/api/errors';
import type { EmployeeSummary, SpringPage } from '@/lib/api/types';

const employeeFormSchema = z.object({
  employeeCode: z.string().trim().min(1).max(32),
  firstName: z.string().trim().min(1).max(64),
  lastName: z.string().trim().min(1).max(64),
  email: z.string().trim().email().max(254),
  phoneNumber: z.string().trim().max(32).optional(),
  dateOfBirth: z.string().optional(),
  dateOfJoining: z.string().min(1),
  employmentStatus: z.enum(['ACTIVE', 'ON_LEAVE', 'TERMINATED', 'RESIGNED', 'ABSCONDED']),
  keycloakUserId: z.string().trim().optional(),
  keycloakPassword: z.string().trim().optional(),
  departmentId: z.string().trim().optional(),
  designationId: z.string().trim().optional(),
  locationId: z.string().trim().optional(),
  managerId: z.string().trim().optional(),
});
type EmployeeFormValues = z.infer<typeof employeeFormSchema>;
type Toast = { id: number; kind: 'success' | 'error'; message: string };

const EMPTY_PAGE: SpringPage<EmployeeSummary> = { content: [], totalElements: 0, totalPages: 1, size: 10, number: 0, first: true, last: true };

function cleanOptional(value?: string): string | undefined { const v = value?.trim(); return v ? v : undefined; }
function formatDate(value: string | null): string { if (!value) return '-'; const d = new Date(value); return Number.isNaN(d.getTime()) ? value : d.toLocaleDateString(); }

export function EmployeesPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [searchInput, setSearchInput] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [editorMode, setEditorMode] = useState<'create' | 'edit' | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [toasts, setToasts] = useState<Toast[]>([]);
  const { data: me } = useMe();
  const canManageEmployees = hasAnyRole(me?.roles ?? [], [Roles.SUPER_ADMIN, Roles.HR_ADMIN]);

  const pushToast = (kind: Toast['kind'], message: string) => {
    const id = Date.now() + Math.floor(Math.random() * 1000);
    setToasts((prev) => [...prev, { id, kind, message }]);
    setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), 3200);
  };

  useEffect(() => {
    const t = setTimeout(() => { setPage(0); setSearchQuery(searchInput.trim()); }, 350);
    return () => clearTimeout(t);
  }, [searchInput]);

  const employees = useQuery({ queryKey: ['employees', page, size, searchQuery], queryFn: () => fetchEmployees({ page, size, query: searchQuery }) });
  const selectedEmployee = useQuery({ queryKey: ['employee', selectedId], queryFn: () => fetchEmployeeById(selectedId as string), enabled: selectedId != null });
  const deptLookups = useQuery({ queryKey: ['lookups', 'departments'], queryFn: fetchDepartmentLookups, staleTime: 5 * 60_000 });
  const desigLookups = useQuery({ queryKey: ['lookups', 'designations'], queryFn: fetchDesignationLookups, staleTime: 5 * 60_000 });
  const locLookups = useQuery({ queryKey: ['lookups', 'locations'], queryFn: fetchLocationLookups, staleTime: 5 * 60_000 });
  const managerOptions = useMemo(() => (employees.data?.content ?? []).map((e) => ({ id: e.id, label: `${e.firstName} ${e.lastName} (${e.employeeCode})` })), [employees.data?.content]);

  const form = useForm<EmployeeFormValues>({
    resolver: zodResolver(employeeFormSchema),
    defaultValues: { employeeCode: '', firstName: '', lastName: '', email: '', phoneNumber: '', dateOfBirth: '', dateOfJoining: '', employmentStatus: 'ACTIVE', keycloakUserId: '', departmentId: '', designationId: '', locationId: '', managerId: '', keycloakPassword: '' },
  });

  const createMutation = useMutation({
    mutationFn: (payload: EmployeeCreatePayload) => createEmployee(payload),
    onMutate: async (payload) => {
      await queryClient.cancelQueries({ queryKey: ['employees', page, size, searchQuery] });
      const key = ['employees', page, size, searchQuery] as const;
      const previous = queryClient.getQueryData<SpringPage<EmployeeSummary>>(key) ?? EMPTY_PAGE;
      const optimistic: EmployeeSummary = { id: `optimistic-${Date.now()}`, employeeCode: payload.employeeCode, firstName: payload.firstName, lastName: payload.lastName, email: payload.email, departmentId: payload.departmentId ?? null, departmentName: deptLookups.data?.find((d) => d.id === payload.departmentId)?.name ?? null, designationId: payload.designationId ?? null, designationTitle: desigLookups.data?.find((d) => d.id === payload.designationId)?.title ?? null, locationId: payload.locationId ?? null, locationName: locLookups.data?.find((l) => l.id === payload.locationId)?.name ?? null, dateOfJoining: payload.dateOfJoining, employmentStatus: payload.employmentStatus ?? 'ACTIVE' };
      queryClient.setQueryData<SpringPage<EmployeeSummary>>(key, { ...previous, content: [optimistic, ...previous.content].slice(0, previous.size), totalElements: previous.totalElements + 1 });
      return { previous, key };
    },
    onError: (err, _p, ctx) => {
      if (ctx) queryClient.setQueryData(ctx.key, ctx.previous);
      const message = err instanceof ApiError ? err.message : 'Failed to create employee';
      setSubmitError(message); pushToast('error', message);
    },
    onSuccess: (created) => { queryClient.invalidateQueries({ queryKey: ['employees'] }); queryClient.setQueryData(['employee', created.id], created); setSelectedId(created.id); setEditorMode(null); form.reset(); setSubmitError(null); pushToast('success', 'Employee created successfully'); },
  });

  const editMutation = useMutation({
    mutationFn: (values: EmployeeFormValues) => {
      if (!selectedEmployee.data) throw new Error('No selected employee');
      return updateEmployee(selectedEmployee.data.id, { firstName: values.firstName.trim(), lastName: values.lastName.trim(), email: values.email.trim(), phoneNumber: cleanOptional(values.phoneNumber), dateOfBirth: cleanOptional(values.dateOfBirth), dateOfJoining: values.dateOfJoining, employmentStatus: values.employmentStatus, keycloakUserId: cleanOptional(values.keycloakUserId), departmentId: cleanOptional(values.departmentId), designationId: cleanOptional(values.designationId), locationId: cleanOptional(values.locationId), managerId: cleanOptional(values.managerId), version: selectedEmployee.data.version });
    },
    onMutate: async (values) => {
      await queryClient.cancelQueries({ queryKey: ['employees', page, size, searchQuery] });
      const key = ['employees', page, size, searchQuery] as const;
      const previous = queryClient.getQueryData<SpringPage<EmployeeSummary>>(key) ?? EMPTY_PAGE;
      if (!selectedEmployee.data) return { previous, key };
      const updatedList = previous.content.map((r) => r.id === selectedEmployee.data!.id ? { ...r, firstName: values.firstName.trim(), lastName: values.lastName.trim(), email: values.email.trim(), employmentStatus: values.employmentStatus, departmentId: cleanOptional(values.departmentId) ?? null, designationId: cleanOptional(values.designationId) ?? null, locationId: cleanOptional(values.locationId) ?? null, departmentName: deptLookups.data?.find((d) => d.id === cleanOptional(values.departmentId))?.name ?? null, designationTitle: desigLookups.data?.find((d) => d.id === cleanOptional(values.designationId))?.title ?? null, locationName: locLookups.data?.find((l) => l.id === cleanOptional(values.locationId))?.name ?? null } : r);
      queryClient.setQueryData<SpringPage<EmployeeSummary>>(key, { ...previous, content: updatedList });
      return { previous, key };
    },
    onError: (err, _v, ctx) => {
      if (ctx) queryClient.setQueryData(ctx.key, ctx.previous);
      const message = err instanceof ApiError ? err.message : 'Failed to update employee';
      setSubmitError(message); pushToast('error', message);
    },
    onSuccess: (updated) => { queryClient.invalidateQueries({ queryKey: ['employees'] }); queryClient.setQueryData(['employee', updated.id], updated); setEditorMode(null); setSubmitError(null); pushToast('success', 'Employee updated successfully'); },
  });

  const openCreate = () => { setSubmitError(null); form.reset({ employeeCode: '', firstName: '', lastName: '', email: '', phoneNumber: '', dateOfBirth: '', dateOfJoining: '', employmentStatus: 'ACTIVE', keycloakUserId: '', departmentId: '', designationId: '', locationId: '', managerId: '', keycloakPassword: '' }); setEditorMode('create'); };
  const openEdit = () => {
    if (!selectedEmployee.data) return;
    const e = selectedEmployee.data;
    form.reset({ employeeCode: e.employeeCode, firstName: e.firstName, lastName: e.lastName, email: e.email, phoneNumber: e.phoneNumber ?? '', dateOfBirth: e.dateOfBirth ?? '', dateOfJoining: e.dateOfJoining, employmentStatus: e.employmentStatus as EmployeeFormValues['employmentStatus'], keycloakUserId: e.keycloakUserId ?? '', departmentId: e.departmentId ?? '', designationId: e.designationId ?? '', locationId: e.locationId ?? '', managerId: e.managerId ?? '', keycloakPassword: '' });
    setSubmitError(null); setEditorMode('edit');
  };

  const onSubmit = form.handleSubmit((values) => {
    setSubmitError(null);
    if (editorMode === 'create') createMutation.mutate({ employeeCode: values.employeeCode.trim(), firstName: values.firstName.trim(), lastName: values.lastName.trim(), email: values.email.trim(), phoneNumber: cleanOptional(values.phoneNumber), dateOfBirth: cleanOptional(values.dateOfBirth), dateOfJoining: values.dateOfJoining, employmentStatus: values.employmentStatus, keycloakUserId: cleanOptional(values.keycloakUserId), departmentId: cleanOptional(values.departmentId), designationId: cleanOptional(values.designationId), locationId: cleanOptional(values.locationId), managerId: cleanOptional(values.managerId), keycloakPassword: cleanOptional(values.keycloakPassword) });
    if (editorMode === 'edit') editMutation.mutate(values);
  });

  const isSubmitting = createMutation.isPending || editMutation.isPending;
  const rows = employees.data?.content ?? [];

  return (
    <div className="space-y-6">
      <div className="fixed right-4 top-4 z-50 space-y-2">
        {toasts.map((t) => <div key={t.id} className={`rounded-md px-3 py-2 text-sm text-white shadow ${t.kind === 'success' ? 'bg-emerald-600' : 'bg-rose-600'}`}>{t.message}</div>)}
      </div>
      <header className="flex flex-wrap items-center justify-between gap-4">
        <div><h1 className="text-2xl font-semibold tracking-tight">Employees</h1><p className="mt-1 text-sm text-muted-foreground">Manage employee master data, profiles, and lifecycle.</p></div>
        {canManageEmployees && <Button type="button" onClick={openCreate}>Add employee</Button>}
      </header>
      <div className="grid gap-6 xl:grid-cols-[2fr_1fr]">
        <Card><CardHeader><CardTitle className="flex items-center gap-2"><Users className="h-5 w-5" />Employee directory</CardTitle><CardDescription>Showing {employees.data?.totalElements ?? 0} total records.</CardDescription></CardHeader><CardContent className="space-y-4">
          <div className="relative"><Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" /><input value={searchInput} onChange={(e) => setSearchInput(e.target.value)} placeholder="Search name, code, email..." className="h-10 w-full rounded-md border border-border bg-background pl-9 pr-3 text-sm outline-none focus:ring-2 focus:ring-primary/30" /></div>
          {employees.isLoading ? <div className="space-y-2"><Skeleton className="h-10 w-full" /><Skeleton className="h-10 w-full" /><Skeleton className="h-10 w-full" /></div> : employees.isError ? <p className="text-sm text-red-600">Unable to load employees.</p> : <div className="overflow-x-auto rounded-md border border-border"><table className="w-full min-w-[760px] text-sm"><thead className="bg-muted/50 text-left"><tr><th className="px-3 py-2 font-medium">Code</th><th className="px-3 py-2 font-medium">Name</th><th className="px-3 py-2 font-medium">Email</th><th className="px-3 py-2 font-medium">Department</th><th className="px-3 py-2 font-medium">Status</th></tr></thead><tbody>{rows.map((r) => <tr key={r.id} className="cursor-pointer border-t border-border hover:bg-muted/30" onClick={() => setSelectedId(r.id)}><td className="px-3 py-2">{r.employeeCode}</td><td className="px-3 py-2">{r.firstName} {r.lastName}</td><td className="px-3 py-2">{r.email}</td><td className="px-3 py-2">{r.departmentName ?? '-'}</td><td className="px-3 py-2">{r.employmentStatus}</td></tr>)}{rows.length === 0 && <tr><td className="px-3 py-6 text-center text-muted-foreground" colSpan={5}>No employees found.</td></tr>}</tbody></table></div>}
          <div className="flex items-center justify-between"><p className="text-xs text-muted-foreground">Page {(employees.data?.number ?? 0) + 1} of {Math.max(1, employees.data?.totalPages ?? 1)}</p><div className="flex items-center gap-2"><Button type="button" variant="outline" size="sm" onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={employees.data?.first ?? true}>Previous</Button><Button type="button" variant="outline" size="sm" onClick={() => setPage((p) => p + 1)} disabled={employees.data?.last ?? true}>Next</Button></div></div>
        </CardContent></Card>
        <Card><CardHeader><CardTitle>Employee details</CardTitle><CardDescription>Select a row to view full profile details.</CardDescription></CardHeader><CardContent className="space-y-3">
          {selectedId == null ? <p className="text-sm text-muted-foreground">No employee selected.</p> : selectedEmployee.isLoading ? <div className="space-y-2"><Skeleton className="h-5 w-40" /><Skeleton className="h-5 w-32" /><Skeleton className="h-5 w-48" /></div> : selectedEmployee.isError || !selectedEmployee.data ? <p className="text-sm text-red-600">Unable to load employee details.</p> : <><dl className="space-y-2 text-sm"><div className="flex justify-between gap-2"><dt className="text-muted-foreground">Name</dt><dd>{selectedEmployee.data.firstName} {selectedEmployee.data.lastName}</dd></div><div className="flex justify-between gap-2"><dt className="text-muted-foreground">Employee Code</dt><dd>{selectedEmployee.data.employeeCode}</dd></div><div className="flex justify-between gap-2"><dt className="text-muted-foreground">Email</dt><dd>{selectedEmployee.data.email}</dd></div><div className="flex justify-between gap-2"><dt className="text-muted-foreground">Department</dt><dd>{selectedEmployee.data.departmentName ?? '-'}</dd></div><div className="flex justify-between gap-2"><dt className="text-muted-foreground">Designation</dt><dd>{selectedEmployee.data.designationTitle ?? '-'}</dd></div><div className="flex justify-between gap-2"><dt className="text-muted-foreground">Location</dt><dd>{selectedEmployee.data.locationName ?? '-'}</dd></div><div className="flex justify-between gap-2"><dt className="text-muted-foreground">Date of Joining</dt><dd>{formatDate(selectedEmployee.data.dateOfJoining)}</dd></div><div className="flex justify-between gap-2"><dt className="text-muted-foreground">Status</dt><dd>{selectedEmployee.data.employmentStatus}</dd></div></dl>{canManageEmployees && <Button type="button" variant="outline" size="sm" onClick={openEdit}>Edit employee</Button>}</>}
        </CardContent></Card>
      </div>
      {editorMode != null && <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/40 p-4"><Card className="w-full max-w-3xl"><CardHeader><CardTitle>{editorMode === 'create' ? 'Create employee' : 'Edit employee'}</CardTitle><CardDescription>{editorMode === 'create' ? 'Add a new employee profile.' : 'Update employee details and metadata.'}</CardDescription></CardHeader><CardContent><form onSubmit={onSubmit} className="grid gap-3 sm:grid-cols-2">
        <label className="flex flex-col gap-1 text-sm"><span className="text-muted-foreground">Employee Code</span><input type="text" disabled={editorMode === 'edit'} className="h-9 rounded-md border border-border bg-background px-3 outline-none focus:ring-2 focus:ring-primary/30 disabled:bg-muted/40" {...form.register('employeeCode')} /></label>
        <label className="flex flex-col gap-1 text-sm"><span className="text-muted-foreground">First Name</span><input type="text" className="h-9 rounded-md border border-border bg-background px-3 outline-none focus:ring-2 focus:ring-primary/30" {...form.register('firstName')} /></label>
        <label className="flex flex-col gap-1 text-sm"><span className="text-muted-foreground">Last Name</span><input type="text" className="h-9 rounded-md border border-border bg-background px-3 outline-none focus:ring-2 focus:ring-primary/30" {...form.register('lastName')} /></label>
        <label className="flex flex-col gap-1 text-sm"><span className="text-muted-foreground">Email</span><input type="email" className="h-9 rounded-md border border-border bg-background px-3 outline-none focus:ring-2 focus:ring-primary/30" {...form.register('email')} /></label>
        <label className="flex flex-col gap-1 text-sm"><span className="text-muted-foreground">Phone Number</span><input type="text" className="h-9 rounded-md border border-border bg-background px-3 outline-none focus:ring-2 focus:ring-primary/30" {...form.register('phoneNumber')} /></label>
        <label className="flex flex-col gap-1 text-sm"><span className="text-muted-foreground">Date of Birth</span><input type="date" className="h-9 rounded-md border border-border bg-background px-3 outline-none focus:ring-2 focus:ring-primary/30" {...form.register('dateOfBirth')} /></label>
        <label className="flex flex-col gap-1 text-sm"><span className="text-muted-foreground">Date of Joining</span><input type="date" className="h-9 rounded-md border border-border bg-background px-3 outline-none focus:ring-2 focus:ring-primary/30" {...form.register('dateOfJoining')} /></label>
        <label className="flex flex-col gap-1 text-sm"><span className="text-muted-foreground">Employment Status</span><select className="h-9 rounded-md border border-border bg-background px-3 outline-none focus:ring-2 focus:ring-primary/30" {...form.register('employmentStatus')}><option value="ACTIVE">ACTIVE</option><option value="ON_LEAVE">ON_LEAVE</option><option value="TERMINATED">TERMINATED</option><option value="RESIGNED">RESIGNED</option><option value="ABSCONDED">ABSCONDED</option></select></label>
        <label className="flex flex-col gap-1 text-sm"><span className="text-muted-foreground">Department</span><select className="h-9 rounded-md border border-border bg-background px-3 outline-none focus:ring-2 focus:ring-primary/30" {...form.register('departmentId')}><option value="">Unassigned</option>{(deptLookups.data ?? []).map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}</select></label>
        <label className="flex flex-col gap-1 text-sm"><span className="text-muted-foreground">Designation</span><select className="h-9 rounded-md border border-border bg-background px-3 outline-none focus:ring-2 focus:ring-primary/30" {...form.register('designationId')}><option value="">Unassigned</option>{(desigLookups.data ?? []).map((d) => <option key={d.id} value={d.id}>{d.title}</option>)}</select></label>
        <label className="flex flex-col gap-1 text-sm"><span className="text-muted-foreground">Location</span><select className="h-9 rounded-md border border-border bg-background px-3 outline-none focus:ring-2 focus:ring-primary/30" {...form.register('locationId')}><option value="">Unassigned</option>{(locLookups.data ?? []).map((l) => <option key={l.id} value={l.id}>{l.name}</option>)}</select></label>
        <label className="flex flex-col gap-1 text-sm"><span className="text-muted-foreground">Manager</span><input list="manager-options" className="h-9 rounded-md border border-border bg-background px-3 outline-none focus:ring-2 focus:ring-primary/30" placeholder="Paste manager UUID or pick suggestion" {...form.register('managerId')} /><datalist id="manager-options">{managerOptions.map((m) => <option key={m.id} value={m.id}>{m.label}</option>)}</datalist></label>
        {editorMode === 'create' && (
          <label className="flex flex-col gap-1 text-sm sm:col-span-2">
            <span className="text-muted-foreground">Initial Password (Optional)</span>
            <input
              type="password"
              className="h-9 rounded-md border border-border bg-background px-3 outline-none focus:ring-2 focus:ring-primary/30"
              {...form.register('keycloakPassword')}
            />
          </label>
        )}
        {submitError && <p className="sm:col-span-2 text-sm text-red-600">{submitError}</p>}
        <div className="sm:col-span-2 mt-2 flex justify-end gap-2"><Button type="button" variant="outline" onClick={() => setEditorMode(null)} disabled={isSubmitting}>Cancel</Button><Button type="submit" disabled={isSubmitting}>{isSubmitting ? 'Saving...' : editorMode === 'create' ? 'Create employee' : 'Save changes'}</Button></div>
      </form></CardContent></Card></div>}
    </div>
  );
}
