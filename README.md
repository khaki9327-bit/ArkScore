# ArkScore

**AI-powered onchain reputation infrastructure for Solana.**

ArkScore analyzes Solana wallet behavior, transaction patterns, token holdings, and risk signals to generate wallet reputation scores, behavioral tags, risk breakdowns, and AI-generated wallet intelligence.

> Understand wallets before you trust them.

---

## Overview

Web2 has mature trust infrastructure: credit scoring, fraud detection, behavioral risk analysis, and identity signals.

Web3 still mostly relies on anonymous wallet addresses. Users, protocols, DAOs, and future AI agents interact with wallets every day without enough context:

- Is this wallet trustworthy?
- Is it bot-driven?
- Is it a dormant or organic wallet?
- Does it show suspicious activity patterns?
- Is it potentially part of a Sybil or farming pattern?
- What risks should be considered before interacting with it?

ArkScore introduces an AI-powered wallet reputation layer for the Solana ecosystem.

---

## What ArkScore Does

Given a Solana wallet address, ArkScore generates:

- **Reputation Score**  
  A rule-based wallet trust score from 0 to 100.

- **Risk Level**  
  A simple risk classification such as Low, Medium, or High.

- **Behavior Tags**  
  Human-readable labels such as Limited Activity, Low Rug Risk, Organic Activity, or High Sybil Risk.

- **Risk Breakdown**  
  Separate risk dimensions including bot risk, rug risk, Sybil risk, and meme exposure.

- **AI Summary**  
  A concise AI-generated explanation of the wallet’s trust and risk profile.

---

## Product Positioning

ArkScore is not a trading bot.

ArkScore is not a financial advisor.

ArkScore is a wallet intelligence and reputation layer designed to help users and applications better understand wallet behavior before interacting onchain.

Long-term, ArkScore can become infrastructure for:

- Wallet reputation APIs
- DeFi risk checks
- DAO treasury risk review
- Marketplace counterparty analysis
- AI agent wallet trust evaluation
- Onchain identity and reputation systems

---

## Demo Flow

1. Enter a Solana wallet address.
2. ArkScore fetches wallet data from Solana RPC.
3. The backend extracts basic wallet behavior signals.
4. The rule engine calculates reputation score and risk breakdown.
5. DeepSeek generates a professional wallet intelligence summary.
6. The frontend displays the wallet reputation profile.

---

## Tech Stack

### Frontend

- Next.js
- TypeScript
- TailwindCSS
- Responsive dark UI
- Solana-inspired gradient styling

### Backend

- Java
- Spring Boot
- Solana JSON-RPC
- DeepSeek API
- Rule-based scoring engine
- AI summary generation

### Infrastructure

- Public Solana RPC / configurable RPC provider
- Vercel-ready frontend
- Railway / Render-ready backend
- Monorepo structure

---

## Project Structure

```text
arkscore/
├── frontend/                 # Next.js frontend application
│   ├── app/
│   ├── components/
│   ├── lib/
│   ├── public/
│   └── package.json
│
├── backend/                  # Spring Boot backend application
│   ├── src/
│   └── pom.xml
│
├── docs/                     # Project plans, pitch notes, and architecture docs
├── screenshots/              # Product screenshots
├── AGENTS.md                 # Codex project instructions
├── README.md
└── LICENSE
```

---

## Architecture

```text
User
  ↓
Next.js Frontend
  ↓
Spring Boot Backend
  ↓
Solana JSON-RPC
  ↓
Wallet Signal Extraction
  ↓
Rule-Based Reputation Engine
  ↓
DeepSeek AI Summary
  ↓
Wallet Reputation Result
```

---

## Core Scoring Signals

ArkScore currently focuses on lightweight MVP-level signals:

### Activity Signals

- Recent transaction count
- Failed transaction ratio
- Activity depth
- Average transaction interval
- Burst activity behavior

### Portfolio Signals

- SOL balance
- Token account count
- Non-zero token holdings
- Token diversity
- Sparse balance pattern

### Risk Signals

- Bot-like behavior
- Sybil risk
- Rug exposure
- Meme exposure
- Low-balance high-activity pattern

