import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { useMe } from '@/hooks/useMe';
import { Roles, hasAnyRole } from '@/lib/roles';
import {
  fetchMyLeaveBalances,
  fetchMyLeaveRequests,
  createLeaveRequest,
  submitLeaveRequest,
  cancelLeaveRequest,
  fetchTeamLeaveCalendar,
  fetchMyLeaveLedger,
  type LeaveBalanceResponse
} from '@/api/services/leaveService';
import {
  fetchPendingApprovals,
  approveRequest,
  rejectRequest
} from '@/api/services/workflowService';

type TabType = 'balances' | 'history' | 'approvals' | 'calendar';

export function LeavePage() {
  const queryClient = useQueryClient();
  const { data: me } = useMe();
  const roles = me?.roles ?? [];
  const isManagerOrHr = hasAnyRole(roles, [Roles.MANAGER, Roles.HR_ADMIN, Roles.SUPER_ADMIN]);

  const [activeTab, setActiveTab] = useState<TabType>('balances');
  const [selectedLeaveType, setSelectedLeaveType] = useState<LeaveBalanceResponse | null>(null);
  const [isRequestModalOpen, setIsRequestModalOpen] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Form state
  const [leaveTypeId, setLeaveTypeId] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [reason, setReason] = useState('');

  // Calendar dates
  const [calStart, setCalStart] = useState('2026-07-01');
  const [calEnd, setCalEnd] = useState('2026-07-31');

  // Queries
  const { data: balances, isLoading: balancesLoading } = useQuery({
    queryKey: ['leave', 'balances'],
    queryFn: fetchMyLeaveBalances,
  });

  const { data: requests, isLoading: requestsLoading } = useQuery({
    queryKey: ['leave', 'requests'],
    queryFn: fetchMyLeaveRequests,
  });

  const { data: ledger, isLoading: ledgerLoading } = useQuery({
    queryKey: ['leave', 'ledger', selectedLeaveType?.leaveTypeId],
    queryFn: () => fetchMyLeaveLedger(selectedLeaveType!.leaveTypeId),
    enabled: !!selectedLeaveType,
  });

  const { data: approvals, isLoading: approvalsLoading } = useQuery({
    queryKey: ['workflow', 'approvals'],
    queryFn: fetchPendingApprovals,
    enabled: isManagerOrHr,
  });

  const { data: teamCalendar, isLoading: calLoading } = useQuery({
    queryKey: ['leave', 'team-calendar', calStart, calEnd],
    queryFn: () => fetchTeamLeaveCalendar(calStart, calEnd),
    enabled: isManagerOrHr,
  });

  // Mutations
  const createRequestMutation = useMutation({
    mutationFn: async (payload: { leaveTypeId: string; startDate: string; endDate: string; reason?: string }) => {
      const draft = await createLeaveRequest(payload);
      await submitLeaveRequest(draft.id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['leave', 'balances'] });
      queryClient.invalidateQueries({ queryKey: ['leave', 'requests'] });
      queryClient.invalidateQueries({ queryKey: ['workflow', 'approvals'] });
      setIsRequestModalOpen(false);
      setLeaveTypeId('');
      setStartDate('');
      setEndDate('');
      setReason('');
      setMessage({ type: 'success', text: 'Leave request submitted successfully for approval.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to submit leave request.' });
    },
  });

  const cancelRequestMutation = useMutation({
    mutationFn: cancelLeaveRequest,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['leave', 'balances'] });
      queryClient.invalidateQueries({ queryKey: ['leave', 'requests'] });
      setMessage({ type: 'success', text: 'Leave request cancelled successfully.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to cancel request.' });
    },
  });

  const approveMutation = useMutation({
    mutationFn: approveRequest,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workflow', 'approvals'] });
      queryClient.invalidateQueries({ queryKey: ['leave', 'team-calendar'] });
      setMessage({ type: 'success', text: 'Request approved successfully.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to approve request.' });
    },
  });

  const rejectMutation = useMutation({
    mutationFn: rejectRequest,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workflow', 'approvals'] });
      setMessage({ type: 'success', text: 'Request rejected successfully.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to reject request.' });
    },
  });

  const handleApplyLeave = (e: React.FormEvent) => {
    e.preventDefault();
    if (!leaveTypeId || !startDate || !endDate) return;
    setMessage(null);
    createRequestMutation.mutate({ leaveTypeId, startDate, endDate, reason });
  };

  return (
    <div className="space-y-6">
      <header className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">Leaves & Absence</h1>
          <p className="text-sm text-muted-foreground">Manage your leave balances, view calendars, and submit leave requests.</p>
        </div>
        <Button onClick={() => { setIsRequestModalOpen(true); setMessage(null); }} className="sm:self-end">
          Request Time Off
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
          onClick={() => { setActiveTab('balances'); setMessage(null); }}
          className={`pb-2 text-sm font-medium transition-colors border-b-2 px-1 ${
            activeTab === 'balances'
              ? 'border-primary text-foreground'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          Leave Balances
        </button>
        <button
          onClick={() => { setActiveTab('history'); setMessage(null); }}
          className={`pb-2 text-sm font-medium transition-colors border-b-2 px-1 ${
            activeTab === 'history'
              ? 'border-primary text-foreground'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          Request History
        </button>
        {isManagerOrHr && (
          <>
            <button
              onClick={() => { setActiveTab('approvals'); setMessage(null); }}
              className={`pb-2 text-sm font-medium transition-colors border-b-2 px-1 ${
                activeTab === 'approvals'
                  ? 'border-primary text-foreground'
                  : 'border-transparent text-muted-foreground hover:text-foreground'
              }`}
            >
              Pending Approvals
            </button>
            <button
              onClick={() => { setActiveTab('calendar'); setMessage(null); }}
              className={`pb-2 text-sm font-medium transition-colors border-b-2 px-1 ${
                activeTab === 'calendar'
                  ? 'border-primary text-foreground'
                  : 'border-transparent text-muted-foreground hover:text-foreground'
              }`}
            >
              Team Calendar
            </button>
          </>
        )}
      </div>

      {/* Leave Balances Tab */}
      {activeTab === 'balances' && (
        <div className="space-y-6">
          {balancesLoading ? (
            <p className="text-muted-foreground text-sm">Loading leave balances…</p>
          ) : !balances || balances.length === 0 ? (
            <p className="text-muted-foreground text-sm">No leave balances set up for your account.</p>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {balances.map((b) => (
                <Card
                  key={b.leaveTypeId}
                  className="cursor-pointer hover:border-primary/50 transition-colors"
                  onClick={() => setSelectedLeaveType(b)}
                >
                  <CardHeader className="pb-2">
                    <CardDescription>{b.leaveTypeCode}</CardDescription>
                    <CardTitle className="text-lg">{b.leaveTypeName}</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-3xl font-semibold tracking-tight text-primary">
                      {b.balance} <span className="text-xs text-muted-foreground font-normal">days left</span>
                    </p>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}

          {/* Selected Leave Type Ledger */}
          {selectedLeaveType && (
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <div>
                  <CardTitle>{selectedLeaveType.leaveTypeName} Ledger History</CardTitle>
                  <CardDescription>Transaction entries for {selectedLeaveType.leaveTypeCode} balance adjustments.</CardDescription>
                </div>
                <Button variant="outline" size="sm" onClick={() => setSelectedLeaveType(null)}>
                  Close
                </Button>
              </CardHeader>
              <CardContent>
                {ledgerLoading ? (
                  <p className="text-muted-foreground text-sm">Loading transactions…</p>
                ) : !ledger || ledger.length === 0 ? (
                  <p className="text-muted-foreground text-sm">No transactions posted.</p>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left text-sm divide-y divide-border">
                      <thead>
                        <tr className="text-muted-foreground">
                          <th className="pb-3 font-medium">Effective Date</th>
                          <th className="pb-3 font-medium">Transaction Type</th>
                          <th className="pb-3 font-medium">Quantity</th>
                          <th className="pb-3 font-medium">Reference</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-border">
                        {ledger.map((entry) => (
                          <tr key={entry.id}>
                            <td className="py-3 font-medium">{entry.effectiveDate}</td>
                            <td className="py-3">
                              <span
                                className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${
                                  entry.transactionType === 'ACCRUAL' || entry.transactionType === 'OPENING_BALANCE'
                                    ? 'bg-green-500/10 text-green-500'
                                    : entry.transactionType === 'RESERVATION'
                                    ? 'bg-yellow-500/10 text-yellow-500'
                                    : 'bg-red-500/10 text-red-500'
                                }`}
                              >
                                {entry.transactionType}
                              </span>
                            </td>
                            <td className="py-3 font-semibold">
                              {entry.quantity > 0 ? `+${entry.quantity}` : entry.quantity}
                            </td>
                            <td className="py-3 text-muted-foreground text-xs">{entry.sourceReference}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </CardContent>
            </Card>
          )}
        </div>
      )}

      {/* Request History Tab */}
      {activeTab === 'history' && (
        <Card>
          <CardHeader>
            <CardTitle>My Leave Requests</CardTitle>
            <CardDescription>Track status of your submitted time off requests.</CardDescription>
          </CardHeader>
          <CardContent>
            {requestsLoading ? (
              <p className="text-muted-foreground text-sm">Loading requests…</p>
            ) : !requests || requests.length === 0 ? (
              <p className="text-muted-foreground text-sm">No leave requests submitted yet.</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm divide-y divide-border">
                  <thead>
                    <tr className="text-muted-foreground">
                      <th className="pb-3 font-medium">Leave Period</th>
                      <th className="pb-3 font-medium">Duration</th>
                      <th className="pb-3 font-medium">Status</th>
                      <th className="pb-3 font-medium">Reason</th>
                      <th className="pb-3 font-medium text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-border">
                    {requests.map((r) => (
                      <tr key={r.id}>
                        <td className="py-3 font-medium">
                          {r.startDate} to {r.endDate}
                        </td>
                        <td className="py-3 font-semibold">{r.totalDays} days</td>
                        <td className="py-3">
                          <span
                            className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${
                              r.status === 'APPROVED'
                                ? 'bg-green-500/10 text-green-500'
                                : r.status === 'PENDING_APPROVAL' || r.status === 'SUBMITTED'
                                ? 'bg-yellow-500/10 text-yellow-500'
                                : 'bg-red-500/10 text-red-500'
                            }`}
                          >
                            {r.status}
                          </span>
                        </td>
                        <td className="py-3 text-muted-foreground max-w-xs truncate">{r.reason || '—'}</td>
                        <td className="py-3 text-right">
                          {r.status === 'PENDING_APPROVAL' && (
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={() => cancelRequestMutation.mutate(r.id)}
                              disabled={cancelRequestMutation.isPending}
                            >
                              Cancel
                            </Button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {/* Pending Approvals Tab */}
      {activeTab === 'approvals' && isManagerOrHr && (
        <Card>
          <CardHeader>
            <CardTitle>Team Leave Approvals</CardTitle>
            <CardDescription>Review pending time off requests in your reporting line.</CardDescription>
          </CardHeader>
          <CardContent>
            {approvalsLoading ? (
              <p className="text-muted-foreground text-sm">Loading approvals…</p>
            ) : !approvals || approvals.filter(a => a.type === 'LEAVE_REQUEST' && a.status === 'PENDING').length === 0 ? (
              <p className="text-muted-foreground text-sm">No pending leave requests in your queue.</p>
            ) : (
              <div className="space-y-4">
                {approvals
                  .filter(a => a.type === 'LEAVE_REQUEST' && a.status === 'PENDING')
                  .map((app) => (
                    <div key={app.id} className="p-4 border border-border rounded-lg bg-card text-card-foreground flex flex-col gap-3 justify-between sm:flex-row sm:items-center">
                      <div>
                        <p className="text-xs text-muted-foreground">Request ID: {app.id}</p>
                        <p className="text-sm font-semibold mt-1">Leave Request Details:</p>
                        <p className="text-xs text-muted-foreground mt-1 font-mono">{app.changeJson}</p>
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

      {/* Team Calendar Tab */}
      {activeTab === 'calendar' && isManagerOrHr && (
        <Card>
          <CardHeader>
            <CardTitle>Team Absence Calendar</CardTitle>
            <CardDescription>Absences in your reporting line for the selected date range.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex flex-wrap gap-3 items-end">
              <div>
                <label className="text-xs text-muted-foreground">Start Date</label>
                <input
                  type="date"
                  value={calStart}
                  onChange={(e) => setCalStart(e.target.value)}
                  className="mt-1 block rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                />
              </div>
              <div>
                <label className="text-xs text-muted-foreground">End Date</label>
                <input
                  type="date"
                  value={calEnd}
                  onChange={(e) => setCalEnd(e.target.value)}
                  className="mt-1 block rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                />
              </div>
            </div>

            {calLoading ? (
              <p className="text-muted-foreground text-sm">Loading calendar details…</p>
            ) : !teamCalendar || teamCalendar.length === 0 ? (
              <p className="text-muted-foreground text-sm">No scheduled leaves in this period.</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm divide-y divide-border">
                  <thead>
                    <tr className="text-muted-foreground">
                      <th className="pb-3 font-medium">Employee ID</th>
                      <th className="pb-3 font-medium">Leave Dates</th>
                      <th className="pb-3 font-medium">Duration</th>
                      <th className="pb-3 font-medium">Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-border">
                    {teamCalendar.map((item) => (
                      <tr key={item.id}>
                        <td className="py-3 font-medium">{item.employeeId}</td>
                        <td className="py-3">
                          {item.startDate} to {item.endDate}
                        </td>
                        <td className="py-3 font-semibold">{item.totalDays} days</td>
                        <td className="py-3">
                          <span className="inline-flex items-center rounded-full bg-green-500/10 text-green-500 px-2 py-0.5 text-xs font-medium">
                            {item.status}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {/* Request Time Off Modal */}
      {isRequestModalOpen && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
          <Card className="w-full max-w-md">
            <CardHeader>
              <CardTitle>Request Time Off</CardTitle>
              <CardDescription>Submit leave requests for manager approval.</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleApplyLeave} className="space-y-4">
                <div>
                  <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Leave Type</label>
                  <select
                    value={leaveTypeId}
                    onChange={(e) => setLeaveTypeId(e.target.value)}
                    required
                    className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                  >
                    <option value="">Select a leave type</option>
                    {balances?.map((b) => (
                      <option key={b.leaveTypeId} value={b.leaveTypeId}>
                        {b.leaveTypeName} ({b.balance} days left)
                      </option>
                    ))}
                  </select>
                </div>
                <div className="grid gap-4 grid-cols-2">
                  <div>
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Start Date</label>
                    <input
                      type="date"
                      value={startDate}
                      onChange={(e) => setStartDate(e.target.value)}
                      required
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                    />
                  </div>
                  <div>
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">End Date</label>
                    <input
                      type="date"
                      value={endDate}
                      onChange={(e) => setEndDate(e.target.value)}
                      required
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                    />
                  </div>
                </div>
                <div>
                  <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Reason</label>
                  <textarea
                    value={reason}
                    onChange={(e) => setReason(e.target.value)}
                    placeholder="Enter reason for leave (optional)"
                    rows={3}
                    className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                  />
                </div>
                <div className="flex justify-end gap-3 pt-4 border-t border-border">
                  <Button type="button" variant="outline" onClick={() => setIsRequestModalOpen(false)}>
                    Cancel
                  </Button>
                  <Button type="submit" disabled={createRequestMutation.isPending}>
                    {createRequestMutation.isPending ? 'Submitting…' : 'Submit Request'}
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
