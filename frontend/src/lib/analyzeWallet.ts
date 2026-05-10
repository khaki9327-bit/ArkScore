import type { WalletAnalysisResult } from "@/types/analysis";

const BASE58_SOLANA_ADDRESS = /^[1-9A-HJ-NP-Za-km-z]+$/;
const DEFAULT_API_BASE_URL = "http://localhost:8080";

type ApiErrorResponse = {
  message?: string;
};

export function validateSolanaAddress(address: string): string | null {
  const normalizedAddress = address.trim();

  if (!normalizedAddress) {
    return "Enter a Solana wallet address.";
  }

  if (
    normalizedAddress.length < 32 ||
    normalizedAddress.length > 44 ||
    !BASE58_SOLANA_ADDRESS.test(normalizedAddress)
  ) {
    return "Use a valid base58 Solana wallet address.";
  }

  return null;
}

function getApiBaseUrl() {
  return (process.env.NEXT_PUBLIC_API_BASE_URL ?? DEFAULT_API_BASE_URL).replace(
    /\/+$/,
    ""
  );
}

function isApiErrorResponse(value: unknown): value is ApiErrorResponse {
  return (
    typeof value === "object" &&
    value !== null &&
    "message" in value &&
    typeof (value as { message?: unknown }).message === "string"
  );
}

async function readErrorMessage(response: Response) {
  try {
    const responseBody: unknown = await response.json();

    if (isApiErrorResponse(responseBody) && responseBody.message) {
      return responseBody.message;
    }
  } catch {
    // Fall through to the generic message when the API does not return JSON.
  }

  return `Analysis failed (${response.status}). Try again.`;
}

export async function analyzeWallet(
  address: string
): Promise<WalletAnalysisResult> {
  const normalizedAddress = address.trim();
  const validationError = validateSolanaAddress(normalizedAddress);

  if (validationError) {
    throw new Error(validationError);
  }

  let response: Response;

  try {
    response = await fetch(`${getApiBaseUrl()}/api/analyze`, {
      body: JSON.stringify({ walletAddress: normalizedAddress }),
      headers: {
        "Content-Type": "application/json"
      },
      method: "POST"
    });
  } catch {
    throw new Error(
      "Unable to reach ArkScore API. Make sure the backend is running."
    );
  }

  if (!response.ok) {
    throw new Error(await readErrorMessage(response));
  }

  return (await response.json()) as WalletAnalysisResult;
}
