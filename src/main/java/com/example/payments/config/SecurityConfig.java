package com.example.payments.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

@Configuration
public class SecurityConfig {

    /**
     * Configures the Spring Security filter chain for web security.
     *
     * This method disables CSRF protection, which is typically done
     * when stateless REST APIs are used or other protections apply.
     *
     * The HTTP response headers are configured to enhance security:
     * - X-XSS-Protection header is enabled with blocking mode to instruct
     *   browsers to block rendering if an XSS attack is detected.
     * - Content-Type Options header is set to prevent MIME type sniffing.
     * - Frame options are set to DENY to prevent clickjacking by blocking
     *   the site from being framed in any context.
     *
     * Returns a built SecurityFilterChain with the above security configurations.
     *
     * @param http the HttpSecurity object to configure
     * @return the configured SecurityFilterChain bean
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SecurityHeadersFilter securityHeadersFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/app.js", "/favicon.ico", "/assets/**").permitAll()
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/api/stripe/webhook").permitAll()
                .anyRequest().permitAll()
            )
            .headers(headers -> headers
                //X-XSS-Protection: 1; mode=block
                .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                .contentTypeOptions(Customizer.withDefaults()) //("X-Content-Type-Options", new String[]{"nosniff"});
                .frameOptions(frame -> frame.deny())
            )
            .addFilterBefore(securityHeadersFilter, UsernamePasswordAuthenticationFilter.class);
        // -- Added CSP header to further prevent XSS attack

        return http.build();
    }
}
