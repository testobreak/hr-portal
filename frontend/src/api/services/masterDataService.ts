import { api } from '@/lib/api/client';
import type { SpringPage } from '@/lib/api/types';

export type Department = { id: string; code: string; name: string; description: string | null; version: number };
export type Designation = { id: string; title: string; level: string | null; description: string | null; version: number };
export type Location = { id: string; code: string; name: string; city: string | null; country: string | null; version: number };

const pageQuery = (page: number, size: number, q: string, sort: string) => {
  const s = new URLSearchParams({ page: String(page), size: String(size), sort });
  if (q.trim()) s.set('q', q.trim());
  return s.toString();
};

export const masterDataApi = {
  listDepartments: (page: number, size: number, q: string) =>
    api.get<SpringPage<Department>>(`/api/departments?${pageQuery(page, size, q, 'name,asc')}`),
  createDepartment: (payload: { code: string; name: string; description?: string }) =>
    api.post<Department>('/api/departments', payload),
  updateDepartment: (id: string, payload: { name: string; description?: string; version: number }) =>
    api.put<Department>(`/api/departments/${id}`, payload),
  deleteDepartment: (id: string) => api.delete<void>(`/api/departments/${id}`),

  listDesignations: (page: number, size: number, q: string) =>
    api.get<SpringPage<Designation>>(`/api/designations?${pageQuery(page, size, q, 'title,asc')}`),
  createDesignation: (payload: { title: string; level?: string; description?: string }) =>
    api.post<Designation>('/api/designations', payload),
  updateDesignation: (id: string, payload: { title: string; level?: string; description?: string; version: number }) =>
    api.put<Designation>(`/api/designations/${id}`, payload),
  deleteDesignation: (id: string) => api.delete<void>(`/api/designations/${id}`),

  listLocations: (page: number, size: number, q: string) =>
    api.get<SpringPage<Location>>(`/api/locations?${pageQuery(page, size, q, 'name,asc')}`),
  createLocation: (payload: { code: string; name: string; city?: string; country?: string }) =>
    api.post<Location>('/api/locations', payload),
  updateLocation: (id: string, payload: { name: string; city?: string; country?: string; version: number }) =>
    api.put<Location>(`/api/locations/${id}`, payload),
  deleteLocation: (id: string) => api.delete<void>(`/api/locations/${id}`),
};
