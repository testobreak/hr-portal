import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { useMe } from '@/hooks/useMe';
import { Roles, hasAnyRole } from '@/lib/roles';
import {
  createPayrollRun,
  calculatePayrollRun,
  approvePayrollRun,
  markPaidPayrollRun,
  fetchPayrollRuns,
  fetchPayslipsForRun,
  fetchEmployeePayslipsHistory,
  downloadPayrollLedger,
  downloadPayrollBankDisbursements,
  type PayrollRunResponse,
  type PayslipResponse
} from '@/api/services/payrollService';
import { Calculator, Download, DollarSign, CheckCircle, Play, ChevronRight } from 'lucide-react';

export function PayrollPage() {
  const queryClient = useQueryClient();
  const { data: me } = useMe();
  const roles = me?.roles ?? [];
  const isHrOrFinance = hasAnyRole(roles, [Roles.SUPER_ADMIN, Roles.HR_ADMIN, Roles.FINANCE_ADMIN]);

  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  
  // Selection states
  const [selectedRun, setSelectedRun] = useState<PayrollRunResponse | null>(null);
  const [selectedPayslip, setSelectedPayslip] = useState<PayslipResponse | null>(null);

  // Form states (create run)
  const [periodStart, setPeriodStart] = useState('');
  const [periodEnd, setPeriodEnd] = useState('');
  const [payoutDate, setPayoutDate] = useState('');
  const [runType, setRunType] = useState<'REGULAR' | 'OFF_CYCLE'>('REGULAR');

  // Queries
  const { data: runs } = useQuery({
    queryKey: ['payroll', 'runs'],
    queryFn: fetchPayrollRuns,
    enabled: isHrOrFinance,
  });

  const { data: payslips, isLoading: payslipsLoading } = useQuery({
    queryKey: ['payroll', 'runs', selectedRun?.id, 'payslips'],
    queryFn: () => fetchPayslipsForRun(selectedRun!.id),
    enabled: !!selectedRun && isHrOrFinance,
  });

  const { data: myPayslips } = useQuery({
    queryKey: ['payroll', 'history'],
    queryFn: () => fetchEmployeePayslipsHistory(),
  });

  // Mutations
  const createRunMutation = useMutation({
    mutationFn: createPayrollRun,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['payroll', 'runs'] });
      setPeriodStart('');
      setPeriodEnd('');
      setPayoutDate('');
      setSelectedRun(data);
      setMessage({ type: 'success', text: 'Payroll computation period initialized.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to initialize period.' });
    },
  });

  const calculateMutation = useMutation({
    mutationFn: calculatePayrollRun,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['payroll', 'runs'] });
      if (selectedRun?.id === data.id) setSelectedRun(data);
      setMessage({ type: 'success', text: 'Gross-to-net payroll engine computation run complete!' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to compute gross-to-net.' });
    },
  });

  const approveMutation = useMutation({
    mutationFn: approvePayrollRun,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['payroll', 'runs'] });
      if (selectedRun?.id === data.id) setSelectedRun(data);
      setMessage({ type: 'success', text: 'Payroll period approved and locked.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to approve payroll run.' });
    },
  });

  const markPaidMutation = useMutation({
    mutationFn: markPaidPayrollRun,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['payroll', 'runs'] });
      if (selectedRun?.id === data.id) setSelectedRun(data);
      setMessage({ type: 'success', text: 'Payroll marked as PAID. Paystubs released to employees.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to mark as paid.' });
    },
  });

  const handleCreateRunSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!periodStart || !periodEnd) return;
    createRunMutation.mutate({
      periodStart,
      periodEnd,
      payoutDate: payoutDate || undefined,
      runType
    });
  };

  const handleDownloadLedger = async (runId: string) => {
    try {
      await downloadPayrollLedger(runId);
    } catch (err: any) {
      setMessage({ type: 'error', text: err?.message || 'Failed to download general ledger.' });
    }
  };

  const handleDownloadBankFile = async (runId: string) => {
    try {
      await downloadPayrollBankDisbursements(runId);
    } catch (err: any) {
      setMessage({ type: 'error', text: err?.message || 'Failed to download bank direct deposit file.' });
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-foreground">Payroll Cockpit</h1>
        <p className="text-xs text-muted-foreground mt-1">Execute monthly payroll runs, compute gross-to-net calculations, and download files.</p>
      </div>

      {message && (
        <div className={`p-3 rounded-lg text-xs font-semibold ${
          message.type === 'success' ? 'bg-green-500/10 text-green-500 border border-green-500/20' : 'bg-red-500/10 text-red-500 border border-red-500/20'
        }`}>
          {message.text}
        </div>
      )}

      {isHrOrFinance ? (
        <div className="grid gap-6 lg:grid-cols-3">
          {/* LEFT: Initialize & Runs list */}
          <div className="space-y-6 lg:col-span-1">
            {/* Initialize Run Form */}
            <Card className="bg-card/60 backdrop-blur-md">
              <CardHeader>
                <CardTitle className="text-sm font-semibold flex items-center gap-2">
                  <Calculator className="w-4 h-4 text-primary" />
                  New Payroll Run
                </CardTitle>
                <CardDescription className="text-[11px]">Initialize a new draft salary period</CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleCreateRunSubmit} className="space-y-3">
                  <div>
                    <label className="text-[10px] uppercase font-bold text-muted-foreground">Period Start</label>
                    <input
                      type="date"
                      required
                      value={periodStart}
                      onChange={(e) => setPeriodStart(e.target.value)}
                      className="mt-1 block w-full bg-background border border-input rounded px-3 py-1.5 text-xs h-8"
                    />
                  </div>
                  <div>
                    <label className="text-[10px] uppercase font-bold text-muted-foreground">Period End</label>
                    <input
                      type="date"
                      required
                      value={periodEnd}
                      onChange={(e) => setPeriodEnd(e.target.value)}
                      className="mt-1 block w-full bg-background border border-input rounded px-3 py-1.5 text-xs h-8"
                    />
                  </div>
                  <div>
                    <label className="text-[10px] uppercase font-bold text-muted-foreground">Payout Date</label>
                    <input
                      type="date"
                      value={payoutDate}
                      onChange={(e) => setPayoutDate(e.target.value)}
                      className="mt-1 block w-full bg-background border border-input rounded px-3 py-1.5 text-xs h-8"
                    />
                  </div>
                  <div>
                    <label className="text-[10px] uppercase font-bold text-muted-foreground">Run Type</label>
                    <select
                      value={runType}
                      onChange={(e) => setRunType(e.target.value as any)}
                      className="mt-1 block w-full bg-background border border-input rounded px-3 py-1.5 text-xs h-8"
                    >
                      <option value="REGULAR">Regular Monthly Payroll</option>
                      <option value="OFF_CYCLE">Off-Cycle / Special Payout</option>
                    </select>
                  </div>
                  <Button
                    type="submit"
                    className="w-full mt-2"
                    disabled={createRunMutation.isPending}
                  >
                    {createRunMutation.isPending ? 'Initializing…' : 'Initialize Period'}
                  </Button>
                </form>
              </CardContent>
            </Card>

            {/* Payroll Runs List */}
            <Card className="bg-card/60 backdrop-blur-md">
              <CardHeader>
                <CardTitle className="text-sm font-semibold">Payroll Periods</CardTitle>
                <CardDescription className="text-[11px]">List of all processed monthly batches</CardDescription>
              </CardHeader>
              <CardContent className="max-h-[300px] overflow-y-auto space-y-2">
                {(!runs || runs.length === 0) ? (
                  <div className="text-center py-10 text-xs italic text-muted-foreground">
                    No payroll periods initialized.
                  </div>
                ) : (
                  <div className="grid gap-2">
                    {runs.map((r) => (
                      <div
                        key={r.id}
                        onClick={() => { setSelectedRun(r); setSelectedPayslip(null); }}
                        className={`p-3 border rounded-lg text-xs space-y-1.5 cursor-pointer transition-all hover:bg-muted/30 ${
                          selectedRun?.id === r.id ? 'border-primary bg-primary/5' : 'border-border/50 bg-background/50'
                        }`}
                      >
                        <div className="flex justify-between items-start">
                          <span className="font-bold text-foreground">
                            {r.periodStart} to {r.periodEnd}
                          </span>
                          <span className={`px-2 py-0.5 rounded text-[8px] font-bold uppercase ${
                            r.status === 'PAID'
                              ? 'bg-green-500/10 text-green-500'
                              : r.status === 'APPROVED'
                              ? 'bg-blue-500/10 text-blue-500'
                              : r.status === 'COMPLETED'
                              ? 'bg-orange-500/10 text-orange-500'
                              : 'bg-yellow-500/10 text-yellow-500'
                          }`}>
                            {r.status}
                          </span>
                        </div>
                        <div className="text-[10px] text-muted-foreground flex justify-between">
                          <span>Payout: {r.payoutDate || '---'}</span>
                          <span>Net: ${r.totalNet.toLocaleString()}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          {/* RIGHT: Period details, computed slips list, action cockpit */}
          <div className="lg:col-span-2 space-y-6">
            {selectedRun ? (
              <>
                {/* Period Operations Dashboard */}
                <Card className="bg-card/60 backdrop-blur-md relative overflow-hidden">
                  <CardHeader>
                    <div className="flex justify-between items-start">
                      <div>
                        <CardTitle className="text-sm font-semibold">
                          Period Detail Cockpit ({selectedRun.periodStart} to {selectedRun.periodEnd})
                        </CardTitle>
                        <CardDescription className="text-[11px]">Execute and approve the gross-to-net calculations</CardDescription>
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    {/* Totals overview */}
                    <div className="grid grid-cols-3 gap-4 bg-muted/40 p-3 rounded-lg border border-border/50 text-center">
                      <div>
                        <span className="text-[10px] text-muted-foreground uppercase tracking-wider block">Total Gross</span>
                        <span className="text-sm font-bold text-foreground block">${selectedRun.totalGross.toLocaleString()}</span>
                      </div>
                      <div>
                        <span className="text-[10px] text-muted-foreground uppercase tracking-wider block">Deductions</span>
                        <span className="text-sm font-bold text-red-500 block">${selectedRun.totalDeductions.toLocaleString()}</span>
                      </div>
                      <div>
                        <span className="text-[10px] text-muted-foreground uppercase tracking-wider block">Net Disbursement</span>
                        <span className="text-sm font-bold text-green-500 block">${selectedRun.totalNet.toLocaleString()}</span>
                      </div>
                    </div>

                    {/* Operational triggers */}
                    <div className="flex gap-2 flex-wrap border-t border-border/50 pt-4">
                      {/* Calculate / Recalculate */}
                      {['DRAFT', 'COMPUTING', 'COMPLETED'].includes(selectedRun.status) && (
                        <Button
                          size="sm"
                          onClick={() => calculateMutation.mutate(selectedRun.id)}
                          disabled={calculateMutation.isPending}
                        >
                          <Play className="w-3.5 h-3.5 mr-1" />
                          {selectedRun.status === 'COMPLETED' ? 'Recalculate Run' : 'Calculate Payouts'}
                        </Button>
                      )}
                      
                      {/* Approve run */}
                      {selectedRun.status === 'COMPLETED' && (
                        <Button
                          size="sm"
                          className="bg-blue-600 hover:bg-blue-700 text-white font-bold"
                          onClick={() => approveMutation.mutate(selectedRun.id)}
                          disabled={approveMutation.isPending}
                        >
                          <CheckCircle className="w-3.5 h-3.5 mr-1" />
                          Approve Period
                        </Button>
                      )}

                      {/* Pay run */}
                      {selectedRun.status === 'APPROVED' && (
                        <Button
                          size="sm"
                          className="bg-green-600 hover:bg-green-700 text-white font-bold"
                          onClick={() => markPaidMutation.mutate(selectedRun.id)}
                          disabled={markPaidMutation.isPending}
                        >
                          <DollarSign className="w-3.5 h-3.5 mr-1" />
                          Disburse & Mark Paid
                        </Button>
                      )}

                      {/* Direct Deposit ledger & bank files downloads */}
                      {['APPROVED', 'PAID'].includes(selectedRun.status) && (
                        <>
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => handleDownloadLedger(selectedRun.id)}
                          >
                            <Download className="w-3.5 h-3.5 mr-1" />
                            General Ledger CSV
                          </Button>
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => handleDownloadBankFile(selectedRun.id)}
                          >
                            <Download className="w-3.5 h-3.5 mr-1" />
                            Bank Direct Transfer
                          </Button>
                        </>
                      )}
                    </div>
                  </CardContent>
                </Card>

                <div className="grid gap-6 md:grid-cols-2">
                  {/* Generated Payslips list */}
                  <Card className="bg-card/60 backdrop-blur-md">
                    <CardHeader>
                      <CardTitle className="text-sm font-semibold">Generated Paystubs</CardTitle>
                      <CardDescription className="text-[11px]">Individual employee payout details</CardDescription>
                    </CardHeader>
                    <CardContent className="max-h-[300px] overflow-y-auto space-y-2">
                      {payslipsLoading ? (
                        <div className="text-center py-6 text-xs italic text-muted-foreground">Loading payslips...</div>
                      ) : (!payslips || payslips.length === 0) ? (
                        <div className="text-center py-6 text-xs italic text-muted-foreground">No stubs calculated. Run "Calculate Payouts" first.</div>
                      ) : (
                        <div className="grid gap-1">
                          {payslips.map((ps) => (
                            <div
                              key={ps.id}
                              onClick={() => setSelectedPayslip(ps)}
                              className={`p-2 border rounded-lg text-[11px] flex justify-between items-center cursor-pointer hover:bg-muted/30 transition-all ${
                                selectedPayslip?.id === ps.id ? 'border-primary bg-primary/5' : 'border-border/40 bg-background/30'
                              }`}
                            >
                              <div>
                                <div className="font-semibold text-foreground">{ps.employeeName}</div>
                                <div className="text-muted-foreground mt-0.5">Net: ${ps.netSalary.toLocaleString()}</div>
                              </div>
                              <ChevronRight className="w-3.5 h-3.5 text-muted-foreground" />
                            </div>
                          ))}
                        </div>
                      )}
                    </CardContent>
                  </Card>

                  {/* Selected Payslip line items breakdown */}
                  <Card className="bg-card/60 backdrop-blur-md">
                    <CardHeader>
                      <CardTitle className="text-sm font-semibold">Paystub Details</CardTitle>
                      <CardDescription className="text-[11px]">Allowances, deductions, and work days audit</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      {selectedPayslip ? (
                        <div className="space-y-3 text-xs">
                          <div className="pb-2 border-b border-border/50">
                            <span className="font-bold text-sm text-foreground block">{selectedPayslip.employeeName}</span>
                            <span className="text-[10px] text-muted-foreground block">
                              Work Days: {selectedPayslip.presentDays} Present, {selectedPayslip.leaveDays} Leaves (out of {selectedPayslip.workingDays} work days)
                            </span>
                          </div>

                          <div className="space-y-1">
                            <span className="text-[10px] uppercase font-bold text-muted-foreground block">Salary Breakdown</span>
                            <div className="divide-y divide-border/30 bg-muted/20 p-2 rounded border border-border/40 space-y-1.5">
                              {selectedPayslip.items.map((item) => (
                                <div key={item.id} className="flex justify-between items-center pt-1.5 text-[11px]">
                                  <span className="text-foreground">{item.itemName}</span>
                                  <span className={item.itemType === 'ALLOWANCE' ? 'text-green-500 font-semibold' : 'text-red-400 font-semibold'}>
                                    {item.itemType === 'ALLOWANCE' ? '+' : '-'}${item.amount.toLocaleString()}
                                  </span>
                                </div>
                              ))}
                            </div>
                          </div>

                          <div className="flex justify-between items-center pt-2 font-bold text-xs border-t border-border/50">
                            <span className="text-foreground">Net Payout:</span>
                            <span className="text-green-500 text-sm font-bold">${selectedPayslip.netSalary.toLocaleString()}</span>
                          </div>
                        </div>
                      ) : (
                        <div className="text-center py-12 text-xs italic text-muted-foreground">
                          Select an employee stub to inspect details
                        </div>
                      )}
                    </CardContent>
                  </Card>
                </div>
              </>
            ) : (
              <div className="h-48 border border-dashed border-border rounded-lg flex items-center justify-center text-muted-foreground text-xs italic">
                Select a payroll period to load cockpit dashboard
              </div>
            )}
          </div>
        </div>
      ) : (
        /* Ordinary Employee View: My historical approved paystubs */
        <div className="grid gap-6 md:grid-cols-3">
          <Card className="md:col-span-1 bg-card/60 backdrop-blur-md">
            <CardHeader>
              <CardTitle className="text-sm font-semibold">My Payslips</CardTitle>
              <CardDescription className="text-[11px]">Download historical salary slips</CardDescription>
            </CardHeader>
            <CardContent className="max-h-[400px] overflow-y-auto space-y-2">
              {(!myPayslips || myPayslips.length === 0) ? (
                <div className="text-center py-10 text-xs italic text-muted-foreground">
                  No historical payslips found.
                </div>
              ) : (
                <div className="grid gap-1">
                  {myPayslips.map((ps) => (
                    <div
                      key={ps.id}
                      onClick={() => setSelectedPayslip(ps)}
                      className={`p-3 border rounded-lg text-xs flex justify-between items-center cursor-pointer hover:bg-muted/30 transition-all ${
                        selectedPayslip?.id === ps.id ? 'border-primary bg-primary/5' : 'border-border/40 bg-background/30'
                      }`}
                    >
                      <div>
                        <div className="font-semibold text-foreground">Salary Disbursement</div>
                        <div className="text-[10px] text-muted-foreground mt-0.5">Net: ${ps.netSalary.toLocaleString()}</div>
                      </div>
                      <ChevronRight className="w-3.5 h-3.5 text-muted-foreground" />
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          <Card className="md:col-span-2 bg-card/60 backdrop-blur-md">
            <CardHeader>
              <CardTitle className="text-sm font-semibold">Salary Slip Statement</CardTitle>
              <CardDescription className="text-[11px]">Detailed payslip breakdown audit</CardDescription>
            </CardHeader>
            <CardContent>
              {selectedPayslip ? (
                <div className="space-y-4 text-xs">
                  <div className="pb-3 border-b border-border/50 flex justify-between items-center">
                    <div>
                      <span className="font-bold text-sm text-foreground block">{selectedPayslip.employeeName}</span>
                      <span className="text-[10px] text-muted-foreground">
                        Work Days: {selectedPayslip.presentDays} Present, {selectedPayslip.leaveDays} Leaves (out of {selectedPayslip.workingDays} work days)
                      </span>
                    </div>
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => window.print()}
                    >
                      Print Payslip
                    </Button>
                  </div>

                  <div className="space-y-2">
                    <span className="text-[10px] uppercase font-bold text-muted-foreground block">Compensation Structure</span>
                    <div className="divide-y divide-border/30 bg-muted/20 p-3 rounded border border-border/40 space-y-2">
                      {selectedPayslip.items.map((item) => (
                        <div key={item.id} className="flex justify-between items-center pt-2 text-[11px]">
                          <span className="text-foreground">{item.itemName}</span>
                          <span className={item.itemType === 'ALLOWANCE' ? 'text-green-500 font-semibold' : 'text-red-400 font-semibold'}>
                            {item.itemType === 'ALLOWANCE' ? '+' : '-'}${item.amount.toLocaleString()}
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>

                  <div className="flex justify-between items-center pt-3 font-bold text-sm border-t border-border/50">
                    <span className="text-foreground">Net Payout:</span>
                    <span className="text-green-500 text-lg font-bold">${selectedPayslip.netSalary.toLocaleString()}</span>
                  </div>
                </div>
              ) : (
                <div className="text-center py-20 text-xs italic text-muted-foreground">
                  Select a salary statement from the sidebar list to inspect details
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}
