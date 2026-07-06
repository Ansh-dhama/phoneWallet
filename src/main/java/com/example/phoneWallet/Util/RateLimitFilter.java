package com.example.phoneWallet.Util;

import com.example.phoneWallet.Exceptions.RateLimitExceededException;
import com.example.phoneWallet.services.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
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

    public RateLimitFilter(RateLimitService rateLimitService,
                           ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String path = request.getServletPath();
            String method = request.getMethod();
            String clientIp = getClientIp(request);

            // POST /api/auth/login -> 5 requests/minute per IP
            if (HttpMethod.POST.matches(method) && path.equals("/api/auth/login")) {
                String key = "rate:login:" + clientIp;
                rateLimitService.checkRateLimit(key, 5, Duration.ofMinutes(1));
            }

            // POST /api/auth/refresh-token -> 10 requests/minute per IP
            if (HttpMethod.POST.matches(method) && path.equals("/api/auth/refresh-token")) {
                String key = "rate:refresh-token:" + clientIp;
                rateLimitService.checkRateLimit(key, 10, Duration.ofMinutes(1));
            }

            // POST /api/transactions/transfer -> 10 requests/minute per IP
            if (HttpMethod.POST.matches(method) && path.equals("/api/transactions/transfer")) {
                String key = "rate:transfer:" + clientIp;
                rateLimitService.checkRateLimit(key, 10, Duration.ofMinutes(1));
            }

            // POST /api/transactions/pay -> 10 requests/minute per IP
            if (HttpMethod.POST.matches(method) && path.equals("/api/transactions/pay")) {
                String key = "rate:pay:" + clientIp;
                rateLimitService.checkRateLimit(key, 10, Duration.ofMinutes(1));
            }

            // GET /api/wallets/{walletId}/balance -> 30 requests/minute per IP
            if (HttpMethod.GET.matches(method)
                    && path.matches("/api/wallets/\\d+/balance")) {
                String key = "rate:balance:" + clientIp;
                rateLimitService.checkRateLimit(key, 30, Duration.ofMinutes(1));
            }

            filterChain.doFilter(request, response);

        } catch (RateLimitExceededException ex) {
            writeRateLimitError(response, request, ex);
        }
    }

    private String getClientIp(HttpServletRequest request) {

        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    private void writeRateLimitError(HttpServletResponse response,
                                     HttpServletRequest request,
                                     RateLimitExceededException ex) throws IOException {

        response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
        response.setContentType("application/json");

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", 429);
        body.put("errorCode", "RATE_LIMIT_EXCEEDED");
        body.put("message", ex.getMessage());
        body.put("path", request.getRequestURI());

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}