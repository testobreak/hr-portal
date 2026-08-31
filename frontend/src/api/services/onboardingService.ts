import { api } from '@/lib/api/client';

export type PreHireResponse = {
  id: string;
  candidateId: string;
  candidateName: string;
  candidateEmail: string;
  acceptedOfferId: string;
  legalEntityName: string;
  departmentName: string;
  designationTitle: string;
  locationName: string;
  managerName: string;
  startDate: string;
  status: 'CREATED' | 'ONBOARDING_IN_PROGRESS' | 'READY_FOR_ACTIVATION' | 'ACTIVATED' | 'WITHDRAWN';
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type OnboardingTaskResponse = {
  id: string;
  onboardingPlanId: string;
  taskName: string;
  description: string;
  assignedRole: 'CANDIDATE' | 'HR' | 'IT' | 'MANAGER';
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'WAIVED';
  dueDate: string | null;
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type OnboardingPlanResponse = {
  id: string;
  preHireId: string;
  templateName: string;
  tasks: OnboardingTaskResponse[];
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type OnboardingDocumentResponse = {
  id: string;
  preHireId: string;
  documentType: string;
  storageKey: string | null;
  status: 'REQUESTED' | 'UPLOADED' | 'ACCEPTED' | 'REJECTED';
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type EmployeeActivationRequest = {
  employeeCode: string;
  dateOfBirth: string;
  phoneNumber?: string;
};

export function fetchPreHires(): Promise<PreHireResponse[]> {
  return api.get<PreHireResponse[]>('/api/v1/onboarding/pre-hires');
}

export function fetchPreHirePlan(id: string): Promise<OnboardingPlanResponse> {
  return api.get<OnboardingPlanResponse>(`/api/v1/onboarding/pre-hires/${id}/plan`);
}

export function createPreHirePlan(id: string, templateName: string = 'STANDARD'): Promise<OnboardingPlanResponse> {
  return api.post<OnboardingPlanResponse>(`/api/v1/onboarding/pre-hires/${id}/plan?templateName=${encodeURIComponent(templateName)}`);
}

export function updateOnboardingTaskStatus(taskId: string, status: string): Promise<OnboardingTaskResponse> {
  return api.post<OnboardingTaskResponse>(`/api/v1/onboarding/tasks/${taskId}/status?status=${encodeURIComponent(status)}`);
}

export function fetchPreHireDocuments(id: string): Promise<OnboardingDocumentResponse[]> {
  return api.get<OnboardingDocumentResponse[]>(`/api/v1/onboarding/pre-hires/${id}/documents`);
}

export function requestPreHireDocument(id: string, documentType: string): Promise<OnboardingDocumentResponse> {
  return api.post<OnboardingDocumentResponse>(`/api/v1/onboarding/pre-hires/${id}/documents?documentType=${encodeURIComponent(documentType)}`);
}

export function uploadPreHireDocument(docId: string, storageKey: string): Promise<OnboardingDocumentResponse> {
  return api.post<OnboardingDocumentResponse>(`/api/v1/onboarding/documents/${docId}/upload?storageKey=${encodeURIComponent(storageKey)}`);
}

export function reviewPreHireDocument(docId: string, status: string): Promise<OnboardingDocumentResponse> {
  return api.post<OnboardingDocumentResponse>(`/api/v1/onboarding/documents/${docId}/review?status=${encodeURIComponent(status)}`);
}

export function activatePreHire(id: string, payload: EmployeeActivationRequest): Promise<any> {
  return api.post<any>(`/api/v1/onboarding/pre-hires/${id}/activate`, payload);
}

export type BackgroundCheckResponse = {
  id: string;
  preHireId: string;
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'CLEARED' | 'FAILED';
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type AssetRequestResponse = {
  id: string;
  preHireId: string;
  assetType: string;
  status: 'REQUESTED' | 'RESERVED' | 'DELIVERED';
  createdAt: string;
  updatedAt: string;
  version: number;
};

export function fetchPreHireBackgroundChecks(id: string): Promise<BackgroundCheckResponse[]> {
  return api.get<BackgroundCheckResponse[]>(`/api/v1/onboarding/pre-hires/${id}/background-checks`);
}

export function triggerPreHireBackgroundCheck(id: string): Promise<BackgroundCheckResponse> {
  return api.post<BackgroundCheckResponse>(`/api/v1/onboarding/pre-hires/${id}/background-checks`);
}

export function updateBackgroundCheckStatus(checkId: string, status: string): Promise<BackgroundCheckResponse> {
  return api.post<BackgroundCheckResponse>(`/api/v1/onboarding/background-checks/${checkId}/status?status=${encodeURIComponent(status)}`);
}

export function fetchPreHireAssets(id: string): Promise<AssetRequestResponse[]> {
  return api.get<AssetRequestResponse[]>(`/api/v1/onboarding/pre-hires/${id}/assets`);
}

export function requestPreHireAsset(id: string, assetType: string): Promise<AssetRequestResponse> {
  return api.post<AssetRequestResponse>(`/api/v1/onboarding/pre-hires/${id}/assets?assetType=${encodeURIComponent(assetType)}`);
}

export function updateAssetStatus(assetId: string, status: string): Promise<AssetRequestResponse> {
  return api.post<AssetRequestResponse>(`/api/v1/onboarding/assets/${assetId}/status?status=${encodeURIComponent(status)}`);
}
