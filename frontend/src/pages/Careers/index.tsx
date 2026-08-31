import { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import {
  fetchPublicJobs,
  submitGuestResume,
  submitApplication,
  type JobPostingResponse
} from '@/api/services/recruitmentService';

export function CareersPage() {
  const [selectedJob, setSelectedJob] = useState<JobPostingResponse | null>(null);
  const [isApplying, setIsApplying] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Form states
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [skills, setSkills] = useState('');
  const [profileSummary, setProfileSummary] = useState('');
  const [coverLetter, setCoverLetter] = useState('');
  const [resumeFile, setResumeFile] = useState<File | null>(null);
  const [uploadingResume, setUploadingResume] = useState(false);

  const { data: jobs, isLoading } = useQuery({
    queryKey: ['careers', 'jobs'],
    queryFn: fetchPublicJobs,
  });

  const applyMutation = useMutation({
    mutationFn: (payload: { publicId: string; body: any }) => submitApplication(payload.publicId, payload.body),
    onSuccess: () => {
      setMessage({ type: 'success', text: 'Application submitted successfully! Our recruitment team will review your profile shortly.' });
      setIsApplying(false);
      resetForm();
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to submit application. Please try again.' });
    },
  });

  const resetForm = () => {
    setFirstName('');
    setLastName('');
    setEmail('');
    setPhone('');
    setSkills('');
    setProfileSummary('');
    setCoverLetter('');
    setResumeFile(null);
  };

  const handleApplySubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedJob || !firstName || !lastName || !email || !resumeFile) {
      setMessage({ type: 'error', text: 'Please fill in all required fields and upload your resume.' });
      return;
    }

    setMessage(null);
    setUploadingResume(true);

    try {
      // 1. Get presigned upload URL
      const presign = await submitGuestResume(resumeFile.name, resumeFile.type);

      // 2. Upload file directly to S3/MinIO
      const uploadRes = await fetch(presign.uploadUrl, {
        method: presign.httpMethod,
        headers: {
          'Content-Type': resumeFile.type,
        },
        body: resumeFile,
      });

      if (!uploadRes.ok) {
        throw new Error('Resume file upload failed.');
      }

      setUploadingResume(false);

      // 3. Submit applicant info
      applyMutation.mutate({
        publicId: selectedJob.publicId,
        body: {
          firstName,
          lastName,
          email,
          phone: phone || undefined,
          resumeStorageKey: presign.storageKey,
          skills: skills || undefined,
          profileSummary: profileSummary || undefined,
          coverLetter: coverLetter || undefined,
          source: 'PUBLIC_CAREERS_BOARD',
        },
      });
    } catch (err: any) {
      setUploadingResume(false);
      setMessage({ type: 'error', text: err?.message || 'An error occurred during resume upload.' });
    }
  };

  return (
    <div className="min-h-screen bg-background text-foreground p-6 sm:p-12 space-y-8 max-w-6xl mx-auto">
      <header className="text-center space-y-3 py-8">
        <h1 className="text-4xl font-extrabold tracking-tight sm:text-5xl text-primary">Join Our Team</h1>
        <p className="text-lg text-muted-foreground max-w-xl mx-auto">
          Explore open career opportunities at Acme Corporation and build the future of HR operating systems with us.
        </p>
      </header>

      {message && (
        <div
          className={`p-4 rounded-md text-sm text-center max-w-xl mx-auto ${
            message.type === 'success'
              ? 'bg-green-500/10 text-green-500 border border-green-500/20'
              : 'bg-red-500/10 text-red-500 border border-red-500/20'
          }`}
        >
          {message.text}
        </div>
      )}

      {/* Jobs Grid */}
      {isLoading ? (
        <p className="text-center text-muted-foreground text-sm">Loading job openings…</p>
      ) : !jobs || jobs.length === 0 ? (
        <p className="text-center text-muted-foreground text-sm">No active job openings at the moment. Check back soon!</p>
      ) : (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {jobs.map((job) => (
            <Card
              key={job.id}
              className="cursor-pointer hover:border-primary/50 transition-colors flex flex-col justify-between"
              onClick={() => { setSelectedJob(job); setIsApplying(false); setMessage(null); }}
            >
              <CardHeader>
                <CardDescription className="text-xs uppercase tracking-wider font-semibold text-primary">
                  {job.employmentType} • {job.workArrangement}
                </CardDescription>
                <CardTitle className="text-xl mt-1">{job.title}</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-sm text-muted-foreground line-clamp-3 leading-relaxed">
                  {job.description}
                </p>
                <div className="flex justify-between items-center text-xs pt-4 border-t border-border mt-auto">
                  <span className="text-muted-foreground">{job.locationName || 'Global / Remote'}</span>
                  {job.applicationDeadline && (
                    <span className="text-amber-500 font-medium">Apply by {job.applicationDeadline}</span>
                  )}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Job Details & Apply Modal */}
      {selectedJob && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto">
          <Card className="w-full max-w-2xl max-h-[90vh] overflow-y-auto my-8">
            <CardHeader className="flex flex-row items-center justify-between pb-2 border-b border-border">
              <div>
                <CardDescription className="text-xs uppercase tracking-wider font-semibold text-primary">
                  {selectedJob.employmentType} • {selectedJob.workArrangement}
                </CardDescription>
                <CardTitle className="text-2xl mt-1">{selectedJob.title}</CardTitle>
              </div>
              <Button variant="outline" size="sm" onClick={() => setSelectedJob(null)}>
                Close
              </Button>
            </CardHeader>

            <CardContent className="space-y-6 pt-6">
              {!isApplying ? (
                <>
                  <div className="grid grid-cols-2 gap-4 text-sm bg-muted/40 p-4 rounded-lg">
                    <div>
                      <span className="text-xs text-muted-foreground block">Location</span>
                      <span className="font-semibold">{selectedJob.locationName || 'Global / Remote'}</span>
                    </div>
                    <div>
                      <span className="text-xs text-muted-foreground block">Application Deadline</span>
                      <span className="font-semibold text-amber-500">{selectedJob.applicationDeadline || 'Open until filled'}</span>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <h3 className="text-lg font-bold">Job Description</h3>
                    <p className="text-sm text-foreground/90 whitespace-pre-line leading-relaxed">
                      {selectedJob.description}
                    </p>
                  </div>

                  <div className="flex justify-end gap-3 pt-6 border-t border-border">
                    <Button variant="outline" onClick={() => setSelectedJob(null)}>
                      Back to List
                    </Button>
                    <Button onClick={() => setIsApplying(true)}>
                      Apply Now
                    </Button>
                  </div>
                </>
              ) : (
                <form onSubmit={handleApplySubmit} className="space-y-4">
                  <div className="grid gap-4 sm:grid-cols-2">
                    <div>
                      <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">First Name *</label>
                      <input
                        type="text"
                        required
                        value={firstName}
                        onChange={(e) => setFirstName(e.target.value)}
                        className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                      />
                    </div>
                    <div>
                      <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Last Name *</label>
                      <input
                        type="text"
                        required
                        value={lastName}
                        onChange={(e) => setLastName(e.target.value)}
                        className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                      />
                    </div>
                  </div>

                  <div className="grid gap-4 sm:grid-cols-2">
                    <div>
                      <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Email Address *</label>
                      <input
                        type="email"
                        required
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                      />
                    </div>
                    <div>
                      <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Phone Number</label>
                      <input
                        type="tel"
                        value={phone}
                        onChange={(e) => setPhone(e.target.value)}
                        className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Resume (PDF / DOCX) *</label>
                    <input
                      type="file"
                      required
                      accept=".pdf,.doc,.docx"
                      onChange={(e) => setResumeFile(e.target.files?.[0] || null)}
                      className="mt-1 block w-full text-sm text-muted-foreground file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-primary file:text-primary-foreground hover:file:opacity-90"
                    />
                  </div>

                  <div>
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Skills / Key Competencies</label>
                    <input
                      type="text"
                      placeholder="e.g. React, Spring Boot, AWS"
                      value={skills}
                      onChange={(e) => setSkills(e.target.value)}
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                    />
                  </div>

                  <div>
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Profile Summary</label>
                    <textarea
                      placeholder="Briefly pitch your professional experience…"
                      value={profileSummary}
                      onChange={(e) => setProfileSummary(e.target.value)}
                      rows={2}
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                    />
                  </div>

                  <div>
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Cover Letter</label>
                    <textarea
                      placeholder="Explain why you are a great fit for this position…"
                      value={coverLetter}
                      onChange={(e) => setCoverLetter(e.target.value)}
                      rows={4}
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                    />
                  </div>

                  <div className="flex justify-end gap-3 pt-6 border-t border-border">
                    <Button type="button" variant="outline" onClick={() => setIsApplying(false)}>
                      Back
                    </Button>
                    <Button
                      type="submit"
                      disabled={uploadingResume || applyMutation.isPending}
                    >
                      {uploadingResume ? 'Uploading Resume…' : applyMutation.isPending ? 'Submitting…' : 'Submit Application'}
                    </Button>
                  </div>
                </form>
              )}
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}
