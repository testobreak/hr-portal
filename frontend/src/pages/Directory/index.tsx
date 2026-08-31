import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';
import { fetchDirectory, fetchDirectoryEmployeeDetails } from '@/api/services/directoryService';

export function DirectoryPage() {
  const [page, setPage] = useState(0);
  const [selectedColleagueId, setSelectedColleagueId] = useState<string | null>(null);

  // Queries
  const { data: directoryData, isLoading: directoryLoading } = useQuery({
    queryKey: ['directory', 'employees', page],
    queryFn: () => fetchDirectory(page, 12),
  });

  const { data: details, isLoading: detailsLoading } = useQuery({
    queryKey: ['directory', 'employee-detail', selectedColleagueId],
    queryFn: () => fetchDirectoryEmployeeDetails(selectedColleagueId!),
    enabled: !!selectedColleagueId,
  });

  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-2xl font-semibold tracking-tight">Employee Directory</h1>
        <p className="text-sm text-muted-foreground">Find contact details and reporting info for colleagues across all locations.</p>
      </header>

      {/* Directory Grid */}
      {directoryLoading ? (
        <p className="text-muted-foreground text-sm">Loading directory…</p>
      ) : !directoryData || directoryData.content.length === 0 ? (
        <p className="text-muted-foreground text-sm">No directory records found.</p>
      ) : (
        <div className="space-y-6">
          <div className="grid gap-4 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
            {directoryData.content.map((emp) => (
              <Card
                key={emp.id}
                className="cursor-pointer hover:border-primary/50 transition-colors flex flex-col justify-between"
                onClick={() => setSelectedColleagueId(emp.id)}
              >
                <CardHeader className="pb-2">
                  <CardTitle className="text-base">{emp.fullName}</CardTitle>
                  <CardDescription className="font-medium text-primary text-xs">
                    {emp.designationTitle || 'No title'}
                  </CardDescription>
                </CardHeader>
                <CardContent className="pt-2 text-xs space-y-1">
                  {emp.departmentName && (
                    <p className="text-muted-foreground">
                      <span className="font-semibold text-foreground">Dept:</span> {emp.departmentName}
                    </p>
                  )}
                  {emp.locationName && (
                    <p className="text-muted-foreground">
                      <span className="font-semibold text-foreground">Loc:</span> {emp.locationName}
                    </p>
                  )}
                  {emp.email && (
                    <p className="text-muted-foreground truncate">
                      <span className="font-semibold text-foreground">Email:</span> {emp.email}
                    </p>
                  )}
                </CardContent>
              </Card>
            ))}
          </div>

          {/* Pagination Controls */}
          <div className="flex items-center justify-between pt-4 border-t border-border">
            <p className="text-xs text-muted-foreground">
              Page {directoryData.number + 1} of {directoryData.totalPages}
            </p>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={directoryData.first}
              >
                Previous
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => Math.min(directoryData.totalPages - 1, p + 1))}
                disabled={directoryData.last}
              >
                Next
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* Details Modal */}
      {selectedColleagueId && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
          <Card className="w-full max-w-md">
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <div>
                <CardTitle>Colleague Profile</CardTitle>
                <CardDescription>Contact card & team info</CardDescription>
              </div>
              <Button variant="outline" size="sm" onClick={() => setSelectedColleagueId(null)}>
                Close
              </Button>
            </CardHeader>
            <CardContent className="pt-4">
              {detailsLoading ? (
                <p className="text-muted-foreground text-sm">Loading details…</p>
              ) : !details ? (
                <p className="text-red-500 text-sm">Colleague details not found.</p>
              ) : (
                <div className="space-y-4">
                  <div className="space-y-1">
                    <h2 className="text-lg font-semibold">{details.fullName}</h2>
                    <p className="text-sm font-medium text-primary">{details.designationTitle || '—'}</p>
                  </div>

                  <Separator />

                  <div className="grid gap-3 text-sm">
                    <div>
                      <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider block">Department</span>
                      <span className="font-medium">{details.departmentName || '—'}</span>
                    </div>
                    <div>
                      <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider block">Work Location</span>
                      <span className="font-medium">{details.locationName || '—'}</span>
                    </div>
                    <div>
                      <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider block">Email Address</span>
                      <span className="font-mono font-medium">{details.email}</span>
                    </div>
                    <div>
                      <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider block">Phone Number</span>
                      <span className="font-mono font-medium">{details.phoneNumber || '—'}</span>
                    </div>
                    <div>
                      <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider block">Reports To (Manager)</span>
                      <span className="font-medium">{details.managerName || '—'}</span>
                    </div>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}
