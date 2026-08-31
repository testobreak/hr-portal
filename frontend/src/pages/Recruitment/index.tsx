import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { useMe } from '@/hooks/useMe';
import { Roles, hasAnyRole } from '@/lib/roles';
import {
  fetchRequisitions,
  createRequisition,
  submitRequisition,
  closeRequisition,
  fetchJobPostings,
  createJobPosting,
  publishJobPosting,
  unpublishJobPosting,
  fetchApplicationsForOpening,
  moveApplicationStage,
  rejectApplication,
  getResumeDownloadUrl,
  scheduleInterview,
  fetchInterviewsForApplication,
  submitFeedback,
  fetchFeedbackForApplication,
  completeInterview,
  cancelInterview,
  createOffer,
  fetchOffersForApplication,
  approveOffer,
  submitOfferForApproval,
  releaseOffer,
  type JobRequisitionResponse,
  type JobPostingResponse,
  type CandidateApplicationResponse
} from '@/api/services/recruitmentService';
import {
  fetchPendingApprovals,
  approveRequest,
  rejectRequest
} from '@/api/services/workflowService';
import {
  fetchDepartmentLookups,
  fetchLocationLookups,
  fetchLegalEntityLookups,
  fetchDesignationLookups
} from '@/api/services/lookupsService';
import { projectApi } from '@/api/services/projectManagementService';

type TabType = 'requisitions' | 'postings' | 'approvals';

const PIPELINE_STAGES = [
  { key: 'APPLIED', label: 'Applied', next: 'SCREENING' },
  { key: 'SCREENING', label: 'Screening', next: 'TECHNICAL_INTERVIEW' },
  { key: 'TECHNICAL_INTERVIEW', label: 'Tech Interview', next: 'MANAGER_INTERVIEW' },
  { key: 'MANAGER_INTERVIEW', label: 'Manager Interview', next: 'OFFER' },
  { key: 'OFFER', label: 'Offer', next: 'HIRED' },
  { key: 'HIRED', label: 'Hired', next: null },
];

