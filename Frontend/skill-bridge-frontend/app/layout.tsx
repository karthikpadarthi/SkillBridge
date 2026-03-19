import type { Metadata } from "next";
import "./globals.css";
import { Providers } from "@/app/components/providers";

export const metadata: Metadata = {
  title: "Skill Bridge",
  description:
    "Career transition frontend for auth, analysis, roadmaps, job intelligence, interviews, and progress tracking.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="antialiased">
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
