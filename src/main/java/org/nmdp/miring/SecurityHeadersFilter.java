package org.nmdp.miring;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
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
        if (uri != null && (uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(".png")
                || uri.endsWith(".jpg") || uri.endsWith(".gif") || uri.endsWith(".ico")
                || uri.endsWith(".woff") || uri.endsWith(".woff2") || uri.endsWith(".ttf"))) {
            // Static assets: allow caching with revalidation
            res.setHeader("Cache-Control", "public, max-age=86400, must-revalidate");
        } else {
            // All other responses (HTML pages, API endpoints): no caching
            res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            res.setHeader("Pragma", "no-cache");
            res.setHeader("Expires", "0");
        }

        // Remove server identification headers
        res.setHeader("Server", "");

        // Wrap response to suppress X-Powered-By and ensure charset on Content-Type
        HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper(res) {
            @Override
            public void setHeader(String name, String value) {
                if ("X-Powered-By".equalsIgnoreCase(name)) {
                    return; // suppress entirely
                }
                if ("Content-Type".equalsIgnoreCase(name) && value != null
                        && !value.contains("charset")) {
                    super.setHeader(name, value + "; charset=UTF-8");
                    return;
                }
                super.setHeader(name, value);
            }

            @Override
            public void addHeader(String name, String value) {
                if ("X-Powered-By".equalsIgnoreCase(name)) {
                    return; // suppress entirely
                }
                if ("Content-Type".equalsIgnoreCase(name) && value != null
                        && !value.contains("charset")) {
                    super.addHeader(name, value + "; charset=UTF-8");
                    return;
                }
                super.addHeader(name, value);
            }

            @Override
            public void setContentType(String type) {
                if (type != null && !type.contains("charset")) {
                    super.setContentType(type + "; charset=UTF-8");
                } else {
                    super.setContentType(type);
                }
            }
        };

        chain.doFilter(request, wrapper);
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}
}
