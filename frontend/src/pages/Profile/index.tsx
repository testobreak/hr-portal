import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
import { Button } from '@/components/ui/button';
import { useMe } from '@/hooks/useMe';
import { Roles, hasAnyRole } from '@/lib/roles';
import {
  fetchMyProfile,
  updateMyProfile,
  submitProfileChangeRequest,
  fetchPendingChangeRequests,
  approveChangeRequest,
  rejectChangeRequest
} from '@/api/services/profileService';

type TabType = 'personal' | 'family' | 'professional' | 'requests';

export function ProfilePage() {
  const queryClient = useQueryClient();
  const { data: me } = useMe();
  const roles = me?.roles ?? [];
  const isHr = hasAnyRole(roles, [Roles.HR_ADMIN, Roles.SUPER_ADMIN]);

  const [activeTab, setActiveTab] = useState<TabType>('personal');
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState<Record<string, string>>({});
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Queries
  const { data: profile, isLoading: profileLoading, error: profileError } = useQuery({
    queryKey: ['profile', 'me'],
    queryFn: fetchMyProfile,
  });

  const { data: requests, isLoading: requestsLoading } = useQuery({
    queryKey: ['profile-change-requests', 'pending'],
    queryFn: fetchPendingChangeRequests,
    enabled: isHr,
  });

  // Mutations
  const updateMutation = useMutation({
    mutationFn: async (updates: Record<string, string>) => {
      // Split direct updates and approval-required updates
      const directUpdates: Record<string, string> = {};
      const approvalUpdates: Record<string, string> = {};

      Object.entries(updates).forEach(([key, val]) => {
        if (key === 'bankAccountNumber' || key === 'taxId') {
          approvalUpdates[`personal.${key}`] = val;
        } else {
          directUpdates[`personal.${key}`] = val;
        }
      });

      if (Object.keys(directUpdates).length > 0) {
        await updateMyProfile(directUpdates);
      }
      if (Object.keys(approvalUpdates).length > 0) {
        await submitProfileChangeRequest(approvalUpdates);
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['profile', 'me'] });
      queryClient.invalidateQueries({ queryKey: ['profile-change-requests', 'pending'] });
      setIsEditing(false);
      setMessage({
        type: 'success',
        text: 'Profile updated. Sensitive changes (e.g. Bank Account, Tax ID) have been submitted to HR for approval.',
      });
    },
    onError: (err: any) => {
      setMessage({
        type: 'error',
        text: err?.message || 'Failed to update profile.',
      });
    },
  });

  const approveMutation = useMutation({
    mutationFn: approveChangeRequest,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['profile', 'me'] });
      queryClient.invalidateQueries({ queryKey: ['profile-change-requests', 'pending'] });
      setMessage({ type: 'success', text: 'Change request approved successfully.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to approve request.' });
    },
  });

  const rejectMutation = useMutation({
    mutationFn: rejectChangeRequest,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['profile-change-requests', 'pending'] });
      setMessage({ type: 'success', text: 'Change request rejected.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to reject request.' });
    },
  });


  const startEditing = () => {
    if (!profile?.personal) return;
    setFormData({
      preferredName: profile.personal.preferredName || '',
      email: profile.personal.email || '',
      phoneNumber: profile.personal.phoneNumber || '',
      bankAccountNumber: '', // Do not pre-fill masked or sensitive data to modify
      taxId: '',
    });
    setIsEditing(true);
    setMessage(null);
  };

  const handleInputChange = (field: string, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const saveChanges = () => {
    // Filter out unchanged or empty values to avoid submitting empty request changes
    const updates: Record<string, string> = {};
    if (formData.preferredName !== (profile?.personal?.preferredName ?? '')) {
      updates.preferredName = formData.preferredName;
    }
    if (formData.email !== (profile?.personal?.email ?? '')) {
      updates.email = formData.email;
    }
    if (formData.phoneNumber !== (profile?.personal?.phoneNumber ?? '')) {
      updates.phoneNumber = formData.phoneNumber;
    }
    if (formData.bankAccountNumber) {
      updates.bankAccountNumber = formData.bankAccountNumber;
    }
    if (formData.taxId) {
      updates.taxId = formData.taxId;
    }

    if (Object.keys(updates).length === 0) {
      setIsEditing(false);
      return;
    }

    updateMutation.mutate(updates);
  };

  if (profileLoading) {
    return <div className="p-6 text-muted-foreground">Loading profile information…</div>;
  }

  if (profileError || !profile) {
    return <div className="p-6 text-red-600">Error loading profile data.</div>;
  }

  return (
    <div className="space-y-6">
      <header className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">My Profile</h1>
          <p className="text-sm text-muted-foreground">Manage your personal, professional, and payroll details.</p>
        </div>
        {!isEditing && (
          <Button onClick={startEditing} className="sm:self-end">
            Edit Details
          </Button>
        )}
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

      {/* Tabs Bar */}
      <div className="flex border-b border-border gap-4 overflow-x-auto pb-px">
        <button
          onClick={() => { setActiveTab('personal'); setMessage(null); }}
          className={`pb-2 text-sm font-medium transition-colors border-b-2 px-1 ${
            activeTab === 'personal'
              ? 'border-primary text-foreground'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          Personal Info
        </button>
        <button
          onClick={() => { setActiveTab('family'); setMessage(null); }}
          className={`pb-2 text-sm font-medium transition-colors border-b-2 px-1 ${
            activeTab === 'family'
              ? 'border-primary text-foreground'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          Emergency & Dependents
        </button>
        <button
          onClick={() => { setActiveTab('professional'); setMessage(null); }}
          className={`pb-2 text-sm font-medium transition-colors border-b-2 px-1 ${
            activeTab === 'professional'
              ? 'border-primary text-foreground'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          Professional & Education
        </button>
        {isHr && (
          <button
            onClick={() => { setActiveTab('requests'); setMessage(null); }}
            className={`pb-2 text-sm font-medium transition-colors border-b-2 px-1 ${
              activeTab === 'requests'
                ? 'border-primary text-foreground'
                : 'border-transparent text-muted-foreground hover:text-foreground'
            }`}
          >
            HR Request Queue
          </button>
        )}
      </div>

      {/* Tab Panels */}
      {activeTab === 'personal' && (
        <Card>
          <CardHeader>
            <CardTitle>Personal Details</CardTitle>
            <CardDescription>Your contact details, legal identification, and bank accounts.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="grid gap-4 sm:grid-cols-2">
              <div>
                <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">First Name</label>
                <p className="mt-1 font-medium">{profile.personal.firstName}</p>
              </div>
              <div>
                <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Last Name</label>
                <p className="mt-1 font-medium">{profile.personal.lastName}</p>
              </div>
              <div>
                <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Preferred Name</label>
                {isEditing ? (
                  <input
                    type="text"
                    value={formData.preferredName}
                    onChange={(e) => handleInputChange('preferredName', e.target.value)}
                    className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                  />
                ) : (
                  <p className="mt-1 font-medium">{profile.personal.preferredName || '—'}</p>
                )}
              </div>
              <div>
                <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Date of Birth</label>
                <p className="mt-1 font-medium">{profile.personal.dateOfBirth || '—'}</p>
              </div>
              <div>
                <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Work Email</label>
                {isEditing ? (
                  <input
                    type="email"
                    value={formData.email}
                    onChange={(e) => handleInputChange('email', e.target.value)}
                    className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                  />
                ) : (
                  <p className="mt-1 font-medium">{profile.personal.email || '—'}</p>
                )}
              </div>
              <div>
                <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Phone Number</label>
                {isEditing ? (
                  <input
                    type="text"
                    value={formData.phoneNumber}
                    onChange={(e) => handleInputChange('phoneNumber', e.target.value)}
                    className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                  />
                ) : (
                  <p className="mt-1 font-medium">{profile.personal.phoneNumber || '—'}</p>
                )}
              </div>
            </div>

            <Separator />

            <div className="grid gap-4 sm:grid-cols-2">
              <div>
                <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider flex items-center gap-1.5">
                  Bank Account Number
                  {isEditing && <span className="text-[10px] text-yellow-500 font-normal lowercase">(requires approval)</span>}
                </label>
                {isEditing ? (
                  <input
                    type="text"
                    placeholder="Enter new bank account number"
                    value={formData.bankAccountNumber}
                    onChange={(e) => handleInputChange('bankAccountNumber', e.target.value)}
                    className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                  />
                ) : (
                  <p className="mt-1 font-mono font-medium">{profile.personal.bankAccountNumber || '—'}</p>
                )}
              </div>
              <div>
                <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider flex items-center gap-1.5">
                  Tax Identifier (PAN/TIN)
                  {isEditing && <span className="text-[10px] text-yellow-500 font-normal lowercase">(requires approval)</span>}
                </label>
                {isEditing ? (
                  <input
                    type="text"
                    placeholder="Enter new tax identifier"
                    value={formData.taxId}
                    onChange={(e) => handleInputChange('taxId', e.target.value)}
                    className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                  />
                ) : (
                  <p className="mt-1 font-mono font-medium">{profile.personal.taxId || '—'}</p>
                )}
              </div>
            </div>

            {isEditing && (
              <div className="flex justify-end gap-3 pt-4 border-t border-border">
                <Button variant="outline" onClick={() => setIsEditing(false)}>
                  Cancel
                </Button>
                <Button onClick={saveChanges} disabled={updateMutation.isPending}>
                  {updateMutation.isPending ? 'Saving…' : 'Save & Request Changes'}
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {activeTab === 'family' && (
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Emergency Contacts</CardTitle>
              <CardDescription>Key individuals to contact in case of emergency events.</CardDescription>
            </CardHeader>
            <CardContent>
              {profile.emergencyContacts.length === 0 ? (
                <p className="text-muted-foreground text-sm">No emergency contacts listed.</p>
              ) : (
                <div className="divide-y divide-border">
                  {profile.emergencyContacts.map((c) => (
                    <div key={c.id} className="py-3 first:pt-0 last:pb-0 flex justify-between items-center">
                      <div>
                        <p className="font-medium text-sm">{c.name}</p>
                        <p className="text-xs text-muted-foreground">{c.relationship}</p>
                      </div>
                      <p className="text-sm font-medium">{c.phone}</p>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Dependents</CardTitle>
              <CardDescription>Your registered family members and dependents.</CardDescription>
            </CardHeader>
            <CardContent>
              {profile.dependents.length === 0 ? (
                <p className="text-muted-foreground text-sm">No dependents listed.</p>
              ) : (
                <div className="divide-y divide-border">
                  {profile.dependents.map((d) => (
                    <div key={d.id} className="py-3 first:pt-0 last:pb-0 flex justify-between items-center">
                      <div>
                        <p className="font-medium text-sm">{d.name}</p>
                        <p className="text-xs text-muted-foreground">{d.relationship}</p>
                      </div>
                      <p className="text-sm">{d.dateOfBirth || '—'}</p>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      )}

      {activeTab === 'professional' && (
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Skills Profile</CardTitle>
              <CardDescription>Your primary core capabilities and proficiency levels.</CardDescription>
            </CardHeader>
            <CardContent>
              {profile.skills.length === 0 ? (
                <p className="text-muted-foreground text-sm">No skills added.</p>
              ) : (
                <div className="flex flex-wrap gap-2">
                  {profile.skills.map((s) => (
                    <span key={s.id} className="inline-flex items-center gap-1.5 rounded-full bg-primary/10 border border-primary/20 px-3 py-1 text-xs font-medium text-primary">
                      {s.skillName}
                      {s.proficiency && <span className="opacity-60">({s.proficiency})</span>}
                    </span>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Work Experience</CardTitle>
              <CardDescription>Previous professional records and employment history.</CardDescription>
            </CardHeader>
            <CardContent>
              {profile.experience.length === 0 ? (
                <p className="text-muted-foreground text-sm">No experience history registered.</p>
              ) : (
                <div className="relative border-l border-border pl-6 space-y-6 ml-2">
                  {profile.experience.map((e) => (
                    <div key={e.id} className="relative">
                      <div className="absolute -left-[31px] top-1.5 h-3 w-3 rounded-full bg-border border border-background" />
                      <p className="font-semibold text-sm">{e.role}</p>
                      <p className="text-xs text-muted-foreground">{e.companyName}</p>
                      <p className="text-xs text-muted-foreground mt-0.5">
                        {e.startDate} to {e.endDate || 'Present'}
                      </p>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Education Background</CardTitle>
              <CardDescription>Academic degrees, courses, and certifications.</CardDescription>
            </CardHeader>
            <CardContent>
              {profile.education.length === 0 ? (
                <p className="text-muted-foreground text-sm">No education details recorded.</p>
              ) : (
                <div className="divide-y divide-border">
                  {profile.education.map((edu) => (
                    <div key={edu.id} className="py-3 first:pt-0 last:pb-0">
                      <p className="font-medium text-sm">{edu.degree}</p>
                      <p className="text-xs text-muted-foreground">
                        {edu.institution} {edu.yearOfPassing ? `(Class of ${edu.yearOfPassing})` : ''}
                      </p>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Certifications</CardTitle>
              <CardDescription>Professional certificates and active licenses.</CardDescription>
            </CardHeader>
            <CardContent>
              {profile.certifications.length === 0 ? (
                <p className="text-muted-foreground text-sm">No certifications listed.</p>
              ) : (
                <div className="divide-y divide-border">
                  {profile.certifications.map((c) => (
                    <div key={c.id} className="py-3 first:pt-0 last:pb-0 flex justify-between items-center">
                      <div>
                        <p className="font-medium text-sm">{c.certificationName}</p>
                        <p className="text-xs text-muted-foreground">{c.issuer || '—'}</p>
                      </div>
                      {c.expiryDate && <p className="text-xs text-muted-foreground">Expires: {c.expiryDate}</p>}
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      )}

      {activeTab === 'requests' && isHr && (
        <Card>
          <CardHeader>
            <CardTitle>Profile Change Request Queue</CardTitle>
            <CardDescription>Approve or reject sensitive profile field changes submitted by employees.</CardDescription>
          </CardHeader>
          <CardContent>
            {requestsLoading ? (
              <p className="text-muted-foreground text-sm">Loading requests…</p>
            ) : !requests || requests.length === 0 ? (
              <p className="text-muted-foreground text-sm">No pending change requests in queue.</p>
            ) : (
              <div className="space-y-4">
                {requests.map((req) => {
                  let parsedChanges: Record<string, string> = {};
                  try {
                    parsedChanges = JSON.parse(req.changeJson);
                  } catch {}

                  return (
                    <div key={req.id} className="p-4 border border-border rounded-lg bg-card text-card-foreground flex flex-col gap-3 justify-between sm:flex-row sm:items-center">
                      <div className="space-y-1">
                        <p className="text-xs text-muted-foreground">Request ID: {req.id}</p>
                        <p className="text-sm font-semibold">Change proposed on fields:</p>
                        <div className="text-xs space-y-1 bg-muted p-2 rounded-md font-mono mt-1.5">
                          {Object.entries(parsedChanges).map(([key, val]) => (
                            <div key={key}>
                              <span className="text-amber-500">{key}:</span> {val}
                            </div>
                          ))}
                        </div>
                      </div>
                      <div className="flex gap-2 sm:self-center">
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => rejectMutation.mutate(req.id)}
                          disabled={rejectMutation.isPending}
                        >
                          Reject
                        </Button>
                        <Button
                          size="sm"
                          onClick={() => approveMutation.mutate(req.id)}
                          disabled={approveMutation.isPending}
                        >
                          Approve
                        </Button>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}
