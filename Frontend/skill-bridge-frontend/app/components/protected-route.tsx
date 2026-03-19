"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";
import type { ReactNode } from "react";
import { useAppContext } from "@/app/context/app-context";
import { LoadingState } from "@/app/components/ui";

export function ProtectedRoute({ children }: { children: ReactNode }) {
  const router = useRouter();
  const { hydrated, session } = useAppContext();

  useEffect(() => {
    if (hydrated && !session) {
      router.replace("/auth");
    }
  }, [hydrated, router, session]);

  if (!hydrated || !session) {
    return <LoadingState label="Checking your session..." />;
  }

  return <>{children}</>;
}
