"use client";

import type {
  AnalysisResult,
  InterviewEvaluation,
  InterviewQuestion,
  RoadmapResult,
} from "@/app/lib/types";
import { EmptyState, PillList, StatCard } from "@/app/components/ui";

export function AnalysisPanel({ result }: { result?: AnalysisResult | null }) {
  if (!result) {
    return (
      <EmptyState
        title="No analysis yet"
        body="Submit a resume upload or text profile to render extracted skills, gaps, evidence, and next steps."
      />
    );
  }

  const evidenceEntries = Object.entries(result.skillEvidence ?? {});

  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-3">
        <StatCard
          label="Coverage Score"
          value={result.coverageScore ?? "N/A"}
          hint="Overall alignment with the target role."
        />
        <StatCard
          label="Next Step"
          value={result.nextStep ?? "Review"}
          hint="Backend-suggested action."
        />
        <StatCard
          label="Mode"
          value={result.analysisMode ?? "Unknown"}
          hint="AI or fallback analysis mode."
        />
      </div>

      <div className="grid gap-5 lg:grid-cols-2">
        <div className="space-y-4">
          <div>
            <h3 className="mb-2 text-sm font-semibold text-slate-900">Extracted skills</h3>
            <PillList items={result.extractedSkills} />
          </div>
          <div>
            <h3 className="mb-2 text-sm font-semibold text-slate-900">Matched skills</h3>
            <PillList items={result.matchedSkills} tone="green" />
          </div>
          <div>
            <h3 className="mb-2 text-sm font-semibold text-slate-900">Missing skills</h3>
            <PillList items={result.missingSkills} tone="rose" />
          </div>
          <div>
            <h3 className="mb-2 text-sm font-semibold text-slate-900">
              Prioritized missing skills
            </h3>
            <PillList items={result.prioritizedMissingSkills} tone="amber" />
          </div>
        </div>

        <div className="space-y-4">
          <div>
            <h3 className="mb-2 text-sm font-semibold text-slate-900">Recommendations</h3>
            <ul className="space-y-2 text-sm text-slate-700">
              {(result.recommendations ?? []).map((item) => (
                <li key={item} className="rounded-2xl bg-slate-100 px-4 py-3">
                  {item}
                </li>
              ))}
            </ul>
          </div>

          {evidenceEntries.length ? (
            <div>
              <h3 className="mb-2 text-sm font-semibold text-slate-900">Skill evidence</h3>
              <div className="space-y-3">
                {evidenceEntries.map(([skill, evidence]) => (
                  <div key={skill} className="rounded-2xl border border-slate-200 bg-white p-4">
                    <p className="font-medium text-slate-900">{skill}</p>
                    <ul className="mt-2 list-disc space-y-1 pl-5 text-sm text-slate-600">
                      {evidence.map((item) => (
                        <li key={item}>{item}</li>
                      ))}
                    </ul>
                  </div>
                ))}
              </div>
            </div>
          ) : null}
        </div>
      </div>
    </div>
  );
}

export function RoadmapPanel({ roadmap }: { roadmap?: RoadmapResult | null }) {
  if (!roadmap) {
    return (
      <EmptyState
        title="No roadmap yet"
        body="Generate a roadmap from either a resume upload or text analysis profile to view milestones and resources."
      />
    );
  }

  const milestones = roadmap.weeks ?? roadmap.milestones ?? [];

  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-2">
        <StatCard
          label="Baseline Coverage"
          value={roadmap.baselineCoverage ?? "N/A"}
        />
        <StatCard
          label="Checkpoint"
          value={roadmap.checkpoint ?? "Review final milestone"}
        />
      </div>
      <div className="rounded-3xl bg-gradient-to-br from-sky-900 via-teal-800 to-emerald-700 p-5 text-slate-50">
        <p className="text-xs uppercase tracking-[0.24em] text-sky-100/75">Summary</p>
        <p className="mt-3 text-sm leading-6 text-sky-50/95">{roadmap.summary ?? "No summary returned."}</p>
      </div>
      <div className="space-y-3">
        {milestones.map((milestone, index) => (
          <div key={`${milestone.title ?? "week"}-${index}`} className="rounded-3xl border border-slate-200 bg-white p-5">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <h3 className="text-lg font-semibold text-slate-900">
                Week {milestone.week ?? index + 1}: {milestone.title ?? milestone.objective ?? "Milestone"}
              </h3>
              <span className="rounded-full bg-amber-100 px-3 py-1 text-xs font-semibold uppercase tracking-[0.2em] text-amber-700">
                Checkpoint
              </span>
            </div>
            <p className="mt-3 text-sm text-slate-600">{milestone.objective ?? "Objective not supplied."}</p>
            <div className="mt-4">
              <p className="mb-2 text-sm font-medium text-slate-900">Resources</p>
              {milestone.resources?.length ? (
                <div className="space-y-3">
                  {milestone.resources.map((resource) => (
                    <div
                      key={`${resource.title}-${resource.url ?? resource.provider ?? "resource"}`}
                      className="rounded-2xl bg-emerald-50 p-4 text-sm text-slate-700"
                    >
                      <p className="font-medium text-slate-900">{resource.title}</p>
                      <p className="mt-1 text-slate-600">
                        {resource.provider ?? "Provider not specified"}
                        {typeof resource.estimatedHours === "number"
                          ? ` • ${resource.estimatedHours}h`
                          : ""}
                        {typeof resource.free === "boolean"
                          ? resource.free
                            ? " • Free"
                            : " • Paid"
                          : ""}
                      </p>
                      {resource.url ? (
                        <a
                          href={resource.url}
                          target="_blank"
                          rel="noreferrer"
                          className="mt-2 inline-flex text-sm font-semibold text-teal-700 underline"
                        >
                          Open resource
                        </a>
                      ) : null}
                    </div>
                  ))}
                </div>
              ) : (
                <PillList items={[]} tone="green" />
              )}
            </div>
            {milestone.handsOnProject ? (
              <p className="mt-4 text-sm text-slate-700">
                <span className="font-medium text-slate-900">Hands-on project:</span>{" "}
                {milestone.handsOnProject}
              </p>
            ) : null}
            {milestone.checkpoint ? (
              <p className="mt-4 text-sm text-slate-700">
                <span className="font-medium text-slate-900">Checkpoint:</span> {milestone.checkpoint}
              </p>
            ) : null}
          </div>
        ))}
      </div>
      {roadmap.project ? (
        <div className="rounded-3xl border border-slate-200 bg-white p-5">
          <h3 className="text-lg font-semibold text-slate-900">Hands-on project</h3>
          <p className="mt-3 text-sm text-slate-600">{roadmap.project}</p>
        </div>
      ) : null}
    </div>
  );
}

