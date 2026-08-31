import { api } from '@/lib/api/client';
import { apiBase } from '@/api/apiConfig';
import { getAccessToken } from '@/auth/keycloak';

export type PayrollRunResponse = {
  id: string;
  periodStart: string;
  periodEnd: string;
  status: 'DRAFT' | 'COMPUTING' | 'COMPLETED' | 'APPROVED' | 'PAID' | 'CANCELLED';
  payoutDate: string | null;
  totalGross: number;
  totalDeductions: number;
  totalNet: number;
  runType: 'REGULAR' | 'OFF_CYCLE';
};

export type PayslipItemResponse = {
  id: string;
  itemName: string;
  itemType: 'ALLOWANCE' | 'DEDUCTION' | 'TAX';
  amount: number;
};

export type PayslipResponse = {
  id: string;
  payrollRunId: string;
  employeeId: string;
  employeeName: string;
  basicSalary: number;
  allowances: number;
  deductions: number;
  taxDeductions: number;
  netSalary: number;
  workingDays: number;
  presentDays: number;
  leaveDays: number;
  currencyCode: string;
  status: 'DRAFT' | 'APPROVED' | 'PAID';
  sentAt: string | null;
  items: PayslipItemResponse[];
};

export type PayrollRunCreateRequest = {
  periodStart: string;
  periodEnd: string;
  payoutDate?: string;
  runType: 'REGULAR' | 'OFF_CYCLE';
};

// Payroll Runs API
export function createPayrollRun(payload: PayrollRunCreateRequest): Promise<PayrollRunResponse> {
  return api.post<PayrollRunResponse>('/api/v1/payroll/runs', payload);
}

export function calculatePayrollRun(runId: string): Promise<PayrollRunResponse> {
  return api.post<PayrollRunResponse>(`/api/v1/payroll/runs/${runId}/calculate`);
}

export function approvePayrollRun(runId: string): Promise<PayrollRunResponse> {
  return api.post<PayrollRunResponse>(`/api/v1/payroll/runs/${runId}/approve`);
}

export function markPaidPayrollRun(runId: string): Promise<PayrollRunResponse> {
  return api.post<PayrollRunResponse>(`/api/v1/payroll/runs/${runId}/pay`);
}

export function fetchPayrollRuns(): Promise<PayrollRunResponse[]> {
  return api.get<PayrollRunResponse[]>('/api/v1/payroll/runs');
}

// Payslips API
export function fetchPayslipsForRun(runId: string): Promise<PayslipResponse[]> {
  return api.get<PayslipResponse[]>(`/api/v1/payroll/runs/${runId}/payslips`);
}

export function fetchEmployeePayslipForRun(runId: string): Promise<PayslipResponse> {
  return api.get<PayslipResponse>(`/api/v1/payroll/runs/${runId}/payslips/me`);
}

export function fetchEmployeePayslipsHistory(): Promise<PayslipResponse[]> {
  return api.get<PayslipResponse[]>('/api/v1/payroll/me/payslips');
}

// Exporters
export async function downloadPayrollLedger(runId: string) {
  const token = await getAccessToken();
  const res = await fetch(`${apiBase}/api/v1/payroll/runs/${runId}/export/ledger`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  if (!res.ok) throw new Error('Failed to download general ledger export');
  const blob = await res.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `ledger_run_${runId}.csv`;
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
}

export async function downloadPayrollBankDisbursements(runId: string) {
  const token = await getAccessToken();
  const res = await fetch(`${apiBase}/api/v1/payroll/runs/${runId}/export/bank`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  if (!res.ok) throw new Error('Failed to download bank direct deposit file');
  const blob = await res.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `bank_disbursements_run_${runId}.csv`;
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
}
