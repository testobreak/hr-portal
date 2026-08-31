import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { useMe } from '@/hooks/useMe';
import { Roles, hasAnyRole } from '@/lib/roles';
import {
  fetchPreHires,
  fetchPreHirePlan,
  createPreHirePlan,
  updateOnboardingTaskStatus,
  fetchPreHireDocuments,
  requestPreHireDocument,
  uploadPreHireDocument,
  reviewPreHireDocument,
  activatePreHire,
  fetchPreHireBackgroundChecks,
  triggerPreHireBackgroundCheck,
  updateBackgroundCheckStatus,
  fetchPreHireAssets,
  requestPreHireAsset,
  updateAssetStatus,
  type PreHireResponse
} from '@/api/services/onboardingService';

export function OnboardingPage() {
  const queryClient = useQueryClient();
  const { data: me } = useMe();
  const roles = me?.roles ?? [];
  const isHrOrAdmin = hasAnyRole(roles, [Roles.HR_ADMIN, Roles.SUPER_ADMIN]);

  const [selectedPreHire, setSelectedPreHire] = useState<PreHireResponse | null>(null);
  const [detailTab, setDetailTab] = useState<'overview' | 'tasks' | 'documents' | 'background' | 'assets' | 'activation'>('overview');
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Form states
  const [employeeCode, setEmployeeCode] = useState('');
  const [dateOfBirth, setDateOfBirth] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [newDocType, setNewDocType] = useState('DIPLOMA_DEGREE');
  const [newAssetType, setNewAssetType] = useState('LAPTOP');

  // Queries
  const { data: preHires, isLoading: preHiresLoading } = useQuery({
    queryKey: ['onboarding', 'prehires'],
    queryFn: fetchPreHires,
  });

  // Query for onboarding plan details
  const { data: plan, isLoading: planLoading, error: planError } = useQuery({
    queryKey: ['onboarding', 'prehires', selectedPreHire?.id, 'plan'],
    queryFn: () => fetchPreHirePlan(selectedPreHire!.id),
    enabled: !!selectedPreHire,
    retry: false,
  });

  // Query for documents
  const { data: documents, isLoading: docsLoading } = useQuery({
    queryKey: ['onboarding', 'prehires', selectedPreHire?.id, 'documents'],
    queryFn: () => fetchPreHireDocuments(selectedPreHire!.id),
    enabled: !!selectedPreHire,
  });

  // Query for background checks
  const { data: backgroundChecks } = useQuery({
    queryKey: ['onboarding', 'prehires', selectedPreHire?.id, 'background-checks'],
    queryFn: () => fetchPreHireBackgroundChecks(selectedPreHire!.id),
    enabled: !!selectedPreHire,
  });

  // Query for assets
  const { data: assetRequests } = useQuery({
    queryKey: ['onboarding', 'prehires', selectedPreHire?.id, 'assets'],
    queryFn: () => fetchPreHireAssets(selectedPreHire!.id),
    enabled: !!selectedPreHire,
  });

  // Mutations
  const initPlanMutation = useMutation({
    mutationFn: () => createPreHirePlan(selectedPreHire!.id, 'STANDARD'),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['onboarding', 'prehires', selectedPreHire?.id, 'plan'] });
      queryClient.invalidateQueries({ queryKey: ['onboarding', 'prehires'] });
      setMessage({ type: 'success', text: 'Onboarding tasks checklist initialized successfully.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to initialize plan.' });
    },
  });

  const updateTaskMutation = useMutation({
    mutationFn: (payload: { taskId: string; status: string }) =>
      updateOnboardingTaskStatus(payload.taskId, payload.status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['onboarding', 'prehires', selectedPreHire?.id, 'plan'] });
      queryClient.invalidateQueries({ queryKey: ['onboarding', 'prehires'] });
      setMessage({ type: 'success', text: 'Task status updated.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to update task status.' });
    },
  });

  const requestDocMutation = useMutation({
    mutationFn: (docType: string) => requestPreHireDocument(selectedPreHire!.id, docType),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['onboarding', 'prehires', selectedPreHire?.id, 'documents'] });
      setMessage({ type: 'success', text: 'Document request generated.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to request document.' });
    },
  });

  const uploadDocMutation = useMutation({
    mutationFn: (payload: { docId: string; storageKey: string }) =>
      uploadPreHireDocument(payload.docId, payload.storageKey),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['onboarding', 'prehires', selectedPreHire?.id, 'documents'] });
      setMessage({ type: 'success', text: 'Verification document uploaded successfully.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to upload document.' });
    },
  });

  const reviewDocMutation = useMutation({
    mutationFn: (payload: { docId: string; status: string }) =>
      reviewPreHireDocument(payload.docId, payload.status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['onboarding', 'prehires', selectedPreHire?.id, 'documents'] });
      setMessage({ type: 'success', text: 'Document review submitted.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to submit document review.' });
    },
  });

  const activateMutation = useMutation({
    mutationFn: (payload: any) => activatePreHire(selectedPreHire!.id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['onboarding', 'prehires'] });
      if (selectedPreHire) {
        setSelectedPreHire({
          ...selectedPreHire,
          status: 'ACTIVATED'
        });
      }
      setEmployeeCode('');
      setDateOfBirth('');
      setPhoneNumber('');
      setMessage({ type: 'success', text: 'Pre-Hire record has been successfully activated as a full active Employee!' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to convert Pre-Hire to active Employee.' });
    },
  });

  const triggerBgCheckMutation = useMutation({
    mutationFn: () => triggerPreHireBackgroundCheck(selectedPreHire!.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['onboarding', 'prehires', selectedPreHire?.id, 'background-checks'] });
      setMessage({ type: 'success', text: 'Background check verification triggered successfully.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to trigger background check.' });
    },
  });

  const updateBgCheckStatusMutation = useMutation({
    mutationFn: (payload: { checkId: string; status: string }) =>
      updateBackgroundCheckStatus(payload.checkId, payload.status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['onboarding', 'prehires', selectedPreHire?.id, 'background-checks'] });
      setMessage({ type: 'success', text: 'Background verification status updated.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to update background verification status.' });
    },
  });

  const requestAssetMutation = useMutation({
    mutationFn: (assetType: string) => requestPreHireAsset(selectedPreHire!.id, assetType),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['onboarding', 'prehires', selectedPreHire?.id, 'assets'] });
      setMessage({ type: 'success', text: 'Asset request created.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to request asset.' });
    },
  });

  const updateAssetStatusMutation = useMutation({
    mutationFn: (payload: { assetId: string; status: string }) =>
      updateAssetStatus(payload.assetId, payload.status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['onboarding', 'prehires', selectedPreHire?.id, 'assets'] });
      setMessage({ type: 'success', text: 'Asset provisioning status updated.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to update asset status.' });
    },
  });

  const handleActivationSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!employeeCode || !dateOfBirth) return;
    setMessage(null);
    activateMutation.mutate({
      employeeCode,
      dateOfBirth,
      phoneNumber: phoneNumber || undefined,
    });
  };

  const handleRequestDoc = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newDocType) return;
    setMessage(null);
    requestDocMutation.mutate(newDocType);
  };

  const handleSimulateUpload = (docId: string, docType: string) => {
    setMessage(null);
    const mockStorageKey = `onboarding/documents/${selectedPreHire?.id}/${docType.toLowerCase()}_copy.pdf`;
    uploadDocMutation.mutate({ docId, storageKey: mockStorageKey });
  };

  // Calculate task progress percentage
  const getProgress = () => {
    if (!plan || !plan.tasks || plan.tasks.length === 0) return 0;
    const completed = plan.tasks.filter(t => t.status === 'COMPLETED' || t.status === 'WAIVED').length;
    return Math.round((completed / plan.tasks.length) * 100);
  };

  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-2xl font-semibold tracking-tight">Pre-Employment Onboarding Portal</h1>
        <p className="text-sm text-muted-foreground">Verify candidate pre-joining credentials, track checklists, and activate employees.</p>
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

      {/* Main Pre-Hires Dashboard */}
      {preHiresLoading ? (
        <p className="text-muted-foreground text-sm">Loading pre-hires pipeline…</p>
      ) : !preHires || preHires.length === 0 ? (
        <p className="text-muted-foreground text-sm">No pre-hire records created. Candidate offers must be accepted first.</p>
      ) : (
        <div className="grid gap-6 lg:grid-cols-3">
          {/* Pre-Hires profiles List */}
          <div className="lg:col-span-1 space-y-4">
            <span className="font-semibold text-xs uppercase tracking-wider text-muted-foreground block">Pre-Hire Profiles</span>
            <div className="space-y-3">
              {preHires.map((ph) => (
                <Card
                  key={ph.id}
                  className={`cursor-pointer transition-colors hover:border-primary/50 ${
                    selectedPreHire?.id === ph.id ? 'border-primary bg-primary/5' : ''
                  }`}
                  onClick={() => { setSelectedPreHire(ph); setDetailTab('overview'); setMessage(null); }}
                >
                  <CardHeader className="pb-2">
                    <CardDescription>{ph.designationTitle} • {ph.departmentName}</CardDescription>
                    <CardTitle className="text-base">{ph.candidateName}</CardTitle>
                  </CardHeader>
                  <CardContent className="text-xs space-y-2">
                    <div className="flex justify-between">
                      <span className="text-muted-foreground">Start Date:</span>
                      <span className="font-semibold">{ph.startDate}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-muted-foreground">Status:</span>
                      <span className={`font-semibold uppercase ${
                        ph.status === 'ACTIVATED'
                          ? 'text-green-500'
                          : ph.status === 'READY_FOR_ACTIVATION'
                          ? 'text-yellow-500'
                          : 'text-primary'
                      }`}>{ph.status}</span>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>

          {/* Details Workspace Panel */}
          <div className="lg:col-span-2 space-y-4">
            {selectedPreHire ? (
              <Card className="shadow-md">
                <CardHeader className="border-b border-border pb-4">
                  <div className="flex justify-between items-start">
                    <div>
                      <CardTitle className="text-xl">{selectedPreHire.candidateName}</CardTitle>
                      <CardDescription>{selectedPreHire.candidateEmail}</CardDescription>
                    </div>
                    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                      selectedPreHire.status === 'ACTIVATED'
                        ? 'bg-green-500/10 text-green-500'
                        : 'bg-yellow-500/10 text-yellow-500'
                    }`}>
                      {selectedPreHire.status}
                    </span>
                  </div>

                  {/* Details Sub-Tabs */}
                  <div className="flex gap-4 pt-4 border-t border-border mt-4 overflow-x-auto">
                    <button
                      onClick={() => setDetailTab('overview')}
                      className={`pb-2 text-xs font-semibold transition-colors border-b-2 ${
                        detailTab === 'overview' ? 'border-primary text-foreground' : 'border-transparent text-muted-foreground'
                      }`}
                    >
                      Overview Details
                    </button>
                    <button
                      onClick={() => setDetailTab('tasks')}
                      className={`pb-2 text-xs font-semibold transition-colors border-b-2 ${
                        detailTab === 'tasks' ? 'border-primary text-foreground' : 'border-transparent text-muted-foreground'
                      }`}
                    >
                      Tasks Checklist
                    </button>
                    <button
                      onClick={() => setDetailTab('documents')}
                      className={`pb-2 text-xs font-semibold transition-colors border-b-2 ${
                        detailTab === 'documents' ? 'border-primary text-foreground' : 'border-transparent text-muted-foreground'
                      }`}
                    >
                      Documents Verification
                    </button>
                    <button
                      onClick={() => setDetailTab('background')}
                      className={`pb-2 text-xs font-semibold transition-colors border-b-2 ${
                        detailTab === 'background' ? 'border-primary text-foreground' : 'border-transparent text-muted-foreground'
                      }`}
                    >
                      Background Verification
                    </button>
                    <button
                      onClick={() => setDetailTab('assets')}
                      className={`pb-2 text-xs font-semibold transition-colors border-b-2 ${
                        detailTab === 'assets' ? 'border-primary text-foreground' : 'border-transparent text-muted-foreground'
                      }`}
                    >
                      IT Asset Provisioning
                    </button>
                    <button
                      onClick={() => setDetailTab('activation')}
                      className={`pb-2 text-xs font-semibold transition-colors border-b-2 ${
                        detailTab === 'activation' ? 'border-primary text-foreground' : 'border-transparent text-muted-foreground'
                      }`}
                    >
                      Employee Activation
                    </button>
                  </div>
                </CardHeader>
                <CardContent className="pt-6 space-y-4 text-sm max-h-[60vh] overflow-y-auto">
                  
                  {/* TAB 1: Overview */}
                  {detailTab === 'overview' && (
                    <div className="grid gap-4 sm:grid-cols-2 text-xs">
                      <div>
                        <span className="text-muted-foreground block">Legal Entity</span>
                        <span className="font-semibold text-foreground text-sm">{selectedPreHire.legalEntityName}</span>
                      </div>
                      <div>
                        <span className="text-muted-foreground block">Work Location</span>
                        <span className="font-semibold text-foreground text-sm">{selectedPreHire.locationName}</span>
                      </div>
                      <div>
                        <span className="text-muted-foreground block">Hiring Department</span>
                        <span className="font-semibold text-foreground text-sm">{selectedPreHire.departmentName}</span>
                      </div>
                      <div>
                        <span className="text-muted-foreground block">Designation Title</span>
                        <span className="font-semibold text-foreground text-sm">{selectedPreHire.designationTitle}</span>
                      </div>
                      <div>
                        <span className="text-muted-foreground block">Reporting Manager</span>
                        <span className="font-semibold text-foreground text-sm">{selectedPreHire.managerName}</span>
                      </div>
                      <div>
                        <span className="text-muted-foreground block">Contract Start Date</span>
                        <span className="font-semibold text-foreground text-sm">{selectedPreHire.startDate}</span>
                      </div>
                    </div>
                  )}

                  {/* TAB 2: Tasks Checklist */}
                  {detailTab === 'tasks' && (
                    <div className="space-y-4">
                      {planLoading ? (
                        <p className="text-xs text-muted-foreground">Loading onboarding plan checklist…</p>
                      ) : planError || !plan ? (
                        <div className="text-center py-6 space-y-3">
                          <p className="text-xs text-muted-foreground italic">No onboarding tasks checklist initialized for this pre-hire.</p>
                          <Button
                            size="sm"
                            onClick={() => initPlanMutation.mutate()}
                            disabled={initPlanMutation.isPending}
                          >
                            {initPlanMutation.isPending ? 'Initializing…' : 'Initialize Standard Onboarding Template'}
                          </Button>
                        </div>
                      ) : (
                        <div className="space-y-4">
                          {/* Progress bar */}
                          <div className="space-y-1">
                            <div className="flex justify-between text-xs font-semibold">
                              <span>Checklist Progress</span>
                              <span>{getProgress()}%</span>
                            </div>
                            <div className="w-full bg-muted rounded-full h-2">
                              <div className="bg-primary h-2 rounded-full transition-all duration-300" style={{ width: `${getProgress()}%` }} />
                            </div>
                          </div>

                          <div className="space-y-3 pt-2">
                            {plan.tasks.map((t) => (
                              <div key={t.id} className="p-3 border border-border/80 rounded-lg bg-muted/20 flex flex-col gap-2 justify-between sm:flex-row sm:items-center">
                                <div>
                                  <div className="font-semibold text-xs text-foreground">{t.taskName}</div>
                                  <div className="text-xs text-muted-foreground leading-relaxed mt-0.5">{t.description}</div>
                                  <div className="flex gap-2 text-[10px] mt-1.5 text-muted-foreground">
                                    <span>Assigned Role: <strong className="text-foreground">{t.assignedRole}</strong></span>
                                    {t.dueDate && <span>Due Date: <strong className="text-foreground">{t.dueDate}</strong></span>}
                                  </div>
                                </div>
                                <select
                                  value={t.status}
                                  onChange={(e) => updateTaskMutation.mutate({ taskId: t.id, status: e.target.value })}
                                  className="text-xs bg-background border border-input rounded px-2 py-1 h-8 sm:self-center font-medium"
                                >
                                  <option value="NOT_STARTED">Not Started</option>
                                  <option value="IN_PROGRESS">In Progress</option>
                                  <option value="COMPLETED">Completed</option>
                                  <option value="WAIVED">Waived</option>
                                </select>
                              </div>
                            ))}
                          </div>
                        </div>
                      )}
                    </div>
                  )}

                  {/* TAB 3: Documents Verification */}
                  {detailTab === 'documents' && (
                    <div className="space-y-4">
                      {/* Document Request trigger */}
                      <form onSubmit={handleRequestDoc} className="flex gap-2 items-end max-w-sm pb-2 border-b border-border/50">
                        <div className="flex-1">
                          <label className="text-xs text-muted-foreground">Request Document Type</label>
                          <select
                            value={newDocType}
                            onChange={(e) => setNewDocType(e.target.value)}
                            className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-xs h-8"
                          >
                            <option value="DIPLOMA_DEGREE">Degree/Diploma Verification</option>
                            <option value="VISA_PASSPORT">Passport/Visa Copy</option>
                            <option value="TAX_FORM">Tax Withholding Declaration</option>
                            <option value="MEDICAL_CERTIFICATE">Health/Medical Checkup</option>
                          </select>
                        </div>
                        <Button type="submit" size="sm" className="h-8" disabled={requestDocMutation.isPending}>
                          Request
                        </Button>
                      </form>

                      {docsLoading ? (
                        <p className="text-xs text-muted-foreground">Loading documents list…</p>
                      ) : !documents || documents.length === 0 ? (
                        <p className="text-xs text-muted-foreground italic">No verification documents requested yet.</p>
                      ) : (
                        <div className="space-y-3 pt-2">
                          {documents.map((d) => (
                            <div key={d.id} className="p-3 border border-border/80 rounded-lg bg-card text-xs flex flex-col gap-2 justify-between sm:flex-row sm:items-center">
                              <div>
                                <div className="font-semibold text-foreground uppercase tracking-wider">{d.documentType.replace('_', ' ')}</div>
                                <div className="text-[10px] text-muted-foreground mt-0.5">
                                  Status: <span className="font-semibold uppercase text-primary">{d.status}</span>
                                </div>
                                {d.storageKey && (
                                  <div className="text-[10px] font-mono text-muted-foreground mt-1 bg-muted/40 p-1 rounded max-w-xs truncate">
                                    {d.storageKey}
                                  </div>
                                )}
                              </div>
                              <div className="flex gap-1.5">
                                {d.status === 'REQUESTED' && (
                                  <Button
                                    size="sm"
                                    variant="outline"
                                    onClick={() => handleSimulateUpload(d.id, d.documentType)}
                                  >
                                    Simulate Upload
                                  </Button>
                                )}
                                {d.status === 'UPLOADED' && isHrOrAdmin && (
                                  <>
                                    <Button
                                      size="sm"
                                      onClick={() => reviewDocMutation.mutate({ docId: d.id, status: 'ACCEPTED' })}
                                    >
                                      Accept
                                    </Button>
                                    <Button
                                      size="sm"
                                      variant="outline"
                                      className="text-red-500"
                                      onClick={() => reviewDocMutation.mutate({ docId: d.id, status: 'REJECTED' })}
                                    >
                                      Reject
                                    </Button>
                                  </>
                                )}
                              </div>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  )}

                  {/* TAB 4: Background Verification */}
                  {detailTab === 'background' && (
                    <div className="space-y-4">
                      <div className="flex justify-between items-center pb-2 border-b border-border">
                        <div>
                          <span className="font-semibold text-xs text-foreground block">Background Screenings</span>
                          <span className="text-[11px] text-muted-foreground block mt-0.5">
                            Verify security checks, academic records, and past references.
                          </span>
                        </div>
                        {isHrOrAdmin && (
                          <Button
                            size="sm"
                            onClick={() => triggerBgCheckMutation.mutate()}
                            disabled={triggerBgCheckMutation.isPending}
                          >
                            {triggerBgCheckMutation.isPending ? 'Starting…' : 'Trigger Screening'}
                          </Button>
                        )}
                      </div>

                      {(!backgroundChecks || backgroundChecks.length === 0) ? (
                        <div className="text-center py-6 text-muted-foreground text-xs italic">
                          No background checks initiated yet.
                        </div>
                      ) : (
                        <div className="grid gap-3 pt-2">
                          {backgroundChecks.map((bc) => (
                            <div key={bc.id} className="p-3 border border-border rounded-lg bg-card text-xs flex justify-between items-center">
                              <div>
                                <div className="font-bold text-foreground">Verification Check</div>
                                <div className="text-muted-foreground mt-0.5">
                                  Started: {new Date(bc.createdAt).toLocaleDateString()}
                                </div>
                              </div>
                              <div className="flex items-center gap-3">
                                <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-semibold uppercase ${
                                  bc.status === 'CLEARED'
                                    ? 'bg-green-500/10 text-green-500'
                                    : bc.status === 'FAILED'
                                    ? 'bg-red-500/10 text-red-500'
                                    : 'bg-yellow-500/10 text-yellow-500'
                                }`}>
                                  {bc.status}
                                </span>
                                {isHrOrAdmin && bc.status === 'IN_PROGRESS' && (
                                  <div className="flex gap-1.5">
                                    <Button
                                      size="sm"
                                      onClick={() => updateBgCheckStatusMutation.mutate({ checkId: bc.id, status: 'CLEARED' })}
                                      disabled={updateBgCheckStatusMutation.isPending}
                                    >
                                      Clear
                                    </Button>
                                    <Button
                                      size="sm"
                                      variant="outline"
                                      className="text-red-500"
                                      onClick={() => updateBgCheckStatusMutation.mutate({ checkId: bc.id, status: 'FAILED' })}
                                      disabled={updateBgCheckStatusMutation.isPending}
                                    >
                                      Fail
                                    </Button>
                                  </div>
                                )}
                              </div>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  )}

                  {/* TAB 5: IT Asset Provisioning */}
                  {detailTab === 'assets' && (
                    <div className="space-y-4">
                      <div className="pb-2 border-b border-border">
                        <span className="font-semibold text-xs text-foreground block">Provision Hardware & Assets</span>
                        <span className="text-[11px] text-muted-foreground block mt-0.5">
                          Request and track setup for pre-hire work devices.
                        </span>
                      </div>

                      {isHrOrAdmin && (
                        <div className="flex gap-2 items-center bg-muted/50 p-3 rounded-lg border border-border">
                          <select
                            value={newAssetType}
                            onChange={(e) => setNewAssetType(e.target.value)}
                            className="rounded-md border border-input bg-background px-3 py-1.5 text-xs h-8 flex-1"
                          >
                            <option value="LAPTOP">MacBook / ThinkPad Laptop</option>
                            <option value="MONITOR">4K UltraWide Monitor</option>
                            <option value="PHONE">iPhone / Android Device</option>
                            <option value="ACCESSORIES">Keyboard & Mouse Set</option>
                          </select>
                          <Button
                            size="sm"
                            onClick={() => requestAssetMutation.mutate(newAssetType)}
                            disabled={requestAssetMutation.isPending}
                          >
                            Request Allocation
                          </Button>
                        </div>
                      )}

                      {(!assetRequests || assetRequests.length === 0) ? (
                        <div className="text-center py-6 text-muted-foreground text-xs italic">
                          No hardware allocations requested.
                        </div>
                      ) : (
                        <div className="grid gap-3 pt-2">
                          {assetRequests.map((asset) => (
                            <div key={asset.id} className="p-3 border border-border rounded-lg bg-card text-xs flex justify-between items-center">
                              <div>
                                <div className="font-bold text-foreground capitalize">{asset.assetType.toLowerCase()}</div>
                                <div className="text-muted-foreground mt-0.5">
                                  Requested: {new Date(asset.createdAt).toLocaleDateString()}
                                </div>
                              </div>
                              <div className="flex items-center gap-3">
                                <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-semibold uppercase ${
                                  asset.status === 'DELIVERED'
                                    ? 'bg-green-500/10 text-green-500'
                                    : asset.status === 'RESERVED'
                                    ? 'bg-blue-500/10 text-blue-500'
                                    : 'bg-yellow-500/10 text-yellow-500'
                                }`}>
                                  {asset.status}
                                </span>
                                {isHrOrAdmin && (
                                  <div className="flex gap-1.5">
                                    {asset.status === 'REQUESTED' && (
                                      <Button
                                        size="sm"
                                        onClick={() => updateAssetStatusMutation.mutate({ assetId: asset.id, status: 'RESERVED' })}
                                        disabled={updateAssetStatusMutation.isPending}
                                      >
                                        Reserve Device
                                      </Button>
                                    )}
                                    {asset.status === 'RESERVED' && (
                                      <Button
                                        size="sm"
                                        onClick={() => updateAssetStatusMutation.mutate({ assetId: asset.id, status: 'DELIVERED' })}
                                        disabled={updateAssetStatusMutation.isPending}
                                      >
                                        Mark Delivered
                                      </Button>
                                    )}
                                  </div>
                                )}
                              </div>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  )}

                  {/* TAB 6: Employee Activation */}
                  {detailTab === 'activation' && (
                    <div className="space-y-4">
                      {selectedPreHire.status === 'ACTIVATED' ? (
                        <div className="text-center py-6 space-y-2 bg-green-500/10 border border-green-500/20 rounded-lg p-4">
                          <span className="text-green-500 font-bold block text-sm">✓ Candidate Activated as Employee</span>
                          <p className="text-xs text-muted-foreground">
                            This pre-hire profile has been converted into an active employee record successfully.
                          </p>
                        </div>
                      ) : (
                        <form onSubmit={handleActivationSubmit} className="space-y-4 max-w-md">
                          <span className="font-semibold text-xs block text-foreground">Activate Pre-Hire Employee Profile</span>
                          <span className="text-[11px] text-muted-foreground block mt-0.5">
                            Before activation, please verify that all verification tasks are complete and right-to-work documents have been verified.
                          </span>

                          <div className="space-y-3 pt-2">
                            <div>
                              <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider block">Employee Code</label>
                              <input
                                type="text"
                                placeholder="e.g. EMP1001"
                                required
                                value={employeeCode}
                                onChange={(e) => setEmployeeCode(e.target.value)}
                                className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-xs h-8"
                              />
                            </div>
                            <div>
                              <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider block">Date of Birth</label>
                              <input
                                type="date"
                                required
                                value={dateOfBirth}
                                onChange={(e) => setDateOfBirth(e.target.value)}
                                className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-xs h-8"
                              />
                            </div>
                            <div>
                              <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider block">Phone Number</label>
                              <input
                                type="text"
                                placeholder="e.g. +1 555 0199"
                                value={phoneNumber}
                                onChange={(e) => setPhoneNumber(e.target.value)}
                                className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-xs h-8"
                              />
                            </div>
                          </div>

                          <div className="flex justify-end gap-3 pt-4 border-t border-border">
                            <Button
                              type="submit"
                              disabled={activateMutation.isPending || !employeeCode || !dateOfBirth}
                            >
                              {activateMutation.isPending ? 'Activating…' : 'Activate Employee Record'}
                            </Button>
                          </div>
                        </form>
                      )}
                    </div>
                  )}

                </CardContent>
              </Card>
            ) : (
              <div className="h-48 border border-dashed border-border rounded-lg flex items-center justify-center text-muted-foreground text-xs italic">
                Select a pre-hire profile to review their details workspace
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