### Trust Signals

- Consistent wallet activity
- Low failed transaction ratio
- Reasonable token diversity
- Lower suspicious behavior indicators

---

## AI Usage

ArkScore uses AI for explanation, not for core scoring.

The backend rule engine calculates:

- Reputation score
- Risk level
- Tags
- Bot risk
- Rug risk
- Sybil risk
- Meme exposure

DeepSeek is used only to generate a readable summary based on the already-calculated wallet analysis result.

This keeps the product more deterministic, explainable, and demo-stable.

---

## Local Development

### Prerequisites

- Node.js 18+
- Java 17+
- Maven 3.8+
- A DeepSeek API key
- Network access to Solana RPC

---

## Environment Variables

### Frontend

Create `frontend/.env.local`:

```bash
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

### Backend

Create `backend/.env` or configure environment variables in your IDE:

```bash
DEEPSEEK_API_KEY=your_deepseek_api_key
SOLANA_RPC_URL=https://api.mainnet.solana.com
```

Optional:

```bash
DEEPSEEK_MODEL=deepseek-v4-flash
DEEPSEEK_TEMPERATURE=0.1
```

Do not commit API keys to GitHub.

---

## Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend will usually run at:

```text
http://localhost:3000
```

---

## Run Backend

```bash
cd backend
mvn spring-boot:run
```

Backend will usually run at:

```text
http://localhost:8080
```

---

## API Example

### Analyze Wallet

```http
POST /api/wallets/{walletAddress}/analyze
```

Example response:

```json
{
  "walletAddress": "9xQeWvG816bUx9EPfVhYScN46nCWgQVPk9JyG9GdM2nb",
  "reputationScore": 71,
  "riskLevel": "Low",
  "tags": ["Limited Activity", "Low Rug Risk"],
  "riskBreakdown": {
    "botRisk": "Medium",
    "rugRisk": "Low",
    "sybilRisk": "High",
    "memeExposure": "Low"
  },
  "aiSummary": "This wallet shows limited onchain activity and low rug risk, but the sparse wallet history creates uncertainty around Sybil and bot-like behavior."
}
```

---

## Deployment

Recommended hackathon deployment:

```text
Frontend: Vercel
Backend: Railway or Render
RPC: Solana Public RPC or Alchemy Solana RPC
```

### Frontend Deployment

- Import the GitHub repo into Vercel.
- Set root directory to `frontend`.
- Configure:

```bash
NEXT_PUBLIC_API_BASE_URL=https://your-backend-domain
```

### Backend Deployment

- Deploy the `backend` directory to Railway or Render.
- Configure environment variables:

```bash
DEEPSEEK_API_KEY=your_deepseek_api_key
SOLANA_RPC_URL=https://api.mainnet.solana.com
```

---

## Hackathon MVP Scope

ArkScore is currently a hackathon MVP.

The current version prioritizes:

1. Clear product narrative
2. Working wallet analysis demo
3. Professional UI
4. Deterministic rule-based scoring
5. AI-generated explanation
6. Simple deployment

The current version does not attempt to provide a complete production-grade wallet risk system.

---

## Future Roadmap

Potential future improvements:

- Better Solana transaction parsing
- Wallet identity lookup
- Funding source analysis
- Protocol interaction classification
- Historical wallet reputation timeline
- Reputation API for other applications
- AI agent wallet trust layer
- DAO and DeFi integration
- Onchain reputation attestations
- Risk monitoring and alerting

---

## Why Now

The onchain economy is becoming more automated.

AI agents, autonomous wallets, DeFi protocols, DAOs, and marketplaces will increasingly need trust signals before interacting with wallets.

ArkScore is built around the belief that:

> Reputation will become a foundational primitive for the AI-powered onchain economy.

---

## Disclaimer

ArkScore is an experimental wallet intelligence tool built for hackathon demonstration purposes.

It does not provide financial advice, investment advice, legal advice, or compliance guarantees.

Wallet reputation scores and AI summaries should be interpreted as informational signals only.

---

## License

This project is licensed under the Apache License 2.0.
