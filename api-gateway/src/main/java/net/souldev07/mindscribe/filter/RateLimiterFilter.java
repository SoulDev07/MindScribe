package net.souldev07.mindscribe.filter;

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
        String category = "default";
        if (path.startsWith("/public/login")) {
            limit = 5;
            category = "login";
        } else if (path.startsWith("/journal")) {
            limit = 30;
            category = "journal";
        }

        String key = "rate:" + clientId + ":" + category + ":" + currentMinuteKey();

        Long count = redisTemplate.opsForValue().increment(key);

        // Ensure TTL is always set properly on creation or if key is missing TTL
        if (count != null) {
            if (count == 1) {
                redisTemplate.expire(key, Duration.ofSeconds(60));
            } else {
                Long ttl = redisTemplate.getExpire(key);
                if (ttl != null && ttl == -1) {
                    redisTemplate.expire(key, Duration.ofSeconds(60));
                }
            }
        }

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
        if (exchange == null || exchange.getRequest() == null) {
            return "unknown";
        }
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (userId != null && !userId.trim().isEmpty()) {
            return userId.trim().replaceAll("[\\r\\n\\t:]", "_");
        }

        if (exchange.getRequest().getRemoteAddress() != null &&
                exchange.getRequest().getRemoteAddress().getAddress() != null) {
            String hostAddress = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            if (hostAddress != null && !hostAddress.trim().isEmpty()) {
                return hostAddress.trim();
            }
        }

        return "unknown";
    }

    private String currentMinuteKey() {
        return String.valueOf(System.currentTimeMillis() / 60000);
    }
}
