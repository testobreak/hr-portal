import { api } from '@/lib/api/client';

export type ProfileResponse = {
  personal: {
    firstName: string;
    lastName: string;
    preferredName: string | null;
    email: string;
    phoneNumber: string | null;
    dateOfBirth: string | null;
    bankAccountNumber: string | null;
    taxId: string | null;
  };
  emergencyContacts: Array<{
    id: string;
    name: string;
    relationship: string;
    phone: string;
  }>;
  dependents: Array<{
    id: string;
    name: string;
    relationship: string;
    dateOfBirth: string | null;
  }>;
  education: Array<{
    id: string;
    institution: string;
    degree: string;
    yearOfPassing: number | null;
  }>;
  experience: Array<{
    id: string;
    companyName: string;
    role: string;
    startDate: string;
    endDate: string | null;
  }>;
  skills: Array<{
    id: string;
    skillName: string;
    proficiency: string | null;
  }>;
  certifications: Array<{
    id: string;
    certificationName: string;
    issuer: string | null;
    expiryDate: string | null;
  }>;
};

export type ProfileChangeRequestResponse = {
  id: string;
  employeeId: string;
  employeeName: string | null;
  changeJson: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
  createdAt: string;
  createdBy: string | null;
  updatedAt: string;
  updatedBy: string | null;
};

export function fetchMyProfile(): Promise<ProfileResponse> {
  return api.get<ProfileResponse>('/api/v1/me/profile');
}

export function updateMyProfile(updates: Record<string, string>): Promise<void> {
  return api.patch<void>('/api/v1/me/profile', updates);
}

export function submitProfileChangeRequest(updates: Record<string, string>): Promise<ProfileChangeRequestResponse> {
  return api.post<ProfileChangeRequestResponse>('/api/v1/me/profile-change-requests', updates);
}

export function fetchPendingChangeRequests(): Promise<ProfileChangeRequestResponse[]> {
  return api.get<ProfileChangeRequestResponse[]>('/api/v1/me/profile-change-requests');
}

export function fetchChangeRequestById(requestId: string): Promise<ProfileChangeRequestResponse> {
  return api.get<ProfileChangeRequestResponse>(`/api/v1/profile-change-requests/${requestId}`);
}

export function approveChangeRequest(requestId: string): Promise<void> {
  return api.post<void>(`/api/v1/profile-change-requests/${requestId}/approve`);
}

export function rejectChangeRequest(requestId: string): Promise<void> {
  return api.post<void>(`/api/v1/profile-change-requests/${requestId}/reject`);
}

export function cancelChangeRequest(requestId: string): Promise<void> {
  return api.post<void>(`/api/v1/profile-change-requests/${requestId}/cancel`);
}
