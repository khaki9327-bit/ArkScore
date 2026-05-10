import type { WalletAnalysisResult } from "@/types/analysis";

type ScoreCardProps = {
  result: WalletAnalysisResult;
};

export function ScoreCard({ result }: ScoreCardProps) {
  const score = Math.max(0, Math.min(100, result.reputationScore));

  return (
    <article className="w-full min-w-0 rounded-lg border border-line bg-white/[0.045] p-5 shadow-glow">
      <div className="flex min-w-0 flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div className="min-w-0">
          <p className="text-sm text-slate-400">Reputation Score</p>
          <div className="mt-2 flex items-baseline gap-2">
            <strong className="text-4xl font-semibold text-white sm:text-5xl">
              {score}
            </strong>
            <span className="text-lg text-slate-400">/ 100</span>
          </div>
        </div>
        <span className="w-fit rounded-lg border border-emerald-300/30 bg-emerald-300/10 px-3 py-2 text-sm font-medium text-emerald-200">
          {result.riskLevel}
        </span>
      </div>

      <div className="mt-6 h-3 overflow-hidden rounded-lg bg-slate-950">
        <div
          className="h-full rounded-lg bg-gradient-to-r from-solana-violet via-solana-blue to-solana-cyan"
          style={{ width: `${score}%` }}
        />
      </div>

      <div className="mt-5 grid gap-3 text-sm sm:grid-cols-3">
        <div className="min-w-0 border-l border-solana-cyan/50 pl-3">
          <p className="text-slate-500">Network</p>
          <p className="mt-1 font-medium text-slate-100">Solana</p>
        </div>
        <div className="min-w-0 border-l border-solana-violet/50 pl-3">
          <p className="text-slate-500">Signals</p>
          <p className="mt-1 font-medium text-slate-100">Live-ready</p>
        </div>
        <div className="min-w-0 border-l border-emerald-300/50 pl-3">
          <p className="text-slate-500">Mode</p>
          <p className="mt-1 font-medium text-slate-100">Live Analysis</p>
        </div>
      </div>
    </article>
  );
}
