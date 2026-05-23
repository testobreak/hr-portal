import { api } from '@/lib/api/client';
import type { EmployeeSummary, SpringPage } from '@/lib/api/types';

export type Client = { id: string; code: string; name: string; description: string | null; version: number };
export type Project = { id: string; clientId: string; clientName: string | null; projectCode: string; name: string; description: string | null; projectManagerId: string | null; projectManagerName: string | null; status: string; startDate: string; endDate: string | null; version: number };
export type Allocation = { id: string; projectId: string; projectCode: string; projectName: string; employeeId: string; employeeName: string; allocationPercentage: number; roleTitle: string | null; startDate: string; endDate: string | null; version: number };

const query = (page: number, size: number, q: string, sort: string) => {
  const params = new URLSearchParams({ page: String(page), size: String(size), sort });
  if (q.trim()) params.set('q', q.trim());
  return params.toString();
};

export const projectApi = {
  employees: () => api.get<SpringPage<EmployeeSummary>>('/api/employees?page=0&size=200&sort=firstName,asc'),
  clients: (page: number, size: number, q: string) => api.get<SpringPage<Client>>(`/api/clients?${query(page, size, q, 'name,asc')}`),
  createClient: (body: { code: string; name: string; description?: string }) => api.post<Client>('/api/clients', body),
  updateClient: (id: string, body: { name: string; description?: string; version: number }) => api.put<Client>(`/api/clients/${id}`, body),
  deleteClient: (id: string) => api.delete<void>(`/api/clients/${id}`),

  projects: (page: number, size: number, q: string) => api.get<SpringPage<Project>>(`/api/projects?${query(page, size, q, 'name,asc')}`),
  createProject: (body: { clientId: string; projectCode: string; name: string; description?: string; projectManagerId?: string; status?: string; startDate: string; endDate?: string }) => api.post<Project>('/api/projects', body),
  updateProject: (id: string, body: { clientId: string; name: string; description?: string; projectManagerId?: string; status: string; startDate: string; endDate?: string; version: number }) => api.put<Project>(`/api/projects/${id}`, body),
  deleteProject: (id: string) => api.delete<void>(`/api/projects/${id}`),

  allocations: (page: number, size: number, q: string) => api.get<SpringPage<Allocation>>(`/api/allocations?${query(page, size, q, 'startDate,desc')}`),
  createAllocation: (body: { projectId: string; employeeId: string; allocationPercentage: number; roleTitle?: string; startDate: string; endDate?: string }) => api.post<Allocation>('/api/allocations', body),
  updateAllocation: (id: string, body: { projectId: string; employeeId: string; allocationPercentage: number; roleTitle?: string; startDate: string; endDate?: string; version: number }) => api.put<Allocation>(`/api/allocations/${id}`, body),
  deleteAllocation: (id: string) => api.delete<void>(`/api/allocations/${id}`),
};
