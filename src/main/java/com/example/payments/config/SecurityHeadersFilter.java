package com.example.payments.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Sets a strict Content Security Policy and other security headers.
 * Allows Stripe and Vue CDN while disallowing inline scripts.
 */
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String csp = String.join("; ",
            "default-src 'self'",
            "script-src 'self' https://js.stripe.com https://cdn.jsdelivr.net https://m.stripe.network https://m.stripe.com 'unsafe-eval'",
            "style-src 'self' 'unsafe-inline'",
            "img-src 'self' data:",
            "connect-src 'self' https://api.stripe.com",
            "frame-src https://js.stripe.com https://hooks.stripe.com",
            "object-src 'none'",
            "base-uri 'none'",
            "frame-ancestors 'none'",
            "upgrade-insecure-requests"
        );
        response.setHeader("Content-Security-Policy", csp);
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");

        filterChain.doFilter(request, response);
    }
}
