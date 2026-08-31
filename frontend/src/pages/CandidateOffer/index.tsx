import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import {
  fetchPublicOfferDetails,
  acceptPublicOffer,
  rejectPublicOffer
} from '@/api/services/recruitmentService';

export function CandidateOfferPage() {
  const { secureToken } = useParams<{ secureToken: string }>();
  const queryClient = useQueryClient();
  const [signatureName, setSignatureName] = useState('');
  const [isSigning, setIsSigning] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const { data: offer, isLoading, error } = useQuery({
    queryKey: ['candidate', 'offer', secureToken],
    queryFn: () => fetchPublicOfferDetails(secureToken!),
    enabled: !!secureToken,
  });

  const acceptMutation = useMutation({
    mutationFn: () => acceptPublicOffer(secureToken!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['candidate', 'offer', secureToken] });
      setIsSigning(false);
      setMessage({ type: 'success', text: 'Congratulations! You have successfully accepted the offer. Welcome to Acme Corporation!' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to accept offer.' });
    },
  });

  const rejectMutation = useMutation({
    mutationFn: () => rejectPublicOffer(secureToken!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['candidate', 'offer', secureToken] });
      setMessage({ type: 'success', text: 'You have declined the employment offer.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to decline offer.' });
    },
  });

  const handleAcceptSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!signatureName.trim()) return;
    acceptMutation.mutate();
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background text-foreground">
        <p className="text-muted-foreground text-sm">Loading offer details…</p>
      </div>
    );
  }

  if (error || !offer) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background text-foreground">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle className="text-red-500">Offer Not Found</CardTitle>
            <CardDescription>The secure token is invalid or the offer has expired.</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-xs text-muted-foreground">
              Please contact the recruitment team if you believe this is an error.
            </p>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background text-foreground p-6 sm:p-12 max-w-4xl mx-auto space-y-8">
      <header className="text-center py-6">
        <h1 className="text-3xl font-extrabold tracking-tight sm:text-4xl text-primary">Employment Offer Portal</h1>
        <p className="text-muted-foreground mt-2">Acme Corporation Career Services</p>
      </header>

      {message && (
        <div
          className={`p-4 rounded-md text-sm text-center max-w-lg mx-auto ${
            message.type === 'success'
              ? 'bg-green-500/10 text-green-500 border border-green-500/20'
              : 'bg-red-500/10 text-red-500 border border-red-500/20'
          }`}
        >
          {message.text}
        </div>
      )}

      <Card className="border border-border/80 shadow-lg">
        <CardHeader className="border-b border-border/60 pb-6">
          <div className="flex justify-between items-start">
            <div>
              <CardTitle className="text-2xl font-bold">{offer.jobTitle}</CardTitle>
              <CardDescription className="mt-1">Prepared for: <span className="font-semibold text-foreground">{offer.candidateName}</span> ({offer.candidateEmail})</CardDescription>
            </div>
            <span className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold ${
              offer.status === 'ACCEPTED'
                ? 'bg-green-500/10 text-green-500'
                : offer.status === 'REJECTED'
                ? 'bg-red-500/10 text-red-500'
                : 'bg-yellow-500/10 text-yellow-500'
            }`}>
              {offer.status}
            </span>
          </div>
        </CardHeader>
        <CardContent className="space-y-6 pt-6 text-sm">
          <div className="grid gap-4 sm:grid-cols-3 bg-muted/40 p-4 rounded-lg">
            <div>
              <span className="text-xs text-muted-foreground block">Base Salary Package</span>
              <span className="font-bold text-lg text-primary">{offer.salaryAmount.toLocaleString()} {offer.currencyCode} / year</span>
            </div>
            <div>
              <span className="text-xs text-muted-foreground block">Target Start Date</span>
              <span className="font-bold text-lg text-foreground">{offer.startDate}</span>
            </div>
            <div>
              <span className="text-xs text-muted-foreground block">Corporate Benefits</span>
              <span className="font-semibold text-foreground block">Full Coverage (Health, Dental)</span>
            </div>
          </div>

          <div className="space-y-3 leading-relaxed">
            <h3 className="text-base font-bold">Standard Terms & Conditions</h3>
            <p className="text-muted-foreground text-xs">
              1. **At-Will Employment**: Your employment with Acme Corporation is at-will, meaning either you or the company may terminate the employment relationship at any time for any reason.
            </p>
            <p className="text-muted-foreground text-xs">
              2. **Compliance & Verification**: This offer is contingent upon successful reference checks, background screenings, and validation of right-to-work documentation in your local jurisdiction.
            </p>
            <p className="text-muted-foreground text-xs">
              3. **Onboarding Tasks**: Upon acceptance of this offer, you will receive a secure portal link to complete compliance task items, policy check-offs, and asset provisioning requests before your start date.
            </p>
          </div>

          {/* Interactive Actions */}
          {offer.status === 'SENT' && (
            <div className="border-t border-border pt-6 flex flex-col gap-4">
              {!isSigning ? (
                <div className="flex justify-end gap-3">
                  <Button
                    variant="outline"
                    className="text-red-500 border-red-500/20 hover:bg-red-500/10"
                    onClick={() => { if (confirm('Are you sure you want to decline this offer?')) rejectMutation.mutate(); }}
                    disabled={rejectMutation.isPending}
                  >
                    Decline Offer
                  </Button>
                  <Button onClick={() => setIsSigning(true)}>
                    Accept Offer & Sign
                  </Button>
                </div>
              ) : (
                <form onSubmit={handleAcceptSubmit} className="space-y-4 max-w-md ml-auto w-full">
                  <div>
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider block">Candidate Digital Signature</label>
                    <span className="text-[11px] text-muted-foreground block mt-0.5">Please type your full name exactly to sign this document.</span>
                    <input
                      type="text"
                      placeholder="e.g. John Doe"
                      required
                      value={signatureName}
                      onChange={(e) => setSignatureName(e.target.value)}
                      className="mt-2 block w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    />
                  </div>
                  <div className="flex justify-end gap-3">
                    <Button type="button" variant="outline" onClick={() => setIsSigning(false)}>
                      Back
                    </Button>
                    <Button type="submit" disabled={acceptMutation.isPending || !signatureName.trim()}>
                      {acceptMutation.isPending ? 'Confirming…' : 'Sign & Accept Offer'}
                    </Button>
                  </div>
                </form>
              )}
            </div>
          )}

          {offer.status === 'ACCEPTED' && (
            <div className="border-t border-border pt-6 text-center space-y-2">
              <span className="text-green-500 font-bold block">✓ Offer Accepted & Signed</span>
              <p className="text-xs text-muted-foreground">
                Your pre-hire registration has been successfully created. Our HR team will reach out with the onboarding portal links and checklist tasks shortly.
              </p>
            </div>
          )}

          {offer.status === 'REJECTED' && (
            <div className="border-t border-border pt-6 text-center">
              <span className="text-red-500 font-semibold">✗ Offer Declined</span>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
