# ArkScore Backend

Spring Boot REST API skeleton for ArkScore wallet reputation analysis.

## Run

```bash
mvn spring-boot:run
```

The API runs on `http://localhost:8080`.

## Configuration

### Logging

Local runs use debug logging for ArkScore application code by default. It logs
API request/response payloads and DeepSeek call metadata without printing the
DeepSeek API key.

Use the `prod` profile in production to print only error logs:

```bash
SPRING_PROFILES_ACTIVE=prod
```

### DeepSeek

`/api/analyze` uses DeepSeek to generate the AI summary. Provide the API key
and model before starting the app:

```bash
DEEPSEEK_API_KEY=your-api-key
DEEPSEEK_MODEL=deepseek-v4-flash
```

The app reads these properties:

```properties
arkscore.deepseek.base-url=https://api.deepseek.com
arkscore.deepseek.api-key=${DEEPSEEK_API_KEY}
arkscore.deepseek.model=${DEEPSEEK_MODEL}
arkscore.deepseek.max-tokens=180
arkscore.deepseek.temperature=0.0
arkscore.deepseek.thinking-enabled=false
```

`api-key` and `model` are required. The app fails startup when either is
missing. DeepSeek request failures return `502 Bad Gateway` from
`/api/analyze`.

### Solana Public RPC

`/api/analyze` fetches wallet data from Solana public RPC before calculating
rule-based reputation fields. No API key is required by default.

The app reads these properties:

```properties
arkscore.solana.rpc-url=https://api.mainnet-beta.solana.com
arkscore.solana.transaction-limit=20
arkscore.solana.transaction-detail-limit=3
arkscore.solana.commitment=finalized
arkscore.solana.wallet-cache-ttl=PT15M
arkscore.solana.request-min-interval=800ms
arkscore.solana.rate-limit-retry-max-attempts=2
arkscore.solana.rate-limit-retry-backoff=3s
arkscore.solana.max-concurrent-requests=1
arkscore.solana.allow-partial-transaction-details=true
```

For each cache miss, the backend fetches SOL balance, non-zero SPL token
holdings, up to 20 recent transaction signatures, and transaction account keys
for up to 3 of those signatures. RPC requests include the configured commitment
level, are paced by `request-min-interval`, and are guarded by
`max-concurrent-requests` to avoid public RPC throttling. Rate-limited calls
are retried with exponential backoff and honor the HTTP `Retry-After` header
when present. If transaction detail fetches are still rate-limited, the backend
can return partial detail data so the demo remains available. Wallet data is
cached per address for 15 minutes. Solana RPC request failures return
`502 Bad Gateway` from `/api/analyze`.

Solana public RPC is useful for demos, but it is not intended for production
traffic. For production deployments, use a dedicated RPC provider such as
Helius, QuickNode, Alchemy, or another private Solana RPC service.

### CORS

The API allows frontend origins from configuration:

```properties
arkscore.cors.allowed-origins=http://localhost:3000,http://127.0.0.1:3000
```

Override it with an environment variable when deploying:

```bash
ARKSCORE_CORS_ALLOWED_ORIGINS=https://arkscore-demo.vercel.app
```

## Endpoints

### Health

```http
GET /actuator/health
```

### Analyze Wallet

```http
POST /api/analyze
Content-Type: application/json

{
  "walletAddress": "9xQeWvG816bUx9EPfVhYScN46nCWgQVPk9JyG9GdM2nb"
}
```

The endpoint returns rule-based score, tag, and risk data calculated from
Solana public RPC metrics. The AI summary is generated from those rule-based
fields and cached in memory for repeat requests with the same wallet and
analysis JSON.

## Test

```bash
mvn test
```
