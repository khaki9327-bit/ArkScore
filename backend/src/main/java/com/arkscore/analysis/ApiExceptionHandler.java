package com.arkscore.analysis;

import com.arkscore.deepseek.DeepSeekClientException;
import com.arkscore.solana.SolanaRpcClientException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Invalid request.");

        return new ErrorResponse(message, Instant.now());
    }

    @ExceptionHandler(InvalidWalletAddressException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidWalletAddress(InvalidWalletAddressException exception) {
        return new ErrorResponse(exception.getMessage(), Instant.now());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnreadableRequest() {
        return new ErrorResponse("Request body must be valid JSON.", Instant.now());
    }

    @ExceptionHandler(DeepSeekClientException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorResponse handleDeepSeekFailure() {
        return new ErrorResponse("AI summary generation failed.", Instant.now());
    }

    @ExceptionHandler(SolanaRpcClientException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorResponse handleSolanaRpcFailure() {
        return new ErrorResponse("Wallet data provider failed.", Instant.now());
    }
}
