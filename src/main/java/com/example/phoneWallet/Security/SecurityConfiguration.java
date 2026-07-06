package com.example.phoneWallet.Security;

import com.example.phoneWallet.Util.JwtAuthenticationFilter;
import com.example.phoneWallet.Util.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter,
                                 RateLimitFilter rateLimitFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        System.out.println("========== CUSTOM SECURITY CONFIG LOADED ==========");

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // public auth APIs
                        .requestMatchers("/api/auth/**").permitAll()

                        // Swagger/OpenAPI public APIs
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api-docs/**"
                        ).permitAll()

                        // admin APIs
                        .requestMatchers(HttpMethod.GET, "/api/admin/**")
                        .hasRole("ADMIN")

                        // wallet APIs
                        .requestMatchers(HttpMethod.POST, "/api/wallets")
                        .hasAnyRole("USER", "MERCHANT")

                        .requestMatchers(HttpMethod.GET, "/api/wallets/*")
                        .hasAnyRole("USER", "MERCHANT", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/wallets/*/balance")
                        .hasAnyRole("USER", "MERCHANT", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/wallets/*/load-money")
                        .hasRole("USER")

                        .requestMatchers(HttpMethod.POST, "/api/wallets/*/freeze")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/wallets/*/unfreeze")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/wallets/*/blacklist")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/wallets/*/unblacklist")
                        .hasRole("ADMIN")

                        // transaction APIs
                        .requestMatchers(HttpMethod.POST, "/api/transactions/transfer")
                        .hasRole("USER")

                        .requestMatchers(HttpMethod.POST, "/api/transactions/pay")
                        .hasRole("USER")

                        .requestMatchers(HttpMethod.POST, "/api/transactions/refund")
                        .hasAnyRole("ADMIN", "MERCHANT")

                        .requestMatchers(HttpMethod.POST, "/api/transactions/reversal")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/transactions/**")
                        .hasAnyRole("USER", "MERCHANT", "ADMIN")

                        // statement APIs
                        .requestMatchers(HttpMethod.GET, "/api/statements/**")
                        .hasAnyRole("USER", "MERCHANT", "ADMIN")

                        // notification APIs, if you have them
                        .requestMatchers(HttpMethod.GET, "/api/notifications/**")
                        .hasAnyRole("USER", "MERCHANT", "ADMIN")

                        // everything else secured
                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}