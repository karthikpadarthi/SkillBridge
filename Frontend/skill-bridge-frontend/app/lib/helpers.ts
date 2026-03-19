import type {
  AnalysisResult,
  InterviewEvaluation,
  InterviewQuestion,
  RoleOption,
  RoadmapResult,
} from "@/app/lib/types";

export function normalizeRoles(items: unknown[]): string[] {
  return items
    .map((item) => {
      const role = item as RoleOption;
      return role.roleName ?? role.name ?? role.title ?? "";
    })
    .filter(Boolean);
}

export function parseTags(value: string): string[] {
  return value
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);
}

export function compactNumber(value: unknown) {
  if (typeof value === "number") {
    return value;
  }

  if (typeof value === "string" && value.trim()) {
    return value;
  }

  return "N/A";
}

export function safeArray(value: unknown): string[] {
  return Array.isArray(value)
    ? value.filter((item): item is string => typeof item === "string")
    : [];
}

export function normalizeAuthSession(payload: Record<string, unknown>) {
  return {
    jwt: String(payload.token ?? payload.jwt ?? ""),
    userId: String(payload.userId ?? ""),
    email:
      typeof payload.email === "string" && payload.email.trim()
        ? payload.email
        : undefined,
  };
}

export function normalizeAnalysisResult(
  response: Record<string, unknown>,
): AnalysisResult {
  const evidenceItems = Array.isArray(response.extractedSkillEvidence)
    ? response.extractedSkillEvidence
    : [];
  const skillEvidence = evidenceItems.reduce<Record<string, string[]>>(
    (acc, item) => {
      if (!item || typeof item !== "object") {
        return acc;
      }

      const skill = String((item as { skill?: unknown }).skill ?? "").trim();
      const source = String((item as { source?: unknown }).source ?? "").trim();
      const confidence = (item as { confidence?: unknown }).confidence;

      if (!skill) {
        return acc;
      }

      const detail = source
        ? `${source}${typeof confidence === "number" ? ` (${confidence})` : ""}`
        : typeof confidence === "number"
          ? `Confidence ${confidence}`
          : "Detected";

      acc[skill] = [...(acc[skill] ?? []), detail];
      return acc;
    },
    {},
  );

  const prioritizedMissingSkillDetails = Array.isArray(response.prioritizedMissingSkills)
    ? response.prioritizedMissingSkills
        .filter((item): item is Record<string, unknown> => !!item && typeof item === "object")
        .map((item) => ({
          skill: String(item.skill ?? ""),
          urgency: typeof item.urgency === "string" ? item.urgency : undefined,
          marketDemandScore:
            typeof item.marketDemandScore === "number"
              ? item.marketDemandScore
              : undefined,
        }))
        .filter((item) => item.skill)
    : [];

  return {
    ...response,
    extractedSkills: safeArray(response.extractedSkills),
    matchedSkills: safeArray(response.matchedSkills),
    missingSkills: safeArray(response.missingSkills),
    recommendations: safeArray(response.recommendations),
    prioritizedMissingSkills: prioritizedMissingSkillDetails.map((item) =>
      item.urgency
        ? `${item.skill} (${item.urgency}${typeof item.marketDemandScore === "number" ? `, ${item.marketDemandScore}` : ""})`
        : item.skill,
    ),
    prioritizedMissingSkillDetails,
    skillEvidence,
  };
}

export function normalizeRoadmapResult(
  response: Record<string, unknown>,
): RoadmapResult {
  const milestones = Array.isArray(response.milestones)
    ? response.milestones
        .filter((item): item is Record<string, unknown> => !!item && typeof item === "object")
        .map((item) => ({
          week: typeof item.week === "number" ? item.week : undefined,
          skill: typeof item.skill === "string" ? item.skill : undefined,
          title:
            typeof item.title === "string"
              ? item.title
              : typeof item.skill === "string"
                ? item.skill
                : undefined,
          objective: typeof item.objective === "string" ? item.objective : undefined,
          handsOnProject:
            typeof item.handsOnProject === "string"
              ? item.handsOnProject
              : undefined,
          checkpoint: typeof item.checkpoint === "string" ? item.checkpoint : undefined,
          resources: Array.isArray(item.resources)
            ? item.resources
                .filter(
                  (resource): resource is Record<string, unknown> =>
                    !!resource && typeof resource === "object",
                )
                .map((resource) => ({
                  title: String(resource.title ?? "Resource"),
                  provider:
                    typeof resource.provider === "string"
                      ? resource.provider
                      : undefined,
                  url: typeof resource.url === "string" ? resource.url : undefined,
                  free:
                    typeof resource.free === "boolean" ? resource.free : undefined,
                  estimatedHours:
                    typeof resource.estimatedHours === "number"
                      ? resource.estimatedHours
                      : undefined,
                }))
            : [],
        }))
    : [];

  return {
    ...response,
    baselineCoverage:
      typeof response.baselineCoverageScore === "number"
        ? response.baselineCoverageScore
        : typeof response.baselineCoverage === "number"
          ? response.baselineCoverage
          : undefined,
    milestones,
    project:
      milestones.find((item) => item.handsOnProject)?.handsOnProject ??
      (typeof response.project === "string" ? response.project : undefined),
    checkpoint:
      milestones.at(-1)?.checkpoint ??
      (typeof response.checkpoint === "string" ? response.checkpoint : undefined),
  };
}

export function normalizeInterviewQuestions(
  response: Record<string, unknown>,
): InterviewQuestion[] {
  const questions = Array.isArray(response.questions) ? response.questions : [];
  return questions.filter(
    (item): item is InterviewQuestion => !!item && typeof item === "object",
  );
}

export function normalizeInterviewEvaluation(
  response: Record<string, unknown>,
): InterviewEvaluation {
  const evaluations = Array.isArray(response.evaluations) ? response.evaluations : [];
  const perQuestionScores = evaluations
    .filter((item): item is Record<string, unknown> => !!item && typeof item === "object")
    .map((item) => ({
      questionId:
        typeof item.questionId === "string" ? item.questionId : undefined,
      skill: typeof item.skill === "string" ? item.skill : undefined,
      relevance:
        typeof item.relevanceScore === "number" ? item.relevanceScore : undefined,
      technical:
        typeof item.technicalScore === "number" ? item.technicalScore : undefined,
      clarity:
        typeof item.clarityScore === "number" ? item.clarityScore : undefined,
      overall:
        typeof item.overallScore === "number" ? item.overallScore : undefined,
      strengths: safeArray(item.strengths),
      improvements: safeArray(item.improvements),
      feedback: typeof item.feedback === "string" ? item.feedback : undefined,
    }));

  return {
    ...response,
    strengths: [...new Set(perQuestionScores.flatMap((item) => item.strengths ?? []))],
    improvements: [
      ...new Set(perQuestionScores.flatMap((item) => item.improvements ?? [])),
    ],
    perQuestionScores,
  };
}
