package com.trustlayer.shared.security;

import com.trustlayer.shared.error.ErrorCode;
import com.trustlayer.shared.error.ErrorWriter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> LIMITED_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/verify-email",
            "/api/v1/auth/password-reset/request",
            "/api/v1/auth/password-reset/confirm");
    private static final int MAX_TRACKED_KEYS = 10_000;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ErrorWriter errorWriter;
    private final int perMinute;

    public RateLimitFilter(ErrorWriter errorWriter, @Value("${trustlayer.rate-limit.per-minute}") int perMinute) {
        this.errorWriter = errorWriter;
        this.perMinute = perMinute;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod()) || !LIMITED_PATHS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (buckets.size() > MAX_TRACKED_KEYS) {
            buckets.clear();
        }
        String key = request.getRemoteAddr() + "|" + request.getRequestURI();
        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            chain.doFilter(request, response);
            return;
        }
        long retryAfter = Math.max(1, Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds());
        response.setHeader("Retry-After", String.valueOf(retryAfter));
        errorWriter.write(response, ErrorCode.RATE_LIMITED, "Too many requests, try again later");
    }

    private Bucket newBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(perMinute).refillIntervally(perMinute, Duration.ofMinutes(1)).build())
                .build();
    }
}
