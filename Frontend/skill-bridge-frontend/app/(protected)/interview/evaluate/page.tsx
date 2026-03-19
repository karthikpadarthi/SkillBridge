"use client";

import { useState } from "react";
import { InterviewEvaluationPanel } from "@/app/components/result-panels";
import { Button, Field, TextArea } from "@/app/components/form-controls";
import { ErrorBanner, PageSection } from "@/app/components/ui";
import { useAppContext } from "@/app/context/app-context";
import { appApi } from "@/app/lib/api";
import { normalizeInterviewEvaluation } from "@/app/lib/helpers";
import type { ApiError, InterviewEvaluation } from "@/app/lib/types";

export default function InterviewEvaluationPage() {
  const {
    session,
    profile,
    interviewState,
    saveInterviewState,
    clearInterviewState,
  } = useAppContext();
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const questions = interviewState?.questions ?? [];
  const answers = interviewState?.answers ?? [];
  const evaluation = (interviewState?.evaluation as InterviewEvaluation | null) ?? null;

  const updateAnswer = (index: number, value: string) => {
    if (!interviewState) {
      return;
    }

    const nextAnswers = [...answers];
    nextAnswers[index] = value;
    saveInterviewState({
      ...interviewState,
      answers: nextAnswers,
    });
  };

  const submit = async () => {
    if (!session?.jwt || !interviewState) {
      return;
    }

    if (answers.some((item) => !item.trim())) {
      setError("Answer every generated question before submitting.");
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      const response = await appApi.evaluateInterview(session.jwt, {
        targetRole: profile?.targetRole ?? "",
        answers: questions.map((question, index) => ({
          questionId: question.questionId ?? `question-${index + 1}`,
          question: question.question ?? "",
          expectedFocus: question.expectedFocus ?? "",
          userAnswer: answers[index] ?? "",
          skill: question.skill ?? "",
          difficulty: question.difficulty ?? "",
        })),
      });
      saveInterviewState({
        ...interviewState,
        evaluation: normalizeInterviewEvaluation(response),
      });
    } catch (err) {
      setError((err as ApiError).message ?? "Interview evaluation failed.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <PageSection
        title="Interview Evaluation"
        subtitle="Capture answers and submit them for scoring and feedback."
        action={
          <div className="flex gap-3">
            <Button
              type="button"
              tone="ghost"
              onClick={() => {
                if (!interviewState) {
                  return;
                }

                saveInterviewState({
                  ...interviewState,
                  answers: questions.map(() => ""),
                  evaluation: null,
                });
              }}
              disabled={!questions.length}
            >
              Retry attempt
            </Button>
            <Button type="button" tone="secondary" onClick={clearInterviewState}>
              Clear session
            </Button>
          </div>
        }
      >
        <ErrorBanner message={error} onDismiss={() => setError(null)} />
        <div className="space-y-4">
          {questions.length ? (
            questions.map((question, index) => (
              <div key={`${question.question ?? "q"}-${index}`} className="rounded-3xl border border-slate-200 bg-white p-5">
                <p className="text-sm font-semibold uppercase tracking-[0.2em] text-slate-500">
                  {question.skill ?? "General"} / {question.difficulty ?? "Mixed"}
                </p>
                <p className="mt-3 text-base font-medium text-slate-900">{question.question}</p>
                <Field label={`Answer ${index + 1}`}>
                  <TextArea
                    rows={5}
                    value={answers[index] ?? ""}
                    onChange={(event) => updateAnswer(index, event.target.value)}
                    placeholder="Write your answer here..."
                  />
                </Field>
              </div>
            ))
          ) : (
            <p className="text-sm text-slate-500">Generate a question set first.</p>
          )}
        </div>
        <Button type="button" className="mt-5" onClick={submit} disabled={submitting || !questions.length}>
          {submitting ? "Evaluating..." : "Evaluate Answers"}
        </Button>
      </PageSection>

      <PageSection title="Evaluation Result" subtitle="Overall score, summary, strengths, improvements, and next actions.">
        <InterviewEvaluationPanel evaluation={evaluation} />
      </PageSection>
    </div>
  );
}
