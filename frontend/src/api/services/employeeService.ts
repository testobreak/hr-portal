import { api } from '@/lib/api/client';
import type { EmployeeResponse, EmployeeSummary, SpringPage } from '@/lib/api/types';

type ListParams = {
  page: number;
  size: number;
  query?: string;
};

export async function fetchEmployees(params: ListParams): Promise<SpringPage<EmployeeSummary>> {
  const query = new URLSearchParams({
    page: String(params.page),
    size: String(params.size),
    sort: 'firstName,asc',
  });
  if (params.query && params.query.trim()) {
    query.set('q', params.query.trim());
  }
  return api.get<SpringPage<EmployeeSummary>>(`/api/employees?${query.toString()}`);
}

export function fetchEmployeeById(id: string): Promise<EmployeeResponse> {
  return api.get<EmployeeResponse>(`/api/employees/${id}`);
}

export type EmployeeCreatePayload = {
  employeeCode: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
  dateOfBirth?: string;
  dateOfJoining: string;
  employmentStatus?: string;
  keycloakUserId?: string;
  departmentId?: string;
  designationId?: string;
  locationId?: string;
  managerId?: string;
  keycloakPassword?: string;
};

export type EmployeeUpdatePayload = Omit<EmployeeCreatePayload, 'employeeCode'> & {
  version: number;
};

export function createEmployee(payload: EmployeeCreatePayload): Promise<EmployeeResponse> {
  return api.post<EmployeeResponse>('/api/employees', payload);
}

export function updateEmployee(id: string, payload: EmployeeUpdatePayload): Promise<EmployeeResponse> {
  return api.put<EmployeeResponse>(`/api/employees/${id}`, payload);
}
