package com.arkscore.solana;

public class SolanaRpcClientException extends RuntimeException {

    public SolanaRpcClientException(String message) {
        super(message);
    }

    public SolanaRpcClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