export function RecruitmentPage() {
  const queryClient = useQueryClient();
  const { data: me } = useMe();
  const roles = me?.roles ?? [];
  const isHrOrAdmin = hasAnyRole(roles, [Roles.HR_ADMIN, Roles.SUPER_ADMIN]);

  const [activeTab, setActiveTab] = useState<TabType>('requisitions');
  const [selectedReq, setSelectedReq] = useState<JobRequisitionResponse | null>(null);
  const [selectedPosting, setSelectedPosting] = useState<JobPostingResponse | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [isPostingFormOpen, setIsPostingFormOpen] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Requisition Form State
  const [jobTitle, setJobTitle] = useState('');
  const [departmentId, setDepartmentId] = useState('');
  const [designationId, setDesignationId] = useState('');
  const [locationId, setLocationId] = useState('');
  const [legalEntityId, setLegalEntityId] = useState('');
  const [employmentType, setEmploymentType] = useState('FULL_TIME');
  const [openingsCount, setOpeningsCount] = useState(1);
  const [hiringManagerId, setHiringManagerId] = useState('');
  const [targetStartDate, setTargetStartDate] = useState('');
  const [minSalary, setMinSalary] = useState('');
  const [maxSalary, setMaxSalary] = useState('');
  const [requiredSkills, setRequiredSkills] = useState('');
  const [minExperienceYears, setMinExperienceYears] = useState('');
  const [description, setDescription] = useState('');
  const [justification, setJustification] = useState('');

  // Posting Form State
  const [targetOpeningId, setTargetOpeningId] = useState('');
  const [postingTitle, setPostingTitle] = useState('');
  const [postingDescription, setPostingDescription] = useState('');
  const [postingLocation, setPostingLocation] = useState('');
  const [postingWorkArrangement, setPostingWorkArrangement] = useState<'REMOTE' | 'ONSITE' | 'HYBRID'>('REMOTE');
  const [postingEmploymentType, setPostingEmploymentType] = useState('FULL_TIME');
  const [postingDeadline, setPostingDeadline] = useState('');

  // Candidate detail state
  const [selectedAppDetail, setSelectedAppDetail] = useState<CandidateApplicationResponse | null>(null);
  const [detailTab, setDetailTab] = useState<'overview' | 'interviews' | 'offer'>('overview');

  // Interview Schedule Form State
  const [isSchedulingOpen, setIsSchedulingOpen] = useState(false);
  const [schedType, setSchedType] = useState<'SCREENING' | 'TECHNICAL' | 'MANAGERIAL' | 'HR'>('TECHNICAL');
  const [schedTime, setSchedTime] = useState('');

  // Feedback Scorecard Form State
  const [isFeedbackOpen, setIsFeedbackOpen] = useState(false);
  const [feedbackInterviewId, setFeedbackInterviewId] = useState('');
  const [feedbackInterviewerId, setFeedbackInterviewerId] = useState('');
  const [feedbackScore, setFeedbackScore] = useState(5);
  const [feedbackRec, setFeedbackRec] = useState<'STRONG_HIRE' | 'HIRE' | 'MIXED' | 'NO_HIRE' | 'STRONG_NO_HIRE'>('HIRE');
  const [feedbackComments, setFeedbackComments] = useState('');

  // Offer Form State
  const [offerSalaryAmount, setOfferSalaryAmount] = useState('');
  const [offerCurrency, setOfferCurrency] = useState('USD');
  const [offerStartDate, setOfferStartDate] = useState('');

  // Queries
  const { data: requisitions, isLoading: reqsLoading } = useQuery({
    queryKey: ['recruitment', 'requisitions'],
    queryFn: fetchRequisitions,
  });

  const { data: postings, isLoading: postingsLoading } = useQuery({
    queryKey: ['recruitment', 'postings'],
    queryFn: fetchJobPostings,
  });

  const { data: approvals, isLoading: approvalsLoading } = useQuery({
    queryKey: ['workflow', 'approvals'],
    queryFn: fetchPendingApprovals,
    enabled: isHrOrAdmin,
  });

  // Query for candidates of selected posting
  const { data: applications, isLoading: appsLoading } = useQuery({
    queryKey: ['recruitment', 'postings', selectedPosting?.jobOpeningId, 'applications'],
    queryFn: () => fetchApplicationsForOpening(selectedPosting!.jobOpeningId),
    enabled: !!selectedPosting,
  });

  // Query for interviews of selected candidate
  const { data: candidateInterviews } = useQuery({
    queryKey: ['recruitment', 'candidates', selectedAppDetail?.id, 'interviews'],
    queryFn: () => fetchInterviewsForApplication(selectedAppDetail!.id),
    enabled: !!selectedAppDetail,
  });

  // Query for feedbacks of selected candidate
  const { data: candidateFeedbacks } = useQuery({
    queryKey: ['recruitment', 'candidates', selectedAppDetail?.id, 'feedbacks'],
    queryFn: () => fetchFeedbackForApplication(selectedAppDetail!.id),
    enabled: !!selectedAppDetail,
  });

  // Query for offers of selected candidate
  const { data: candidateOffers } = useQuery({
    queryKey: ['recruitment', 'candidates', selectedAppDetail?.id, 'offers'],
    queryFn: () => fetchOffersForApplication(selectedAppDetail!.id),
    enabled: !!selectedAppDetail,
  });

  // Lookups Queries
  const { data: departments } = useQuery({
    queryKey: ['lookups', 'departments'],
    queryFn: fetchDepartmentLookups,
  });

  const { data: designations } = useQuery({
    queryKey: ['lookups', 'designations'],
    queryFn: fetchDesignationLookups,
  });

  const { data: locations } = useQuery({
    queryKey: ['lookups', 'locations'],
    queryFn: fetchLocationLookups,
  });

  const { data: legalEntities } = useQuery({
    queryKey: ['lookups', 'legal-entities'],
    queryFn: fetchLegalEntityLookups,
  });

  const { data: employeesData } = useQuery({
    queryKey: ['project-mgmt', 'employees'],
    queryFn: projectApi.employees,
  });
  const employees = (employeesData as any)?.content ?? [];

  // Mutations
  const createMutation = useMutation({
    mutationFn: createRequisition,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recruitment', 'requisitions'] });
      setIsFormOpen(false);
      resetForm();
      setMessage({ type: 'success', text: 'Requisition draft created successfully.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to create requisition.' });
    },
  });

  const submitMutation = useMutation({
    mutationFn: submitRequisition,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recruitment', 'requisitions'] });
      queryClient.invalidateQueries({ queryKey: ['workflow', 'approvals'] });
      setSelectedReq(null);
      setMessage({ type: 'success', text: 'Requisition submitted for approval.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to submit requisition.' });
    },
  });

  const approveMutation = useMutation({
    mutationFn: approveRequest,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workflow', 'approvals'] });
      queryClient.invalidateQueries({ queryKey: ['recruitment', 'requisitions'] });
      setMessage({ type: 'success', text: 'Requisition request approved successfully.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to approve requisition request.' });
    },
  });

  const rejectMutation = useMutation({
    mutationFn: rejectRequest,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workflow', 'approvals'] });
      queryClient.invalidateQueries({ queryKey: ['recruitment', 'requisitions'] });
      setMessage({ type: 'success', text: 'Requisition request rejected.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to reject request.' });
    },
  });

  const closeMutation = useMutation({
    mutationFn: closeRequisition,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recruitment', 'requisitions'] });
      setSelectedReq(null);
      setMessage({ type: 'success', text: 'Requisition closed.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to close requisition.' });
    },
  });

  // Posting Mutations
  const createPostingMutation = useMutation({
    mutationFn: (payload: { openingId: string; body: any }) => createJobPosting(payload.openingId, payload.body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recruitment', 'postings'] });
      setIsPostingFormOpen(false);
      setMessage({ type: 'success', text: 'Job advertisement posting draft created.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to create job posting.' });
    },
  });

  const publishPostingMutation = useMutation({
    mutationFn: publishJobPosting,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recruitment', 'postings'] });
      setSelectedPosting(null);
      setMessage({ type: 'success', text: 'Job posting published successfully!' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to publish job posting.' });
    },
  });

  const unpublishPostingMutation = useMutation({
    mutationFn: unpublishJobPosting,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recruitment', 'postings'] });
      setSelectedPosting(null);
      setMessage({ type: 'success', text: 'Job posting unpublished.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to unpublish job posting.' });
    },
  });

  // Candidate evaluation mutations
  const advanceStageMutation = useMutation({
    mutationFn: (payload: { id: string; stage: string }) => moveApplicationStage(payload.id, payload.stage),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recruitment', 'postings', selectedPosting?.jobOpeningId, 'applications'] });
      setMessage({ type: 'success', text: 'Candidate advanced to the next stage successfully.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to move candidate stage.' });
    },
  });

  const rejectAppMutation = useMutation({
    mutationFn: rejectApplication,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recruitment', 'postings', selectedPosting?.jobOpeningId, 'applications'] });
      setSelectedAppDetail(null);
      setMessage({ type: 'success', text: 'Candidate application rejected.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to reject candidate.' });
    },
  });

  // Interview Mutations
  const scheduleInterviewMutation = useMutation({
    mutationFn: (payload: { appId: string; body: any }) => scheduleInterview(payload.appId, payload.body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recruitment', 'candidates', selectedAppDetail?.id, 'interviews'] });
      setIsSchedulingOpen(false);
      setSchedTime('');
      setMessage({ type: 'success', text: 'Interview slot scheduled successfully.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to schedule interview.' });
    },
  });

  const completeInterviewMutation = useMutation({
    mutationFn: completeInterview,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recruitment', 'candidates', selectedAppDetail?.id, 'interviews'] });
      setMessage({ type: 'success', text: 'Interview status marked as COMPLETED.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to complete interview.' });
    },
  });

  const cancelInterviewMutation = useMutation({
    mutationFn: cancelInterview,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recruitment', 'candidates', selectedAppDetail?.id, 'interviews'] });
      setMessage({ type: 'success', text: 'Interview cancelled.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to cancel interview.' });
    },
  });

  const submitFeedbackMutation = useMutation({
    mutationFn: (payload: { interviewId: string; body: any }) => submitFeedback(payload.interviewId, payload.body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recruitment', 'candidates', selectedAppDetail?.id, 'feedbacks'] });
      setIsFeedbackOpen(false);
      setFeedbackInterviewerId('');
      setFeedbackComments('');
      setFeedbackScore(5);
      setMessage({ type: 'success', text: 'Interviewer scorecard feedback recorded successfully.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to submit feedback scorecard.' });
    },
  });

  // Offer Mutations
  const createOfferMutation = useMutation({
    mutationFn: (payload: { appId: string; body: any }) => createOffer(payload.appId, payload.body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recruitment', 'candidates', selectedAppDetail?.id, 'offers'] });
      setOfferSalaryAmount('');
      setOfferStartDate('');
      setMessage({ type: 'success', text: 'Offer letter draft created.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to create offer letter.' });
    },
  });

  const approveOfferMutation = useMutation({
    mutationFn: approveOffer,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recruitment', 'candidates', selectedAppDetail?.id, 'offers'] });
      setMessage({ type: 'success', text: 'Offer approved successfully.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to approve offer.' });
    },
  });

  const submitOfferMutation = useMutation({
    mutationFn: submitOfferForApproval,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recruitment', 'candidates', selectedAppDetail?.id, 'offers'] });
      setMessage({ type: 'success', text: 'Offer submitted for approval successfully.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to submit offer for approval.' });
    },
  });

  const releaseOfferMutation = useMutation({
    mutationFn: releaseOffer,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recruitment', 'candidates', selectedAppDetail?.id, 'offers'] });
      setMessage({ type: 'success', text: 'Offer letter released and sent to the candidate!' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to release offer.' });
    },
  });

  const handleDownloadResume = async (appId: string) => {
    try {
      const res = await getResumeDownloadUrl(appId);
      window.open(res.downloadUrl, '_blank');
    } catch (err: any) {
      alert(err?.message || 'Failed to get resume download link.');
    }
  };

  const resetForm = () => {
    setJobTitle('');
    setDepartmentId('');
    setDesignationId('');
    setLocationId('');
    setLegalEntityId('');
    setEmploymentType('FULL_TIME');
    setOpeningsCount(1);
    setHiringManagerId('');
    setTargetStartDate('');
    setMinSalary('');
    setMaxSalary('');
    setRequiredSkills('');
    setMinExperienceYears('');
    setDescription('');
    setJustification('');
  };

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault();
    if (!jobTitle || !employmentType) return;
    setMessage(null);
    createMutation.mutate({
      jobTitle,
      departmentId: departmentId || undefined,
      designationId: designationId || undefined,
      locationId: locationId || undefined,
      legalEntityId: legalEntityId || undefined,
      employmentType,
      openingsCount,
      hiringManagerId: hiringManagerId || undefined,
      targetStartDate: targetStartDate || undefined,
      minSalary: minSalary ? Number(minSalary) : undefined,
      maxSalary: maxSalary ? Number(maxSalary) : undefined,
      requiredSkills: requiredSkills || undefined,
      minExperienceYears: minExperienceYears ? Number(minExperienceYears) : undefined,
      description: description || undefined,
      justification: justification || undefined,
    });
  };

  const handleCreatePosting = (e: React.FormEvent) => {
    e.preventDefault();
    if (!targetOpeningId || !postingTitle || !postingDescription) return;
    setMessage(null);
    createPostingMutation.mutate({
      openingId: targetOpeningId,
      body: {
        title: postingTitle,
        description: postingDescription,
        locationName: postingLocation || undefined,
        workArrangement: postingWorkArrangement,
        employmentType: postingEmploymentType,
        applicationDeadline: postingDeadline || undefined,
      },
    });
  };

  const handleScheduleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedAppDetail || !schedTime) return;
    setMessage(null);
    scheduleInterviewMutation.mutate({
      appId: selectedAppDetail.id,
      body: {
        interviewType: schedType,
        scheduledTime: new Date(schedTime).toISOString(),
      },
    });
  };

  const handleFeedbackSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!feedbackInterviewId || !feedbackInterviewerId) return;
    setMessage(null);
    submitFeedbackMutation.mutate({
      interviewId: feedbackInterviewId,
      body: {
        interviewerId: feedbackInterviewerId,
        score: feedbackScore,
        recommendation: feedbackRec,
        comments: feedbackComments || undefined,
      },
    });
  };

  const handleOfferSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedAppDetail || !offerSalaryAmount || !offerStartDate) return;
    setMessage(null);
    createOfferMutation.mutate({
      appId: selectedAppDetail.id,
      body: {
        salaryAmount: Number(offerSalaryAmount),
        currencyCode: offerCurrency,
        startDate: offerStartDate,
      },
    });
  };

  const startCreatePosting = (req: JobRequisitionResponse) => {
    if (!req.jobOpeningId) return;
    setTargetOpeningId(req.jobOpeningId);
    setPostingTitle(req.jobTitle);
    setPostingDescription(req.description || '');
    setPostingLocation(req.locationName || '');
    setPostingEmploymentType(req.employmentType);
    setPostingDeadline('');
    setPostingWorkArrangement('REMOTE');
    setIsPostingFormOpen(true);
    setMessage(null);
  };

  return (
    <div className="space-y-6">
      <header className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">Hiring & Recruitment</h1>
          <p className="text-sm text-muted-foreground">Raise hiring requisitions, verify open slots, and manage recruitment pipeline.</p>
        </div>
        <Button onClick={() => { setIsFormOpen(true); setMessage(null); }} className="sm:self-end">
          Raise Requisition
        </Button>
      </header>

      {message && (
        <div
          className={`p-4 rounded-md text-sm ${
            message.type === 'success'
              ? 'bg-green-500/10 text-green-500 border border-green-500/20'
              : 'bg-red-500/10 text-red-500 border border-red-500/20'
          }`}
        >
          {message.text}
        </div>
      )}

      {/* Tabs */}
      <div className="flex border-b border-border gap-4 overflow-x-auto pb-px">
        <button
          onClick={() => { setActiveTab('requisitions'); setMessage(null); setSelectedPosting(null); }}
          className={`pb-2 text-sm font-medium transition-colors border-b-2 px-1 ${
            activeTab === 'requisitions'
              ? 'border-primary text-foreground'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          Requisitions List
        </button>
        <button
          onClick={() => { setActiveTab('postings'); setMessage(null); }}
          className={`pb-2 text-sm font-medium transition-colors border-b-2 px-1 ${
            activeTab === 'postings'
              ? 'border-primary text-foreground'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          Job Postings
        </button>
        {isHrOrAdmin && (
          <button
            onClick={() => { setActiveTab('approvals'); setMessage(null); setSelectedPosting(null); }}
            className={`pb-2 text-sm font-medium transition-colors border-b-2 px-1 ${
              activeTab === 'approvals'
                ? 'border-primary text-foreground'
                : 'border-transparent text-muted-foreground hover:text-foreground'
            }`}
          >
            Pending Approvals
          </button>
        )}
      </div>

      {/* Requisitions List Tab */}
      {activeTab === 'requisitions' && (
        <div className="space-y-6">
          {reqsLoading ? (
            <p className="text-muted-foreground text-sm">Loading requisitions…</p>
          ) : !requisitions || requisitions.length === 0 ? (
            <p className="text-muted-foreground text-sm">No requisitions created yet.</p>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {requisitions.map((req) => (
                <Card
                  key={req.id}
                  className="cursor-pointer hover:border-primary/50 transition-colors"
                  onClick={() => setSelectedReq(req)}
                >
                  <CardHeader className="pb-2">
                    <CardDescription>{req.reqNumber}</CardDescription>
                    <CardTitle className="text-lg">{req.jobTitle}</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-2 text-xs">
                    <div className="flex justify-between">
                      <span className="text-muted-foreground">Department:</span>
                      <span className="font-medium">{req.departmentName || '—'}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-muted-foreground">Openings:</span>
                      <span className="font-semibold text-primary">{req.openingsCount}</span>
                    </div>
                    <div className="flex justify-between items-center pt-2">
                      <span
                        className={`inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-medium ${
                          req.status === 'APPROVED' || req.status === 'OPEN'
                            ? 'bg-green-500/10 text-green-500'
                            : req.status === 'PENDING_APPROVAL'
                            ? 'bg-yellow-500/10 text-yellow-500'
                            : req.status === 'DRAFT'
                            ? 'bg-gray-500/10 text-gray-500'
                            : 'bg-red-500/10 text-red-500'
                        }`}
                      >
                        {req.status}
                      </span>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}

          {/* Requisition Details Panel */}
          {selectedReq && (
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <div>
                  <CardTitle>{selectedReq.jobTitle} details</CardTitle>
                  <CardDescription>{selectedReq.reqNumber}</CardDescription>
                </div>
                <div className="flex gap-2">
                  {selectedReq.status === 'DRAFT' && (
                    <Button
                      size="sm"
                      onClick={() => submitMutation.mutate(selectedReq.id)}
                      disabled={submitMutation.isPending}
                    >
                      Submit for Approval
                    </Button>
                  )}
                  {selectedReq.status === 'APPROVED' && selectedReq.jobOpeningId && (
                    <Button
                      size="sm"
                      onClick={() => startCreatePosting(selectedReq)}
                    >
                      Create Job Posting
                    </Button>
                  )}
                  {selectedReq.status === 'APPROVED' && (
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => closeMutation.mutate(selectedReq.id)}
                      disabled={closeMutation.isPending}
                    >
                      Close Requisition
                    </Button>
                  )}
                  <Button variant="outline" size="sm" onClick={() => setSelectedReq(null)}>
                    Close Panel
                  </Button>
                </div>
              </CardHeader>
              <CardContent className="grid gap-4 sm:grid-cols-2 text-sm pt-4">
                <div>
                  <span className="text-xs text-muted-foreground block">Legal Entity</span>
                  <span className="font-medium">{selectedReq.legalEntityName || '—'}</span>
                </div>
                <div>
                  <span className="text-xs text-muted-foreground block">Location</span>
                  <span className="font-medium">{selectedReq.locationName || '—'}</span>
                </div>
                <div>
                  <span className="text-xs text-muted-foreground block">Employment Type</span>
                  <span className="font-medium">{selectedReq.employmentType}</span>
                </div>
                <div>
                  <span className="text-xs text-muted-foreground block">Hiring Manager</span>
                  <span className="font-medium">{selectedReq.hiringManagerName || '—'}</span>
                </div>
                <div>
                  <span className="text-xs text-muted-foreground block">Target Start Date</span>
                  <span className="font-medium">{selectedReq.targetStartDate || '—'}</span>
                </div>
                <div>
                  <span className="text-xs text-muted-foreground block">Salary Range</span>
                  <span className="font-medium">
                    {selectedReq.minSalary} to {selectedReq.maxSalary} {selectedReq.currencyCode}
                  </span>
                </div>
                <div className="sm:col-span-2">
                  <span className="text-xs text-muted-foreground block">Required Skills</span>
                  <span className="font-medium">{selectedReq.requiredSkills || '—'}</span>
                </div>
                <div className="sm:col-span-2">
                  <span className="text-xs text-muted-foreground block">Justification</span>
                  <p className="mt-1 text-muted-foreground leading-relaxed">{selectedReq.justification || '—'}</p>
                </div>
              </CardContent>
            </Card>
          )}
        </div>
      )}

      {/* Job Postings Tab */}
      {activeTab === 'postings' && (
        <div className="space-y-6">
          {postingsLoading ? (
            <p className="text-muted-foreground text-sm">Loading postings…</p>
          ) : !postings || postings.length === 0 ? (
            <p className="text-muted-foreground text-sm">No postings created yet. Navigate to an approved requisition to create one.</p>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {postings.map((post) => (
                <Card
                  key={post.id}
                  className="cursor-pointer hover:border-primary/50 transition-colors"
                  onClick={() => { setSelectedPosting(post); setSelectedAppDetail(null); setMessage(null); }}
                >
                  <CardHeader className="pb-2">
                    <CardDescription>{post.employmentType} • {post.workArrangement}</CardDescription>
                    <CardTitle className="text-lg">{post.title}</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-2 text-xs">
                    <div className="flex justify-between">
                      <span className="text-muted-foreground">Location:</span>
                      <span className="font-medium">{post.locationName || 'Remote / Global'}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-muted-foreground">Public ID:</span>
                      <span className="font-mono text-muted-foreground">{post.publicId.slice(0, 8)}…</span>
                    </div>
                    <div className="flex justify-between items-center pt-2">
                      <span
                        className={`inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-medium ${
                          post.status === 'PUBLISHED'
                            ? 'bg-green-500/10 text-green-500'
                            : post.status === 'UNPUBLISHED'
                            ? 'bg-red-500/10 text-red-500'
                            : 'bg-gray-500/10 text-gray-500'
                        }`}
                      >
                        {post.status}
                      </span>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}

          {/* Posting Details & Candidate Pipeline Kanban Board */}
          {selectedPosting && (
            <div className="space-y-6">
              <Card>
                <CardHeader className="flex flex-row items-center justify-between pb-2">
                  <div>
                    <CardTitle>{selectedPosting.title} Details</CardTitle>
                    <CardDescription>Status: {selectedPosting.status}</CardDescription>
                  </div>
                  <div className="flex gap-2">
                    {(selectedPosting.status === 'DRAFT' || selectedPosting.status === 'UNPUBLISHED') && (
                      <Button
                        size="sm"
                        onClick={() => publishPostingMutation.mutate(selectedPosting.id)}
                        disabled={publishPostingMutation.isPending}
                      >
                        Publish Posting
                      </Button>
                    )}
                    {selectedPosting.status === 'PUBLISHED' && (
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => unpublishPostingMutation.mutate(selectedPosting.id)}
                        disabled={unpublishPostingMutation.isPending}
                      >
                        Unpublish
                      </Button>
                    )}
                    <Button variant="outline" size="sm" onClick={() => setSelectedPosting(null)}>
                      Close Posting Panel
                    </Button>
                  </div>
                </CardHeader>
                <CardContent className="space-y-4 pt-4 text-sm">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <span className="text-xs text-muted-foreground block">Public Careers URL</span>
                      <span className="font-mono font-medium text-xs">/careers/jobs/{selectedPosting.publicId}</span>
                    </div>
                    <div>
                      <span className="text-xs text-muted-foreground block">Deadline</span>
                      <span className="font-medium">{selectedPosting.applicationDeadline || 'Open until filled'}</span>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Kanban Pipeline Board */}
              <div className="space-y-4">
                <div>
                  <h3 className="text-lg font-semibold tracking-tight">Applicant Evaluation Pipeline</h3>
                  <p className="text-xs text-muted-foreground">Track stages, view resumes, and move applicants forward.</p>
                </div>

                {appsLoading ? (
                  <p className="text-sm text-muted-foreground">Loading applications…</p>
                ) : (
                  <div className="flex gap-4 overflow-x-auto pb-4 pt-2">
                    {PIPELINE_STAGES.map((stage) => {
                      const stageApps = applications?.filter(a => a.currentStage === stage.key && a.status !== 'REJECTED') || [];
                      return (
                        <div key={stage.key} className="flex-1 min-w-[250px] bg-muted/30 rounded-lg p-3 border border-border/50 flex flex-col gap-3">
                          <div className="flex justify-between items-center pb-2 border-b border-border">
                            <span className="text-xs font-bold uppercase tracking-wider text-muted-foreground">{stage.label}</span>
                            <span className="text-xs font-semibold bg-muted px-2 py-0.5 rounded-full text-foreground/80">{stageApps.length}</span>
                          </div>

                          <div className="space-y-3 flex-1 overflow-y-auto max-h-[400px]">
                            {stageApps.length === 0 ? (
                              <p className="text-[11px] text-muted-foreground text-center py-6 italic">No candidates</p>
                            ) : (
                              stageApps.map((app) => (
                                <Card
                                  key={app.id}
                                  className="p-3 border border-border/60 bg-card hover:border-primary/40 cursor-pointer transition-colors space-y-2 text-xs"
                                  onClick={() => { setSelectedAppDetail(app); setDetailTab('overview'); }}
                                >
                                  <div className="font-semibold text-sm text-foreground">{app.candidateName}</div>
                                  <div className="text-muted-foreground leading-relaxed">{app.candidateEmail}</div>
                                  <div className="flex justify-between items-center pt-2">
                                    <div className="flex gap-1.5">
                                      {stage.next && (
                                        <Button
                                          size="sm"
                                          className="text-[10px] h-6 px-2"
                                          onClick={(e) => {
                                            e.stopPropagation();
                                            advanceStageMutation.mutate({ id: app.id, stage: stage.next! });
                                          }}
                                        >
                                          Advance
                                        </Button>
                                      )}
                                      <Button
                                        size="sm"
                                        variant="outline"
                                        className="text-[10px] h-6 px-2 text-red-500 border-red-500/20 hover:bg-red-500/10"
                                        onClick={(e) => {
                                          e.stopPropagation();
                                          rejectAppMutation.mutate(app.id);
                                        }}
                                      >
                                        Reject
                                      </Button>
                                    </div>
                                    {app.resumeStorageKey && (
                                      <button
                                        onClick={(e) => {
                                          e.stopPropagation();
                                          handleDownloadResume(app.id);
                                        }}
                                        className="text-[10px] text-primary font-semibold hover:underline"
                                      >
                                        Resume
                                      </button>
                                    )}
                                  </div>
                                </Card>
                              ))
                            )}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>

              {/* Candidate detail modal/panel */}
              {selectedAppDetail && (
                <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto">
                  <Card className="w-full max-w-2xl my-8">
                    <CardHeader className="flex flex-row items-center justify-between pb-2 border-b border-border">
                      <div>
                        <CardTitle>{selectedAppDetail.candidateName}</CardTitle>
                        <CardDescription>Pipeline Stage: {selectedAppDetail.currentStage}</CardDescription>
                      </div>
                      <Button variant="outline" size="sm" onClick={() => setSelectedAppDetail(null)}>
                        Close
                      </Button>
                    </CardHeader>

                    {/* Tabs inside modal */}
                    <div className="flex border-b border-border px-6 gap-4 pt-2">
                      <button
                        onClick={() => setDetailTab('overview')}
                        className={`pb-2 text-xs font-semibold transition-colors border-b-2 ${
                          detailTab === 'overview' ? 'border-primary text-foreground' : 'border-transparent text-muted-foreground'
                        }`}
                      >
                        Overview & Profile
                      </button>
                      <button
                        onClick={() => setDetailTab('interviews')}
                        className={`pb-2 text-xs font-semibold transition-colors border-b-2 ${
                          detailTab === 'interviews' ? 'border-primary text-foreground' : 'border-transparent text-muted-foreground'
                        }`}
                      >
                        Interviews & Scorecards
                      </button>
                      <button
                        onClick={() => setDetailTab('offer')}
                        className={`pb-2 text-xs font-semibold transition-colors border-b-2 ${
                          detailTab === 'offer' ? 'border-primary text-foreground' : 'border-transparent text-muted-foreground'
                        }`}
                      >
                        Job Offer Letter
                      </button>
                    </div>

                    <CardContent className="space-y-4 pt-4 text-sm max-h-[60vh] overflow-y-auto">
                      {detailTab === 'overview' && (
                        <>
                          <div className="grid grid-cols-2 gap-4">
                            <div>
                              <span className="text-xs text-muted-foreground block">Email</span>
                              <span className="font-semibold">{selectedAppDetail.candidateEmail}</span>
                            </div>
                            <div>
                              <span className="text-xs text-muted-foreground block">Phone</span>
                              <span className="font-semibold">{selectedAppDetail.candidatePhone || '—'}</span>
                            </div>
                          </div>

                          <div>
                            <span className="text-xs text-muted-foreground block">Cover Letter</span>
                            <p className="mt-1 text-muted-foreground whitespace-pre-line leading-relaxed bg-muted/40 p-3 rounded-md">
                              {selectedAppDetail.coverLetter || 'No cover letter provided.'}
                            </p>
                          </div>

                          <div className="flex justify-end gap-3 pt-4 border-t border-border">
                            {selectedAppDetail.resumeStorageKey && (
                              <Button variant="outline" onClick={() => handleDownloadResume(selectedAppDetail.id)}>
                                Download Resume
                              </Button>
                            )}
                            <Button variant="outline" onClick={() => setSelectedAppDetail(null)}>
                              Back
                            </Button>
                          </div>
                        </>
                      )}

                      {detailTab === 'interviews' && (
                        <div className="space-y-6">
                          {/* Schedule Button Trigger */}
                          <div className="flex justify-between items-center">
                            <span className="font-semibold text-xs uppercase tracking-wider text-muted-foreground">Scheduled Slots</span>
                            <Button size="sm" onClick={() => setIsSchedulingOpen(true)}>
                              Schedule Interview
                            </Button>
                          </div>

                          {/* Interviews list */}
                          <div className="space-y-3">
                            {!candidateInterviews || candidateInterviews.length === 0 ? (
                              <p className="text-xs text-muted-foreground italic">No interviews scheduled yet.</p>
                            ) : (
                              candidateInterviews.map((intr) => (
                                <div key={intr.id} className="p-3 border border-border rounded-lg bg-muted/30 flex flex-col gap-2 justify-between sm:flex-row sm:items-center">
                                  <div>
                                    <div className="font-semibold text-xs text-primary">{intr.interviewType} INTERVIEW</div>
                                    <div className="text-xs text-muted-foreground mt-1">
                                      Date: {new Date(intr.scheduledTime).toLocaleString()}
                                    </div>
                                    <div className="text-[10px] mt-1">
                                      Status: <span className="font-semibold uppercase text-primary">{intr.status}</span>
                                    </div>
                                  </div>
                                  <div className="flex gap-1.5 self-start sm:self-center">
                                    {intr.status === 'SCHEDULED' && (
                                      <>
                                        <Button
                                          size="sm"
                                          onClick={() => {
                                            setFeedbackInterviewId(intr.id);
                                            setIsFeedbackOpen(true);
                                          }}
                                        >
                                          Record Scorecard
                                        </Button>
                                        <Button
                                          size="sm"
                                          variant="outline"
                                          onClick={() => completeInterviewMutation.mutate(intr.id)}
                                        >
                                          Mark Done
                                        </Button>
                                        <Button
                                          size="sm"
                                          variant="outline"
                                          className="text-red-500"
                                          onClick={() => cancelInterviewMutation.mutate(intr.id)}
                                        >
                                          Cancel
                                        </Button>
                                      </>
                                    )}
                                  </div>
                                </div>
                              ))
                            )}
                          </div>

                          {/* Feedback Scorecards list */}
                          <div className="space-y-3 pt-4 border-t border-border">
                            <span className="font-semibold text-xs uppercase tracking-wider text-muted-foreground block">Interviewer Scorecards</span>
                            {!candidateFeedbacks || candidateFeedbacks.length === 0 ? (
                              <p className="text-xs text-muted-foreground italic">No feedback card recorded yet.</p>
                            ) : (
                              candidateFeedbacks.map((fdb) => (
                                <div key={fdb.id} className="p-3 border border-border/80 rounded-lg bg-card text-xs space-y-2">
                                  <div className="flex justify-between items-center">
                                    <span className="font-semibold text-foreground">{fdb.interviewerName}</span>
                                    <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-semibold ${
                                      fdb.recommendation.includes('HIRE') && !fdb.recommendation.includes('NO')
                                        ? 'bg-green-500/10 text-green-500'
                                        : 'bg-red-500/10 text-red-500'
                                    }`}>
                                      {fdb.recommendation} ({fdb.score}/10)
                                    </span>
                                  </div>
                                  {fdb.comments && (
                                    <p className="text-muted-foreground leading-relaxed italic bg-muted/20 p-2 rounded">
                                      "{fdb.comments}"
                                    </p>
                                  )}
                                </div>
                              ))
                            )}
                          </div>
                        </div>
                      )}

                      {detailTab === 'offer' && (
                        <div className="space-y-6">
                          <span className="font-semibold text-xs uppercase tracking-wider text-muted-foreground block">Employment Offer Letter</span>
                          
                          {/* Create Offer Form (only if no offer accepts exists or to draft a new one) */}
                          {(!candidateOffers || candidateOffers.length === 0) ? (
                            <form onSubmit={handleOfferSubmit} className="p-4 border border-border rounded-lg bg-muted/20 space-y-4">
                              <span className="font-semibold text-xs block text-foreground">Draft New Offer Package</span>
                              <div className="grid gap-4 sm:grid-cols-2">
                                <div>
                                  <label className="text-xs text-muted-foreground">Salary Amount / Year</label>
                                  <input
                                    type="number"
                                    placeholder="e.g. 80000"
                                    required
                                    value={offerSalaryAmount}
                                    onChange={(e) => setOfferSalaryAmount(e.target.value)}
                                    className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                                  />
                                </div>
                                <div>
                                  <label className="text-xs text-muted-foreground">Currency Code</label>
                                  <select
                                    value={offerCurrency}
                                    onChange={(e) => setOfferCurrency(e.target.value)}
                                    className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                                  >
                                    <option value="USD">USD</option>
                                    <option value="EUR">EUR</option>
                                    <option value="INR">INR</option>
                                    <option value="GBP">GBP</option>
                                  </select>
                                </div>
                              </div>
                              <div>
                                <label className="text-xs text-muted-foreground">Target Start Date</label>
                                <input
                                  type="date"
                                  required
                                  value={offerStartDate}
                                  onChange={(e) => setOfferStartDate(e.target.value)}
                                  className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                                />
                              </div>
                              <Button type="submit" disabled={createOfferMutation.isPending} size="sm">
                                {createOfferMutation.isPending ? 'Generating…' : 'Generate Offer Draft'}
                              </Button>
                            </form>
                          ) : (
                            <div className="space-y-4">
                              {candidateOffers.map((o) => (
                                <div key={o.id} className="p-4 border border-border rounded-lg bg-card text-xs space-y-3">
                                  <div className="flex justify-between items-start">
                                    <div>
                                      <div className="font-bold text-sm text-foreground">
                                        {o.salaryAmount.toLocaleString()} {o.currencyCode} / year
                                      </div>
                                      <div className="text-muted-foreground mt-1">Start Date: {o.startDate}</div>
                                    </div>
                                    <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-semibold uppercase ${
                                      o.status === 'ACCEPTED'
                                        ? 'bg-green-500/10 text-green-500'
                                        : o.status === 'REJECTED'
                                        ? 'bg-red-500/10 text-red-500'
                                        : 'bg-yellow-500/10 text-yellow-500'
                                    }`}>
                                      {o.status}
                                    </span>
                                  </div>

                                  {/* Maker-checker approval and release actions */}
                                  <div className="flex gap-2 pt-2 border-t border-border/50">
                                    {o.status === 'DRAFT' && (
                                      <Button
                                        size="sm"
                                        onClick={() => submitOfferMutation.mutate(o.id)}
                                        disabled={submitOfferMutation.isPending}
                                      >
                                        Submit for Approval
                                      </Button>
                                    )}
                                    {o.status === 'PENDING_APPROVAL' && isHrOrAdmin && (
                                      <Button
                                        size="sm"
                                        onClick={() => approveOfferMutation.mutate(o.id)}
                                        disabled={approveOfferMutation.isPending}
                                      >
                                        Approve Offer
                                      </Button>
                                    )}
                                    {o.status === 'APPROVED' && (
                                      <Button
                                        size="sm"
                                        onClick={() => releaseOfferMutation.mutate(o.id)}
                                        disabled={releaseOfferMutation.isPending}
                                      >
                                        Release to Candidate
                                      </Button>
                                    )}
                                    {(o.status === 'SENT' || o.status === 'ACCEPTED') && (
                                      <div className="w-full space-y-1 pt-1">
                                        <span className="text-[10px] text-muted-foreground block">Shareable Portal Link:</span>
                                        <div className="flex gap-2 items-center">
                                          <input
                                            type="text"
                                            readOnly
                                            value={`${window.location.origin}/candidate/offers/${o.secureToken}`}
                                            className="bg-muted px-2 py-1 rounded text-[10px] font-mono flex-1 border border-border"
                                          />
                                          <Button
                                            size="sm"
                                            variant="outline"
                                            className="text-[10px] h-7"
                                            onClick={() => {
                                              navigator.clipboard.writeText(`${window.location.origin}/candidate/offers/${o.secureToken}`);
                                              alert('Link copied to clipboard!');
                                            }}
                                          >
                                            Copy
                                          </Button>
                                        </div>
                                      </div>
                                    )}
                                  </div>
                                </div>
                              ))}
                            </div>
                          )}
                        </div>
                      )}
                    </CardContent>
                  </Card>
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* Pending Approvals Tab */}
      {activeTab === 'approvals' && isHrOrAdmin && (
        <Card>
          <CardHeader>
            <CardTitle>Pending Requisition Approvals</CardTitle>
            <CardDescription>Review and approve internal job requisitions raised by managers.</CardDescription>
          </CardHeader>
          <CardContent>
            {approvalsLoading ? (
              <p className="text-muted-foreground text-sm">Loading approvals…</p>
            ) : !approvals || approvals.filter(a => a.type === 'JOB_REQUISITION' && a.status === 'PENDING').length === 0 ? (
              <p className="text-muted-foreground text-sm">No pending job requisitions in the approval queue.</p>
            ) : (
              <div className="space-y-4">
                {approvals
                  .filter(a => a.type === 'JOB_REQUISITION' && a.status === 'PENDING')
                  .map((app) => (
                    <div key={app.id} className="p-4 border border-border rounded-lg bg-card text-card-foreground flex flex-col gap-3 justify-between sm:flex-row sm:items-center">
                      <div>
                        <p className="text-xs text-muted-foreground">Request ID: {app.id}</p>
                        <p className="text-sm font-semibold mt-1">Requisition details:</p>
                        <p className="text-xs text-muted-foreground font-mono mt-1">{app.changeJson}</p>
                      </div>
                      <div className="flex gap-2 sm:self-center">
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => rejectMutation.mutate(app.id)}
                          disabled={rejectMutation.isPending}
                        >
                          Reject
                        </Button>
                        <Button
                          size="sm"
                          onClick={() => approveMutation.mutate(app.id)}
                          disabled={approveMutation.isPending}
                        >
                          Approve
                        </Button>
                      </div>
                    </div>
                  ))}
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {/* Raise Requisition Modal Form */}
      {isFormOpen && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto">
          <Card className="w-full max-w-lg my-8">
            <CardHeader>
              <CardTitle>Raise Job Requisition</CardTitle>
              <CardDescription>Raise a new vacancy requisition for HR approval.</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleCreate} className="space-y-4 max-h-[70vh] overflow-y-auto pr-2">
                <div className="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Job Title</label>
                    <input
                      type="text"
                      placeholder="e.g. Senior Software Engineer"
                      value={jobTitle}
                      onChange={(e) => setJobTitle(e.target.value)}
                      required
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                    />
                  </div>
                  <div>
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Employment Type</label>
                    <select
                      value={employmentType}
                      onChange={(e) => setEmploymentType(e.target.value)}
                      required
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    >
                      <option value="FULL_TIME">Full Time</option>
                      <option value="PART_TIME">Part Time</option>
                      <option value="CONTRACTOR">Contractor</option>
                      <option value="INTERN">Intern</option>
                    </select>
                  </div>
                </div>

                <div className="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Department</label>
                    <select
                      value={departmentId}
                      onChange={(e) => setDepartmentId(e.target.value)}
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    >
                      <option value="">Select Department</option>
                      {departments?.map((d) => (
                        <option key={d.id} value={d.id}>
                          {d.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Designation</label>
                    <select
                      value={designationId}
                      onChange={(e) => setDesignationId(e.target.value)}
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    >
                      <option value="">Select Designation</option>
                      {designations?.map((d) => (
                        <option key={d.id} value={d.id}>
                          {d.title}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Work Location</label>
                    <select
                      value={locationId}
                      onChange={(e) => setLocationId(e.target.value)}
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    >
                      <option value="">Select Location</option>
                      {locations?.map((l) => (
                        <option key={l.id} value={l.id}>
                          {l.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Legal Entity</label>
                    <select
                      value={legalEntityId}
                      onChange={(e) => setLegalEntityId(e.target.value)}
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    >
                      <option value="">Select Legal Entity</option>
                      {legalEntities?.map((le) => (
                        <option key={le.id} value={le.id}>
                          {le.name}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Openings Count</label>
                    <input
                      type="number"
                      min="1"
                      value={openingsCount}
                      onChange={(e) => setOpeningsCount(Number(e.target.value))}
                      required
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                    />
                  </div>
                  <div>
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Hiring Manager</label>
                    <select
                      value={hiringManagerId}
                      onChange={(e) => setHiringManagerId(e.target.value)}
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    >
                      <option value="">Select Hiring Manager</option>
                      {employees.map((emp: any) => (
                        <option key={emp.id} value={emp.id}>
                          {emp.firstName} {emp.lastName}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="grid gap-4 sm:grid-cols-3">
                  <div>
                    <label className="text-xs text-muted-foreground">Target Start Date</label>
                    <input
                      type="date"
                      value={targetStartDate}
                      onChange={(e) => setTargetStartDate(e.target.value)}
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                    />
                  </div>
                  <div>
                    <label className="text-xs text-muted-foreground">Min Salary</label>
                    <input
                      type="number"
                      placeholder="e.g. 5000"
                      value={minSalary}
                      onChange={(e) => setMinSalary(e.target.value)}
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                    />
                  </div>
                  <div>
                    <label className="text-xs text-muted-foreground">Max Salary</label>
                    <input
                      type="number"
                      placeholder="e.g. 8000"
                      value={maxSalary}
                      onChange={(e) => setMaxSalary(e.target.value)}
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                    />
                  </div>
                </div>

                <div className="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label className="text-xs text-muted-foreground">Required Skills</label>
                    <input
                      type="text"
                      placeholder="e.g. React, Java, Spring Boot"
                      value={requiredSkills}
                      onChange={(e) => setRequiredSkills(e.target.value)}
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                    />
                  </div>
                  <div>
                    <label className="text-xs text-muted-foreground">Min Experience (Years)</label>
                    <input
                      type="number"
                      placeholder="e.g. 3"
                      value={minExperienceYears}
                      onChange={(e) => setMinExperienceYears(e.target.value)}
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                    />
                  </div>
                </div>

                <div>
                  <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Business Justification</label>
                  <textarea
                    placeholder="Enter hiring justification details…"
                    value={justification}
                    onChange={(e) => setJustification(e.target.value)}
                    rows={2}
                    className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                  />
                </div>

                <div>
                  <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Description</label>
                  <textarea
                    placeholder="Enter job description details…"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    rows={3}
                    className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                  />
                </div>

                <div className="flex justify-end gap-3 pt-4 border-t border-border">
                  <Button type="button" variant="outline" onClick={() => setIsFormOpen(false)}>
                    Cancel
                  </Button>
                  <Button type="submit" disabled={createMutation.isPending}>
                    {createMutation.isPending ? 'Saving…' : 'Save Draft'}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Create Job Posting Modal Form */}
      {isPostingFormOpen && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto">
          <Card className="w-full max-w-lg my-8">
            <CardHeader>
              <CardTitle>Create Job Posting</CardTitle>
              <CardDescription>Advertise this vacancy slot on the careers portal.</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleCreatePosting} className="space-y-4 max-h-[70vh] overflow-y-auto pr-2">
                <div>
                  <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Posting Title</label>
                  <input
                    type="text"
                    placeholder="e.g. Senior Software Engineer"
                    value={postingTitle}
                    onChange={(e) => setPostingTitle(e.target.value)}
                    required
                    className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                  />
                </div>

                <div className="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Work Arrangement</label>
                    <select
                      value={postingWorkArrangement}
                      onChange={(e) => setPostingWorkArrangement(e.target.value as any)}
                      required
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    >
                      <option value="REMOTE">Remote</option>
                      <option value="ONSITE">Onsite</option>
                      <option value="HYBRID">Hybrid</option>
                    </select>
                  </div>
                  <div>
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Employment Type</label>
                    <input
                      type="text"
                      placeholder="e.g. FULL_TIME"
                      value={postingEmploymentType}
                      onChange={(e) => setPostingEmploymentType(e.target.value)}
                      required
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                    />
                  </div>
                </div>

                <div className="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label className="text-xs text-muted-foreground">Location Name</label>
                    <input
                      type="text"
                      placeholder="e.g. New York Office"
                      value={postingLocation}
                      onChange={(e) => setPostingLocation(e.target.value)}
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                    />
                  </div>
                  <div>
                    <label className="text-xs text-muted-foreground">Application Deadline</label>
                    <input
                      type="date"
                      value={postingDeadline}
                      onChange={(e) => setPostingDeadline(e.target.value)}
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                    />
                  </div>
                </div>

                <div>
                  <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Job Description</label>
                  <textarea
                    placeholder="Enter full job advertisement details…"
                    value={postingDescription}
                    onChange={(e) => setPostingDescription(e.target.value)}
                    required
                    rows={6}
                    className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                  />
                </div>

                <div className="flex justify-end gap-3 pt-4 border-t border-border">
                  <Button type="button" variant="outline" onClick={() => setIsPostingFormOpen(false)}>
                    Cancel
                  </Button>
                  <Button type="submit" disabled={createPostingMutation.isPending}>
                    {createPostingMutation.isPending ? 'Creating…' : 'Create Advertisement'}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Schedule Interview Modal Form */}
      {isSchedulingOpen && selectedAppDetail && (
        <div className="fixed inset-0 z-[60] bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto">
          <Card className="w-full max-w-md">
            <CardHeader>
              <CardTitle>Schedule Interview Slot</CardTitle>
              <CardDescription>Configure the interview details for {selectedAppDetail.candidateName}.</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleScheduleSubmit} className="space-y-4">
                <div>
                  <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Interview Stage</label>
                  <select
                    value={schedType}
                    onChange={(e) => setSchedType(e.target.value as any)}
                    required
                    className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                  >
                    <option value="SCREENING">Screening</option>
                    <option value="TECHNICAL">Technical Interview</option>
                    <option value="MANAGERIAL">Managerial Interview</option>
                    <option value="HR">HR Interview</option>
                  </select>
                </div>

                <div>
                  <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Date & Time</label>
                  <input
                    type="datetime-local"
                    value={schedTime}
                    onChange={(e) => setSchedTime(e.target.value)}
                    required
                    className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                  />
                </div>

                <div className="flex justify-end gap-3 pt-4 border-t border-border">
                  <Button type="button" variant="outline" onClick={() => setIsSchedulingOpen(false)}>
                    Cancel
                  </Button>
                  <Button type="submit" disabled={scheduleInterviewMutation.isPending}>
                    {scheduleInterviewMutation.isPending ? 'Scheduling…' : 'Schedule Slot'}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Record Scorecard Feedback Modal Form */}
      {isFeedbackOpen && (
        <div className="fixed inset-0 z-[60] bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto">
          <Card className="w-full max-w-md">
            <CardHeader>
              <CardTitle>Submit Interview Scorecard</CardTitle>
              <CardDescription>Record candidate feedback and decision recommendation.</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleFeedbackSubmit} className="space-y-4">
                <div>
                  <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Interviewer</label>
                  <select
                    value={feedbackInterviewerId}
                    onChange={(e) => setFeedbackInterviewerId(e.target.value)}
                    required
                    className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                  >
                    <option value="">Select Interviewer</option>
                    {employees.map((emp: any) => (
                      <option key={emp.id} value={emp.id}>
                        {emp.firstName} {emp.lastName}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Score (0 - 10)</label>
                    <input
                      type="number"
                      min="0"
                      max="10"
                      value={feedbackScore}
                      onChange={(e) => setFeedbackScore(Number(e.target.value))}
                      required
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                    />
                  </div>
                  <div>
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Recommendation</label>
                    <select
                      value={feedbackRec}
                      onChange={(e) => setFeedbackRec(e.target.value as any)}
                      required
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    >
                      <option value="STRONG_HIRE">Strong Hire</option>
                      <option value="HIRE">Hire</option>
                      <option value="MIXED">Mixed</option>
                      <option value="NO_HIRE">No Hire</option>
                      <option value="STRONG_NO_HIRE">Strong No Hire</option>
                    </select>
                  </div>
                </div>

                <div>
                  <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Evaluation Comments</label>
                  <textarea
                    placeholder="Enter details of coding test, questions, answers, fit…"
                    value={feedbackComments}
                    onChange={(e) => setFeedbackComments(e.target.value)}
                    required
                    rows={4}
                    className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                  />
                </div>

                <div className="flex justify-end gap-3 pt-4 border-t border-border">
                  <Button type="button" variant="outline" onClick={() => setIsFeedbackOpen(false)}>
                    Cancel
                  </Button>
                  <Button type="submit" disabled={submitFeedbackMutation.isPending}>
                    {submitFeedbackMutation.isPending ? 'Submitting…' : 'Submit Scorecard'}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}
