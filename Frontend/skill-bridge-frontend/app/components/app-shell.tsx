"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import type { ReactNode } from "react";
import { useAppContext } from "@/app/context/app-context";
import { Button } from "@/app/components/form-controls";

const links = [
  { href: "/dashboard", label: "Dashboard" },
  { href: "/resume", label: "Resume Upload" },
  { href: "/analysis", label: "Gap Analysis" },
  { href: "/roadmap", label: "Roadmap" },
  { href: "/jobs", label: "Job Intelligence" },
  { href: "/interview/questions", label: "Questions" },
  { href: "/interview/evaluate", label: "Evaluation" },
  { href: "/history", label: "History" },
  { href: "/metrics", label: "Metrics" },
];

export function AppShell({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const { session, logout } = useAppContext();

  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_top_left,_rgba(59,130,246,0.22),_transparent_26%),radial-gradient(circle_at_top_right,_rgba(16,185,129,0.18),_transparent_24%),linear-gradient(180deg,_#f8f1e6_0%,_#efe3cf_100%)]">
      <div className="mx-auto flex min-h-screen max-w-7xl gap-6 px-4 py-6 lg:px-8">
        <aside className="hidden w-72 shrink-0 rounded-[32px] border border-sky-900/20 bg-gradient-to-br from-sky-950 via-cyan-900 to-emerald-800 p-6 text-slate-50 shadow-[0_24px_80px_rgba(15,68,87,0.38)] lg:block">
          <div>
            <p className="text-xs uppercase tracking-[0.28em] text-amber-300">
              Skill Bridge
            </p>
            <h1 className="mt-3 text-2xl font-semibold">Career transition control room</h1>
            <p className="mt-3 text-sm text-slate-300">
              Track role readiness, analyze gaps, and convert profile data into roadmaps and interview loops.
            </p>
          </div>
          <nav className="mt-8 space-y-2">
            {links.map((link) => {
              const active = pathname.startsWith(link.href);
              return (
                <Link
                  key={link.href}
                  href={link.href}
                  className={`block rounded-2xl px-4 py-3 text-sm transition ${
                    active
                      ? "bg-[#fff5e7] text-sky-950"
                      : "text-slate-100 hover:bg-[#fff5e7] hover:text-sky-950"
                  }`}
                >
                  {link.label}
                </Link>
              );
            })}
          </nav>
          <div className="mt-8 rounded-3xl border border-white/10 bg-white/10 p-4">
            <p className="text-xs uppercase tracking-[0.24em] text-sky-100/70">Session</p>
            <p className="mt-2 break-all text-sm text-white">{session?.email ?? session?.userId}</p>
            <Button
              type="button"
              tone="secondary"
              className="mt-4 w-full"
              onClick={() => {
                logout();
                router.replace("/auth");
              }}
            >
              Sign out
            </Button>
          </div>
        </aside>

        <div className="flex-1">
          <div className="mb-4 flex items-center justify-between rounded-[28px] border border-[#eadfcb] bg-[#fbf5ea]/90 px-5 py-4 shadow-[0_24px_80px_rgba(64,88,104,0.12)] backdrop-blur lg:hidden">
            <Link href="/dashboard" className="text-sm font-semibold text-slate-900">
              Skill Bridge
            </Link>
            <Button
              type="button"
              tone="ghost"
              onClick={() => {
                logout();
                router.replace("/auth");
              }}
            >
              Sign out
            </Button>
          </div>
          <main className="space-y-6">{children}</main>
        </div>
      </div>
    </div>
  );
}
