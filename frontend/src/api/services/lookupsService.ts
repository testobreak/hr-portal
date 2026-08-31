import { api } from '@/lib/api/client';
import type { SpringPage } from '@/lib/api/types';

export type DepartmentLookup = { id: string; code: string; name: string };
export type DesignationLookup = { id: string; title: string; level: string | null };
export type LocationLookup = { id: string; code: string; name: string };
export type LegalEntityLookup = { id: string; code: string; name: string };

export async function fetchDepartmentLookups(): Promise<DepartmentLookup[]> {
  const page = await api.get<SpringPage<DepartmentLookup>>('/api/departments?page=0&size=200&sort=name,asc');
  return page.content;
}

export async function fetchDesignationLookups(): Promise<DesignationLookup[]> {
  const page = await api.get<SpringPage<DesignationLookup>>('/api/designations?page=0&size=200&sort=title,asc');
  return page.content;
}

export async function fetchLocationLookups(): Promise<LocationLookup[]> {
  const page = await api.get<SpringPage<LocationLookup>>('/api/locations?page=0&size=200&sort=name,asc');
  return page.content;
}

export async function fetchLegalEntityLookups(): Promise<LegalEntityLookup[]> {
  const page = await api.get<SpringPage<LegalEntityLookup>>('/api/legal-entities?page=0&size=200&sort=name,asc');
  return page.content;
}
