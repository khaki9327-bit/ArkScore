import { DEMO_WALLET_ADDRESS } from "@/data/demoWallet";
import type { WalletAnalysisResult } from "@/types/analysis";

export { DEMO_WALLET_ADDRESS };

export const MOCK_ANALYSIS: WalletAnalysisResult = {
  walletAddress: DEMO_WALLET_ADDRESS,
  reputationScore: 82,
  riskLevel: "Low to Medium",
  tags: [
    "Long-term Holder",
    "Organic Activity",
    "Low Rug Risk",
    "Moderate Meme Exposure"
  ],
  risks: [
    {
      label: "Bot Risk",
      level: "Low",
      value: 18,
      description: "Transaction cadence looks human and avoids repeated bursts."
    },
    {
      label: "Rug Risk",
      level: "Low",
      value: 22,
      description: "Limited exposure to newly created or highly concentrated assets."
    },
    {
      label: "Sybil Risk",
      level: "Medium",
      value: 48,
      description: "Some cluster overlap exists, but it is not dominant."
    },
    {
      label: "Meme Exposure",
      level: "Medium",
      value: 55,
      description: "Meme token activity is present without overwhelming the wallet."
    }
  ],
  summary:
    "This wallet shows relatively healthy onchain behavior with consistent activity, moderate token diversity, and limited signs of bot-like patterns. Some meme token exposure exists, but overall risk remains controlled.",
  generatedAt: new Date().toISOString()
};
