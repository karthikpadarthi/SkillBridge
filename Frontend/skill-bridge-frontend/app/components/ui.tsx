"use client";

import type { ReactNode } from "react";

export function PageSection({
  title,
  subtitle,
  action,
  children,
}: {
  title: string;
  subtitle?: string;
  action?: ReactNode;
  children: ReactNode;
}) {
  return (
    <section className="rounded-[28px] border border-[#eadfcb] bg-[#fbf5ea]/90 p-6 shadow-[0_24px_80px_rgba(64,88,104,0.12)] backdrop-blur">
      <div className="mb-5 flex flex-wrap items-start justify-between gap-3">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">{title}</h2>
          {subtitle ? (
            <p className="mt-1 text-sm text-slate-600">{subtitle}</p>
          ) : null}
        </div>
        {action}
      </div>
      {children}
    </section>
  );
}

export function StatCard({
  label,
  value,
  hint,
}: {
  label: string;
  value: ReactNode;
  hint?: string;
}) {
  return (
    <div className="rounded-3xl border border-emerald-900/30 bg-gradient-to-br from-sky-900 via-teal-800 to-emerald-700 p-5 text-white">
      <p className="text-xs uppercase tracking-[0.24em] text-sky-100/80">{label}</p>
      <div className="mt-3 text-3xl font-semibold">{value}</div>
      {hint ? <p className="mt-2 text-sm text-emerald-50/85">{hint}</p> : null}
    </div>
  );
}

export function EmptyState({
  title,
  body,
}: {
  title: string;
  body: string;
}) {
  return (
    <div className="rounded-3xl border border-dashed border-[#d8c8ae] bg-[#fff8ee] p-8 text-center text-slate-600">
      <h3 className="text-lg font-semibold text-slate-900">{title}</h3>
      <p className="mt-2 text-sm">{body}</p>
    </div>
  );
}

export function LoadingState({ label = "Loading..." }: { label?: string }) {
  return (
    <div className="rounded-3xl border border-[#e5d8c2] bg-[#fff8ef] p-6 text-sm text-slate-600">
      {label}
    </div>
  );
}

export function ErrorBanner({
  message,
  onDismiss,
}: {
  message?: string | null;
  onDismiss?: () => void;
}) {
  if (!message) {
    return null;
  }

  return (
    <div className="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
      <div className="flex items-start justify-between gap-3">
        <span>{message}</span>
        {onDismiss ? (
          <button
            type="button"
            onClick={onDismiss}
            className="text-xs font-semibold uppercase tracking-[0.2em]"
          >
            Clear
          </button>
        ) : null}
      </div>
    </div>
  );
}

export function PillList({
  items,
  tone = "slate",
}: {
  items?: Array<string | undefined>;
  tone?: "slate" | "green" | "amber" | "rose";
}) {
  const filtered = (items ?? []).filter(Boolean) as string[];
  const styles = {
    slate: "border-[#d8ccb8] bg-[#f3eadc] text-[#5b5348]",
    green: "border-emerald-200 bg-emerald-50 text-emerald-700",
    amber: "border-[#e7cf94] bg-[#fff1cc] text-[#8f5d13]",
    rose: "border-[#efc4b5] bg-[#fff0e8] text-[#b4533a]",
  };

  if (!filtered.length) {
    return <span className="text-sm text-slate-500">No items available.</span>;
  }

  return (
    <div className="flex flex-wrap gap-2">
      {filtered.map((item) => (
        <span
          key={item}
          className={`rounded-full border px-3 py-1 text-sm ${styles[tone]}`}
        >
          {item}
        </span>
      ))}
    </div>
  );
}
