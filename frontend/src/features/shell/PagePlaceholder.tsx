import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
import { Clock3, Database, ShieldCheck } from 'lucide-react';

type Props = {
  title: string;
  description?: string;
  phase?: string;
};

export function PagePlaceholder({ title, description, phase = 'Phase 1+' }: Props) {
  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
        {description ? (
          <p className="mt-1 text-sm text-muted-foreground">{description}</p>
        ) : null}
      </header>
      <Card>
        <CardHeader>
          <CardTitle>{title} workspace is being expanded</CardTitle>
          <CardDescription>
            This module is connected in navigation and role checks. Full workflow UI is shipping in {phase}.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid gap-3 sm:grid-cols-3">
            <div className="rounded-md border bg-muted/30 p-3">
              <p className="flex items-center gap-2 text-sm font-medium"><Database className="h-4 w-4" /> API Ready</p>
              <p className="mt-1 text-xs text-muted-foreground">Backend endpoints are available for this module.</p>
            </div>
            <div className="rounded-md border bg-muted/30 p-3">
              <p className="flex items-center gap-2 text-sm font-medium"><ShieldCheck className="h-4 w-4" /> RBAC Enforced</p>
              <p className="mt-1 text-xs text-muted-foreground">Role and scope rules are already active.</p>
            </div>
            <div className="rounded-md border bg-muted/30 p-3">
              <p className="flex items-center gap-2 text-sm font-medium"><Clock3 className="h-4 w-4" /> UI In Progress</p>
              <p className="mt-1 text-xs text-muted-foreground">Data forms, filters, and actions are in rollout.</p>
            </div>
          </div>
          <Separator className="my-4" />
          <p className="text-sm text-muted-foreground">Use Dashboard, Employees, and existing APIs while this page is being finalized.</p>
        </CardContent>
      </Card>
    </div>
  );
}
