"use client";

import { useState } from "react";
import { Button, Field, TextInput } from "@/app/components/form-controls";
import { ErrorBanner, PageSection, PillList, StatCard } from "@/app/components/ui";
import { useAppContext } from "@/app/context/app-context";
import { appApi } from "@/app/lib/api";
import type { ApiError } from "@/app/lib/types";

export default function JobsPage() {
  const { session, profile } = useAppContext();
  const [role, setRole] = useState(profile?.targetRole ?? "");
  const [source, setSource] = useState("");
  const [ingestResult, setIngestResult] = useState<Record<string, unknown> | null>(null);
  const [aggregateResult, setAggregateResult] = useState<Record<string, unknown> | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loadingIngest, setLoadingIngest] = useState(false);
  const [loadingAggregate, setLoadingAggregate] = useState(false);

  const ingest = async () => {
    if (!session?.jwt) {
      return;
    }

    if (!role.trim()) {
      setError("Role is required for ingestion.");
      return;
    }

    setLoadingIngest(true);
    setError(null);

    try {
      const response = await appApi.ingestJobs(session.jwt, {
        role: role.trim(),
        source: source.trim(),
      });
      setIngestResult(response);
    } catch (err) {
      setError((err as ApiError).message ?? "Job ingestion failed.");
    } finally {
      setLoadingIngest(false);
    }
  };

  const aggregate = async () => {
    if (!session?.jwt) {
      return;
    }

    if (!role.trim()) {
      setError("Role is required to aggregate demand.");
      return;
    }

    setLoadingAggregate(true);
    setError(null);

    try {
      const response = await appApi.aggregateJobs(session.jwt, role.trim());
      setAggregateResult(response);
    } catch (err) {
      setError((err as ApiError).message ?? "Job aggregation failed.");
    } finally {
      setLoadingAggregate(false);
    }
  };

  const topSkills =
    (aggregateResult?.topSkills as string[] | undefined) ??
    (aggregateResult?.top_skills as string[] | undefined) ??
    [];

  const sourceQuality = aggregateResult?.sourceQualityBreakdown as
    | Record<string, number>
    | undefined;
  const demandMap = aggregateResult?.aggregatedDemandMap as
    | Record<string, number>
    | undefined;

  return (
    <div className="space-y-6">
      <PageSection title="Job Intelligence" subtitle="Ingest postings, then aggregate role demand and skill signals.">
        <div className="grid gap-5 lg:grid-cols-[0.85fr_1.15fr]">
          <div className="space-y-4">
            <ErrorBanner message={error} onDismiss={() => setError(null)} />
            <Field label="Role">
              <TextInput value={role} onChange={(event) => setRole(event.target.value)} />
            </Field>
            <Field label="Source / query" hint="Optional source descriptor for job ingestion.">
              <TextInput value={source} onChange={(event) => setSource(event.target.value)} />
            </Field>
            <div className="flex flex-wrap gap-3">
              <Button type="button" onClick={ingest} disabled={loadingIngest}>
                {loadingIngest ? "Ingesting..." : "Ingest Jobs"}
              </Button>
              <Button type="button" tone="secondary" onClick={aggregate} disabled={loadingAggregate}>
                {loadingAggregate ? "Aggregating..." : "Aggregate Role Demand"}
              </Button>
            </div>
          </div>

          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            <StatCard label="Ingested Count" value={String(ingestResult?.ingestedCount ?? "0")} />
            <StatCard label="Duplicates Skipped" value={String(ingestResult?.duplicateSkippedCount ?? "0")} />
            <StatCard label="Total Postings" value={String(aggregateResult?.totalPostings ?? "0")} />
            <StatCard label="Avg Reliability" value={String(aggregateResult?.avgSourceReliability ?? "N/A")} />
          </div>
        </div>
      </PageSection>

      <div className="grid gap-6 xl:grid-cols-2">
        <PageSection title="Top Skills" subtitle="Most demanded skills across aggregated postings.">
          <PillList items={topSkills} tone="amber" />
        </PageSection>
        <PageSection title="Source Quality" subtitle="HIGH / MEDIUM / LOW quality breakdown.">
          <KeyValueList data={sourceQuality} />
        </PageSection>
      </div>

      <PageSection title="Aggregated Demand Map" subtitle="Normalized view of role demand for the selected target role.">
        <KeyValueList data={demandMap} bars />
      </PageSection>
    </div>
  );
}

function KeyValueList({
  data,
  bars,
}: {
  data?: Record<string, number>;
  bars?: boolean;
}) {
  if (!data || !Object.keys(data).length) {
    return <p className="text-sm text-slate-500">No data returned yet.</p>;
  }

  const maxValue = Math.max(...Object.values(data), 1);

  return (
    <div className="space-y-3">
      {Object.entries(data).map(([key, value]) => (
        <div key={key} className="rounded-2xl bg-white p-4">
          <div className="flex items-center justify-between gap-3 text-sm">
            <span className="font-medium text-slate-900">{key}</span>
            <span className="text-slate-600">{value}</span>
          </div>
          {bars ? (
            <div className="mt-3 h-3 rounded-full bg-slate-100">
              <div
                className="h-3 rounded-full bg-slate-950"
                style={{ width: `${Math.max((value / maxValue) * 100, 8)}%` }}
              />
            </div>
          ) : null}
        </div>
      ))}
    </div>
  );
}
