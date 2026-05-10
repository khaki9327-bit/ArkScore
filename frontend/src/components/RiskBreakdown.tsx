import type { RiskLevel, RiskSignal } from "@/types/analysis";

const levelStyles: Record<RiskLevel, string> = {
  Low: "border-emerald-300/30 bg-emerald-300/10 text-emerald-200",
  Medium: "border-amber-300/30 bg-amber-300/10 text-amber-200",
  High: "border-rose-300/30 bg-rose-300/10 text-rose-200"
};

type RiskBreakdownProps = {
  risks: RiskSignal[];
};

export function RiskBreakdown({ risks }: RiskBreakdownProps) {
  return (
    <article className="w-full min-w-0 rounded-lg border border-line bg-white/[0.045] p-5">
      <div className="flex min-w-0 items-center justify-between gap-3">
        <div className="min-w-0">
          <p className="text-sm text-slate-400">Risk Breakdown</p>
          <h2 className="mt-1 text-xl font-semibold text-white">
            How ArkScore evaluates this wallet
          </h2>
        </div>
      </div>

      <div className="mt-5 space-y-4">
        {risks.map((risk) => (
          <div className="min-w-0" key={risk.label}>
            <div className="flex min-w-0 flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
              <div className="min-w-0">
                <p className="font-medium text-slate-100">{risk.label}</p>
                <p className="mt-1 break-words text-sm text-slate-500">
                  {risk.description}
                </p>
              </div>
              <span
                className={`w-fit rounded-lg border px-2.5 py-1 text-xs font-medium ${levelStyles[risk.level]}`}
              >
                {risk.level}
              </span>
            </div>
            <div className="mt-3 h-2 overflow-hidden rounded-lg bg-slate-950">
              <div
                className="h-full rounded-lg bg-gradient-to-r from-solana-violet to-solana-cyan"
                style={{ width: `${risk.value}%` }}
              />
            </div>
          </div>
        ))}
      </div>
    </article>
  );
}
