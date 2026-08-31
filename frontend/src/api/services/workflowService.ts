import { api } from '@/lib/api/client';

export type ApprovalRequestResponse = {
  id: string;
  requesterId: string;
  employeeId: string;
  type: string;
  changeJson: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  approvedBy: string | null;
  approvedAt: string | null;
  createdAt: string;
  version: number;
};

export function fetchPendingApprovals(): Promise<ApprovalRequestResponse[]> {
  return api.get<ApprovalRequestResponse[]>('/api/approvals/requests');
}

export function fetchApprovalById(id: string): Promise<ApprovalRequestResponse> {
  return api.get<ApprovalRequestResponse>(`/api/approvals/requests/${id}`);
}

export function approveRequest(id: string): Promise<ApprovalRequestResponse> {
  return api.post<ApprovalRequestResponse>(`/api/approvals/requests/${id}/approve`);
}

export function rejectRequest(id: string): Promise<ApprovalRequestResponse> {
  return api.post<ApprovalRequestResponse>(`/api/approvals/requests/${id}/reject`);
}
