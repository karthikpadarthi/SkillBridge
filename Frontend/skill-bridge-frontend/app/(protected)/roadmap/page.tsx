"use client";

import { useState } from "react";
import { RoadmapPanel } from "@/app/components/result-panels";
import { Button, Field, TextInput, Toggle } from "@/app/components/form-controls";
import { ErrorBanner, PageSection } from "@/app/components/ui";
import { useAppContext } from "@/app/context/app-context";
import { appApi } from "@/app/lib/api";
import { normalizeRoadmapResult } from "@/app/lib/helpers";
import type { ApiError, RoadmapResult } from "@/app/lib/types";

export default function RoadmapPage() {
  const { session, profile, analysisResult, roadmapResult, saveRoadmap } = useAppContext();
  const [durationWeeks, setDurationWeeks] = useState("8");
  const [weeklyHours, setWeeklyHours] = useState("8");
  const [includePaidResources, setIncludePaidResources] = useState(false);
  const [resumeFile, setResumeFile] = useState<File | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const submit = async () => {
    if (!session?.jwt) {
      return;
    }

    if (!profile?.targetRole) {
      setError("Create a profile with analysis or resume upload before generating a roadmap.");
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      let response: Record<string, unknown>;

      if (resumeFile) {
        if (resumeFile.type !== "application/pdf") {
          throw { message: "Roadmap PDF input must be a PDF file." } satisfies ApiError;
        }

        const formData = new FormData();
        formData.append("resumeFile", resumeFile);
        formData.append("targetRole", profile.targetRole);
        formData.append("githubLikeText", profile.githubText ?? "");
        profile.currentSkills.forEach((skill) => formData.append("currentSkills", skill));
        formData.append("durationWeeks", durationWeeks);
        formData.append("weeklyHours", weeklyHours);
        formData.append("includePaidResources", String(includePaidResources));
        response = await appApi.generateResumeRoadmap(session.jwt, formData);
      } else if (profile.resumeText) {
        response = await appApi.generateRoadmap(session.jwt, {
          profile: {
            targetRole: profile.targetRole,
            resumeText: profile.resumeText ?? "",
            githubLikeText: profile.githubText ?? "",
            currentSkills: profile.currentSkills,
          },
          durationWeeks: Number(durationWeeks),
          weeklyHours: Number(weeklyHours),
          includePaidResources,
        });
      } else {
        throw {
          message:
            "This profile came from the PDF flow. Re-upload the PDF here to generate a roadmap from the resume flow.",
        } satisfies ApiError;
      }

      saveRoadmap(normalizeRoadmapResult(response) as RoadmapResult);
    } catch (err) {
      setError((err as ApiError).message ?? "Roadmap generation failed.");
    } finally {
      setSubmitting(false);
    }
  };

  const exportRoadmap = () => {
    if (!roadmapResult) {
      return;
    }

    const blob = new Blob([JSON.stringify(roadmapResult, null, 2)], {
      type: "application/json",
    });
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = "skill-bridge-roadmap.json";
    anchor.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="space-y-6">
      <PageSection
        title="Roadmap"
        subtitle="Generate a learning plan from the current profile using either the text profile flow or resume flow."
        action={
          <Button type="button" tone="ghost" onClick={exportRoadmap} disabled={!roadmapResult}>
            Export roadmap
          </Button>
        }
      >
        <div className="grid gap-5 lg:grid-cols-[0.9fr_1.1fr]">
          <div className="space-y-4">
            <ErrorBanner message={error} onDismiss={() => setError(null)} />
            <Field label="Duration (weeks)">
              <TextInput
                type="number"
                min={1}
                value={durationWeeks}
                onChange={(event) => setDurationWeeks(event.target.value)}
              />
            </Field>
            <Field label="Weekly hours">
              <TextInput
                type="number"
                min={1}
                value={weeklyHours}
                onChange={(event) => setWeeklyHours(event.target.value)}
              />
            </Field>
            <Field
              label="Optional PDF for roadmap flow"
              hint="Attach a PDF here to use the resume flow. Leave empty to use the text profile flow."
            >
              <TextInput
                type="file"
                accept="application/pdf"
                onChange={(event) => setResumeFile(event.target.files?.[0] ?? null)}
              />
            </Field>
            <Toggle
              checked={includePaidResources}
              onChange={setIncludePaidResources}
              label="Include paid resources"
            />
            <Button type="button" onClick={submit} disabled={submitting}>
              {submitting ? "Generating..." : "Generate Roadmap"}
            </Button>
          </div>
          <div className="rounded-[28px] bg-gradient-to-br from-sky-900 via-teal-800 to-emerald-700 p-6 text-slate-50">
            <p className="text-xs uppercase tracking-[0.24em] text-sky-100/75">Current profile</p>
            <div className="mt-4 space-y-2 text-sm text-sky-50/90">
              <p>
                <span className="font-semibold text-white">Target role:</span>{" "}
                {profile?.targetRole ?? "Not set"}
              </p>
              <p>
                <span className="font-semibold text-white">Current skills:</span>{" "}
                {(profile?.currentSkills ?? []).join(", ") || "None"}
              </p>
              <p>
                <span className="font-semibold text-white">Analysis coverage:</span>{" "}
                {String(analysisResult?.coverageScore ?? "N/A")}
              </p>
            </div>
          </div>
        </div>
      </PageSection>

      <PageSection title="Roadmap View" subtitle="Baseline coverage, summary, milestones, resources, project, and checkpoints.">
        <RoadmapPanel roadmap={roadmapResult} />
      </PageSection>
    </div>
  );
}
