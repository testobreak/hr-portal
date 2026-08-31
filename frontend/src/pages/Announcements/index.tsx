import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';
import { useMe } from '@/hooks/useMe';
import { Roles, hasAnyRole } from '@/lib/roles';
import {
  fetchMyAnnouncements,
  markAnnouncementAsRead,
  acknowledgeAnnouncement,
  publishAnnouncement
} from '@/api/services/announcementService';
import {
  fetchDepartmentLookups,
  fetchLocationLookups,
  fetchLegalEntityLookups
} from '@/api/services/lookupsService';

type TabType = 'feed' | 'publish';

export function AnnouncementsPage() {
  const queryClient = useQueryClient();
  const { data: me } = useMe();
  const roles = me?.roles ?? [];
  const isHr = hasAnyRole(roles, [Roles.HR_ADMIN, Roles.SUPER_ADMIN]);

  const [activeTab, setActiveTab] = useState<TabType>('feed');
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Form State for publishing
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [legalEntityId, setLegalEntityId] = useState('');
  const [departmentId, setDepartmentId] = useState('');
  const [locationId, setLocationId] = useState('');

  // Queries
  const { data: announcements, isLoading: feedLoading } = useQuery({
    queryKey: ['announcements', 'my-feed'],
    queryFn: fetchMyAnnouncements,
  });

  const { data: departments } = useQuery({
    queryKey: ['lookups', 'departments'],
    queryFn: fetchDepartmentLookups,
    enabled: isHr,
  });

  const { data: locations } = useQuery({
    queryKey: ['lookups', 'locations'],
    queryFn: fetchLocationLookups,
    enabled: isHr,
  });

  const { data: legalEntities } = useQuery({
    queryKey: ['lookups', 'legal-entities'],
    queryFn: fetchLegalEntityLookups,
    enabled: isHr,
  });

  // Mark all loaded announcements as read
  useEffect(() => {
    if (announcements && announcements.length > 0) {
      announcements.forEach((ann) => {
        markAnnouncementAsRead(ann.id).catch((err) => {
          console.error(`Failed to mark announcement ${ann.id} as read:`, err);
        });
      });
    }
  }, [announcements]);

  // Mutations
  const ackMutation = useMutation({
    mutationFn: acknowledgeAnnouncement,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['announcements', 'my-feed'] });
      setMessage({ type: 'success', text: 'Notice acknowledged.' });
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to acknowledge.' });
    },
  });

  const publishMutation = useMutation({
    mutationFn: publishAnnouncement,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['announcements', 'my-feed'] });
      setTitle('');
      setContent('');
      setLegalEntityId('');
      setDepartmentId('');
      setLocationId('');
      setMessage({ type: 'success', text: 'Announcement published successfully.' });
      setActiveTab('feed');
    },
    onError: (err: any) => {
      setMessage({ type: 'error', text: err?.message || 'Failed to publish announcement.' });
    },
  });

  const handlePublish = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title || !content) return;
    setMessage(null);
    publishMutation.mutate({
      title,
      content,
      legalEntityId: legalEntityId || undefined,
      departmentId: departmentId || undefined,
      locationId: locationId || undefined,
    });
  };

  return (
    <div className="space-y-6">
      <header className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">Company Announcements</h1>
          <p className="text-sm text-muted-foreground">Stay updated with critical information, policies, and team alerts.</p>
        </div>
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

      {/* Tabs bar */}
      {isHr && (
        <div className="flex border-b border-border gap-4 overflow-x-auto pb-px">
          <button
            onClick={() => { setActiveTab('feed'); setMessage(null); }}
            className={`pb-2 text-sm font-medium transition-colors border-b-2 px-1 ${
              activeTab === 'feed'
                ? 'border-primary text-foreground'
                : 'border-transparent text-muted-foreground hover:text-foreground'
            }`}
          >
            Announcements Board
          </button>
          <button
            onClick={() => { setActiveTab('publish'); setMessage(null); }}
            className={`pb-2 text-sm font-medium transition-colors border-b-2 px-1 ${
              activeTab === 'publish'
                ? 'border-primary text-foreground'
                : 'border-transparent text-muted-foreground hover:text-foreground'
            }`}
          >
            Publish New Alert
          </button>
        </div>
      )}

      {/* Announcements Feed */}
      {activeTab === 'feed' && (
        <div className="space-y-4">
          {feedLoading ? (
            <p className="text-muted-foreground text-sm">Loading announcements feed…</p>
          ) : !announcements || announcements.length === 0 ? (
            <p className="text-muted-foreground text-sm">No active announcements targeted to your profile.</p>
          ) : (
            announcements.map((ann) => (
              <Card key={ann.id} className="relative overflow-hidden">
                {ann.requiresAcknowledgment && (
                  <div className="absolute top-0 right-0 bg-yellow-500 text-black text-[9px] font-bold uppercase px-2 py-0.5 rounded-bl">
                    Requires Ack
                  </div>
                )}
                <CardHeader>
                  <CardTitle className="text-base">{ann.title}</CardTitle>
                  <CardDescription className="text-xs">
                    Published: {ann.publishedAt ? new Date(ann.publishedAt).toLocaleDateString() : '—'}
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <p className="text-sm text-foreground/90 whitespace-pre-line leading-relaxed">{ann.content}</p>
                  
                  {ann.requiresAcknowledgment && (
                    <div className="flex items-center justify-between pt-3 border-t border-border mt-3">
                      <p className="text-xs text-amber-500 font-medium">Please review and confirm read receipt.</p>
                      <Button
                        size="sm"
                        onClick={() => ackMutation.mutate(ann.id)}
                        disabled={ackMutation.isPending}
                      >
                        Acknowledge Read
                      </Button>
                    </div>
                  )}
                </CardContent>
              </Card>
            ))
          )}
        </div>
      )}

      {/* Publish Form */}
      {activeTab === 'publish' && isHr && (
        <Card>
          <CardHeader>
            <CardTitle>Publish Targeted Announcement</CardTitle>
            <CardDescription>Target alerts to employees based on legal entity, department, or location rules.</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handlePublish} className="space-y-4">
              <div>
                <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Title</label>
                <input
                  type="text"
                  placeholder="Announcement title"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  required
                  className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                />
              </div>

              <div>
                <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Content</label>
                <textarea
                  placeholder="Compose notice content here…"
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  required
                  rows={6}
                  className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-1.5 text-sm"
                />
              </div>

              <Separator />

              <div>
                <h3 className="text-sm font-semibold mb-3">Audience Targeting Rules (Optional)</h3>
                <div className="grid gap-4 sm:grid-cols-3">
                  <div>
                    <label className="text-xs text-muted-foreground">Target Legal Entity</label>
                    <select
                      value={legalEntityId}
                      onChange={(e) => setLegalEntityId(e.target.value)}
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    >
                      <option value="">All Entities</option>
                      {legalEntities?.map((le) => (
                        <option key={le.id} value={le.id}>
                          {le.name}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="text-xs text-muted-foreground">Target Department</label>
                    <select
                      value={departmentId}
                      onChange={(e) => setDepartmentId(e.target.value)}
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    >
                      <option value="">All Departments</option>
                      {departments?.map((dept) => (
                        <option key={dept.id} value={dept.id}>
                          {dept.name}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="text-xs text-muted-foreground">Target Location</label>
                    <select
                      value={locationId}
                      onChange={(e) => setLocationId(e.target.value)}
                      className="mt-1 block w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    >
                      <option value="">All Locations</option>
                      {locations?.map((loc) => (
                        <option key={loc.id} value={loc.id}>
                          {loc.name}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-border mt-6">
                <Button type="button" variant="outline" onClick={() => setActiveTab('feed')}>
                  Cancel
                </Button>
                <Button type="submit" disabled={publishMutation.isPending}>
                  {publishMutation.isPending ? 'Publishing…' : 'Publish Alert'}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
