import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { apiBase } from '@/api/apiConfig';
import { useAuth } from '@/auth/useAuth';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Lock, Mail, AlertCircle, Loader2 } from 'lucide-react';

const loginSchema = z.object({
  email: z.string().min(1, 'Email is required').email('Invalid email address'),
  password: z.string().min(1, 'Password is required'),
});

type LoginValues = z.infer<typeof loginSchema>;

export function LoginPage() {
  const auth = useAuth();
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });

  const onSubmit = async (values: LoginValues) => {
    setError(null);
    setLoading(true);

    try {
      const payload = {
        email: values.email.trim(),
        password: values.password.trim(),
      };
      const res = await fetch(`${apiBase}/api/v1/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Accept: 'application/json',
        },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        if (res.status === 401) {
          throw new Error('Invalid email or password');
        }
        const data = await res.json().catch(() => ({}));
        throw new Error(data.detail || 'Failed to authenticate');
      }

      const data = await res.json();
      
      // Parse redirect query param
      const searchParams = new URLSearchParams(window.location.search);
      const redirect = searchParams.get('redirect') || '/dashboard';
      
      auth.authenticate(data.token, redirect);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred during login');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="relative flex min-h-screen items-center justify-center overflow-hidden bg-gradient-to-br from-slate-900 via-slate-850 to-zinc-900 px-4 py-12">
      {/* Decorative ambient background glows */}
      <div className="absolute -top-40 -left-40 h-80 w-80 rounded-full bg-violet-600/10 blur-[128px]" />
      <div className="absolute -bottom-40 -right-40 h-80 w-80 rounded-full bg-cyan-600/10 blur-[128px]" />

      <Card className="w-full max-w-md border-slate-800 bg-slate-950/80 backdrop-blur-md shadow-2xl">
        <CardHeader className="space-y-1 text-center">
          <CardTitle className="text-3xl font-bold tracking-tight bg-gradient-to-r from-violet-400 to-cyan-400 bg-clip-text text-transparent">
            HR Portal
          </CardTitle>
          <CardDescription className="text-slate-400">
            Enter your credentials to access your workspace
          </CardDescription>
        </CardHeader>
        <CardContent>
          {error && (
            <div className="mb-6 flex items-start gap-3 rounded-lg border border-red-500/20 bg-red-500/10 p-3 text-sm text-red-400">
              <AlertCircle className="h-5 w-5 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-2">
              <label className="text-sm font-medium text-slate-300" htmlFor="email">
                Email Address
              </label>
              <div className="relative">
                <Mail className="absolute top-2.5 left-3 h-4 w-4 text-slate-500" />
                <input
                  {...register('email')}
                  id="email"
                  type="email"
                  autoComplete="email"
                  className="h-10 w-full rounded-md border border-slate-800 bg-slate-900/50 pl-10 pr-3 py-2 text-sm text-slate-100 placeholder-slate-500 outline-none transition-all focus:border-violet-500 focus:ring-1 focus:ring-violet-500"
                  placeholder="name@company.com"
                  disabled={loading}
                />
              </div>
              {errors.email && (
                <p className="text-xs text-red-400">{errors.email.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <label className="text-sm font-medium text-slate-300" htmlFor="password">
                  Password
                </label>
              </div>
              <div className="relative">
                <Lock className="absolute top-2.5 left-3 h-4 w-4 text-slate-500" />
                <input
                  {...register('password')}
                  id="password"
                  type="password"
                  autoComplete="current-password"
                  className="h-10 w-full rounded-md border border-slate-800 bg-slate-900/50 pl-10 pr-3 py-2 text-sm text-slate-100 placeholder-slate-500 outline-none transition-all focus:border-violet-500 focus:ring-1 focus:ring-violet-500"
                  placeholder="••••••••"
                  disabled={loading}
                />
              </div>
              {errors.password && (
                <p className="text-xs text-red-400">{errors.password.message}</p>
              )}
            </div>

            <Button
              type="submit"
              className="mt-2 h-10 w-full bg-gradient-to-r from-violet-600 to-indigo-600 hover:from-violet-500 hover:to-indigo-500 text-white font-medium shadow-lg shadow-violet-600/20 transition-all focus:ring-2 focus:ring-violet-500/50"
              disabled={loading}
            >
              {loading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Signing in...
                </>
              ) : (
                'Sign In'
              )}
            </Button>
          </form>

          <div className="mt-6 text-center text-xs text-slate-500">
            <p>Acme Corporation HR Management System</p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
export default LoginPage;
