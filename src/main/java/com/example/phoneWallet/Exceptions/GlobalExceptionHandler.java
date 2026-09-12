package com.example.phoneWallet.Exceptions;

import com.example.phoneWallet.dto.ErrorResponse;
import com.example.phoneWallet.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WalletNotFoundException.class)
    ResponseEntity<ErrorResponse> walletNotFound(WalletNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ErrorCode.WALLET_NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateWalletException.class)
    ResponseEntity<ErrorResponse> duplicateWallet(DuplicateWalletException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ErrorCode.DUPLICATE_WALLET, ex.getMessage(), req);
    }

    @ExceptionHandler(WalletFrozenException.class)
    ResponseEntity<ErrorResponse> walletFrozen(WalletFrozenException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ErrorCode.WALLET_FROZEN, ex.getMessage(), req);
    }

    @ExceptionHandler(InsufficentAmountException.class)
    ResponseEntity<ErrorResponse> insufficient(InsufficentAmountException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.INSUFFICIENT_BALANCE, ex.getMessage(), req);
    }

    @ExceptionHandler(AmountNegativeException.class)
    ResponseEntity<ErrorResponse> invalidAmount(AmountNegativeException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_AMOUNT, ex.getMessage(), req);
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    ResponseEntity<ErrorResponse> txNotFound(TransactionNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ErrorCode.TRANSACTION_NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(TransactionNotComplete.class)
    ResponseEntity<ErrorResponse> txIncomplete(TransactionNotComplete ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ErrorCode.TRANSACTION_NOT_COMPLETED, ex.getMessage(), req);
    }

    @ExceptionHandler({InvalidTransactionException.class, IllegalArgumentException.class})
    ResponseEntity<ErrorResponse> invalidTx(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_TRANSACTION, ex.getMessage(), req);
    }

    @ExceptionHandler(LedgerValidationException.class)
    ResponseEntity<ErrorResponse> ledger(LedgerValidationException ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.LEDGER_VALIDATION_FAILED, ex.getMessage(), req);
    }

    @ExceptionHandler(AmountMismatch.class)
    ResponseEntity<ErrorResponse> amountMismatch(AmountMismatch ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.AMOUNT_MISMATCH, ex.getMessage(), req);
    }

    @ExceptionHandler(UsernameExists.class)
    ResponseEntity<ErrorResponse> usernameExists(UsernameExists ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ErrorCode.USERNAME_ALREADY_EXISTS, ex.getMessage(), req);
    }

    @ExceptionHandler(TransactionBlock.class)
    ResponseEntity<ErrorResponse> transactionBlock(TransactionBlock ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ErrorCode.TRANSACTION_BLOCKED, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream().findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage()).orElse("Validation failed");
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, message, req);
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ErrorResponse> badCredentials(BadCredentialsException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_CREDENTIALS, "Invalid username or password", req);
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ErrorResponse> authentication(AuthenticationException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, ex.getMessage(), req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> denied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ErrorCode.ACCESS_DENIED, ex.getMessage() == null ? "Access denied" : ex.getMessage(), req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> constraint(DataIntegrityViolationException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ErrorCode.DATABASE_CONSTRAINT_ERROR, "Database constraint violation", req);
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    ResponseEntity<ErrorResponse> refreshNotFound(RefreshTokenNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ErrorCode.REFRESH_TOKEN_NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    ResponseEntity<ErrorResponse> refreshExpired(RefreshTokenExpiredException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, ErrorCode.REFRESH_TOKEN_EXPIRED, ex.getMessage(), req);
    }

    @ExceptionHandler(RefreshTokenRevokedException.class)
    ResponseEntity<ErrorResponse> refreshRevoked(RefreshTokenRevokedException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, ErrorCode.REFRESH_TOKEN_REVOKED, ex.getMessage(), req);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    ResponseEntity<ErrorResponse> rateLimit(RateLimitExceededException ex, HttpServletRequest req) {
        return build(HttpStatus.TOO_MANY_REQUESTS, ErrorCode.RATE_LIMIT_EXCEEDED, ex.getMessage(), req);
    }

    @ExceptionHandler(DependencyUnavailableException.class)
    ResponseEntity<ErrorResponse> dependency(DependencyUnavailableException ex, HttpServletRequest req) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.DEPENDENCY_UNAVAILABLE, ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidTopUpException.class)
    ResponseEntity<ErrorResponse> topup(InvalidTopUpException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_TOP_UP, ex.getMessage(), req);
    }

    @ExceptionHandler(IdempotencyInProgressException.class)
    ResponseEntity<ErrorResponse> idem(IdempotencyInProgressException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ErrorCode.IDEMPOTENCY_IN_PROGRESS, ex.getMessage(), req);
    }


    @ExceptionHandler({CannotAcquireLockException.class, PessimisticLockingFailureException.class, ObjectOptimisticLockingFailureException.class})
    ResponseEntity<ErrorResponse> concurrency(Exception ex, HttpServletRequest req) {
        log.warn("Concurrent wallet operation conflict path={} requestId={} type={}",
                req.getRequestURI(), req.getHeader("X-Request-Id"), ex.getClass().getSimpleName());
        return build(HttpStatus.CONFLICT, ErrorCode.IDEMPOTENCY_IN_PROGRESS,
                "Another wallet operation is being processed. Please retry with the same idempotency key.", req);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> generic(Exception ex, HttpServletRequest req) {
        log.error("Unhandled API exception path={} requestId={}",
                req.getRequestURI(), req.getHeader("X-Request-Id"), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, "Unexpected server error", req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, ErrorCode code, String message, HttpServletRequest req) {
        return new ResponseEntity<>(new ErrorResponse(status.value(), code.name(), message, req.getRequestURI()), status);
    }
}
