"use client";

import { useRouter } from "next/navigation";
import { useMemo, useState } from "react";
import { InterviewQuestionsPanel } from "@/app/components/result-panels";
import { Button, Field, Select, TextInput } from "@/app/components/form-controls";
import { ErrorBanner, PageSection } from "@/app/components/ui";
import { useAppContext } from "@/app/context/app-context";
import { appApi } from "@/app/lib/api";
import { normalizeInterviewQuestions, parseTags } from "@/app/lib/helpers";
import type { ApiError } from "@/app/lib/types";

export default function InterviewQuestionsPage() {
  const router = useRouter();
  const { session, profile, analysisResult, interviewState, saveInterviewState } =
    useAppContext();
  const [questionCount, setQuestionCount] = useState("5");
  const [difficulty, setDifficulty] = useState("mixed");
  const [addedSkills, setAddedSkills] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const existingQuestions = interviewState?.questions ?? [];
  const defaultSkills = useMemo(() => {
    const prioritized = analysisResult?.prioritizedMissingSkills ?? [];
    return prioritized.slice(0, 5).join(", ");
  }, [analysisResult?.prioritizedMissingSkills]);

  const submit = async () => {
    if (!session?.jwt) {
      return;
    }

    if (!profile?.targetRole) {
      setError("A profile is required before generating interview questions.");
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      const skillList = parseTags(addedSkills || defaultSkills);
      const response = await appApi.generateInterviewQuestions(session.jwt, {
        profile: {
          targetRole: profile.targetRole,
          resumeText: profile.resumeText ?? "",
          githubLikeText: profile.githubText ?? "",
          currentSkills: profile.currentSkills,
        },
        newlyAddedSkills: skillList,
        questionCount: Number(questionCount),
        preferredDifficulty: difficulty,
      });
      const questions = normalizeInterviewQuestions(response);
      saveInterviewState({
        questions,
        answers: questions.map(() => ""),
        evaluation: null,
      });
    } catch (err) {
      setError((err as ApiError).message ?? "Question generation failed.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <PageSection
        title="Interview Questions"
        subtitle="Generate a question set from the shared profile and newly added skills."
        action={
          <Button
            type="button"
            tone="secondary"
            onClick={() => router.push("/interview/evaluate")}
            disabled={!existingQuestions.length}
          >
            Start answer session
          </Button>
        }
      >
        <div className="grid gap-5 lg:grid-cols-[0.85fr_1.15fr]">
          <div className="space-y-4">
            <ErrorBanner message={error} onDismiss={() => setError(null)} />
            <Field label="Question count">
              <TextInput
                type="number"
                min={1}
                max={20}
                value={questionCount}
                onChange={(event) => setQuestionCount(event.target.value)}
              />
            </Field>
            <Field label="Preferred difficulty">
              <Select value={difficulty} onChange={(event) => setDifficulty(event.target.value)}>
                <option value="easy">Easy</option>
                <option value="mixed">Mixed</option>
                <option value="hard">Hard</option>
              </Select>
            </Field>
            <Field label="Newly added skills" hint={`Suggested: ${defaultSkills || "Use current profile"}`}>
              <TextInput
                value={addedSkills}
                onChange={(event) => setAddedSkills(event.target.value)}
                placeholder="System design, SQL optimization, testing"
              />
            </Field>
            <Button type="button" onClick={submit} disabled={submitting}>
              {submitting ? "Generating..." : "Generate Questions"}
            </Button>
          </div>
          <div className="rounded-[28px] bg-gradient-to-br from-sky-900 via-teal-800 to-emerald-700 p-6 text-slate-50">
            <p className="text-xs uppercase tracking-[0.24em] text-sky-100/75">Profile source</p>
            <p className="mt-3 text-sm text-sky-50/90">
              Target role: <span className="font-semibold text-white">{profile?.targetRole ?? "Not set"}</span>
            </p>
            <p className="mt-2 text-sm text-sky-50/90">
              Current skills: {(profile?.currentSkills ?? []).join(", ") || "None"}
            </p>
          </div>
        </div>
      </PageSection>

      <PageSection title="Generated Question Set" subtitle="Skill, difficulty, type, question, and expected focus.">
        <InterviewQuestionsPanel questions={existingQuestions} />
      </PageSection>
    </div>
  );
}
