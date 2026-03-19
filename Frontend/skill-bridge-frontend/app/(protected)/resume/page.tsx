"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { AnalysisPanel } from "@/app/components/result-panels";
import { Button, Field, Select, TextInput } from "@/app/components/form-controls";
import { ErrorBanner, LoadingState, PageSection } from "@/app/components/ui";
import { useAppContext } from "@/app/context/app-context";
import { appApi } from "@/app/lib/api";
import { normalizeAnalysisResult, normalizeRoles, parseTags } from "@/app/lib/helpers";
import type { ApiError, ProfileInput } from "@/app/lib/types";

export default function ResumePage() {
  const router = useRouter();
  const { session, analysisResult, profile, saveProfileBundle } = useAppContext();
  const [roles, setRoles] = useState<string[]>([]);
  const [loadingRoles, setLoadingRoles] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [file, setFile] = useState<File | null>(null);
  const [targetRole, setTargetRole] = useState(profile?.targetRole ?? "");
  const [githubText, setGithubText] = useState(profile?.githubText ?? "");
  const [skillsInput, setSkillsInput] = useState(profile?.currentSkills.join(", ") ?? "");

  useEffect(() => {
    if (!session?.jwt) {
      return;
    }

    const loadRoles = async () => {
      try {
        setLoadingRoles(true);
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

    if (!file) {
      setError("A PDF resume is required.");
      return;
    }

    if (file.type !== "application/pdf") {
      setError("Resume upload must be a PDF file.");
      return;
    }

    if (!targetRole) {
      setError("Select a target role.");
      return;
    }

    const formData = new FormData();
    const currentSkills = parseTags(skillsInput);
    formData.append("resumeFile", file);
    formData.append("targetRole", targetRole);
    formData.append("githubLikeText", githubText.trim());
    currentSkills.forEach((skill) => formData.append("currentSkills", skill));

    setSubmitting(true);
    setError(null);

    try {
      const response = await appApi.analyzeResume(session.jwt, formData);
      const nextProfile: ProfileInput = {
        targetRole,
        githubText: githubText.trim(),
        currentSkills,
        resumeFileName: file.name,
      };
      saveProfileBundle({
        profile: nextProfile,
        analysisResult: normalizeAnalysisResult(response),
      });
    } catch (err) {
      setError((err as ApiError).message ?? "Resume analysis failed.");
    } finally {
      setSubmitting(false);
    }
  };

  if (loadingRoles) {
    return <LoadingState label="Loading resume analysis form..." />;
  }

  return (
    <div className="space-y-6">
      <PageSection
        title="Resume Upload + Analysis"
        subtitle="Upload a PDF, set the role target, and generate a resume-based analysis."
        action={
          <Button type="button" tone="ghost" onClick={() => router.push("/roadmap")}>
            Generate roadmap from latest profile
          </Button>
        }
      >
        <div className="grid gap-5 lg:grid-cols-2">
          <div className="space-y-4">
            <ErrorBanner message={error} onDismiss={() => setError(null)} />
            <Field label="Resume PDF" hint="Only PDF files are accepted.">
              <TextInput
                type="file"
                accept="application/pdf"
                onChange={(event) => setFile(event.target.files?.[0] ?? null)}
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
            <Field label="GitHub-like text" hint="Optional profile links, projects, or GitHub summary.">
              <TextInput
                value={githubText}
                onChange={(event) => setGithubText(event.target.value)}
                placeholder="GitHub summary, portfolio notes, repo highlights"
              />
            </Field>
            <Field label="Current skills" hint="Comma-separated tags.">
              <TextInput
                value={skillsInput}
                onChange={(event) => setSkillsInput(event.target.value)}
                placeholder="React, TypeScript, Node.js"
              />
            </Field>
            <Button type="button" onClick={submit} disabled={submitting}>
              {submitting ? "Analyzing..." : "Analyze Resume"}
            </Button>
          </div>

          <div className="rounded-[28px] bg-gradient-to-br from-sky-900 via-teal-800 to-emerald-700 p-6 text-slate-50">
            <p className="text-xs uppercase tracking-[0.24em] text-sky-100/75">Flow notes</p>
            <ul className="mt-4 space-y-3 text-sm text-sky-50/90">
              <li>JWT is attached automatically to protected requests.</li>
              <li>Backend errors surface from the `error` or `message` field.</li>
              <li>Latest profile and analysis are cached in frontend storage for roadmap and interview reuse.</li>
            </ul>
          </div>
        </div>
      </PageSection>

      <PageSection title="Analysis Result" subtitle="Coverage score, skills, recommendations, next step, and mode.">
        <AnalysisPanel result={analysisResult} />
      </PageSection>
    </div>
  );
}
