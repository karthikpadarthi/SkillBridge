"use client";

import type { ReactNode } from "react";
import { AppShell } from "@/app/components/app-shell";
import { ProtectedRoute } from "@/app/components/protected-route";

export default function ProtectedLayout({ children }: { children: ReactNode }) {
  return (
    <ProtectedRoute>
      <AppShell>{children}</AppShell>
    </ProtectedRoute>
  );
}
