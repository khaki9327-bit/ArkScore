export type RiskLevel = "Low" | "Medium" | "High";

export type RiskSignal = {
  label: string;
  level: RiskLevel;
  value: number;
  description: string;
};

export type WalletAnalysisResult = {
  walletAddress: string;
  reputationScore: number;
  riskLevel: string;
  tags: string[];
  risks: RiskSignal[];
  summary: string;
  generatedAt: string;
};
