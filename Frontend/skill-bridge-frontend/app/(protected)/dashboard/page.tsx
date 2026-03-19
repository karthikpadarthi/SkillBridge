"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { ErrorBanner, LoadingState, PageSection, StatCard } from "@/app/components/ui";
import { useAppContext } from "@/app/context/app-context";
import { appApi } from "@/app/lib/api";
import { compactNumber, normalizeRoles } from "@/app/lib/helpers";

export default function DashboardPage() {
  const { session } = useAppContext();
  const [roles, setRoles] = useState<string[]>([]);
  const [summary, setSummary] = useState<Record<string, unknown> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!session?.jwt) {
      return;
    }

    const load = async () => {
      try {
        setLoading(true);
        const [rolesResponse, summaryResponse] = await Promise.all([
          appApi.getRoles(session.jwt),
          appApi.getMetricsSummary(session.jwt),
        ]);
        setRoles(normalizeRoles(rolesResponse));
        setSummary(summaryResponse);
      } catch (err) {
        setError((err as { message?: string }).message ?? "Failed to load dashboard.");
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [session?.jwt]);

  if (loading) {
    return <LoadingState label="Loading dashboard snapshot..." />;
  }

  return (
    <>
      <PageSection
        title="Dashboard"
        subtitle="Available target roles, progress summary, and entry points into the main flows."
      >
        <ErrorBanner message={error} onDismiss={() => setError(null)} />
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <StatCard label="Total Events" value={compactNumber(summary?.totalEvents)} />
          <StatCard
            label="Analyze Count"
            value={compactNumber(summary?.analyzeCount)}
            hint={`Last activity: ${String(summary?.lastActivityAt ?? "N/A")}`}
          />
          <StatCard label="Roadmap Count" value={compactNumber(summary?.roadmapCount)} />
          <StatCard
            label="Interview Count"
            value={compactNumber(
              (summary?.interviewSetCount as number | undefined) ??
                summary?.interviewCount,
            )}
          />
        </div>
      </PageSection>

      <div className="grid gap-6 xl:grid-cols-[1.15fr_0.85fr]">
        <PageSection title="Target Roles" subtitle="Available role targets from your current catalog.">
          <div className="flex flex-wrap gap-2">
            {roles.length ? (
              roles.map((role) => (
                <span
                  key={role}
                  className="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm text-slate-700"
                >
                  {role}
                </span>
              ))
            ) : (
              <p className="text-sm text-slate-500">No roles returned.</p>
            )}
          </div>
        </PageSection>

        <PageSection title="Quick Progress Snapshot" subtitle="Latest timestamps and role summary.">
          <div className="space-y-3 text-sm text-slate-700">
            <SnapshotRow
              label="Average coverage"
              value={compactNumber(
                (summary?.averageCoverageScore as number | undefined) ??
                  summary?.avgCoverageScore,
              )}
            />
            <SnapshotRow
              label="Average interview score"
              value={compactNumber(
                (summary?.averageInterviewScore as number | undefined) ??
                  summary?.avgInterviewScore,
              )}
            />
            <SnapshotRow label="Top target role" value={String(summary?.topTargetRole ?? "N/A")} />
            <SnapshotRow
              label="Last activity"
              value={String(summary?.lastActivityAt ?? summary?.lastActivity ?? "N/A")}
            />
          </div>
        </PageSection>
      </div>

      <PageSection title="Actions" subtitle="Launch the core product flows from here.">
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <ActionCard href="/resume" title="Upload Resume" body="Analyze a PDF resume against a target role." />
          <ActionCard href="/analysis" title="Analyze Profile" body="Paste manual text and generate a full gap analysis." />
          <ActionCard href="/roadmap" title="Generate Roadmap" body="Create a guided weekly plan from your profile." />
          <ActionCard href="/interview/questions" title="Start Mock Interview" body="Generate and answer role-specific questions." />
        </div>
      </PageSection>
    </>
  );
}

function SnapshotRow({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="flex items-center justify-between rounded-2xl bg-slate-100 px-4 py-3">
      <span className="font-medium text-slate-900">{label}</span>
      <span>{value}</span>
    </div>
  );
}

function ActionCard({
  href,
  title,
  body,
}: {
  href: string;
  title: string;
  body: string;
}) {
  return (
    <div className="rounded-3xl border border-slate-200 bg-white p-5">
      <h3 className="text-lg font-semibold text-slate-900">{title}</h3>
      <p className="mt-3 text-sm text-slate-600">{body}</p>
      <Link
        href={href}
        className="mt-5 inline-flex rounded-full bg-gradient-to-r from-blue-600 to-emerald-500 px-5 py-3 text-sm font-semibold text-white transition hover:from-blue-500 hover:to-emerald-400"
      >
        Open
      </Link>
    </div>
  );
}
