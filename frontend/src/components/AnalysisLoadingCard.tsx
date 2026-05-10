const loadingStages = ["Wallet graph", "Risk signals", "AI summary"];

export function AnalysisLoadingCard() {
  return (
    <article
      aria-live="polite"
      aria-busy="true"
      className="relative w-full min-w-0 overflow-hidden rounded-lg border border-line bg-white/[0.045] p-5 shadow-glow"
    >
      <div className="absolute inset-x-0 top-0 h-px animate-pulse bg-gradient-to-r from-transparent via-solana-cyan to-transparent" />
      <div className="absolute -left-1/2 top-0 h-full w-1/2 animate-[scan_1.8s_ease-in-out_infinite] bg-gradient-to-r from-transparent via-white/[0.08] to-transparent" />

      <div className="relative flex min-w-0 flex-col gap-4 sm:flex-row sm:items-start">
        <div className="grid size-12 shrink-0 place-items-center rounded-lg border border-solana-cyan/30 bg-solana-cyan/10">
          <div className="size-6 animate-spin rounded-full border-2 border-solana-cyan/20 border-t-solana-cyan" />
        </div>
        <div className="min-w-0 flex-1">
          <p className="text-sm text-slate-400">Analysis Console</p>
          <h2 className="mt-1 text-2xl font-semibold text-white">
            Analyzing wallet
          </h2>
          <p className="mt-2 text-sm leading-6 text-slate-400">
            Scoring onchain behavior and generating AI reputation context.
          </p>
        </div>
      </div>

      <div className="relative mt-6 grid gap-3 sm:grid-cols-3">
        {loadingStages.map((stage, index) => (
          <div
            className="rounded-lg border border-slate-800 bg-slate-950/55 p-4"
            key={stage}
          >
            <div className="flex items-center justify-between gap-3">
              <p className="text-sm font-medium text-slate-200">{stage}</p>
              <span className="size-2 rounded-full bg-solana-cyan shadow-[0_0_16px_rgba(20,241,217,0.65)]" />
            </div>
            <div className="mt-4 space-y-2">
              <div
                className="h-2 animate-pulse rounded-lg bg-gradient-to-r from-solana-violet/50 to-solana-cyan/60"
                style={{ animationDelay: `${index * 140}ms` }}
              />
              <div
                className="h-2 w-2/3 animate-pulse rounded-lg bg-slate-700/70"
                style={{ animationDelay: `${index * 180}ms` }}
              />
            </div>
          </div>
        ))}
      </div>
    </article>
  );
}
