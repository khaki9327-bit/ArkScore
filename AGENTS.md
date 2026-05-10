# AGENTS.md

## Project

Project name: ArkScore

ArkScore is an AI-powered onchain wallet reputation product for Solana.

Core positioning:

> AI-powered onchain reputation infrastructure for Solana.

The frontend should look like a modern crypto / AI infrastructure startup, not an admin dashboard.

## Current Goal

Build a polished MVP landing + wallet analysis page for hackathon submission.

The page must support:

1. Hero section
2. Wallet address input
3. Analyze button
4. Reputation score result
5. Wallet behavior tags
6. Risk breakdown
7. AI-generated summary
8. Clear demo-ready visual layout

## Tech Stack

Use:

- Next.js
- TypeScript
- TailwindCSS
- React components
- Responsive design

Avoid:

- jQuery
- Bootstrap
- Heavy UI libraries unless already installed
- Over-engineered state management

## Design Direction

Style should be:

- Dark background
- Futuristic
- Fintech / crypto native
- Clean and premium
- Startup landing page quality
- Solana-inspired accent colors are acceptable
- Strong contrast
- Rounded cards
- Subtle gradients
- Minimal animations

Do not make it look like:

- Enterprise admin console
- Plain CRUD app
- Meme coin website
- Student assignment

## Page Copy

Use this core copy:

Title:

ArkScore

Subtitle:

AI-powered onchain reputation infrastructure for Solana.

Hero description:

Understand wallets before you trust them. ArkScore analyzes wallet behavior, transaction patterns, token interactions, and risk signals to generate reputation scores and AI-powered wallet intelligence.

CTA:

Analyze Wallet

## Demo Data

If real API integration is not ready, use mock data.

Example output:

- Reputation Score: 82 / 100
- Risk Level: Low to Medium
- Tags:
  - Long-term Holder
  - Organic Activity
  - Low Rug Risk
  - Moderate Meme Exposure
- Bot Risk: Low
- Rug Risk: Low
- Sybil Risk: Medium
- Meme Exposure: Medium

AI Summary:

This wallet shows relatively healthy onchain behavior with consistent activity, moderate token diversity, and limited signs of bot-like patterns. Some meme token exposure exists, but overall risk remains controlled.

## Component Guidelines

Prefer small reusable components:

- HeroSection
- WalletAnalyzer
- ScoreCard
- TagList
- RiskBreakdown
- AiSummaryCard
- FeatureSection

Keep components simple and readable.

## Code Quality Rules

- Use TypeScript types.
- Avoid `any` unless necessary.
- Keep business mock data separated from UI components.
- Use semantic HTML where possible.
- Ensure mobile responsive layout.
- Do not hardcode messy inline styles.
- Prefer Tailwind utility classes.
- Keep the page demo-stable.

## Hackathon Priority

Prioritize in this order:

1. Demo works
2. UI looks professional
3. Narrative is clear
4. Code is readable
5. Architecture is simple

Do not overbuild.

Do not add complex Solana smart contract logic unless explicitly requested.

Do not block progress waiting for perfect backend integration.

## Commands

Use the package manager already present in the repo.

Common commands:

```bash
npm install
npm run dev
npm run build