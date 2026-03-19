"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { AnalysisPanel } from "@/app/components/result-panels";
import { Button, Field, Select, TextArea, TextInput } from "@/app/components/form-controls";
import { ErrorBanner, LoadingState, PageSection } from "@/app/components/ui";
import { useAppContext } from "@/app/context/app-context";
import { appApi } from "@/app/lib/api";
import { normalizeAnalysisResult, normalizeRoles, parseTags } from "@/app/lib/helpers";
import type { ApiError, ProfileInput } from "@/app/lib/types";

export default function AnalysisPage() {
  const router = useRouter();
  const { session, profile, analysisResult, saveProfileBundle } = useAppContext();
  const [roles, setRoles] = useState<string[]>([]);
  const [loadingRoles, setLoadingRoles] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [resumeText, setResumeText] = useState(profile?.resumeText ?? "");
  const [githubText, setGithubText] = useState(profile?.githubText ?? "");
  const [targetRole, setTargetRole] = useState(profile?.targetRole ?? "");
  const [skillsInput, setSkillsInput] = useState(profile?.currentSkills.join(", ") ?? "");

  useEffect(() => {
    if (!session?.jwt) {
      return;
    }

    const loadRoles = async () => {
      try {
        const response = await appApi.getRoles(session.jwt);
        setRoles(normalizeRoles(response));
      } catch (err) {
        setError((err as ApiError).message ?? "Failed to load roles.");
      } finally {
        setLoadingRoles(false);
      }
    };

    void loadRoles();
  }, [session?.jwt]);

  const submit = async () => {
    if (!session?.jwt) {
      return;
    }

    if (!resumeText.trim()) {
      setError("Resume text is required.");
      return;
    }

    if (!targetRole) {
      setError("Select a target role.");
      return;
    }

    const currentSkills = parseTags(skillsInput);

    setSubmitting(true);
    setError(null);

    try {
      const response = await appApi.analyzeText(session.jwt, {
        resumeText: resumeText.trim(),
        githubLikeText: githubText.trim(),
        targetRole,
        currentSkills,
      });
      const nextProfile: ProfileInput = {
        targetRole,
        resumeText: resumeText.trim(),
        githubText: githubText.trim(),
        currentSkills,
      };
      saveProfileBundle({
        profile: nextProfile,
        analysisResult: normalizeAnalysisResult(response),
      });
    } catch (err) {
      setError((err as ApiError).message ?? "Analysis failed.");
    } finally {
      setSubmitting(false);
    }
  };

  if (loadingRoles) {
    return <LoadingState label="Loading analysis workspace..." />;
  }

  return (
    <div className="space-y-6">
      <PageSection
        title="Gap Analysis"
        subtitle="Paste resume text and GitHub-like details to generate a structured gap analysis."
        action={
          <div className="flex gap-3">
            <Button type="button" tone="ghost" onClick={() => router.push("/roadmap")}>
              Generate Roadmap
            </Button>
            <Button type="button" tone="secondary" onClick={() => router.push("/interview/questions")}>
              Generate Questions
            </Button>
          </div>
        }
      >
        <div className="grid gap-5 lg:grid-cols-2">
          <div className="space-y-4">
            <ErrorBanner message={error} onDismiss={() => setError(null)} />
            <Field label="Resume text">
              <TextArea
                rows={10}
                value={resumeText}
                onChange={(event) => setResumeText(event.target.value)}
                placeholder="Paste the resume content here..."
              />
            </Field>
            <Field label="GitHub-like text">
              <TextArea
                rows={6}
                value={githubText}
                onChange={(event) => setGithubText(event.target.value)}
                placeholder="Projects, repositories, profile summary, links..."
              />
            </Field>
            <Field label="Target role">
              <Select value={targetRole} onChange={(event) => setTargetRole(event.target.value)}>
                <option value="">Select a role</option>
                {roles.map((role) => (
                  <option key={role} value={role}>
                    {role}
                  </option>
                ))}
              </Select>
            </Field>
            <Field label="Current skills">
              <TextInput
                value={skillsInput}
                onChange={(event) => setSkillsInput(event.target.value)}
                placeholder="React, SQL, Data structures"
              />
            </Field>
            <Button type="button" onClick={submit} disabled={submitting}>
              {submitting ? "Analyzing..." : "Run Gap Analysis"}
            </Button>
          </div>
          <div className="rounded-[28px] border border-slate-200 bg-white p-6">
            <p className="text-sm leading-7 text-slate-600">
              This page keeps the profile object reusable across the roadmap and interview flows. Once analysis completes, the current target role, pasted text, GitHub summary, and current skills remain available through shared frontend state and local storage.
            </p>
          </div>
        </div>
      </PageSection>

      <PageSection title="Full Analysis Response" subtitle="Includes skill evidence and actionable recommendations.">
        <AnalysisPanel result={analysisResult} />
      </PageSection>
    </div>
  );
}
