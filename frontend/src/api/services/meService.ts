import { api } from '@/lib/api/client';
import type { MeResponse } from '@/lib/api/types';

export function fetchMe(): Promise<MeResponse> {
  return api.get<MeResponse>('/api/me');
}
