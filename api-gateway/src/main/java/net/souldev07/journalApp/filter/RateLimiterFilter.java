package net.souldev07.journalApp.filter;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RateLimiterFilter implements GlobalFilter, Ordered {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final int MAX_REQUESTS_PER_MINUTE = 60;

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientId = getClientId(exchange);
        String path = exchange.getRequest().getURI().getPath();

        int limit = MAX_REQUESTS_PER_MINUTE;
        if (path.startsWith("/public/login"))
            limit = 5;
        else if (path.startsWith("/journal"))
            limit = 30;

        String key = "rate:" + clientId + ":" + currentMinuteKey();

        Long count = redisTemplate.opsForValue().increment(key);

        // Set TTL on first request
        if (count != null && count == 1)
            redisTemplate.expire(key, Duration.ofSeconds(60));

        if (count != null && count > limit) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(limit));
            exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", "0");
            return exchange.getResponse().setComplete();
        }

        // Add rate limit headers
        exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(limit));
        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining",
                String.valueOf(count != null ? Math.max(0, limit - count) : 0));

        return chain.filter(exchange);
    }

    private String getClientId(ServerWebExchange exchange) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (userId != null)
            return userId;

        if (exchange.getRequest().getRemoteAddress() != null &&
                exchange.getRequest().getRemoteAddress().getAddress() != null)
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();

        return "unknown";
    }

    private String currentMinuteKey() {
        return String.valueOf(System.currentTimeMillis() / 60000);
    }
}
