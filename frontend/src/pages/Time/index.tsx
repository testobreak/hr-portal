import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { useMe } from '@/hooks/useMe';
import { Roles, hasAnyRole } from '@/lib/roles';
import {
  clockIn,
  clockOut,
  fetchLatestAttendance,
  fetchEmployeeAttendanceLogs,
  saveTimesheetDraft,
  submitTimesheetForApproval,
  fetchEmployeeTimesheets,
  fetchPendingTimesheets,
  approveTimesheet,
  rejectTimesheet,
  type TimesheetLineCreateRequest
} from '@/api/services/attendanceService';
import { Clock } from 'lucide-react';

export function TimePage() {
  const queryClient = useQueryClient();
  const { data: me } = useMe();
  const roles = me?.roles ?? [];
  const isManagerOrAdmin = hasAnyRole(roles, [Roles.SUPER_ADMIN, Roles.HR_ADMIN, Roles.MANAGER]);

  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [clockNotes, setClockNotes] = useState('');
  const [elapsedTime, setElapsedTime] = useState('00:00:00');
  
  // Weekly timesheet edit states
  const [tsStartDate, setTsStartDate] = useState(() => {
    const d = new Date();
    const day = d.getDay();
    const diff = d.getDate() - day + (day === 0 ? -6 : 1); // Monday
    const mon = new Date(d.setDate(diff));
    return mon.toISOString().split('T')[0];
  });
  const [tsSubmissionComments, setTsSubmissionComments] = useState('');
  const [dailyHours, setDailyHours] = useState<Record<string, { hours: number; notes: string }>>({});

  // Manager action comments
  const [actionComments, setActionComments] = useState<Record<string, string>>({});

  // 1. Clock queries
  const { data: activeSession } = useQuery({
    queryKey: ['attendance', 'activeSession'],
    queryFn: () => fetchLatestAttendance(),
  });

  // Calculate elapsed time for active session
  useEffect(() => {
    if (!activeSession?.clockIn) {
      setElapsedTime('00:00:00');
      return;
    }
    const interval = setInterval(() => {
      const diffMs = Date.now() - new Date(activeSession.clockIn).getTime();
      const secs = Math.floor(diffMs / 1000) % 60;
      const mins = Math.floor(diffMs / (1000 * 60)) % 60;
      const hours = Math.floor(diffMs / (1000 * 60 * 60));
      
      const pad = (n: number) => n.toString().padStart(2, '0');
      setElapsedTime(`${pad(hours)}:${pad(mins)}:${pad(secs)}`);
    }, 1000);
    return () => clearInterval(interval);
  }, [activeSession]);

  // 2. Attendance history
  const { data: historyLogs } = useQuery({
    queryKey: ['attendance', 'logs'],
    queryFn: () => {
      const now = new Date();
      const start = new Date(now.getFullYear(), now.getMonth(), 1); // Month start
      return fetchEmployeeAttendanceLogs(start.toISOString(), now.toISOString());
    },
  });

  // 3. Employee timesheets list
  const { data: timesheets } = useQuery({
    queryKey: ['attendance', 'timesheets'],
    queryFn: () => fetchEmployeeTimesheets(),
  });

  // 4. Pending approvals for managers
  const { data: pendingTimesheets } = useQuery({
    queryKey: ['attendance', 'pendingTimesheets'],
    queryFn: fetchPendingTimesheets,
    enabled: isManagerOrAdmin,
  });

  // Mutations
  const clockInMutation = useMutation({
    mutationFn: () => clockIn({ notes: clockNotes, ipAddress: '127.0.0.1' }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['attendance', 'activeSession'] });
      queryClient.invalidateQueries({ queryKey: ['attendance', 'logs'] });
      setClockNotes('');
      setMessage({ type: 'success', text: 'Clock-in recorded successfully!' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to clock in.' });
    },
  });

  const clockOutMutation = useMutation({
    mutationFn: () => clockOut({ notes: clockNotes }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['attendance', 'activeSession'] });
      queryClient.invalidateQueries({ queryKey: ['attendance', 'logs'] });
      setClockNotes('');
      setMessage({ type: 'success', text: 'Clock-out recorded successfully!' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to clock out.' });
    },
  });

  const saveTsMutation = useMutation({
    mutationFn: (payload: any) => saveTimesheetDraft(payload),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['attendance', 'timesheets'] });
      setMessage({ type: 'success', text: `Timesheet draft saved successfully (ID: ${data.id.substring(0, 8)}).` });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to save timesheet.' });
    },
  });

  const submitTsMutation = useMutation({
    mutationFn: submitTimesheetForApproval,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['attendance', 'timesheets'] });
      queryClient.invalidateQueries({ queryKey: ['attendance', 'pendingTimesheets'] });
      setMessage({ type: 'success', text: 'Timesheet submitted for manager approval.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to submit timesheet.' });
    },
  });

  const approveTsMutation = useMutation({
    mutationFn: (payload: { id: string; comments: string }) =>
      approveTimesheet(payload.id, { approvalComments: payload.comments }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['attendance', 'pendingTimesheets'] });
      setMessage({ type: 'success', text: 'Timesheet approved.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to approve timesheet.' });
    },
  });

  const rejectTsMutation = useMutation({
    mutationFn: (payload: { id: string; comments: string }) =>
      rejectTimesheet(payload.id, { approvalComments: payload.comments }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['attendance', 'pendingTimesheets'] });
      setMessage({ type: 'success', text: 'Timesheet rejected and sent back to employee.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to reject timesheet.' });
    },
  });

  // Helper: setup daily inputs for week
  useEffect(() => {
    const dates = getWeekDays(tsStartDate);
    const initial: Record<string, { hours: number; notes: string }> = {};
    dates.forEach(d => {
      initial[d] = { hours: 8, notes: 'Regular work day' };
    });
    setDailyHours(initial);
  }, [tsStartDate]);

  const getWeekDays = (startStr: string) => {
    const days = [];
    const date = new Date(startStr);
    for (let i = 0; i < 7; i++) {
      days.push(date.toISOString().split('T')[0]);
      date.setDate(date.getDate() + 1);
    }
    return days;
  };

  const handleSaveTimesheet = (e: React.FormEvent) => {
    e.preventDefault();
    const lines: TimesheetLineCreateRequest[] = Object.entries(dailyHours).map(([date, data]) => ({
      dayDate: date,
      hoursWorked: data.hours,
      notes: data.notes
    }));

    const endDate = new Date(tsStartDate);
    endDate.setDate(endDate.getDate() + 6);

    saveTsMutation.mutate({
      startDate: tsStartDate,
      endDate: endDate.toISOString().split('T')[0],
      lines,
      submissionComments: tsSubmissionComments
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-foreground">Time & Attendance Workspace</h1>
          <p className="text-xs text-muted-foreground mt-1">Clock daily hours, submit weekly timesheets, and manage team approvals.</p>
        </div>
      </div>

      {message && (
        <div className={`p-3 rounded-lg text-xs font-semibold ${
          message.type === 'success' ? 'bg-green-500/10 text-green-500 border border-green-500/20' : 'bg-red-500/10 text-red-500 border border-red-500/20'
        }`}>
          {message.text}
        </div>
      )}

      <div className="grid gap-6 md:grid-cols-3">
        {/* Daily Time Card Widget */}
        <Card className="glassmorphism border-primary/20 relative overflow-hidden bg-card/60 backdrop-blur-md">
          <div className="absolute top-0 right-0 w-24 h-24 bg-primary/10 rounded-full blur-2xl -mr-6 -mt-6"></div>
          <CardHeader>
            <CardTitle className="text-sm font-semibold flex items-center gap-2">
              <Clock className="w-4 h-4 text-primary animate-pulse" />
              Daily Time Card
            </CardTitle>
            <CardDescription className="text-[11px]">Track daily hours in real-time</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="text-center py-4 bg-muted/30 border border-border/50 rounded-lg">
              <span className="text-3xl font-mono font-bold tracking-tight text-foreground block">
                {activeSession ? elapsedTime : '00:00:00'}
              </span>
              <span className="text-[10px] text-muted-foreground uppercase tracking-wider block mt-1">
                {activeSession ? 'Session Active' : 'Off-Clock'}
              </span>
            </div>

            <div className="space-y-2">
              <label className="text-[10px] uppercase font-bold text-muted-foreground">Session Log Comments</label>
              <textarea
                value={clockNotes}
                onChange={(e) => setClockNotes(e.target.value)}
                placeholder="e.g. Working on sprint tasks or remote from client office..."
                className="w-full text-xs bg-background/50 border border-input rounded-md p-2 h-16 resize-none focus:outline-none focus:ring-1 focus:ring-primary"
              />
            </div>

            <div className="pt-2">
              {activeSession ? (
                <Button
                  className="w-full bg-red-600 hover:bg-red-700 text-white font-bold"
                  onClick={() => clockOutMutation.mutate()}
                  disabled={clockOutMutation.isPending}
                >
                  Clock Out Session
                </Button>
              ) : (
                <Button
                  className="w-full bg-primary hover:bg-primary/90 text-white font-bold"
                  onClick={() => clockInMutation.mutate()}
                  disabled={clockInMutation.isPending}
                >
                  Clock In Session
                </Button>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Attendance logs history */}
        <Card className="md:col-span-2 bg-card/60 backdrop-blur-md">
          <CardHeader>
            <CardTitle className="text-sm font-semibold">Attendance Logs (Current Month)</CardTitle>
            <CardDescription className="text-[11px]">Audit log of your completed clock sessions</CardDescription>
          </CardHeader>
          <CardContent className="max-h-[260px] overflow-y-auto space-y-2">
            {(!historyLogs || historyLogs.length === 0) ? (
              <div className="text-center py-10 text-xs italic text-muted-foreground">
                No attendance logs found for this month.
              </div>
            ) : (
              <div className="grid gap-2">
                {historyLogs.map((log) => (
                  <div key={log.id} className="p-2.5 border border-border/50 rounded-lg bg-background/50 text-[11px] flex justify-between items-center hover:bg-muted/30 transition-all">
                    <div className="space-y-1">
                      <div className="font-semibold text-foreground">
                        {new Date(log.clockIn).toLocaleDateString()} — {new Date(log.clockIn).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} to {log.clockOut ? new Date(log.clockOut).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'Active'}
                      </div>
                      <div className="text-muted-foreground flex gap-3">
                        <span>Hours: {log.workingHours ?? '---'}</span>
                        {log.notes && <span className="truncate max-w-[180px]">Notes: {log.notes}</span>}
                      </div>
                    </div>
                    <span className={`px-2 py-0.5 rounded text-[9px] font-bold uppercase ${
                      log.status === 'PRESENT' ? 'bg-green-500/10 text-green-500' : 'bg-yellow-500/10 text-yellow-500'
                    }`}>
                      {log.status}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Weekly Timesheet & Manager approvals */}
      <div className="grid gap-6 lg:grid-cols-3">
        {/* Weekly grid calculator */}
        <Card className="lg:col-span-2 bg-card/60 backdrop-blur-md">
          <CardHeader className="flex flex-row justify-between items-center">
            <div>
              <CardTitle className="text-sm font-semibold">Weekly Timesheet Planner</CardTitle>
              <CardDescription className="text-[11px]">Save draft and submit weekly work hour totals</CardDescription>
            </div>
            <div className="flex gap-2 items-center">
              <span className="text-[10px] text-muted-foreground">Week starting Mon:</span>
              <input
                type="date"
                value={tsStartDate}
                onChange={(e) => setTsStartDate(e.target.value)}
                className="bg-background border border-input rounded px-2 py-1 text-[11px] h-7"
              />
            </div>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSaveTimesheet} className="space-y-4">
              <div className="border border-border/50 rounded-lg overflow-hidden">
                <table className="w-full text-xs text-left">
                  <thead className="bg-muted/50 border-b border-border/50 text-[10px] uppercase font-bold text-muted-foreground">
                    <tr>
                      <th className="p-3">Day</th>
                      <th className="p-3">Date</th>
                      <th className="p-3 w-24">Hours</th>
                      <th className="p-3">Notes</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-border/50">
                    {getWeekDays(tsStartDate).map((dateStr) => {
                      const dayName = new Date(dateStr).toLocaleDateString([], { weekday: 'long' });
                      const current = dailyHours[dateStr] || { hours: 8, notes: '' };
                      return (
                        <tr key={dateStr}>
                          <td className="p-3 font-semibold text-foreground">{dayName}</td>
                          <td className="p-3 text-muted-foreground">{dateStr}</td>
                          <td className="p-3">
                            <input
                              type="number"
                              min="0"
                              max="24"
                              step="0.5"
                              value={current.hours}
                              onChange={(e) => setDailyHours({
                                ...dailyHours,
                                [dateStr]: { ...current, hours: parseFloat(e.target.value) || 0 }
                              })}
                              className="w-full bg-background border border-input rounded px-2 py-1 h-7 text-xs"
                            />
                          </td>
                          <td className="p-3">
                            <input
                              type="text"
                              placeholder="Tasks details..."
                              value={current.notes}
                              onChange={(e) => setDailyHours({
                                ...dailyHours,
                                [dateStr]: { ...current, notes: e.target.value }
                              })}
                              className="w-full bg-background border border-input rounded px-2 py-1 h-7 text-xs"
                            />
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>

              <div className="space-y-1.5">
                <label className="text-[10px] font-bold text-muted-foreground uppercase">Submission Comments</label>
                <input
                  type="text"
                  placeholder="Summarize tasks completed this week..."
                  value={tsSubmissionComments}
                  onChange={(e) => setTsSubmissionComments(e.target.value)}
                  className="w-full bg-background border border-input rounded px-3 py-1.5 text-xs h-8"
                />
              </div>

              <div className="flex justify-between items-center pt-2">
                <div className="text-xs font-semibold text-foreground">
                  Week Total: {Object.values(dailyHours).reduce((sum, h) => sum + h.hours, 0)} hours
                </div>
                <div className="flex gap-2">
                  <Button
                    type="submit"
                    variant="outline"
                    size="sm"
                    disabled={saveTsMutation.isPending}
                  >
                    Save Draft
                  </Button>
                </div>
              </div>
            </form>
          </CardContent>
        </Card>

        {/* Timesheets List */}
        <Card className="bg-card/60 backdrop-blur-md">
          <CardHeader>
            <CardTitle className="text-sm font-semibold">Timesheet Submissions</CardTitle>
            <CardDescription className="text-[11px]">History of saved and submitted weekly timesheets</CardDescription>
          </CardHeader>
          <CardContent className="max-h-[350px] overflow-y-auto space-y-3">
            {(!timesheets || timesheets.length === 0) ? (
              <div className="text-center py-10 text-xs italic text-muted-foreground">
                No timesheet planner records found.
              </div>
            ) : (
              <div className="grid gap-2">
                {timesheets.map((ts) => (
                  <div key={ts.id} className="p-3 border border-border/50 rounded-lg bg-background/50 text-[11px] space-y-2">
                    <div className="flex justify-between items-start">
                      <div>
                        <div className="font-semibold text-foreground">{ts.startDate} to {ts.endDate}</div>
                        <div className="text-[10px] text-muted-foreground mt-0.5">Total Hours: {ts.totalHours} hrs</div>
                      </div>
                      <span className={`px-2 py-0.5 rounded text-[8px] font-bold uppercase ${
                        ts.status === 'APPROVED'
                          ? 'bg-green-500/10 text-green-500'
                          : ts.status === 'SUBMITTED'
                          ? 'bg-blue-500/10 text-blue-500'
                          : ts.status === 'REJECTED'
                          ? 'bg-red-500/10 text-red-500'
                          : 'bg-yellow-500/10 text-yellow-500'
                      }`}>
                        {ts.status}
                      </span>
                    </div>
                    {ts.submissionComments && (
                      <div className="text-muted-foreground italic">"{ts.submissionComments}"</div>
                    )}
                    {ts.status === 'DRAFT' && (
                      <Button
                        className="w-full text-[10px] h-6 bg-blue-600 hover:bg-blue-700 text-white font-bold"
                        onClick={() => submitTsMutation.mutate(ts.id)}
                        disabled={submitTsMutation.isPending}
                      >
                        Submit for Approval
                      </Button>
                    )}
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Manager Timesheet approvals list */}
      {isManagerOrAdmin && (
        <Card className="bg-card/60 backdrop-blur-md">
          <CardHeader>
            <CardTitle className="text-sm font-semibold">Timesheet Approvals Queue</CardTitle>
            <CardDescription className="text-[11px]">Approve or reject submitted team timesheets</CardDescription>
          </CardHeader>
          <CardContent className="max-h-[300px] overflow-y-auto">
            {(!pendingTimesheets || pendingTimesheets.length === 0) ? (
              <div className="text-center py-8 text-xs italic text-muted-foreground">
                No timesheets pending your approval.
              </div>
            ) : (
              <div className="grid gap-3">
                {pendingTimesheets.map((ts) => (
                  <div key={ts.id} className="p-3 border border-border/50 rounded-lg bg-background/50 text-xs flex justify-between items-center hover:bg-muted/20 transition-all">
                    <div className="space-y-1">
                      <div className="font-semibold text-foreground">{ts.employeeName}</div>
                      <div className="text-muted-foreground text-[11px] flex gap-3">
                        <span>Week: {ts.startDate} to {ts.endDate}</span>
                        <span>Total Hours: {ts.totalHours} hrs</span>
                      </div>
                      {ts.submissionComments && (
                        <div className="text-[10px] text-muted-foreground italic">"{ts.submissionComments}"</div>
                      )}
                    </div>
                    <div className="flex gap-2 items-center">
                      <input
                        type="text"
                        placeholder="Review comments..."
                        value={actionComments[ts.id] || ''}
                        onChange={(e) => setActionComments({ ...actionComments, [ts.id]: e.target.value })}
                        className="bg-background border border-input rounded px-2 py-1 text-[11px] h-7 w-44"
                      />
                      <Button
                        size="sm"
                        onClick={() => approveTsMutation.mutate({ id: ts.id, comments: actionComments[ts.id] || '' })}
                        disabled={approveTsMutation.isPending}
                      >
                        Approve
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        className="text-red-500 hover:text-red-600"
                        onClick={() => rejectTsMutation.mutate({ id: ts.id, comments: actionComments[ts.id] || '' })}
                        disabled={rejectTsMutation.isPending}
                      >
                        Reject
                      </Button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}
