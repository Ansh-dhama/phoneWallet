package com.example.phoneWallet.Security;

import com.example.phoneWallet.Util.JwtAuthenticationFilter;
import com.example.phoneWallet.Util.RateLimitFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

@Configuration
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;
    private final ObjectMapper objectMapper;
    private final String allowedOrigins;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter,
                                 RateLimitFilter rateLimitFilter,
                                 ObjectMapper objectMapper,
                                 @Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}") String allowedOrigins) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.objectMapper = objectMapper;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> writeSecurityError(res, req.getRequestURI(), 401, "UNAUTHORIZED", "Authentication is required"))
                        .accessDeniedHandler((req, res, e) -> writeSecurityError(res, req.getRequestURI(), 403, "ACCESS_DENIED", "You do not have permission to access this resource")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/assets/**", "/favicon.ico", "/favicon.svg", "/vite.svg", "/error").permitAll()
                        .requestMatchers("/login", "/signup", "/dashboard", "/wallet", "/transfer", "/pay", "/transactions", "/statements", "/refund", "/admin", "/account").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/topups/razorpay/webhook").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/wallets").hasAnyRole("USER", "MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/wallets/mine").hasAnyRole("USER", "MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/wallets/**").hasAnyRole("USER", "MERCHANT", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/wallets/*/load-money").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/wallets/*/freeze", "/api/wallets/*/unfreeze", "/api/wallets/*/blacklist", "/api/wallets/*/unblacklist").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/topups/wallet/*", "/api/topups/*/verify-payment").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/topups/checkout-config").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/topups/wallet/*").hasAnyRole("USER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/transactions/transfer", "/api/transactions/pay").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/transactions/refund").hasAnyRole("ADMIN", "MERCHANT")
                        .requestMatchers(HttpMethod.POST, "/api/transactions/reversal").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/transactions/**").hasAnyRole("USER", "MERCHANT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/statements/**").hasAnyRole("USER", "MERCHANT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/notifications/**").hasAnyRole("USER", "MERCHANT", "ADMIN")
                        .requestMatchers("/actuator/prometheus", "/actuator/metrics/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Idempotency-Key", "X-Topup-Signature", "X-Razorpay-Signature"));
        configuration.setExposedHeaders(List.of("X-Request-Id"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private void writeSecurityError(HttpServletResponse response, String path, int status, String code, String message) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        var body = new LinkedHashMap<String, Object>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status);
        body.put("errorCode", code);
        body.put("message", message);
        body.put("path", path);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
