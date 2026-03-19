"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import {
  AUTH_STORAGE_KEY,
  INTERVIEW_STORAGE_KEY,
  PROFILE_STORAGE_KEY,
  readStorage,
  removeStorage,
  writeStorage,
} from "@/app/lib/storage";
import type {
  AnalysisResult,
  AuthSession,
  InterviewQuestion,
  Nullable,
  ProfileInput,
  RoadmapResult,
} from "@/app/lib/types";

type SavedInterview = {
  questions: InterviewQuestion[];
  answers: string[];
  evaluation?: Record<string, unknown> | null;
};

type AppContextValue = {
  hydrated: boolean;
  session: Nullable<AuthSession>;
  profile: Nullable<ProfileInput>;
  analysisResult: Nullable<AnalysisResult>;
  roadmapResult: Nullable<RoadmapResult>;
  interviewState: Nullable<SavedInterview>;
  setSession: (session: AuthSession) => void;
  logout: () => void;
  saveProfileBundle: (payload: {
    profile: ProfileInput;
    analysisResult?: Nullable<AnalysisResult>;
  }) => void;
  saveRoadmap: (roadmap: RoadmapResult) => void;
  saveInterviewState: (value: SavedInterview) => void;
  clearInterviewState: () => void;
};

const AppContext = createContext<AppContextValue | undefined>(undefined);

type StoredProfileBundle = {
  profile: Nullable<ProfileInput>;
  analysisResult: Nullable<AnalysisResult>;
  roadmapResult: Nullable<RoadmapResult>;
};

const EMPTY_PROFILE_BUNDLE: StoredProfileBundle = {
  profile: null,
  analysisResult: null,
  roadmapResult: null,
};

export function AppProvider({ children }: { children: ReactNode }) {
  const [hydrated, setHydrated] = useState(false);
  const [session, setSessionState] = useState<Nullable<AuthSession>>(null);
  const [profileBundle, setProfileBundle] =
    useState<StoredProfileBundle>(EMPTY_PROFILE_BUNDLE);
  const [interviewState, setInterviewState] = useState<Nullable<SavedInterview>>(
    null,
  );

  useEffect(() => {
    setSessionState(readStorage<AuthSession | null>(AUTH_STORAGE_KEY, null));
    setProfileBundle(
      readStorage<StoredProfileBundle>(PROFILE_STORAGE_KEY, EMPTY_PROFILE_BUNDLE),
    );
    setInterviewState(
      readStorage<SavedInterview | null>(INTERVIEW_STORAGE_KEY, null),
    );
    setHydrated(true);
  }, []);

  useEffect(() => {
    const handleAuthError = () => {
      setSessionState(null);
      removeStorage(AUTH_STORAGE_KEY);
    };

    window.addEventListener("skill-bridge:auth-error", handleAuthError);
    return () =>
      window.removeEventListener("skill-bridge:auth-error", handleAuthError);
  }, []);

  const setSession = useCallback((nextSession: AuthSession) => {
    setSessionState(nextSession);
    writeStorage(AUTH_STORAGE_KEY, nextSession);
  }, []);

  const logout = useCallback(() => {
    setSessionState(null);
    setProfileBundle(EMPTY_PROFILE_BUNDLE);
    setInterviewState(null);
    removeStorage(AUTH_STORAGE_KEY);
    removeStorage(PROFILE_STORAGE_KEY);
    removeStorage(INTERVIEW_STORAGE_KEY);
  }, []);

  const saveProfileBundle = useCallback(({
    profile,
    analysisResult = profileBundle.analysisResult,
  }: {
    profile: ProfileInput;
    analysisResult?: Nullable<AnalysisResult>;
  }) => {
    const nextValue = {
      ...profileBundle,
      profile,
      analysisResult,
    };
    setProfileBundle(nextValue);
    writeStorage(PROFILE_STORAGE_KEY, nextValue);
  }, [profileBundle]);

  const saveRoadmap = useCallback((roadmap: RoadmapResult) => {
    setProfileBundle((current) => {
      const nextValue = { ...current, roadmapResult: roadmap };
      writeStorage(PROFILE_STORAGE_KEY, nextValue);
      return nextValue;
    });
  }, []);

  const saveInterviewState = useCallback((value: SavedInterview) => {
    setInterviewState(value);
    writeStorage(INTERVIEW_STORAGE_KEY, value);
  }, []);

  const clearInterviewState = useCallback(() => {
    setInterviewState(null);
    removeStorage(INTERVIEW_STORAGE_KEY);
  }, []);

  const value = useMemo<AppContextValue>(
    () => ({
      hydrated,
      session,
      profile: profileBundle.profile,
      analysisResult: profileBundle.analysisResult,
      roadmapResult: profileBundle.roadmapResult,
      interviewState,
      setSession,
      logout,
      saveProfileBundle,
      saveRoadmap,
      saveInterviewState,
      clearInterviewState,
    }),
    [
      clearInterviewState,
      hydrated,
      interviewState,
      logout,
      profileBundle,
      saveInterviewState,
      saveProfileBundle,
      saveRoadmap,
      session,
      setSession,
    ],
  );

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export function useAppContext() {
  const context = useContext(AppContext);

  if (!context) {
    throw new Error("useAppContext must be used within AppProvider");
  }

  return context;
}
