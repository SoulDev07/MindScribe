package net.souldev07.journalApp.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class RateLimiterFilterTest {

    @InjectMocks
    private RateLimiterFilter rateLimiterFilter;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private GatewayFilterChain chain;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
    }

    @Test
    public void testFilterAllowsRequestWithinLimit() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/journal")
                .remoteAddress(new InetSocketAddress("127.0.0.1", 80))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(valueOperations.increment(anyString())).thenReturn(5L);

        Mono<Void> result = rateLimiterFilter.filter(exchange, chain);
        result.block();

        assertEquals("30", exchange.getResponse().getHeaders().getFirst("X-RateLimit-Limit"));
        assertEquals("25", exchange.getResponse().getHeaders().getFirst("X-RateLimit-Remaining"));
    }

    @Test
    public void testFilterBlocksRequestExceedingLimit() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/journal")
                .remoteAddress(new InetSocketAddress("127.0.0.1", 80))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(valueOperations.increment(anyString())).thenReturn(31L);

        Mono<Void> result = rateLimiterFilter.filter(exchange, chain);
        result.block();

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exchange.getResponse().getStatusCode());
        assertEquals("30", exchange.getResponse().getHeaders().getFirst("X-RateLimit-Limit"));
        assertEquals("0", exchange.getResponse().getHeaders().getFirst("X-RateLimit-Remaining"));
    }

    @Test
    public void testFilterUsesCorrectKeyCategories() {
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        when(valueOperations.increment(keyCaptor.capture())).thenReturn(1L);

        MockServerHttpRequest loginRequest = MockServerHttpRequest.get("/public/login")
                .remoteAddress(new InetSocketAddress("127.0.0.1", 80))
                .build();
        rateLimiterFilter.filter(MockServerWebExchange.from(loginRequest), chain).block();

        MockServerHttpRequest journalRequest = MockServerHttpRequest.get("/journal/entries")
                .remoteAddress(new InetSocketAddress("127.0.0.1", 80))
                .build();
        rateLimiterFilter.filter(MockServerWebExchange.from(journalRequest), chain).block();

        MockServerHttpRequest otherRequest = MockServerHttpRequest.get("/other/endpoint")
                .remoteAddress(new InetSocketAddress("127.0.0.1", 80))
                .build();
        rateLimiterFilter.filter(MockServerWebExchange.from(otherRequest), chain).block();

        verify(valueOperations, times(3)).increment(anyString());

        assertTrue(keyCaptor.getAllValues().get(0).contains(":login:"));
        assertTrue(keyCaptor.getAllValues().get(1).contains(":journal:"));
        assertTrue(keyCaptor.getAllValues().get(2).contains(":default:"));
    }
}
