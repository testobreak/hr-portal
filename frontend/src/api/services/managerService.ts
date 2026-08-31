import { api } from '@/lib/api/client';
import type { EmployeeSummary } from '@/lib/api/types';

export type ManagerDelegation = {
  id: string;
  managerId: string;
  delegateId: string;
  startDate: string;
  endDate: string;
  status: 'ACTIVE' | 'REVOKED';
};

export function fetchMyTeam(scope: 'direct' | 'all' = 'direct'): Promise<EmployeeSummary[]> {
  return api.get<EmployeeSummary[]>(`/api/v1/me/team?scope=${scope}`);
}

export function createDelegation(params: {
  delegateId: string;
  startDate: string;
  endDate: string;
}): Promise<ManagerDelegation> {
  const query = new URLSearchParams({
    delegateId: params.delegateId,
    startDate: params.startDate,
    endDate: params.endDate,
  });
  return api.post<ManagerDelegation>(`/api/v1/me/delegations?${query.toString()}`);
}

export function revokeDelegation(delegationId: string): Promise<void> {
  return api.delete<void>(`/api/v1/me/delegations/${delegationId}`);
}
