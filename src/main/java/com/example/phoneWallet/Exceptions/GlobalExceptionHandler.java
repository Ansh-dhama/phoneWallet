package com.example.phoneWallet.Exceptions;

import com.example.phoneWallet.dto.ErrorResponse;
import com.example.phoneWallet.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Wallet not found
    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWalletNotFound(
            WalletNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ErrorCode.WALLET_NOT_FOUND.name(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // Duplicate wallet
    @ExceptionHandler(DuplicateWalletException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateWallet(
            DuplicateWalletException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ErrorCode.DUPLICATE_WALLET.name(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // Wallet frozen
    @ExceptionHandler(WalletFrozenException.class)
    public ResponseEntity<ErrorResponse> handleWalletFrozen(
            WalletFrozenException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.WALLET_FROZEN.name(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // Insufficient balance
    @ExceptionHandler(InsufficentAmountException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientAmount(
            InsufficentAmountException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INSUFFICIENT_BALANCE.name(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // Invalid negative/zero amount
    @ExceptionHandler(AmountNegativeException.class)
    public ResponseEntity<ErrorResponse> handleAmountNegative(
            AmountNegativeException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_AMOUNT.name(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // Transaction not found
    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTransactionNotFound(
            TransactionNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ErrorCode.TRANSACTION_NOT_FOUND.name(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // Transaction not completed
    @ExceptionHandler(TransactionNotComplete.class)
    public ResponseEntity<ErrorResponse> handleTransactionNotComplete(
            TransactionNotComplete ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.TRANSACTION_NOT_COMPLETED.name(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // Invalid transaction
    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransaction(
            InvalidTransactionException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_TRANSACTION.name(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // Ledger validation failed
    @ExceptionHandler(LedgerValidationException.class)
    public ResponseEntity<ErrorResponse> handleLedgerValidation(
            LedgerValidationException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.LEDGER_VALIDATION_FAILED.name(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // Amount mismatch
    @ExceptionHandler(AmountMismatch.class)
    public ResponseEntity<ErrorResponse> handleAmountMismatch(
            AmountMismatch ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.AMOUNT_MISMATCH.name(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // Username already exists
    @ExceptionHandler(UsernameExists.class)
    public ResponseEntity<ErrorResponse> handleUsernameExists(
            UsernameExists ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ErrorCode.USERNAME_ALREADY_EXISTS.name(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // Risk / fraud block
    @ExceptionHandler(TransactionBlock.class)
    public ResponseEntity<ErrorResponse> handleTransactionBlock(
            TransactionBlock ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                ErrorCode.TRANSACTION_BLOCKED.name(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // Validation errors from @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation failed");

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR.name(),
                message,
                request.getRequestURI()
        );
    }

    // Wrong username/password
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.INVALID_CREDENTIALS.name(),
                "Invalid username or password",
                request.getRequestURI()
        );
    }

    // Authentication failed
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.UNAUTHORIZED.name(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // User has token but role not allowed
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                ErrorCode.ACCESS_DENIED.name(),
                "You do not have permission to access this resource",
                request.getRequestURI()
        );
    }

    // DB unique constraint errors
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ErrorCode.DATABASE_CONSTRAINT_ERROR.name(),
                "Database constraint violation",
                request.getRequestURI()
        );
    }

    // Fallback for all unknown errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }
    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenNotFound(
            RefreshTokenNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ErrorCode.REFRESH_TOKEN_NOT_FOUND.name(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenExpired(
            RefreshTokenExpiredException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.REFRESH_TOKEN_EXPIRED.name(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(RefreshTokenRevokedException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenRevoked(
            RefreshTokenRevokedException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.REFRESH_TOKEN_REVOKED.name(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
            RateLimitExceededException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS,
                ErrorCode.RATE_LIMIT_EXCEEDED.name(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String errorCode,
            String message,
            String path
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                errorCode,
                message,
                path
        );

        return new ResponseEntity<>(errorResponse, status);
    }
}