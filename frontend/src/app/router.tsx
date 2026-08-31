import { lazy, Suspense, type ReactNode } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from '@/auth/ProtectedRoute';
import { RoleRoute } from '@/auth/RoleRoute';
import { AppLayout } from '@/layout/AppLayout';
import { Roles } from '@/lib/roles';
import { Skeleton } from '@/components/ui/skeleton';
import { HomePage } from '@/pages/Home';
import { NotFoundPage } from '@/pages/NotFound';

const LoginPage = lazy(() =>
  import('@/pages/Login').then((m) => ({ default: m.LoginPage })),
);

const DashboardPage = lazy(() =>
  import('@/pages/Dashboard').then((m) => ({ default: m.DashboardPage })),
);
const EmployeesPage = lazy(() =>
  import('@/pages/Employees').then((m) => ({ default: m.EmployeesPage })),
);
const DepartmentsPage = lazy(() =>
  import('@/pages/Departments').then((m) => ({ default: m.DepartmentsPage })),
);
const DesignationsPage = lazy(() =>
  import('@/pages/Designations').then((m) => ({ default: m.DesignationsPage })),
);
const LocationsPage = lazy(() =>
  import('@/pages/Locations').then((m) => ({ default: m.LocationsPage })),
);
const DocumentsPage = lazy(() =>
  import('@/pages/Documents').then((m) => ({ default: m.DocumentsPage })),
);
const ClientsPage = lazy(() =>
  import('@/pages/Clients').then((m) => ({ default: m.ClientsPage })),
);

const ProjectsPage = lazy(() =>
  import('@/pages/Projects/index').then((m) => ({
    default: m.ProjectsPage,
  })),
);

const AllocationsPage = lazy(() =>
  import('@/pages/Allocations').then((m) => ({ default: m.AllocationsPage })),
);
const SalaryPage = lazy(() => import('@/pages/Salary').then((m) => ({ default: m.SalaryPage })));
const AuditPage = lazy(() => import('@/pages/Audit').then((m) => ({ default: m.AuditPage })));
const SettingsPage = lazy(() =>
  import('@/pages/Settings').then((m) => ({ default: m.SettingsPage })),
);
const ProfilePage = lazy(() =>
  import('@/pages/Profile').then((m) => ({ default: m.ProfilePage })),
);
const DirectoryPage = lazy(() =>
  import('@/pages/Directory').then((m) => ({ default: m.DirectoryPage })),
);
const LeavePage = lazy(() =>
  import('@/pages/Leave').then((m) => ({ default: m.LeavePage })),
);
const AnnouncementsPage = lazy(() =>
  import('@/pages/Announcements').then((m) => ({ default: m.AnnouncementsPage })),
);
const RecruitmentPage = lazy(() =>
  import('@/pages/Recruitment').then((m) => ({ default: m.RecruitmentPage })),
);
const CareersPage = lazy(() =>
  import('@/pages/Careers').then((m) => ({ default: m.CareersPage })),
);
const CandidateOfferPage = lazy(() =>
  import('@/pages/CandidateOffer').then((m) => ({ default: m.CandidateOfferPage })),
);
const OnboardingPage = lazy(() =>
  import('@/pages/Onboarding').then((m) => ({ default: m.OnboardingPage })),
);
const TimePage = lazy(() =>
  import('@/pages/Time').then((m) => ({ default: m.TimePage })),
);
const PayrollPage = lazy(() =>
  import('@/pages/Payroll').then((m) => ({ default: m.PayrollPage })),
);

function PageFallback() {
  return (
    <div className="space-y-3">
      <Skeleton className="h-8 w-48" />
      <Skeleton className="h-32 w-full" />
    </div>
  );
}

function AppShell({ children }: { children: ReactNode }) {
  return (
    <ProtectedRoute>
      <AppLayout>
        <Suspense fallback={<PageFallback />}>{children}</Suspense>
      </AppLayout>
    </ProtectedRoute>
  );
}

export function AppRouter() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/careers" element={<CareersPage />} />
      <Route path="/candidate/offers/:secureToken" element={<CandidateOfferPage />} />
      <Route
        path="/dashboard"
        element={
          <AppShell>
            <DashboardPage />
          </AppShell>
        }
      />
      <Route
        path="/onboarding"
        element={
          <AppShell>
            <OnboardingPage />
          </AppShell>
        }
      />
      <Route
        path="/time"
        element={
          <AppShell>
            <TimePage />
          </AppShell>
        }
      />
      <Route
        path="/payroll"
        element={
          <AppShell>
            <RoleRoute roles={[Roles.SUPER_ADMIN, Roles.HR_ADMIN, Roles.FINANCE_ADMIN]}>
              <PayrollPage />
            </RoleRoute>
          </AppShell>
        }
      />
      <Route
        path="/profile"
        element={
          <AppShell>
            <ProfilePage />
          </AppShell>
        }
      />
      <Route
        path="/directory"
        element={
          <AppShell>
            <DirectoryPage />
          </AppShell>
        }
      />
      <Route
        path="/leave"
        element={
          <AppShell>
            <LeavePage />
          </AppShell>
        }
      />
      <Route
        path="/announcements"
        element={
          <AppShell>
            <AnnouncementsPage />
          </AppShell>
        }
      />
      <Route
        path="/recruitment"
        element={
          <AppShell>
            <RecruitmentPage />
          </AppShell>
        }
      />
      <Route
        path="/employees"
        element={
          <AppShell>
            <EmployeesPage />
          </AppShell>
        }
      />
      <Route
        path="/departments"
        element={
          <AppShell>
            <DepartmentsPage />
          </AppShell>
        }
      />
      <Route
        path="/designations"
        element={
          <AppShell>
            <DesignationsPage />
          </AppShell>
        }
      />
      <Route
        path="/locations"
        element={
          <AppShell>
            <LocationsPage />
          </AppShell>
        }
      />
      <Route
        path="/documents"
        element={
          <AppShell>
            <RoleRoute
              roles={[Roles.SUPER_ADMIN, Roles.HR_ADMIN, Roles.EMPLOYEE]}
            >
              <DocumentsPage />
            </RoleRoute>
          </AppShell>
        }
      />
      <Route
        path="/clients"
        element={
          <AppShell>
            <ClientsPage />
          </AppShell>
        }
      />
      <Route
        path="/projects"
        element={
          <AppShell>
            <ProjectsPage />
          </AppShell>
        }
      />
      <Route
        path="/allocations"
        element={
          <AppShell>
            <AllocationsPage />
          </AppShell>
        }
      />
      <Route
        path="/salary"
        element={
          <AppShell>
            <RoleRoute
              roles={[
                Roles.SUPER_ADMIN,
                Roles.HR_ADMIN,
                Roles.FINANCE_ADMIN,
                Roles.EMPLOYEE,
              ]}
            >
              <SalaryPage />
            </RoleRoute>
          </AppShell>
        }
      />
      <Route
        path="/audit"
        element={
          <AppShell>
            <RoleRoute roles={[Roles.SUPER_ADMIN]}>
              <AuditPage />
            </RoleRoute>
          </AppShell>
        }
      />
      <Route
        path="/settings"
        element={
          <AppShell>
            <RoleRoute roles={[Roles.SUPER_ADMIN]}>
              <SettingsPage />
            </RoleRoute>
          </AppShell>
        }
      />
      <Route path="/app" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
