import { normalizeAuthSession } from "@/app/lib/helpers";
import type { ApiError, AuthSession } from "@/app/lib/types";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/$/, "") ??
  "http://localhost:8080";

async function parseResponse(res: Response) {
  const contentType = res.headers.get("content-type") ?? "";
  const isJson = contentType.includes("application/json");
  const payload = isJson ? await res.json() : await res.text();

  if (!res.ok) {
    const message =
      (typeof payload === "object" &&
        payload !== null &&
        "error" in payload &&
        typeof payload.error === "string" &&
        payload.error) ||
      (typeof payload === "object" &&
        payload !== null &&
        "message" in payload &&
        typeof payload.message === "string" &&
        payload.message) ||
      (typeof payload === "string" && payload) ||
      `Request failed with status ${res.status}`;

    const error: ApiError = { message, status: res.status };

    if (
      typeof window !== "undefined" &&
      (res.status === 401 || res.status === 403)
    ) {
      window.dispatchEvent(
        new CustomEvent("skill-bridge:auth-error", {
          detail: { status: res.status, message },
        }),
      );
    }

    throw error;
  }

  return payload;
}

type RequestOptions = {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  token?: string;
  body?: BodyInit | null;
  headers?: HeadersInit;
  contentType?: string | null;
};

export async function apiRequest<T>(
  path: string,
  { method = "GET", token, body, headers, contentType = "application/json" }: RequestOptions = {},
): Promise<T> {
  const finalHeaders = new Headers(headers);

  if (contentType) {
    finalHeaders.set("Content-Type", contentType);
  }

  if (token) {
    finalHeaders.set("Authorization", `Bearer ${token}`);
  }

  const res = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers: finalHeaders,
    body,
  });

  return parseResponse(res) as Promise<T>;
}

export const authApi = {
  register: async (payload: { email: string; password: string }) =>
    normalizeAuthSession(
      await apiRequest<Record<string, unknown>>("/api/auth/register", {
        method: "POST",
        body: JSON.stringify(payload),
      }),
    ) as AuthSession,
  login: async (payload: { email: string; password: string }) =>
    normalizeAuthSession(
      await apiRequest<Record<string, unknown>>("/api/auth/login", {
        method: "POST",
        body: JSON.stringify(payload),
      }),
    ) as AuthSession,
};

export const appApi = {
  getRoles: (token?: string) => apiRequest<unknown[]>("/api/roles", { token }),
  getMetricsSummary: (token: string) =>
    apiRequest<Record<string, unknown>>("/api/metrics/me/summary", { token }),
  analyzeText: (token: string, payload: Record<string, unknown>) =>
    apiRequest<Record<string, unknown>>("/api/analyze", {
      method: "POST",
      token,
      body: JSON.stringify(payload),
    }),
  analyzeResume: (token: string, payload: FormData) =>
    apiRequest<Record<string, unknown>>("/api/resume/analyze", {
      method: "POST",
      token,
      body: payload,
      contentType: null,
    }),
  generateRoadmap: (token: string, payload: Record<string, unknown>) =>
    apiRequest<Record<string, unknown>>("/api/roadmap/generate", {
      method: "POST",
      token,
      body: JSON.stringify(payload),
    }),
  generateResumeRoadmap: (token: string, payload: FormData) =>
    apiRequest<Record<string, unknown>>("/api/resume/roadmap", {
      method: "POST",
      token,
      body: payload,
      contentType: null,
    }),
  ingestJobs: (token: string, payload: Record<string, unknown>) =>
    apiRequest<Record<string, unknown>>("/api/jobs/ingest", {
      method: "POST",
      token,
      body: JSON.stringify(payload),
    }),
  aggregateJobs: (token: string, role: string) =>
    apiRequest<Record<string, unknown>>(
      `/api/jobs/aggregate/${encodeURIComponent(role)}`,
      {
        method: "POST",
        token,
      },
    ),
  generateInterviewQuestions: (token: string, payload: Record<string, unknown>) =>
    apiRequest<Record<string, unknown>>("/api/interview/questions", {
      method: "POST",
      token,
      body: JSON.stringify(payload),
    }),
  evaluateInterview: (token: string, payload: Record<string, unknown>) =>
    apiRequest<Record<string, unknown>>("/api/interview/evaluate", {
      method: "POST",
      token,
      body: JSON.stringify(payload),
    }),
  getRoadmapHistory: (token: string) =>
    apiRequest<Record<string, unknown>[]>("/api/history/me/roadmaps", { token }),
  getInterviewHistory: (token: string) =>
    apiRequest<Record<string, unknown>[]>("/api/history/me/interviews", { token }),
  getProfileHistory: (token: string) =>
    apiRequest<Record<string, unknown>[]>("/api/history/me/profiles", { token }),
};
