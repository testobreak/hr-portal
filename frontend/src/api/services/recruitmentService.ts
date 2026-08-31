import { api } from '@/lib/api/client';

export type JobRequisitionResponse = {
  id: string;
  jobOpeningId: string | null;
  reqNumber: string;
  jobTitle: string;
  departmentId: string | null;
  departmentName: string | null;
  designationId: string | null;
  designationTitle: string | null;
  locationId: string | null;
  locationName: string | null;
  legalEntityId: string | null;
  legalEntityName: string | null;
  employmentType: string;
  openingsCount: number;
  hiringManagerId: string | null;
  hiringManagerName: string | null;
  recruiterId: string | null;
  recruiterName: string | null;
  targetStartDate: string | null;
  minSalary: number | null;
  maxSalary: number | null;
  currencyCode: string | null;
  requiredSkills: string | null;
  minExperienceYears: number | null;
  description: string | null;
  justification: string | null;
  status: 'DRAFT' | 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED' | 'OPEN' | 'ON_HOLD' | 'CLOSED' | 'CANCELLED';
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type JobRequisitionCreateRequest = {
  jobTitle: string;
  departmentId?: string;
  designationId?: string;
  locationId?: string;
  legalEntityId?: string;
  employmentType: string;
  openingsCount: number;
  hiringManagerId?: string;
  targetStartDate?: string;
  minSalary?: number;
  maxSalary?: number;
  currencyCode?: string;
  requiredSkills?: string;
  minExperienceYears?: number;
  description?: string;
  justification?: string;
};

export function fetchRequisitions(): Promise<JobRequisitionResponse[]> {
  return api.get<JobRequisitionResponse[]>('/api/v1/recruitment/requisitions');
}

export function fetchRequisitionById(id: string): Promise<JobRequisitionResponse> {
  return api.get<JobRequisitionResponse>(`/api/v1/recruitment/requisitions/${id}`);
}

export function createRequisition(payload: JobRequisitionCreateRequest): Promise<JobRequisitionResponse> {
  return api.post<JobRequisitionResponse>('/api/v1/recruitment/requisitions', payload);
}

export function updateRequisition(id: string, payload: JobRequisitionCreateRequest): Promise<JobRequisitionResponse> {
  return api.patch<JobRequisitionResponse>(`/api/v1/recruitment/requisitions/${id}`, payload);
}

export function submitRequisition(id: string): Promise<void> {
  return api.post<void>(`/api/v1/recruitment/requisitions/${id}/submit`);
}

export function approveRequisition(id: string): Promise<void> {
  return api.post<void>(`/api/v1/recruitment/requisitions/${id}/approve`);
}

export function rejectRequisition(id: string): Promise<void> {
  return api.post<void>(`/api/v1/recruitment/requisitions/${id}/reject`);
}

export function closeRequisition(id: string): Promise<void> {
  return api.post<void>(`/api/v1/recruitment/requisitions/${id}/close`);
}

export type JobPostingResponse = {
  id: string;
  jobOpeningId: string;
  publicId: string;
  title: string;
  description: string;
  locationName: string | null;
  workArrangement: 'REMOTE' | 'ONSITE' | 'HYBRID';
  employmentType: string;
  applicationDeadline: string | null;
  status: 'DRAFT' | 'PUBLISHED' | 'UNPUBLISHED';
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type JobPostingCreateRequest = {
  title: string;
  description: string;
  locationName?: string;
  workArrangement: 'REMOTE' | 'ONSITE' | 'HYBRID';
  employmentType: string;
  applicationDeadline?: string;
};

export function createJobOpening(requisitionId: string): Promise<{ id: string; jobRequisitionId: string; status: string }> {
  return api.post<{ id: string; jobRequisitionId: string; status: string }>(`/api/v1/recruitment/job-openings?requisitionId=${requisitionId}`);
}

export function createJobPosting(openingId: string, payload: JobPostingCreateRequest): Promise<JobPostingResponse> {
  return api.post<JobPostingResponse>(`/api/v1/recruitment/job-openings/${openingId}/postings`, payload);
}

export function fetchJobPostings(): Promise<JobPostingResponse[]> {
  return api.get<JobPostingResponse[]>('/api/v1/recruitment/job-postings');
}

export function publishJobPosting(id: string): Promise<void> {
  return api.post<void>(`/api/v1/recruitment/job-postings/${id}/publish`);
}

export function unpublishJobPosting(id: string): Promise<void> {
  return api.post<void>(`/api/v1/recruitment/job-postings/${id}/unpublish`);
}

import { apiBase } from '@/api/apiConfig';

async function guestRequest<T>(method: string, path: string, body?: unknown, headers?: Record<string, string>): Promise<T> {
  const reqHeaders: Record<string, string> = {
    Accept: 'application/json',
    ...headers,
  };
  let reqBody: string | undefined;
  if (body !== undefined) {
    reqHeaders['Content-Type'] = 'application/json';
    reqBody = JSON.stringify(body);
  }
  const res = await fetch(`${apiBase}${path}`, {
    method,
    headers: reqHeaders,
    body: reqBody,
  });
  if (!res.ok) {
    throw new Error(`Guest API error: ${res.status}`);
  }
  if (res.status === 204) return undefined as T;
  const contentType = res.headers.get('content-type') ?? '';
  if (contentType.includes('json')) {
    return (await res.json()) as T;
  }
  return undefined as T;
}

export type CandidateApplicationResponse = {
  id: string;
  candidateId: string;
  candidateName: string;
  candidateEmail: string;
  candidatePhone: string | null;
  jobOpeningId: string;
  jobTitle: string;
  currentStage: 'APPLIED' | 'SCREENING' | 'TECHNICAL_INTERVIEW' | 'MANAGER_INTERVIEW' | 'OFFER' | 'HIRED' | 'REJECTED' | 'WITHDRAWN';
  status: string;
  source: string;
  resumeStorageKey: string | null;
  coverLetter: string | null;
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type CandidateApplicationCreateRequest = {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  resumeStorageKey: string;
  skills?: string;
  profileSummary?: string;
  coverLetter?: string;
  source?: string;
};

export function fetchPublicJobs(): Promise<JobPostingResponse[]> {
  return guestRequest<JobPostingResponse[]>('GET', '/api/v1/careers/jobs');
}

export function fetchPublicJobDetails(publicId: string): Promise<JobPostingResponse> {
  return guestRequest<JobPostingResponse>('GET', `/api/v1/careers/jobs/${publicId}`);
}

export function submitGuestResume(filename: string, contentType: string): Promise<{ documentId: string; uploadUrl: string; httpMethod: string; storageKey: string }> {
  return guestRequest<{ documentId: string; uploadUrl: string; httpMethod: string; storageKey: string }>(
    'POST',
    `/api/v1/careers/resume/presign-upload?filename=${encodeURIComponent(filename)}&contentType=${encodeURIComponent(contentType)}`
  );
}

export function submitApplication(publicId: string, payload: CandidateApplicationCreateRequest): Promise<CandidateApplicationResponse> {
  return guestRequest<CandidateApplicationResponse>('POST', `/api/v1/careers/jobs/${publicId}/applications`, payload);
}

export function fetchApplicationsForOpening(openingId: string): Promise<CandidateApplicationResponse[]> {
  return api.get<CandidateApplicationResponse[]>(`/api/v1/recruitment/job-openings/${openingId}/applications`);
}

export function moveApplicationStage(id: string, stage: string): Promise<void> {
  return api.post<void>(`/api/v1/recruitment/applications/${id}/move-stage?stage=${encodeURIComponent(stage)}`);
}

export function rejectApplication(id: string): Promise<void> {
  return api.post<void>(`/api/v1/recruitment/applications/${id}/reject`);
}

export function withdrawApplication(id: string): Promise<void> {
  return api.post<void>(`/api/v1/recruitment/applications/${id}/withdraw`);
}

export function getResumeDownloadUrl(id: string): Promise<{ downloadUrl: string }> {
  return api.get<{ downloadUrl: string }>(`/api/v1/recruitment/applications/${id}/resume-download`);
}

export type InterviewResponse = {
  id: string;
  candidateApplicationId: string;
  interviewType: 'SCREENING' | 'TECHNICAL' | 'MANAGERIAL' | 'HR';
  scheduledTime: string;
  status: 'SCHEDULED' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW';
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type InterviewFeedbackResponse = {
  id: string;
  interviewId: string;
  interviewerId: string;
  interviewerName: string;
  score: number;
  recommendation: 'STRONG_HIRE' | 'HIRE' | 'MIXED' | 'NO_HIRE' | 'STRONG_NO_HIRE';
  comments: string | null;
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type InterviewScheduleRequest = {
  interviewType: 'SCREENING' | 'TECHNICAL' | 'MANAGERIAL' | 'HR';
  scheduledTime: string;
};

export type InterviewFeedbackSubmitRequest = {
  interviewerId: string;
  score: number;
  recommendation: 'STRONG_HIRE' | 'HIRE' | 'MIXED' | 'NO_HIRE' | 'STRONG_NO_HIRE';
  comments?: string;
};

export function scheduleInterview(applicationId: string, payload: InterviewScheduleRequest): Promise<InterviewResponse> {
  return api.post<InterviewResponse>(`/api/v1/recruitment/applications/${applicationId}/interviews`, payload);
}

export function fetchInterviewsForApplication(applicationId: string): Promise<InterviewResponse[]> {
  return api.get<InterviewResponse[]>(`/api/v1/recruitment/applications/${applicationId}/interviews`);
}

export function submitFeedback(interviewId: string, payload: InterviewFeedbackSubmitRequest): Promise<InterviewFeedbackResponse> {
  return api.post<InterviewFeedbackResponse>(`/api/v1/recruitment/interviews/${interviewId}/feedback`, payload);
}

export function fetchFeedbackForInterview(interviewId: string): Promise<InterviewFeedbackResponse[]> {
  return api.get<InterviewFeedbackResponse[]>(`/api/v1/recruitment/interviews/${interviewId}/feedback`);
}

export function fetchFeedbackForApplication(applicationId: string): Promise<InterviewFeedbackResponse[]> {
  return api.get<InterviewFeedbackResponse[]>(`/api/v1/recruitment/applications/${applicationId}/feedbacks`);
}

export function completeInterview(id: string): Promise<void> {
  return api.post<void>(`/api/v1/recruitment/interviews/${id}/complete`);
}

export function cancelInterview(id: string): Promise<void> {
  return api.post<void>(`/api/v1/recruitment/interviews/${id}/cancel`);
}

export type OfferResponse = {
  id: string;
  candidateApplicationId: string;
  candidateName: string;
  candidateEmail: string;
  jobTitle: string;
  salaryAmount: number;
  currencyCode: string;
  startDate: string;
  status: 'DRAFT' | 'PENDING_APPROVAL' | 'APPROVED' | 'SENT' | 'ACCEPTED' | 'REJECTED' | 'EXPIRED';
  secureToken: string;
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type OfferCreateRequest = {
  salaryAmount: number;
  currencyCode: string;
  startDate: string;
};

export function createOffer(applicationId: string, payload: OfferCreateRequest): Promise<OfferResponse> {
  return api.post<OfferResponse>(`/api/v1/recruitment/applications/${applicationId}/offers`, payload);
}

export function fetchOffersForApplication(applicationId: string): Promise<OfferResponse[]> {
  return api.get<OfferResponse[]>(`/api/v1/recruitment/applications/${applicationId}/offers`);
}

export function approveOffer(id: string): Promise<void> {
  return api.post<void>(`/api/v1/recruitment/offers/${id}/approve`);
}

export function submitOfferForApproval(id: string): Promise<void> {
  return api.post<void>(`/api/v1/recruitment/offers/${id}/submit`);
}

export function releaseOffer(id: string): Promise<void> {
  return api.post<void>(`/api/v1/recruitment/offers/${id}/release`);
}

export function fetchPublicOfferDetails(secureToken: string): Promise<OfferResponse> {
  return guestRequest<OfferResponse>('GET', `/api/v1/careers/offers/${secureToken}`);
}

export function acceptPublicOffer(secureToken: string): Promise<OfferResponse> {
  return guestRequest<OfferResponse>('POST', `/api/v1/careers/offers/${secureToken}/accept`);
}

export function rejectPublicOffer(secureToken: string): Promise<OfferResponse> {
  return guestRequest<OfferResponse>('POST', `/api/v1/careers/offers/${secureToken}/reject`);
}
