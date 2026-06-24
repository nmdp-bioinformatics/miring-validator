package org.nmdp.miring;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SecurityHeadersFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) response;
        HttpServletRequest req = (HttpServletRequest) request;

        // Clickjacking protection
        res.setHeader("X-Frame-Options", "DENY");

        // Prevent MIME-type sniffing
        res.setHeader("X-Content-Type-Options", "nosniff");

        // XSS filter (legacy browsers)
        res.setHeader("X-XSS-Protection", "1; mode=block");

        // Referrer policy
        res.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Content Security Policy
        res.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline' https://ajax.googleapis.com https://maxcdn.bootstrapcdn.com https://cdnjs.cloudflare.com; " +
                "style-src 'self' 'unsafe-inline' https://maxcdn.bootstrapcdn.com https://cdnjs.cloudflare.com; " +
                "font-src 'self' https://maxcdn.bootstrapcdn.com https://cdnjs.cloudflare.com; " +
                "img-src 'self' data:; " +
                "frame-ancestors 'none'; " +
                "form-action 'self'; " +
                "base-uri 'self'");

        // HSTS - enforce HTTPS (1 year)
        res.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // Permissions Policy - disable unused browser features
        res.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=(), payment=()");

        // Prevent caching of responses that could contain sensitive data
        String uri = req.getRequestURI();
        if (uri != null && uri.contains("/validator/")) {
            res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            res.setHeader("Pragma", "no-cache");
            res.setHeader("Expires", "0");
        }

        // Remove server identification headers where possible
        res.setHeader("Server", "");
        res.setHeader("X-Powered-By", "");

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}
}
