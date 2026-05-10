# ArkScore Frontend

Next.js MVP for ArkScore, an AI-powered onchain reputation product for Solana.

## Run

```bash
npm install
npm run dev
```

Open `http://localhost:3000`.

## API

The wallet analyzer calls the Spring Boot backend at `http://localhost:8080`
by default.

To point the frontend at another API host, set:

```bash
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

## Build

```bash
npm run build
```
