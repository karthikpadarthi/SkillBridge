"use client";

import { useEffect, useMemo, useState } from "react";
import { ErrorBanner, LoadingState, PageSection, StatCard } from "@/app/components/ui";
import { useAppContext } from "@/app/context/app-context";
import { appApi } from "@/app/lib/api";
import { compactNumber } from "@/app/lib/helpers";
import type { ApiError } from "@/app/lib/types";

export default function MetricsPage() {
  const { session } = useAppContext();
  const [summary, setSummary] = useState<Record<string, unknown> | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!session?.jwt) {
      return;
    }

    const load = async () => {
      try {
        const response = await appApi.getMetricsSummary(session.jwt);
        setSummary(response);
      } catch (err) {
        setError((err as ApiError).message ?? "Failed to load metrics.");
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [session?.jwt]);

  const breakdownEntries = useMemo(() => {
    const breakdown =
      (summary?.eventBreakdown as Record<string, number> | undefined) ??
      (summary?.event_breakdown as Record<string, number> | undefined) ??
      {};
    return Object.entries(breakdown);
  }, [summary]);

  if (loading) {
    return <LoadingState label="Loading metrics..." />;
  }

  return (
    <div className="space-y-6">
      <PageSection title="Metrics / Progress" subtitle="Summary metrics and frontend charting over repeated usage.">
        <ErrorBanner message={error} onDismiss={() => setError(null)} />
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <StatCard label="Total Events" value={compactNumber(summary?.totalEvents)} />
          <StatCard label="Analyze Count" value={compactNumber(summary?.analyzeCount)} />
          <StatCard label="Roadmap Count" value={compactNumber(summary?.roadmapCount)} />
          <StatCard
            label="Interview Count"
            value={compactNumber(
              (summary?.interviewSetCount as number | undefined) ??
                summary?.interviewCount,
            )}
          />
          <StatCard
            label="Avg Coverage"
            value={compactNumber(
              (summary?.averageCoverageScore as number | undefined) ??
                summary?.avgCoverageScore,
            )}
          />
          <StatCard
            label="Avg Interview Score"
            value={compactNumber(
              (summary?.averageInterviewScore as number | undefined) ??
                summary?.avgInterviewScore,
            )}
          />
          <StatCard label="Top Role" value={String(summary?.topTargetRole ?? "N/A")} />
          <StatCard
            label="Last Activity"
            value={String(summary?.lastActivityAt ?? summary?.lastActivity ?? "N/A")}
          />
        </div>
      </PageSection>

      <PageSection title="Event Breakdown Chart" subtitle="Simple charted progression from the returned event breakdown map.">
        {breakdownEntries.length ? (
          <div className="space-y-3">
            {breakdownEntries.map(([label, value]) => (
              <div key={label}>
                <div className="mb-2 flex items-center justify-between text-sm">
                  <span className="font-medium text-slate-900">{label}</span>
                  <span className="text-slate-600">{value}</span>
                </div>
                <div className="h-4 rounded-full bg-slate-100">
                  <div
                    className="h-4 rounded-full bg-gradient-to-r from-amber-400 via-orange-500 to-slate-950"
                    style={{
                      width: `${Math.max(
                        (value / Math.max(...breakdownEntries.map((entry) => entry[1]), 1)) * 100,
                        8,
                      )}%`,
                    }}
                  />
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-sm text-slate-500">No event breakdown returned.</p>
        )}
      </PageSection>
    </div>
  );
}
