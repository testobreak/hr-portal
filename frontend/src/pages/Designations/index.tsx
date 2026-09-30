import { useEffect, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { masterDataApi, type Designation } from '@/api/services/masterDataService';
import { Roles, hasAnyRole } from '@/lib/roles';
import { useMe } from '@/hooks/useMe';
import { ApiError } from '@/lib/api/errors';

export function DesignationsPage() {
  const qc = useQueryClient();
  const { data: me } = useMe();
  const canWrite = hasAnyRole(me?.roles ?? [], [Roles.SUPER_ADMIN, Roles.HR_ADMIN]);

  const [page, setPage] = useState(0);
  const [q, setQ] = useState('');
  const [search, setSearch] = useState('');
  const [selected, setSelected] = useState<Designation | null>(null);
  const [form, setForm] = useState({ title: '', level: '', description: '' });
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
    queryKey: ['designations', page, q],
    queryFn: () => masterDataApi.listDesignations(page, 10, q),
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
    mutationFn: () => masterDataApi.createDesignation(form),
    onSuccess: () => {
      setForm({ title: '', level: '', description: '' });
      setErrorMessage(null);
      setSuccessMessage('Designation created successfully');
      setTimeout(() => setSuccessMessage(null), 4000);
      qc.invalidateQueries({ queryKey: ['designations'] });
    },
    onError: (err) => {
      setErrorMessage(extractError(err));
      setSuccessMessage(null);
    },
  });

  const update = useMutation({
    mutationFn: () =>
      selected
        ? masterDataApi.updateDesignation(selected.id, { ...form, version: selected.version })
        : Promise.reject(new Error('No designation selected')),
    onSuccess: () => {
      setSelected(null);
      setForm({ title: '', level: '', description: '' });
      setErrorMessage(null);
      setSuccessMessage('Designation updated successfully');
      setTimeout(() => setSuccessMessage(null), 4000);
      qc.invalidateQueries({ queryKey: ['designations'] });
    },
    onError: (err) => {
      setErrorMessage(extractError(err));
      setSuccessMessage(null);
    },
  });

  const remove = useMutation({
    mutationFn: (id: string) => masterDataApi.deleteDesignation(id),
    onSuccess: () => {
      setSelected(null);
      setForm({ title: '', level: '', description: '' });
      setErrorMessage(null);
      setSuccessMessage('Designation removed successfully');
      setTimeout(() => setSuccessMessage(null), 4000);
      qc.invalidateQueries({ queryKey: ['designations'] });
    },
    onError: (err) => {
      setErrorMessage(extractError(err));
      setSuccessMessage(null);
    },
  });

  const pick = (d: Designation) => {
    setSelected(d);
    setErrorMessage(null);
    setSuccessMessage(null);
    setForm({
      title: d.title,
      level: d.level ?? '',
      description: d.description ?? '',
    });
  };

  const resetForm = () => {
    setSelected(null);
    setErrorMessage(null);
    setSuccessMessage(null);
    setForm({ title: '', level: '', description: '' });
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Designations</h1>
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
            placeholder="Search designations..."
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
                  <th className="text-left py-2 px-3 font-medium">Title</th>
                  <th className="text-left py-2 px-3 font-medium">Level</th>
                  <th className="text-left py-2 px-3 font-medium">Description</th>
                </tr>
              </thead>
              <tbody>
                {list.isLoading ? (
                  <tr>
                    <td colSpan={3} className="py-6 text-center text-muted-foreground">
                      Loading designations...
                    </td>
                  </tr>
                ) : (list.data?.content ?? []).length === 0 ? (
                  <tr>
                    <td colSpan={3} className="py-6 text-center text-muted-foreground">
                      No designations found.
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
                      <td className="py-2.5 px-3 font-medium">{d.title}</td>
                      <td className="py-2.5 px-3">{d.level ?? '-'}</td>
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
            <CardTitle>{selected ? 'Edit Designation' : 'Create Designation'}</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div>
              <label className="text-xs font-medium text-muted-foreground mb-1 block">
                Job Title *
              </label>
              <input
                value={form.title}
                onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))}
                placeholder="e.g. Senior Software Engineer"
                className="h-9 w-full rounded-md border border-border px-3 text-sm"
              />
            </div>

            <div>
              <label className="text-xs font-medium text-muted-foreground mb-1 block">
                Level / Grade
              </label>
              <input
                value={form.level}
                onChange={(e) => setForm((f) => ({ ...f, level: e.target.value }))}
                placeholder="e.g. L4, Principal, Director"
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
                placeholder="Responsibilities or requirements"
                className="h-9 w-full rounded-md border border-border px-3 text-sm"
              />
            </div>

            <div className="pt-2">
              {canWrite && !selected && (
                <Button
                  onClick={() => create.mutate()}
                  disabled={!form.title || create.isPending}
                  className="w-full"
                >
                  {create.isPending ? 'Creating...' : 'Create Designation'}
                </Button>
              )}

              {canWrite && selected && (
                <div className="flex gap-2">
                  <Button
                    onClick={() => update.mutate()}
                    disabled={!form.title || update.isPending}
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
