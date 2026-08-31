import { api } from '@/lib/api/client';
import type { SpringPage } from '@/lib/api/types';

export type DirectoryEmployeeResponse = {
  id: string;
  fullName: string;
  designationTitle: string | null;
  departmentName: string | null;
  locationName: string | null;
  email: string;
  phoneNumber: string | null;
  managerName: string | null;
};

export function fetchDirectory(page: number = 0, size: number = 20): Promise<SpringPage<DirectoryEmployeeResponse>> {
  const query = new URLSearchParams({
    page: String(page),
    size: String(size),
  });
  return api.get<SpringPage<DirectoryEmployeeResponse>>(`/api/v1/directory/employees?${query.toString()}`);
}

export function fetchDirectoryEmployeeDetails(employeeId: string): Promise<DirectoryEmployeeResponse> {
  return api.get<DirectoryEmployeeResponse>(`/api/v1/directory/employees/${employeeId}`);
}
