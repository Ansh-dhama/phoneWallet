package com.example.phoneWallet.Util;

import com.example.phoneWallet.Exceptions.DependencyUnavailableException;
import com.example.phoneWallet.Exceptions.RateLimitExceededException;
import com.example.phoneWallet.services.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;
    private final boolean trustForwardedHeaders;

    public RateLimitFilter(RateLimitService rateLimitService,
                           ObjectMapper objectMapper,
                           @Value("${security.trust-forwarded-headers:false}") boolean trustForwardedHeaders) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
        this.trustForwardedHeaders = trustForwardedHeaders;
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Static React assets and browser resources must never depend on Redis.
        // Rate limiting is intentionally scoped to API traffic only.
        return path == null || !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String path = request.getServletPath();
            String method = request.getMethod();
            String subject = authenticatedSubjectOrIp(request);

            if (HttpMethod.POST.matches(method) && path.equals("/api/auth/login")) {
                rateLimitService.checkRateLimit("rate:login:" + getClientIp(request), 5, Duration.ofMinutes(1), true);
            } else if (HttpMethod.POST.matches(method) && path.equals("/api/auth/refresh-token")) {
                rateLimitService.checkRateLimit("rate:refresh:" + getClientIp(request), 10, Duration.ofMinutes(1), true);
            } else if (HttpMethod.POST.matches(method)
                    && (path.equals("/api/transactions/transfer") || path.equals("/api/transactions/pay")
                    || path.equals("/api/transactions/refund") || path.equals("/api/transactions/reversal"))) {
                rateLimitService.checkRateLimit("rate:money:" + subject, 15, Duration.ofMinutes(1), true);
            } else if (HttpMethod.POST.matches(method) && path.matches("/api/topups/wallet/\\d+")) {
                rateLimitService.checkRateLimit("rate:topup:" + subject, 8, Duration.ofMinutes(1), true);
            } else if (HttpMethod.POST.matches(method) && path.equals("/api/topups/webhook")) {
                rateLimitService.checkRateLimit("rate:topup-webhook:" + getClientIp(request), 60, Duration.ofMinutes(1), true);
            } else if (HttpMethod.GET.matches(method) && path.matches("/api/wallets/\\d+/balance")) {
                rateLimitService.checkRateLimit("rate:balance:" + subject, 60, Duration.ofMinutes(1), false);
            }

            filterChain.doFilter(request, response);
        } catch (RateLimitExceededException ex) {
            writeError(response, request, 429, "RATE_LIMIT_EXCEEDED", ex.getMessage());
        } catch (DependencyUnavailableException ex) {
            writeError(response, request, 503, "DEPENDENCY_UNAVAILABLE", ex.getMessage());
        }
    }

    private String authenticatedSubjectOrIp(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "user:" + auth.getName();
        }
        return "ip:" + getClientIp(request);
    }

    private String getClientIp(HttpServletRequest request) {
        if (trustForwardedHeaders) {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeError(HttpServletResponse response, HttpServletRequest request,
                            int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status);
        body.put("errorCode", code);
        body.put("message", message);
        body.put("path", request.getRequestURI());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
