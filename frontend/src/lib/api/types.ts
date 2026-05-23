/** Hand-maintained until `npm run gen:api` is run against a live backend. */
export type MeResponse = {
  subjectUuid: string;
  username: string | null;
  email: string | null;
  roles: string[];
  requestId: string | null;
};

export type SpringPage<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
};

export type EmployeeSummary = {
  id: string;
  employeeCode: string;
  firstName: string;
  lastName: string;
  email: string;
  departmentId: string | null;
  departmentName: string | null;
  designationId: string | null;
  designationTitle: string | null;
  locationId: string | null;
  locationName: string | null;
  dateOfJoining: string;
  employmentStatus: string;
};

export type EmployeeResponse = {
  id: string;
  employeeCode: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string | null;
  dateOfBirth: string | null;
  dateOfJoining: string;
  employmentStatus: string;
  keycloakUserId: string | null;
  departmentId: string | null;
  departmentName: string | null;
  designationId: string | null;
  designationTitle: string | null;
  locationId: string | null;
  locationName: string | null;
  managerId: string | null;
  managerName: string | null;
  createdAt: string;
  updatedAt: string;
  version: number;
};
