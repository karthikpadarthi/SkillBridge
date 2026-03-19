"use client";

import type { ReactNode } from "react";
import { AppProvider } from "@/app/context/app-context";

export function Providers({ children }: { children: ReactNode }) {
  return <AppProvider>{children}</AppProvider>;
}
