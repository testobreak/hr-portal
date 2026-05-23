import { api } from '@/lib/api/client';
import type { SpringPage } from '@/lib/api/types';

export type Salary = { id: string; employeeId: string; amount: number; currencyCode: string; effectiveFrom: string; effectiveTo: string | null; reason: string | null; createdAt: string };
export type DocumentRecord = { id: string; employeeId: string; documentType: string; restricted: boolean; sharable: boolean; originalFilename: string; contentType: string; sizeBytes: number | null; uploadStatus: string; createdAt: string };
export type AuditEntry = { id: string; at: string; actorId: string | null; actorLabel: string | null; action: string; entity: string; entityId: string | null; requestId: string | null; detail: string | null };

export const operationsApi = {
  salaryMe: () => api.get<Salary[]>('/api/salaries/me'),
  salaryForEmployee: (employeeId: string) => api.get<Salary[]>(`/api/salaries/employee/${employeeId}`),
  createSalary: (body: { employeeId: string; amount: number; currencyCode: string; effectiveFrom: string; effectiveTo?: string; reason?: string }) => api.post<Salary>('/api/salaries', body),

  documentsForEmployee: (employeeId: string) => api.get<DocumentRecord[]>(`/api/documents/employee/${employeeId}`),
  presignUpload: (body: { employeeId: string; documentType: string; contentType: string; originalFilename: string; sharable: boolean }) => api.post<{ documentId: string; uploadUrl: string; httpMethod: string; storageKey: string }>('/api/documents/presign-upload', body),
  completeUpload: (documentId: string, sizeBytes: number) => api.post<DocumentRecord>(`/api/documents/${documentId}/complete`, { sizeBytes }),
  presignDownload: (documentId: string) => api.get<{ documentId: string; downloadUrl: string; httpMethod: string }>(`/api/documents/${documentId}/presign-download`),
  deleteDocument: (documentId: string) => api.delete<void>(`/api/documents/${documentId}`),

  auditLogs: (page: number, size: number) => api.get<SpringPage<AuditEntry>>(`/api/audit-logs?page=${page}&size=${size}&sort=at,desc`),
};
