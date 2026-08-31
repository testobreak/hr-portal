import { api } from '@/lib/api/client';

export type LeaveBalanceResponse = {
  leaveTypeId: string;
  leaveTypeCode: string;
  leaveTypeName: string;
  balance: number;
};

export type LeaveLedgerEntryDto = {
  id: string;
  transactionType: string;
  quantity: number;
  effectiveDate: string;
  sourceReference: string;
  createdAt: string;
};

export type LeaveRequestResponseDto = {
  id: string;
  employeeId: string;
  leaveTypeId: string;
  startDate: string;
  endDate: string;
  status: 'DRAFT' | 'SUBMITTED' | 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
  reason: string | null;
  totalDays: number;
};

export type LeaveRequestPayload = {
  leaveTypeId: string;
  startDate: string;
  endDate: string;
  reason?: string;
};

export function fetchMyLeaveBalances(): Promise<LeaveBalanceResponse[]> {
  return api.get<LeaveBalanceResponse[]>('/api/v1/me/leave-balances');
}

export function fetchMyLeaveLedger(leaveTypeId: string): Promise<LeaveLedgerEntryDto[]> {
  return api.get<LeaveLedgerEntryDto[]>(`/api/v1/me/leave-balances/${leaveTypeId}/ledger`);
}

export function fetchMyLeaveRequests(): Promise<LeaveRequestResponseDto[]> {
  return api.get<LeaveRequestResponseDto[]>('/api/v1/me/leave-requests');
}

export function createLeaveRequest(payload: LeaveRequestPayload): Promise<LeaveRequestResponseDto> {
  const query = new URLSearchParams({
    leaveTypeId: payload.leaveTypeId,
    startDate: payload.startDate,
    endDate: payload.endDate,
  });
  if (payload.reason) {
    query.set('reason', payload.reason);
  }
  return api.post<LeaveRequestResponseDto>(`/api/v1/me/leave-requests?${query.toString()}`);
}

export function submitLeaveRequest(requestId: string): Promise<void> {
  return api.post<void>(`/api/v1/me/leave-requests/${requestId}/submit`);
}

export function cancelLeaveRequest(requestId: string): Promise<void> {
  return api.post<void>(`/api/v1/me/leave-requests/${requestId}/cancel`);
}

export function fetchTeamLeaveCalendar(startDate: string, endDate: string): Promise<LeaveRequestResponseDto[]> {
  const query = new URLSearchParams({ startDate, endDate });
  return api.get<LeaveRequestResponseDto[]>(`/api/v1/me/team/leave-calendar?${query.toString()}`);
}

export function adjustLeaveBalance(params: {
  employeeId: string;
  leaveTypeId: string;
  quantity: number;
  reason: string;
}): Promise<void> {
  const query = new URLSearchParams({
    employeeId: params.employeeId,
    leaveTypeId: params.leaveTypeId,
    quantity: String(params.quantity),
    reason: params.reason,
  });
  return api.post<void>(`/api/v1/admin/leave-adjustments?${query.toString()}`);
}

export function runAccrual(period: string): Promise<void> {
  const query = new URLSearchParams({ period });
  return api.post<void>(`/api/v1/admin/leave-accrual-runs?${query.toString()}`);
}
