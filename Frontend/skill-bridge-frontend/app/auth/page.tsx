"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { Button, Field, TextInput } from "@/app/components/form-controls";
import { ErrorBanner, PageSection } from "@/app/components/ui";
import { useAppContext } from "@/app/context/app-context";
import { authApi } from "@/app/lib/api";
import type { ApiError } from "@/app/lib/types";

export default function AuthPage() {
  const router = useRouter();
  const { hydrated, session, setSession } = useAppContext();
  const [mode, setMode] = useState<"login" | "register">("login");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (hydrated && session) {
      router.replace("/dashboard");
    }
  }, [hydrated, router, session]);

  const submit = async () => {
    if (!email.trim() || !password.trim()) {
      setError("Email and password are required.");
      return;
    }

    if (password.trim().length < 6) {
      setError("Password must be at least 6 characters.");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const payload = { email: email.trim(), password: password.trim() };
      const result =
        mode === "login"
          ? await authApi.login(payload)
          : await authApi.register(payload);
      setSession(result);
      router.replace("/dashboard");
    } catch (err) {
      setError((err as ApiError).message ?? "Authentication failed.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen bg-[radial-gradient(circle_at_top_left,_rgba(59,130,246,0.24),_transparent_24%),radial-gradient(circle_at_bottom_right,_rgba(16,185,129,0.22),_transparent_28%),linear-gradient(180deg,_#083344_0%,_#0f4c5c_55%,_#146c60_100%)] px-4 py-10">
      <div className="mx-auto grid max-w-6xl gap-6 lg:grid-cols-[1.2fr_0.8fr]">
        <section className="rounded-[36px] border border-white/15 bg-white/10 p-8 text-white shadow-[0_24px_80px_rgba(0,58,74,0.28)] backdrop-blur">
          <p className="text-xs uppercase tracking-[0.32em] text-amber-300">
            Skill Bridge Platform
          </p>
          <h1 className="mt-5 max-w-xl text-5xl font-semibold leading-tight">
            Build role readiness from resume signal to interview execution.
          </h1>
          <p className="mt-5 max-w-xl text-base leading-7 text-slate-300">
            Authenticate once, then move through gap analysis, roadmap planning, job intelligence, and interview feedback with one shared profile state.
          </p>
        </section>

        <PageSection
          title={mode === "login" ? "Sign in" : "Create account"}
          subtitle="Email/password auth with backend error messaging."
        >
          <div className="space-y-4">
            <div className="inline-flex rounded-full bg-slate-100 p-1">
              <button
                type="button"
                className={`rounded-full px-4 py-2 text-sm font-semibold ${
                  mode === "login" ? "bg-slate-950 text-white" : "text-slate-600"
                }`}
                onClick={() => setMode("login")}
              >
                Login
              </button>
              <button
                type="button"
                className={`rounded-full px-4 py-2 text-sm font-semibold ${
                  mode === "register" ? "bg-slate-950 text-white" : "text-slate-600"
                }`}
                onClick={() => setMode("register")}
              >
                Register
              </button>
            </div>
            <ErrorBanner message={error} onDismiss={() => setError(null)} />
            <Field label="Email">
              <TextInput
                type="email"
                placeholder="you@example.com"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
              />
            </Field>
            <Field label="Password" hint="Minimum 6 characters.">
              <TextInput
                type="password"
                placeholder="Enter password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
              />
            </Field>
            <Button type="button" className="w-full" onClick={submit} disabled={loading}>
              {loading ? "Submitting..." : mode === "login" ? "Login" : "Register"}
            </Button>
          </div>
        </PageSection>
      </div>
    </main>
  );
}
