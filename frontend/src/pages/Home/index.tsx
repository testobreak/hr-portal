import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useAuth } from '@/auth/useAuth';

export function HomePage() {
  const auth = useAuth();

  if (auth.loading) {
    return (
      <main className="mx-auto flex min-h-screen max-w-lg items-center justify-center p-6">
        <p className="text-muted-foreground">Loading…</p>
      </main>
    );
  }

  if (auth.error) {
    return (
      <main className="mx-auto max-w-lg p-6">
        <h1 className="text-2xl font-semibold">HRMS</h1>
        <p className="mt-2 text-red-600">Auth init failed: {auth.error}</p>
      </main>
    );
  }

  return (
    <main className="mx-auto flex min-h-screen max-w-lg flex-col justify-center gap-6 p-6">
      <header>
        <h1 className="text-3xl font-semibold tracking-tight">HRMS</h1>
        <p className="mt-2 text-muted-foreground">Internal HR / resource management.</p>
      </header>
      <Card>
        <CardHeader>
          <CardTitle>{auth.authenticated ? 'Welcome back' : 'Sign in'}</CardTitle>
          <CardDescription>
            {auth.authenticated
              ? 'Open the dashboard to manage employees, projects, and more.'
              : 'Use your Acme Keycloak account to continue.'}
          </CardDescription>
        </CardHeader>
        <CardContent className="flex flex-wrap gap-3">
          {auth.authenticated ? (
            <>
              <Button asChild>
                <Link to="/dashboard">Open dashboard</Link>
              </Button>
              <Button type="button" variant="outline" onClick={auth.logout}>
                Sign out
              </Button>
            </>
          ) : (
            <Button type="button" onClick={() => auth.login('/dashboard')}>
              Sign in with Keycloak
            </Button>
          )}
        </CardContent>
      </Card>
    </main>
  );
}
