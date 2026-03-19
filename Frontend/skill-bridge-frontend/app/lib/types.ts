export type Nullable<T> = T | null;

export type AuthSession = {
  jwt: string;
  userId: string;
  email?: string;
};

export type ApiError = {
  message: string;
  status?: number;
};

export type RoleOption = {
  id?: string;
  roleName?: string;
  name?: string;
  title?: string;
};

export type ProfileInput = {
  targetRole: string;
  resumeText?: string;
  githubText?: string;
  currentSkills: string[];
  resumeFileName?: string;
};

export type AnalysisResult = {
  coverageScore?: number;
  extractedSkills?: string[];
  matchedSkills?: string[];
  missingSkills?: string[];
  prioritizedMissingSkills?: string[];
  prioritizedMissingSkillDetails?: Array<{
    skill: string;
    urgency?: string;
    marketDemandScore?: number;
  }>;
  recommendations?: string[];
  nextStep?: string;
  analysisMode?: string;
  skillEvidence?: Record<string, string[]>;
  [key: string]: unknown;
};

export type RoadmapMilestone = {
  week?: number;
  skill?: string;
  title?: string;
  objective?: string;
  resources?: Array<{
    title: string;
    provider?: string;
    url?: string;
    free?: boolean;
    estimatedHours?: number;
  }>;
  handsOnProject?: string;
  checkpoint?: string;
};

export type RoadmapResult = {
  baselineCoverage?: number;
  summary?: string;
  weeks?: RoadmapMilestone[];
  milestones?: RoadmapMilestone[];
  project?: string;
  checkpoint?: string;
  [key: string]: unknown;
};

export type InterviewQuestion = {
  questionId?: string;
  skill?: string;
  difficulty?: string;
  type?: string;
  question?: string;
  expectedFocus?: string;
  [key: string]: unknown;
};

export type InterviewEvaluation = {
  overallScore?: number;
  summary?: string;
  strengths?: string[];
  improvements?: string[];
  nextActions?: string[];
  perQuestionScores?: Array<{
    questionId?: string;
    skill?: string;
    relevance?: number;
    technical?: number;
    clarity?: number;
    overall?: number;
    strengths?: string[];
    improvements?: string[];
    feedback?: string;
  }>;
  [key: string]: unknown;
};
