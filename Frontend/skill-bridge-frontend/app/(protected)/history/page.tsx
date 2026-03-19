"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/app/components/form-controls";
import { ErrorBanner, LoadingState, PageSection } from "@/app/components/ui";
import { useAppContext } from "@/app/context/app-context";
import { appApi } from "@/app/lib/api";
import type { AnalysisResult, ApiError, ProfileInput } from "@/app/lib/types";

export default function HistoryPage() {
  const router = useRouter();
  const { session, saveProfileBundle } = useAppContext();
  const [roadmaps, setRoadmaps] = useState<Record<string, unknown>[]>([]);
  const [interviews, setInterviews] = useState<Record<string, unknown>[]>([]);
  const [profiles, setProfiles] = useState<Record<string, unknown>[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!session?.jwt) {
      return;
    }

    const load = async () => {
      try {
        const [roadmapResponse, interviewResponse, profileResponse] =
          await Promise.all([
            appApi.getRoadmapHistory(session.jwt),
            appApi.getInterviewHistory(session.jwt),
            appApi.getProfileHistory(session.jwt),
          ]);
        setRoadmaps(roadmapResponse);
        setInterviews(interviewResponse);
        setProfiles(profileResponse);
      } catch (err) {
        setError((err as ApiError).message ?? "Failed to load history.");
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [session?.jwt]);

  if (loading) {
    return <LoadingState label="Loading history..." />;
  }

  return (
    <div className="space-y-6">
      <ErrorBanner message={error} onDismiss={() => setError(null)} />
      <div className="grid gap-6 xl:grid-cols-3">
        <HistorySection title="Roadmap History" items={roadmaps} />
        <HistorySection title="Interview History" items={interviews} />
        <PageSection title="Profile Snapshots" subtitle="Reuse a historical profile for a new roadmap or interview set.">
          <div className="space-y-3">
            {profiles.length ? (
              profiles.map((item, index) => (
                <div key={`profile-${index}`} className="rounded-3xl border border-slate-200 bg-white p-4">
                  <pre className="overflow-auto text-xs text-slate-600">
                    {JSON.stringify(item, null, 2)}
                  </pre>
                  <div className="mt-4 flex flex-wrap gap-3">
                    <Button
                      type="button"
                      tone="secondary"
                      onClick={() => {
                        const profile = {
                          targetRole: String(item.targetRole ?? item.role ?? ""),
                          resumeText: String(item.resumeText ?? ""),
                          githubText: String(item.githubText ?? ""),
                          currentSkills: Array.isArray(item.currentSkills)
                            ? (item.currentSkills as string[])
                            : [],
                        } satisfies ProfileInput;
                        saveProfileBundle({
                          profile,
                          analysisResult:
                            ((item.analysis as AnalysisResult | undefined) ?? null),
                        });
                        router.push("/roadmap");
                      }}
                    >
                      Reuse for roadmap
                    </Button>
                    <Button
                      type="button"
                      tone="ghost"
                      onClick={() => {
                        const profile = {
                          targetRole: String(item.targetRole ?? item.role ?? ""),
                          resumeText: String(item.resumeText ?? ""),
                          githubText: String(item.githubText ?? ""),
                          currentSkills: Array.isArray(item.currentSkills)
                            ? (item.currentSkills as string[])
                            : [],
                        } satisfies ProfileInput;
                        saveProfileBundle({
                          profile,
                          analysisResult:
                            ((item.analysis as AnalysisResult | undefined) ?? null),
                        });
                        router.push("/interview/questions");
                      }}
                    >
                      Reuse for interview
                    </Button>
                  </div>
                </div>
              ))
            ) : (
              <p className="text-sm text-slate-500">No profile snapshots returned.</p>
            )}
          </div>
        </PageSection>
      </div>
    </div>
  );
}

function HistorySection({
  title,
  items,
}: {
  title: string;
  items: Record<string, unknown>[];
}) {
  return (
    <PageSection title={title} subtitle="View details for historical items.">
      <div className="space-y-3">
        {items.length ? (
          items.map((item, index) => (
            <details
              key={`${title}-${index}`}
              className="rounded-3xl border border-slate-200 bg-white p-4"
            >
              <summary className="cursor-pointer text-sm font-semibold text-slate-900">
                {String(item.name ?? item.title ?? item.id ?? `${title} ${index + 1}`)}
              </summary>
              <pre className="mt-4 overflow-auto text-xs text-slate-600">
                {JSON.stringify(item, null, 2)}
              </pre>
            </details>
          ))
        ) : (
          <p className="text-sm text-slate-500">No history returned.</p>
        )}
      </div>
    </PageSection>
  );
}
