import { api } from '@/lib/api/client';

export type Announcement = {
  id: string;
  title: string;
  content: string;
  authorId: string;
  publishedAt: string;
  requiresAcknowledgment: boolean;
};

export function fetchMyAnnouncements(): Promise<Announcement[]> {
  return api.get<Announcement[]>('/api/v1/me/announcements');
}

export function markAnnouncementAsRead(announcementId: string): Promise<void> {
  return api.post<void>(`/api/v1/me/announcements/${announcementId}/read`);
}

export function acknowledgeAnnouncement(announcementId: string): Promise<void> {
  return api.post<void>(`/api/v1/me/announcements/${announcementId}/acknowledge`);
}

export function publishAnnouncement(params: {
  title: string;
  content: string;
  legalEntityId?: string;
  departmentId?: string;
  locationId?: string;
}): Promise<Announcement> {
  const query = new URLSearchParams({
    title: params.title,
    content: params.content,
  });
  if (params.legalEntityId) {
    query.set('legalEntityId', params.legalEntityId);
  }
  if (params.departmentId) {
    query.set('departmentId', params.departmentId);
  }
  if (params.locationId) {
    query.set('locationId', params.locationId);
  }
  return api.post<Announcement>(`/api/v1/admin/announcements?${query.toString()}`);
}
