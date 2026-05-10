"use client";

import { FormEvent, useState } from "react";
import { DEMO_WALLET_ADDRESS } from "@/data/demoWallet";
import { analyzeWallet, validateSolanaAddress } from "@/lib/analyzeWallet";
import type { WalletAnalysisResult } from "@/types/analysis";
import { AnalysisLoadingCard } from "@/components/AnalysisLoadingCard";
import { AiSummaryCard } from "@/components/AiSummaryCard";
import { RiskBreakdown } from "@/components/RiskBreakdown";
import { ScoreCard } from "@/components/ScoreCard";
import { TagList } from "@/components/TagList";

const productStats = [
  {
    label: "Live scoring",
    value: "Solana"
  },
  {
    label: "4 risk signals",
    value: "Risk"
  },
  {
    label: "AI summary",
    value: "Context"
  }
];

export function WalletAnalyzer() {
  const [address, setAddress] = useState(DEMO_WALLET_ADDRESS);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [result, setResult] = useState<WalletAnalysisResult | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const validationError = validateSolanaAddress(address);

    if (validationError) {
      setError(validationError);
      setResult(null);
      return;
    }

    setError(null);
    setResult(null);
    setIsLoading(true);

    try {
      const analysis = await analyzeWallet(address);
      setResult(analysis);
    } catch (caughtError) {
      setResult(null);
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : "Analysis failed. Try again."
      );
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <section
      aria-label="Wallet analyzer"
      className="grid w-full min-w-0 grid-cols-1 gap-8 lg:grid-cols-2 lg:items-start lg:gap-12 xl:gap-16"
    >
      <div className="min-w-0 space-y-8">
        <div className="space-y-6">
          <div className="inline-flex max-w-full items-center gap-3 rounded-lg border border-white/10 bg-white/[0.04] px-3 py-2 shadow-glow backdrop-blur">
            <span className="grid size-9 shrink-0 place-items-center rounded-lg bg-gradient-to-br from-solana-violet via-solana-blue to-solana-cyan text-sm font-bold text-white">
              AS
            </span>
            <div className="min-w-0">
              <p className="text-sm font-semibold text-white">ArkScore</p>
              <p className="truncate text-xs text-slate-400">
                Trust, scored onchain.
              </p>
            </div>
          </div>

          <div>
            <p className="inline-block max-w-full rounded-lg border border-solana-violet/30 bg-solana-violet/10 px-3 py-2 text-sm text-violet-100">
              AI-powered onchain reputation infrastructure for Solana.
            </p>
            <h1 className="mt-5 text-4xl font-semibold leading-tight text-white sm:text-5xl lg:text-6xl">
              ArkScore
            </h1>
            <p className="mt-5 max-w-2xl text-base leading-7 text-slate-300 sm:text-lg sm:leading-8">
              Understand wallets before you trust them. ArkScore analyzes wallet
              behavior, transaction patterns, token interactions, and risk
              signals to generate reputation scores and AI-powered wallet
              intelligence.
            </p>
          </div>
        </div>

        <form
          className="w-full min-w-0 rounded-lg border border-line bg-panel/80 p-4 shadow-glow backdrop-blur sm:p-5"
          onSubmit={handleSubmit}
        >
          <label className="text-sm font-medium text-slate-300" htmlFor="wallet">
            Enter Solana wallet address
          </label>
          <div className="mt-3 flex min-w-0 flex-col gap-3 sm:flex-row">
            <input
              aria-describedby={error ? "wallet-error" : undefined}
              className="min-h-12 min-w-0 flex-1 truncate rounded-lg border border-slate-700 bg-slate-950/80 px-4 text-sm text-white outline-none transition focus:border-solana-cyan focus:ring-2 focus:ring-solana-cyan/20"
              id="wallet"
              onChange={(event) => setAddress(event.target.value)}
              placeholder="Solana wallet address"
              value={address}
            />
            <button
              className="inline-flex min-h-12 w-full shrink-0 items-center justify-center gap-2 rounded-lg bg-gradient-to-r from-solana-violet to-solana-cyan px-5 text-sm font-semibold text-white shadow-glow transition hover:brightness-110 disabled:cursor-not-allowed disabled:opacity-60 sm:w-auto"
              disabled={isLoading}
              type="submit"
            >
              {isLoading ? (
                <span className="size-4 animate-spin rounded-full border-2 border-white/30 border-t-white" />
              ) : null}
              {isLoading ? "Analyzing..." : "Analyze Wallet"}
            </button>
          </div>
          {error ? (
            <p className="mt-3 break-words text-sm text-rose-200" id="wallet-error">
              {error}
            </p>
          ) : null}
        </form>

        <div className="grid w-full min-w-0 gap-3 sm:grid-cols-3">
          {productStats.map((stat) => (
            <div
              className="min-w-0 rounded-lg border border-line bg-white/[0.035] p-4"
              key={stat.label}
            >
              <p className="text-lg font-semibold text-white">{stat.value}</p>
              <p className="mt-1 text-sm text-slate-400">{stat.label}</p>
            </div>
          ))}
        </div>

        {result ? <AiSummaryCard summary={result.summary} /> : null}
      </div>

      <div className="w-full min-w-0 space-y-5">
        {renderAnalysisPanel(result, isLoading)}
      </div>
    </section>
  );
}

function renderAnalysisPanel(
  result: WalletAnalysisResult | null,
  isLoading: boolean
) {
  if (isLoading) {
    return <AnalysisLoadingCard />;
  }

  if (result) {
    return (
      <>
        <ScoreCard result={result} />
        <article className="w-full min-w-0 rounded-lg border border-line bg-white/[0.045] p-5">
          <p className="text-sm text-slate-400">Behavior Tags</p>
          <h2 className="mt-1 text-xl font-semibold text-white">
            Detected trust and risk signals
          </h2>
          <div className="mt-4 min-w-0">
            <TagList tags={result.tags} />
          </div>
        </article>
        <RiskBreakdown risks={result.risks} />
      </>
    );
  }

  return (
    <article className="w-full min-w-0 rounded-lg border border-line bg-white/[0.045] p-5 shadow-glow">
      <p className="text-sm text-slate-400">Analysis Console</p>
      <h2 className="mt-1 text-2xl font-semibold text-white">
        Reputation engine ready
      </h2>
      <p className="mt-3 text-sm leading-6 text-slate-400">
        Enter a Solana wallet to generate a reputation score, risk breakdown,
        behavior tags, and AI-powered wallet context.
      </p>
      <div className="mt-5 grid gap-3 sm:grid-cols-3">
        {["Behavior", "Risk", "AI summary"].map((item) => (
          <div
            className="min-w-0 rounded-lg border border-slate-800 bg-slate-950/55 p-4"
            key={item}
          >
            <p className="text-sm font-medium text-slate-200">{item}</p>
            <div className="mt-3 h-2 rounded-lg bg-gradient-to-r from-solana-violet/70 to-solana-cyan/70" />
          </div>
        ))}
      </div>
    </article>
  );
}
