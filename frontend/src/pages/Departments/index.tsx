import { useEffect, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { masterDataApi, type Department } from '@/api/services/masterDataService';
import { Roles, hasAnyRole } from '@/lib/roles';
import { useMe } from '@/hooks/useMe';
import { ApiError } from '@/lib/api/errors';

export function DepartmentsPage() {
  const qc = useQueryClient();
  const { data: me } = useMe();
  const canWrite = hasAnyRole(me?.roles ?? [], [Roles.SUPER_ADMIN, Roles.HR_ADMIN]);

  const [page, setPage] = useState(0);
  const [q, setQ] = useState('');
  const [search, setSearch] = useState('');
  const [selected, setSelected] = useState<Department | null>(null);
  const [form, setForm] = useState({ code: '', name: '', description: '' });
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useEffect(() => {
    const t = setTimeout(() => {
      setPage(0);
      setQ(search);
    }, 300);
    return () => clearTimeout(t);
  }, [search]);

  const list = useQuery({
    queryKey: ['departments', page, q],
    queryFn: () => masterDataApi.listDepartments(page, 10, q),
  });

  const extractError = (err: unknown): string => {
    if (err instanceof ApiError) {
      if (err.problem?.fieldErrors && err.problem.fieldErrors.length > 0) {
        return err.problem.fieldErrors
          .map((f) => (f.message ? `${f.field}: ${f.message}` : `${f.field}: ${f.code}`))
          .join('; ');
      }
      return err.problem?.detail || err.problem?.title || err.message || 'Operation failed';
    }
    if (err instanceof Error) {
      return err.message;
    }
    return 'An unexpected error occurred';
  };

  const create = useMutation({
    mutationFn: () => masterDataApi.createDepartment(form),
    onSuccess: () => {
      setForm({ code: '', name: '', description: '' });
      setErrorMessage(null);
      setSuccessMessage('Department created successfully');
      setTimeout(() => setSuccessMessage(null), 4000);
      qc.invalidateQueries({ queryKey: ['departments'] });
    },
    onError: (err) => {
      setErrorMessage(extractError(err));
      setSuccessMessage(null);
    },
  });

  const update = useMutation({
    mutationFn: () =>
      selected
        ? masterDataApi.updateDepartment(selected.id, {
            name: form.name,
            description: form.description,
            version: selected.version,
          })
        : Promise.reject(new Error('No department selected')),
    onSuccess: () => {
      setSelected(null);
      setForm({ code: '', name: '', description: '' });
      setErrorMessage(null);
      setSuccessMessage('Department updated successfully');
      setTimeout(() => setSuccessMessage(null), 4000);
      qc.invalidateQueries({ queryKey: ['departments'] });
    },
    onError: (err) => {
      setErrorMessage(extractError(err));
      setSuccessMessage(null);
    },
  });

  const remove = useMutation({
    mutationFn: (id: string) => masterDataApi.deleteDepartment(id),
    onSuccess: () => {
      setSelected(null);
      setForm({ code: '', name: '', description: '' });
      setErrorMessage(null);
      setSuccessMessage('Department removed successfully');
      setTimeout(() => setSuccessMessage(null), 4000);
      qc.invalidateQueries({ queryKey: ['departments'] });
    },
    onError: (err) => {
      setErrorMessage(extractError(err));
      setSuccessMessage(null);
    },
  });

  const pick = (d: Department) => {
    setSelected(d);
    setErrorMessage(null);
    setSuccessMessage(null);
    setForm({
      code: d.code,
      name: d.name,
      description: d.description ?? '',
    });
  };

  const resetForm = () => {
    setSelected(null);
    setErrorMessage(null);
    setSuccessMessage(null);
    setForm({ code: '', name: '', description: '' });
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Departments</h1>
      </div>

      {errorMessage && (
        <div className="flex items-center justify-between rounded-lg border border-destructive/20 bg-destructive/10 p-3.5 text-sm text-destructive shadow-sm">
          <div className="flex items-center gap-2">
            <span className="font-semibold">Error:</span>
            <span>{errorMessage}</span>
          </div>
          <button
            type="button"
            onClick={() => setErrorMessage(null)}
            className="text-xs font-semibold text-destructive/80 hover:text-destructive transition-colors ml-4 px-2 py-0.5 rounded border border-destructive/30 hover:border-destructive/50"
          >
            Dismiss
          </button>
        </div>
      )}

      {successMessage && (
        <div className="flex items-center justify-between rounded-lg border border-emerald-200 bg-emerald-50 p-3.5 text-sm text-emerald-800 shadow-sm">
          <div className="flex items-center gap-2">
            <span className="font-semibold">Success:</span>
            <span>{successMessage}</span>
          </div>
          <button
            type="button"
            onClick={() => setSuccessMessage(null)}
            className="text-xs font-semibold text-emerald-700 hover:text-emerald-900 transition-colors ml-4 px-2 py-0.5 rounded border border-emerald-300 hover:border-emerald-500"
          >
            Dismiss
          </button>
        </div>
      )}

      <Card>
        <CardContent className="pt-6">
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search departments..."
            className="h-9 w-full rounded-md border border-border px-3 text-sm focus:outline-none focus:ring-1 focus:ring-primary"
          />
        </CardContent>
      </Card>

      <div className="grid gap-4 xl:grid-cols-[2fr_1fr]">
        <Card>
          <CardContent className="pt-6 overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b">
                  <th className="text-left py-2 px-3 font-medium">Code</th>
                  <th className="text-left py-2 px-3 font-medium">Name</th>
                  <th className="text-left py-2 px-3 font-medium">Description</th>
                </tr>
              </thead>
              <tbody>
                {list.isLoading ? (
                  <tr>
                    <td colSpan={3} className="py-6 text-center text-muted-foreground">
                      Loading departments...
                    </td>
                  </tr>
                ) : (list.data?.content ?? []).length === 0 ? (
                  <tr>
                    <td colSpan={3} className="py-6 text-center text-muted-foreground">
                      No departments found.
                    </td>
                  </tr>
                ) : (
                  (list.data?.content ?? []).map((d) => (
                    <tr
                      key={d.id}
                      onClick={() => pick(d)}
                      className={`cursor-pointer border-t hover:bg-muted/30 transition-colors ${
                        selected?.id === d.id ? 'bg-muted/50 font-medium' : ''
                      }`}
                    >
                      <td className="py-2.5 px-3 font-mono text-xs">{d.code}</td>
                      <td className="py-2.5 px-3">{d.name}</td>
                      <td className="py-2.5 px-3 text-muted-foreground">{d.description ?? '-'}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>

            <div className="mt-4 flex items-center justify-between">
              <span className="text-xs text-muted-foreground">
                Page {(list.data?.number ?? 0) + 1} of {list.data?.totalPages ?? 1} (
                {list.data?.totalElements ?? 0} total)
              </span>
              <div className="flex gap-2">
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  disabled={list.data?.first ?? true}
                >
                  Prev
                </Button>
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => setPage((p) => p + 1)}
                  disabled={list.data?.last ?? true}
                >
                  Next
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>{selected ? 'Edit Department' : 'Create Department'}</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div>
              <label className="text-xs font-medium text-muted-foreground mb-1 block">
                Department Code *
              </label>
              <input
                value={form.code}
                disabled={!!selected}
                onChange={(e) => setForm((f) => ({ ...f, code: e.target.value.toUpperCase() }))}
                placeholder="e.g. ENG, HR, FIN"
                className="h-9 w-full rounded-md border border-border px-3 text-sm disabled:bg-muted/40 font-mono"
              />
            </div>

            <div>
              <label className="text-xs font-medium text-muted-foreground mb-1 block">
                Department Name *
              </label>
              <input
                value={form.name}
                onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
                placeholder="e.g. Engineering & Technology"
                className="h-9 w-full rounded-md border border-border px-3 text-sm"
              />
            </div>

            <div>
              <label className="text-xs font-medium text-muted-foreground mb-1 block">
                Description
              </label>
              <input
                value={form.description}
                onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
                placeholder="Department mission or overview"
                className="h-9 w-full rounded-md border border-border px-3 text-sm"
              />
            </div>

            <div className="pt-2">
              {canWrite && !selected && (
                <Button
                  onClick={() => create.mutate()}
                  disabled={!form.code || !form.name || create.isPending}
                  className="w-full"
                >
                  {create.isPending ? 'Creating...' : 'Create Department'}
                </Button>
              )}

              {canWrite && selected && (
                <div className="flex gap-2">
                  <Button
                    onClick={() => update.mutate()}
                    disabled={!form.name || update.isPending}
                    className="flex-1"
                  >
                    {update.isPending ? 'Saving...' : 'Save'}
                  </Button>
                  <Button variant="outline" onClick={resetForm}>
                    Cancel
                  </Button>
                  <Button
                    variant="outline"
                    onClick={() => remove.mutate(selected.id)}
                    disabled={remove.isPending}
                    className="text-destructive hover:bg-destructive/10"
                  >
                    {remove.isPending ? '...' : 'Delete'}
                  </Button>
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
