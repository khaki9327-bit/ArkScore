package com.arkscore.analysis;

public class InvalidWalletAddressException extends RuntimeException {

    public InvalidWalletAddressException(String message) {
        super(message);
    }
}
