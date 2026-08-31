import { api } from '@/lib/api/client';

export type AttendanceResponse = {
  id: string;
  employeeId: string;
  employeeName: string;
  clockIn: string;
  clockOut: string | null;
  status: 'PRESENT' | 'ABSENT' | 'LATE' | 'HALF_DAY' | 'ON_LEAVE';
  workingHours: number | null;
  ipAddress: string | null;
  notes: string | null;
};

export type ClockInRequest = {
  ipAddress?: string;
  notes?: string;
};

export type ClockOutRequest = {
  notes?: string;
};

export type TimesheetLineResponse = {
  id: string;
  dayDate: string;
  hoursWorked: number;
  notes: string | null;
};

export type TimesheetResponse = {
  id: string;
  employeeId: string;
  employeeName: string;
  startDate: string;
  endDate: string;
  totalHours: number;
  status: 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED';
  approvedById: string | null;
  approvedByName: string | null;
  approvedAt: string | null;
  submissionComments: string | null;
  approvalComments: string | null;
  lines: TimesheetLineResponse[];
};

export type TimesheetLineCreateRequest = {
  dayDate: string;
  hoursWorked: number;
  notes?: string;
};

export type TimesheetCreateRequest = {
  startDate: string;
  endDate: string;
  lines: TimesheetLineCreateRequest[];
  submissionComments?: string;
};

export type TimesheetApproveRequest = {
  approvalComments?: string;
};

// Attendance API (Uses /me convenience routes)
export function clockIn(payload: ClockInRequest = {}): Promise<AttendanceResponse> {
  return api.post<AttendanceResponse>('/api/v1/attendance/me/clock-in', payload);
}

export function clockOut(payload: ClockOutRequest = {}): Promise<AttendanceResponse> {
  return api.post<AttendanceResponse>('/api/v1/attendance/me/clock-out', payload);
}

export function fetchLatestAttendance(): Promise<AttendanceResponse | null> {
  return api.get<AttendanceResponse | null>('/api/v1/attendance/me/latest');
}

export function fetchEmployeeAttendanceLogs(startIso: string, endIso: string): Promise<AttendanceResponse[]> {
  return api.get<AttendanceResponse[]>(`/api/v1/attendance/me/logs?startIso=${encodeURIComponent(startIso)}&endIso=${encodeURIComponent(endIso)}`);
}

export function fetchAllAttendanceLogs(startIso: string, endIso: string): Promise<AttendanceResponse[]> {
  return api.get<AttendanceResponse[]>(`/api/v1/attendance/logs?startIso=${encodeURIComponent(startIso)}&endIso=${encodeURIComponent(endIso)}`);
}

// Timesheets API
export function saveTimesheetDraft(payload: TimesheetCreateRequest): Promise<TimesheetResponse> {
  return api.post<TimesheetResponse>('/api/v1/attendance/me/timesheets', payload);
}

export function submitTimesheetForApproval(timesheetId: string): Promise<TimesheetResponse> {
  return api.post<TimesheetResponse>(`/api/v1/attendance/timesheets/${timesheetId}/submit`);
}

export function approveTimesheet(timesheetId: string, payload: TimesheetApproveRequest = {}): Promise<TimesheetResponse> {
  return api.post<TimesheetResponse>(`/api/v1/attendance/timesheets/${timesheetId}/approve`, payload);
}

export function rejectTimesheet(timesheetId: string, payload: TimesheetApproveRequest = {}): Promise<TimesheetResponse> {
  return api.post<TimesheetResponse>(`/api/v1/attendance/timesheets/${timesheetId}/reject`, payload);
}

export function fetchEmployeeTimesheets(): Promise<TimesheetResponse[]> {
  return api.get<TimesheetResponse[]>('/api/v1/attendance/me/timesheets');
}

export function fetchPendingTimesheets(): Promise<TimesheetResponse[]> {
  return api.get<TimesheetResponse[]>('/api/v1/attendance/timesheets/pending');
}

export function fetchTimesheetDetails(timesheetId: string): Promise<TimesheetResponse> {
  return api.get<TimesheetResponse>(`/api/v1/attendance/timesheets/${timesheetId}`);
}
