package net.souldev07.mindscribe.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import net.souldev07.mindscribe.utils.JwtUtil;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JwtValidationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public int getOrder() {
        return -2;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (!path.startsWith("/journal") && !path.startsWith("/user") && !path.startsWith("/admin")) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                String username = jwtUtil.extractUsername(token);
                if (username != null && jwtUtil.validateToken(token)) {
                    // Mutate request headers to inject X-User-Id
                    ServerHttpRequest mutatedRequest = request.mutate()
                            .header("X-User-Id", username)
                            .build();
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                }
            } catch (Exception e) {
                log.error("JWT validation error: {}", e.getMessage());
            }
        }

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }
}