export function InterviewQuestionsPanel({
  questions,
}: {
  questions?: InterviewQuestion[] | null;
}) {
  if (!questions?.length) {
    return (
      <EmptyState
        title="No question set yet"
        body="Generate interview questions from your current role profile and selected skill gaps."
      />
    );
  }

  return (
    <div className="space-y-4">
      {questions.map((question, index) => (
        <article
          key={`${question.question ?? "question"}-${index}`}
          className="rounded-3xl border border-slate-200 bg-white p-5"
        >
          <div className="flex flex-wrap gap-2">
            <span className="rounded-full bg-slate-950 px-3 py-1 text-xs font-semibold uppercase tracking-[0.2em] text-white">
              {question.skill ?? "General"}
            </span>
            <span className="rounded-full bg-amber-100 px-3 py-1 text-xs font-semibold uppercase tracking-[0.2em] text-amber-700">
              {question.difficulty ?? "Mixed"}
            </span>
            <span className="rounded-full bg-sky-100 px-3 py-1 text-xs font-semibold uppercase tracking-[0.2em] text-sky-700">
              {question.type ?? "Question"}
            </span>
          </div>
          <p className="mt-4 text-base font-medium text-slate-900">{question.question}</p>
          <p className="mt-3 text-sm text-slate-600">
            <span className="font-semibold text-slate-900">Expected focus:</span>{" "}
            {question.expectedFocus ?? "Not provided."}
          </p>
        </article>
      ))}
    </div>
  );
}

export function InterviewEvaluationPanel({
  evaluation,
}: {
  evaluation?: InterviewEvaluation | null;
}) {
  if (!evaluation) {
    return (
      <EmptyState
        title="No evaluation yet"
        body="Answer the generated question set and submit it to receive scoring and improvement guidance."
      />
    );
  }

  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-2">
        <StatCard label="Overall Score" value={evaluation.overallScore ?? "N/A"} />
        <StatCard label="Summary" value={evaluation.summary ?? "No summary"} />
      </div>

      <div className="grid gap-5 lg:grid-cols-2">
        <div>
          <h3 className="mb-3 text-sm font-semibold text-slate-900">Strengths</h3>
          <PillList items={evaluation.strengths} tone="green" />
        </div>
        <div>
          <h3 className="mb-3 text-sm font-semibold text-slate-900">Improvements</h3>
          <PillList items={evaluation.improvements} tone="rose" />
        </div>
      </div>

      <div className="space-y-3">
        {(evaluation.perQuestionScores ?? []).map((item, index) => (
          <div key={`score-${index}`} className="rounded-3xl border border-slate-200 bg-white p-5">
            <h4 className="text-sm font-semibold uppercase tracking-[0.2em] text-slate-500">
              Question {index + 1}
            </h4>
            <div className="mt-3 grid gap-3 md:grid-cols-4">
              <ScoreBadge label="Relevance" value={item.relevance} />
              <ScoreBadge label="Technical" value={item.technical} />
              <ScoreBadge label="Clarity" value={item.clarity} />
              <ScoreBadge label="Overall" value={item.overall} />
            </div>
            {item.feedback ? (
              <p className="mt-3 text-sm text-slate-600">{item.feedback}</p>
            ) : null}
          </div>
        ))}
      </div>

      <div>
        <h3 className="mb-3 text-sm font-semibold text-slate-900">Next actions</h3>
        <PillList items={evaluation.nextActions} tone="amber" />
      </div>
    </div>
  );
}

function ScoreBadge({ label, value }: { label: string; value?: number }) {
  return (
    <div className="rounded-2xl bg-slate-100 p-3">
      <p className="text-xs uppercase tracking-[0.2em] text-slate-500">{label}</p>
      <p className="mt-2 text-xl font-semibold text-slate-900">{value ?? "N/A"}</p>
    </div>
  );
}
